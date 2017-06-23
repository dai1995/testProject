package RBFNN;

import java.util.Enumeration;
//import java.util.Scanner;
import java.util.Vector;

import matrix.MatrixException;
import matrix.MatrixObj;

import org.w3c.dom.Node;

import FIFO.MinMoutWeightVariableSizedBuffer;


//
//RBFの中間ユニットの中心位置をk-means法ではなくてEMアルゴリズムで決定するもの
//
public class wRBFNNELM extends RBFNN implements RBFNet, Cloneable {
	//String name;
	boolean DEBUG=true;
	WRBFNNELM_parameters wrbfelm_parameter;
	double max_sigma, min_sigma, max_c_value, min_c_value;
	MatrixObj Wold, Wnew;
	MatrixObj Hat;//Hat matrix

	public wRBFNNELM(Node nd) {
		super(nd);
		if (this.DEBUG) {
			System.out.println("wRBFNNELM.wRBFNNELM()");
		}		
	}
	
	public void ReadParameters(Node nd) {
		this.wrbfelm_parameter = new WRBFNNELM_parameters();
		this.wrbfelm_parameter.getParameter(nd);
		this.NumberOfInputs = this.wrbfelm_parameter.getNumberOfInputs();
		this.NumberOfOutputs = this.wrbfelm_parameter.getNumberOfOutputs();
		this.max_sigma = this.wrbfelm_parameter.getMaxSigma();
		this.max_c_value = this.wrbfelm_parameter.getMaxCenterValue();
		this.min_c_value = this.wrbfelm_parameter.getMinCenterValue();
		this.init_sigma = this.wrbfelm_parameter.getInit_sigma();		
		int NumberOfHiddenUnits = this.wrbfelm_parameter.getNumberOfHiddenUnits();
		if (this.DEBUG) {
			System.out.println("wRBFNNELM.ReadParameters(): NumberOfHiddenUnits:" + NumberOfHiddenUnits);
		}
		this.init_parameters(this.wrbfelm_parameter.getNumberOfHiddenUnits());
	}
	
	public void init_parameters(int NumberOfHiddenUnits) {
		if (this.DEBUG) {
			System.out.println("wRBFNNELM.reset_parameters(): MaxCell=" + NumberOfHiddenUnits);
		}
		
		this.isFirstLearning = true; //最初の学習フラグ
		this.NumberOfHiddenUnits = NumberOfHiddenUnits;

	
		if (this.NumberOfOutputs>1) this.NumberOfOutputs=1;

		this.HiddenUnits = new Vector<HiddenUnit>();

		beta = new double[this.NumberOfOutputs];		
		this.isFirstLearning = true; //最初の学習フラグ
		this.setNumberOfHiddenUnits(NumberOfHiddenUnits);
		if (this.HiddenUnits.size()>0) {
			this.HiddenUnits.removeAllElements();
			
		}
		for (int cell=0; cell<NumberOfHiddenUnits; cell++) {
			this.HiddenUnits.addElement(new RBF(NumberOfInputs, this.init_sigma));
		}
		this.C = new double[this.NumberOfHiddenUnits][this.NumberOfOutputs];//output connections		
		
		this.setNumberOfHiddenUnits(NumberOfHiddenUnits);
		if (this.HiddenUnits.size()>0) {
			this.HiddenUnits.removeAllElements();
		}
		this.initHiddenUnitCenterELM(this.HiddenUnits, 
				NumberOfHiddenUnits,
				this.min_sigma,
				this.max_sigma,
				this.min_c_value,
				this.max_c_value);
		this.C = new double[this.NumberOfHiddenUnits][this.NumberOfOutputs];//output connections			
	}	
	
	//Learning(): 最初の学習にだけ使う
	public void Learning(MinMoutWeightVariableSizedBuffer buffer, int MaxNumberOfHiddenUnits) throws MatrixException {
		//this.DEBUG = true;
		if (this.DEBUG) {
			System.out.println("wRBFNNELM:Learning()");
		}
			
		//this.initHiddenUnitCenterELM(this.HiddenUnits, MaxNumberOfHiddenUnits, 
			//	this.rbfnn_parameters.MinSigma, this.rbfnn_parameters.MaxSigma, 0, 1);

		if (this.DEBUG) {
			this.display_hidden_centers(this.HiddenUnits);
		}
		wLeastSquare(this.HiddenUnits, buffer);
		//this.DEBUG = false;
		//学習データを圧縮保存
		this.CompressRecordPastData(buffer, null);
	}	

	//Incremental Learning仕様。といっても最初の１回目の学習にのみ使われる。
	void wLeastSquare(Vector<HiddenUnit> hidden_unit, MinMoutWeightVariableSizedBuffer buffer) throws MatrixException {
		HiddenUnit h_cell;
		MatrixObj phi = new MatrixObj(buffer.getSize(), hidden_unit.size());
		MatrixObj T[] = new MatrixObj[this.NumberOfOutputs];
		this.Wnew = new MatrixObj(buffer.getSize(), buffer.getSize());
		MatrixObj c;
		MatrixObj c_result[] = new MatrixObj[this.NumberOfOutputs];
		
		//WoldはIncremental learningの時に使われる。このメソッドでは必須のものではない		
		this.Wold = new MatrixObj(buffer.getSize(), buffer.getSize());
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			T[o] = new MatrixObj(buffer.getSize(), 1);
		}
		
		for (int p=0; p<buffer.getSize(); p++) {
			this.Wnew.set_data(p, p, buffer.getActualWeight(p));
			this.Wold.set_data(p, p, buffer.getSourceWeight(p));//sourceWeightの方を保持する。後でλが変わるかもしれないので
			Enumeration<HiddenUnit> e = hidden_unit.elements();
			int m=0;
			while (e.hasMoreElements()) {
				h_cell = (HiddenUnit)e.nextElement();
				phi.set_data(p, m, h_cell.calculate_output(buffer.getInput(p)));
				m++;
			}
//			System.out.println("p=" + p);
			//System.out.println("target[0][" + p + "]=" + target[p][0]);
			for (int o=0; o<this.NumberOfOutputs; o++) {
				T[o].set_data(p, 0, buffer.getOutput(p)[o]);
			}
		}
		this.PHI = phi;//pseudo input生成に使う	
		this.Teacher = T;
		if (this.DEBUG) {
			//T.display("wRBFNNELM:wLeastSquare():T");
			phi.display("wRBFNNELM:wLeastSquare():phi");
		}
		c = phi.Transport().multiply(this.Wnew.Transport()); //phi^{T} W^{T}
		c = c.multiply(phi); //phi^{T} W^{T} phi
		c = c.inverse(); // (phi^{T} W^{T} phi)^{-1}
		c = c.multiply(phi.Transport()); // (phi^{T} W^{T} phi)^{-1} phi^{T}
		c = c.multiply(this.Wnew); //(phi^{T} W^{T} phi)^{-1} phi^{T} W
		this.Hat = new MatrixObj(c.getL(), c.getM(), c.getMatrix()); // hat matrix;
		this.Hat = phi.multiply(this.Hat);

		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			c_result[o] = c.multiply(T[o]); // (phi^{T} W^{T} phi)^{-1} phi^{T} T
		}
		this.C_result = c_result;//レコードに利用される		
		if (this.DEBUG) {
			c.display("wRBFNN:wLeastSquare():Wmap");
		}

		for (int o=0; o<this.NumberOfOutputs; o++) {		
			for (int cell=0; cell<hidden_unit.size(); cell++) {
				this.C[cell][o] = c_result[o].getData(cell, 0);
				System.out.println("C[cell=" + cell + "][" + o + "]=" + this.C[cell][o]);				
			}
		}
		//Incremental learningで使用する変数をセットしておく

		//this.TargetOutputs.display("targetOutputs");
	}
	
	

	//ELMの方式
	//中間ユニットの中心位置も、分散も全部ランダムセットする（こんなんでうまくいくんか？）
	//sigmaはMinSigmaとMaxSigmaの間の一様乱数
	//中心位置の各要素はMinCValueとMaxCValueの間の一様乱数
	void initHiddenUnitCenterELM(Vector<HiddenUnit> hidden_units, 
			int MaxNumberOfHiddenUnits, 
			double MinSigma, double MaxSigma, double MinCValue, double MaxCValue) {
		//Scanner KeyboardScanner = new Scanner(System.in);
		if (this.DEBUG) {
			System.out.println("wRBFNNELM.initHiddenUnitCenterELM()");
		}
		double sigma=0;
		for (int cell=0; cell<MaxNumberOfHiddenUnits; cell++) {
			sigma = MinSigma + Math.random()*(MaxSigma - MinSigma);

			HiddenUnit h_unit = new RBF(this.NumberOfInputs, sigma);
			System.out.println("wRBFNNELM.initHiddenUnitCenterELM():cell="+cell+" sigma=" + sigma);
			
			h_unit.setCenter(this.generate_random_center(this.NumberOfInputs, MinCValue, MaxCValue));
			this.HiddenUnits.add(h_unit);
			System.out.print("center = ");
			for (int i=0; i<this.NumberOfInputs; i++) {
				System.out.print(" " + h_unit.getCenter()[i]);
			}
			System.out.println(" ");

		}
		System.out.println("Number of hidden units = " + this.HiddenUnits.size());
		//KeyboardScanner.next();
	}//initHiddenUnitCenterELM()
	
	double[] generate_random_center(int number_of_inputs, double min_value, double max_value) {
			double[] c = new double[number_of_inputs]; 
			for (int i=0; i<number_of_inputs; i++) {
				c[i] = (max_value-min_value) * Math.random() + min_value;
			}
			return c;
	}//generate_random_center()
	

	

	


	public double get_sqare_error(double[] inputs, double[] target_output, double weight) {
		// TODO Auto-generated method stub
		double output[] = this.getOutputs(inputs);
		double error=0D;
		for (int o=0; o<this.NumberOfOutputs; o++) {
			error += weight * Math.pow(target_output[o]-output[o], 2D);
		}
		return error;
	}	
	
	public double get_hidden_output(double[] inputs, int cell) {
		HiddenUnit h_cell;
		h_cell = (HiddenUnit)this.HiddenUnits.get(cell);
		return h_cell.calculate_output(inputs);
	}

	/**
	 * @return the hat
	 */
	public MatrixObj getHat() {

		return Hat;
	}

	public MatrixObj getWnew() {
		return this.Wnew;
	}
	
	
	/**
	 * @return the wold
	 */
	public MatrixObj getWold() {
		return Wold;
	}


	public Object clone() {  
		wRBFNNELM myclone = (wRBFNNELM) super.clone();
	    myclone.C = this.C.clone();//output connection strength
		myclone.beta = this.beta.clone();// for ARD 将来の拡張のため出力次元数分用意
		myclone.PHI=this.PHI.clone();
		myclone.Wnew = this.Wnew.clone();
		myclone.Wold = this.Wold.clone();
		myclone.Hat = this.Hat.clone();
		myclone.name = this.name;
    	Enumeration<HiddenUnit> e = this.HiddenUnits.elements();
    	int i=0;
    	while (e.hasMoreElements()) {
    		RBF h_cell = (RBF)e.nextElement();
    		myclone.HiddenUnits.setElementAt(h_cell.clone(), i);
    		i++;
    	}		
		myclone.Teacher = this.Teacher.clone();
		myclone.WinnerCell = this.WinnerCell;
		myclone.TargetOutputs = this.TargetOutputs.clone();
		return myclone;
	} 	
	
	/*public void setName(String str) {
		this.name = str;
	}
	public String getName() {
		return this.name;
	}*/

	

		
}
