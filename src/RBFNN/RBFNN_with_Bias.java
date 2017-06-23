package RBFNN;

import java.util.Enumeration;
import java.util.Vector;
import org.w3c.dom.Node;

import FIFO.MinMoutWeightVariableSizedBuffer;


import matrix.DiagonalMatrixObj;
import matrix.Jacobi;
import matrix.MatrixException;
import matrix.MatrixObj;

public class RBFNN_with_Bias implements RBFNet {
	final boolean DEBUG = false;

	Vector<HiddenUnit> HiddenUnits;
	double C[][];//output connection strength
	MatrixObj C_result[];
	int NumberOfInputs, NumberOfOutputs, NumberOfHiddenUnits;
	double init_sigma;
	public RBFNN_parameters rbfnn_parameters;
	double alpha=0;// for ARD
	double beta[];// for ARD 将来の拡張のため出力時減数分用意
	protected MatrixObj PHI;
	
	
	public RBFNN_with_Bias(int NumberOfInputs, int NumberOfOutputs, int NumberOfHiddenUnits, double init_sigma) {
		this.NumberOfInputs = NumberOfInputs;
		this.NumberOfOutputs = NumberOfOutputs; 
		if (this.NumberOfOutputs>1) this.NumberOfOutputs=1;
		this.NumberOfHiddenUnits = NumberOfHiddenUnits;
		this.HiddenUnits = new Vector<HiddenUnit>();
		this.init_sigma = init_sigma;
		this.reset_parameters(NumberOfInputs, NumberOfHiddenUnits);
		beta = new double[this.NumberOfOutputs];
	}
	
	public RBFNN_with_Bias(Node nd) {
		this.rbfnn_parameters = new RBFNN_parameters();		
		this.rbfnn_parameters.getParameter(nd);
		this.NumberOfInputs = this.rbfnn_parameters.getNumberOfInputs();
		this.NumberOfOutputs = this.rbfnn_parameters.getNumberOfOutputs();
		if (this.NumberOfOutputs>1) this.NumberOfOutputs=1;
		this.NumberOfHiddenUnits = this.rbfnn_parameters.getNumberOfHiddenUnits();
		this.init_sigma = this.rbfnn_parameters.getInit_sigma();
		this.HiddenUnits = new Vector<HiddenUnit>();
		this.reset_parameters(NumberOfInputs, NumberOfHiddenUnits);
		beta = new double[this.NumberOfOutputs];
	}
	

	public void reset_parameters(int NumberOfInputs, int NumberOfHiddenUnits) {
		this.setNumberOfHiddenUnits(NumberOfHiddenUnits);
		if (this.HiddenUnits.size()>0) {
			this.HiddenUnits.removeAllElements();
		}
		for (int cell=0; cell<NumberOfHiddenUnits; cell++) {
			this.HiddenUnits.addElement(new RBF(NumberOfInputs, this.init_sigma));
		}
		//this.C = new double[this.NumberOfHiddenUnits][this.NumberOfOutputs];//output connections			
		//閾値を導入していなかったので追加(2009/12/13)
		this.C = new double[this.NumberOfHiddenUnits+1][this.NumberOfOutputs];//output connections					
	}


	public double[] getOutputs(double inputs[]) {
		double sum[];
		int cell; // loop variable
		HiddenUnit h_cell;
		Enumeration<HiddenUnit> e = this.HiddenUnits.elements();
		sum = new double[this.NumberOfOutputs];
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
				sum[o] += this.C[0][o]; //bias	
			//System.out.println("bias is " + this.C[0][o]);
		}
		cell=1;
		while (e.hasMoreElements()) {
			h_cell = (HiddenUnit)e.nextElement();
			for (int o=0; o<this.NumberOfOutputs; o++) {
				sum[o] += this.C[cell][o] * h_cell.calculate_output(inputs);
			}
			//sum += h_cell.calculate_output(inputs);
			cell++;
		}
		return sum;
	}
	
	public double getSumOfHiddenOutputs(double inputs[]) {
		double sum;
		//int i; // loop variable
		HiddenUnit h_cell;
		Enumeration<HiddenUnit> e = this.HiddenUnits.elements();
		sum = 0D;
		//i=0;
		while (e.hasMoreElements()) {
			h_cell = (HiddenUnit)e.nextElement();
			sum += h_cell.calculate_output(inputs);
			//i++;
		}
		return sum;		
	}
	

	public void Learning(int NumberOfLearningSamples, 
			double inputs[][], double target_outputs[][]) throws MatrixException {
		//init hidden centers
		this.init_hidden_centers(this.HiddenUnits, inputs);

		
		//setup hidden units
		this.k_means(this.HiddenUnits, NumberOfLearningSamples, this.rbfnn_parameters.getKMeansChangeThreshold(), inputs,
				this.rbfnn_parameters.getMinSigma(), this.rbfnn_parameters.getMaxSigma(), this.rbfnn_parameters.getOverlap());
		this.display_hidden_centers(this.HiddenUnits);
		
		//setup output connections
		this.LeastSquare(this.HiddenUnits, NumberOfLearningSamples, inputs, target_outputs);
	}
	
	
	public void ARD(int NumberOfLearningSamples, double inputs[][], double target_outputs[][], double termination_threshold) throws MatrixException {
		double previous_alpha;
		double previous_beta[];
		//init hidden centers
		this.init_hidden_centers(this.HiddenUnits, inputs);
		previous_beta = new double[this.NumberOfOutputs];
		//init alpha, beta;
		this.alpha = previous_alpha = 1;
		this.beta[0] = previous_beta[0] = 1; //今は１次元なので０に入れる

		//setup hidden units
		this.k_means(this.HiddenUnits, NumberOfLearningSamples, this.rbfnn_parameters.getKMeansChangeThreshold(), inputs,
				this.rbfnn_parameters.getMinSigma(), this.rbfnn_parameters.getMaxSigma(), this.rbfnn_parameters.getOverlap());
		this.display_hidden_centers(this.HiddenUnits);
		
		do {
		//setup output connections
			previous_alpha = this.alpha;
			previous_beta[0] = this.beta[0];
			this.LeastSquare_with_regularization(this.HiddenUnits, NumberOfLearningSamples, inputs, target_outputs, this.alpha, this.beta[0]);
			double gamma = this.getGamma(this.HiddenUnits, NumberOfLearningSamples, inputs, alpha, beta[0]);
			double norm_w = 0;
			for (int h=0; h<this.HiddenUnits.size(); h++) {
				norm_w += Math.pow(this.C[h][0],2D); 
			}
			this.alpha = gamma / norm_w;
			double err_sum = 0;
			for (int p=0; p<NumberOfLearningSamples; p++) {
				err_sum += this.get_sqare_error(inputs[p], target_outputs[p]);
			}
			this.beta[0] = (NumberOfLearningSamples - gamma)/err_sum;
			System.out.println("Delta alpha = " + Math.abs(previous_alpha-this.alpha));
		}while ((Math.abs(previous_alpha - this.alpha)+
				Math.abs(previous_beta[0]-this.beta[0]))>termination_threshold);
	}
	
	
	
	double getGamma(Vector<HiddenUnit> hidden_unit, int NumberOfLearningSamples, double inputs[][], double alpha, double beta) throws MatrixException {
		MatrixObj A = this.get_A(hidden_unit, NumberOfLearningSamples, inputs, alpha, beta);
		Jacobi pj = new Jacobi(A.getL(), A.getMatrix());
		pj.jacobi();
		double sum=0;
		for (int p=0; p<A.getL(); p++) {
			double gamma_i = pj.get_DescentOrderEigenValue(p); 
			//System.out.println("ARD getGamma " + p + " gamma= " + gamma_i);
			sum += gamma_i/(gamma_i + alpha);
		}
		System.out.println("ARD getGamma gamma= " + sum);		
		return sum;
	}
	
	double getGamma(Vector<HiddenUnit> hidden_unit, MinMoutWeightVariableSizedBuffer buffer, double alpha, double beta) throws MatrixException {
		MatrixObj A = this.get_A(hidden_unit, buffer, alpha, beta);
		Jacobi pj = new Jacobi(A.getL(), A.getMatrix());
		pj.jacobi();
		double sum=0;
		for (int p=0; p<A.getL(); p++) {
			double gamma_i = pj.get_DescentOrderEigenValue(p); 
			//System.out.println("ARD getGamma " + p + " gamma= " + gamma_i);
			sum += gamma_i/(gamma_i + alpha);
		}
		System.out.println("ARD getGamma gamma= " + sum);		
		return sum;
	}	
	
	void init_hidden_centers(Vector<HiddenUnit> hidden_units, double input[][]) {
		Enumeration<HiddenUnit> e = hidden_units.elements();
		HiddenUnit h_cell;
		int p=0;
		while (e.hasMoreElements()) {

			h_cell = (HiddenUnit)e.nextElement();
			h_cell.setCenter(input[p]);
			p++;
		}
	}
	
	
	
	
	void display_hidden_centers(Vector<HiddenUnit> hidden_units) {
		Enumeration<HiddenUnit> e = hidden_units.elements();
		HiddenUnit h_cell;
		int p=0;
		while (e.hasMoreElements()) {
			h_cell = (HiddenUnit)e.nextElement();
			System.out.print("HddenCenter(" + p + ")= " );
			for (int i=0; i<this.NumberOfInputs; i++) {
				System.out.print(" " + h_cell.getCenter()[i]);
			}
			System.out.println(" ");
			p++;
		}		
	}
	
	void k_means(Vector<HiddenUnit> hidden_units, int NumberOfLearningSamples, double change_threshold, double input[][], double MinSigma, double MaxSigma, double Overlap) {
		HiddenUnit h_cell=null;
		double total_change, changes = 10000;
		double sumOfHiddenOutputs;
		Enumeration<HiddenUnit> e = hidden_units.elements();
		while (changes > change_threshold) {
		
			total_change = 0;
			while (e.hasMoreElements()) {
				h_cell = (HiddenUnit)e.nextElement();
				h_cell.init_M_step();
				for (int p=0; p<NumberOfLearningSamples; p++) {
					sumOfHiddenOutputs = this.getSumOfHiddenOutputs(input[p]);
					
					h_cell.M_step_add(input[p], sumOfHiddenOutputs);
				}
				total_change += h_cell.M_step_update_center();
			}
			this.set_variance(hidden_units, MinSigma, MaxSigma, Overlap);
			changes = total_change/hidden_units.size();
		}
	}
	
	

	void set_variance(Vector<HiddenUnit> hidden_units, double MinSigma, double MaxSigma, double Overlap) {
		Enumeration<HiddenUnit> e1 = hidden_units.elements();
		HiddenUnit h_cell, t_cell;
		double result;
		while (e1.hasMoreElements()) {
			double min_distance = 100000000;
			h_cell = (HiddenUnit)e1.nextElement();
			Enumeration<HiddenUnit> e2 = hidden_units.elements();
			while (e2.hasMoreElements()) {
				t_cell = (HiddenUnit)e2.nextElement();
				if (t_cell != h_cell) {
					min_distance = Math.min(min_distance, h_cell.getDistance(t_cell.getCenter()));
				}
			}
			if (this.DEBUG) {
				System.out.println("RBFNN:set_variance: min_distance = " + min_distance);
			}
			result = Overlap * Math.sqrt(min_distance);
			if (result > MaxSigma) result = MaxSigma;
			if (result < MinSigma) result = MinSigma;
			h_cell.setVariance(result);
		}
	}
	

	void LeastSquare(Vector<HiddenUnit> hidden_unit, int NumberOfLearningSamples, double inputs[][], double target[][]) throws MatrixException {
		HiddenUnit h_cell;
		MatrixObj phi = new MatrixObj(NumberOfLearningSamples, hidden_unit.size()+1);
		MatrixObj T[] = new MatrixObj[this.NumberOfOutputs];
		MatrixObj c;
		MatrixObj c_result[] = new MatrixObj[this.NumberOfOutputs];
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
				T[o] =  new MatrixObj(NumberOfLearningSamples, 1);
		}
		
		for (int p=0; p<NumberOfLearningSamples; p++) {
			phi.set_data(p, 0, 1.0D);//bias
			Enumeration<HiddenUnit> e = hidden_unit.elements();
			int m=1;
			while (e.hasMoreElements()) {
				h_cell = (HiddenUnit)e.nextElement();
				phi.set_data(p, m, h_cell.calculate_output(inputs[p]));

				m++;
			}
//			System.out.println("p=" + p);
			System.out.println("target[0][" + p + "]=" + target[p][0]);
			for (int o=0; o<this.NumberOfOutputs; o++){
				T[o].set_data(p, 0, target[p][o]);
			}
		}
		this.PHI = phi;//pseudo input生成に使う
		if (this.DEBUG) {
			//T.display("RBFNN:LeastSquare():T");
			phi.display("RBFNN:LeastSquare():phi");
		}
		c = phi.Transport().multiply(phi);// phi^{T} phi
		c = c.inverse();// (phi^{T} phi)^{-1}
		c = c.multiply(phi.Transport());// (phi^{T} phi)^{-1} phi^{T}
		//c = c.multiply(T);
		for (int o=0; o<this.NumberOfOutputs; o++) {
			c_result[o] = c.multiply(T[o]); // (phi^{T} phi)^{-1} phi^{T} t
		}
		C_result = c_result; //レコードに使われる
		if (this.DEBUG) {
			c.display("RBFNN:LeastSquare():Wmap");
		}

		//得られたW_{MP}を大域変数にコピーする
		for (int cell=0; cell<hidden_unit.size()+1; cell++) {
			for (int o=0; o<this.NumberOfOutputs; o++) {
				this.C[cell][o] = c_result[o].getData(cell, 0);
			}
		}
		System.out.println("RBFNN:LeastSquare");
	}
	

	void LeastSquare_with_regularization(Vector<HiddenUnit> hidden_unit, int NumberOfLearningSamples, double inputs[][], double target[][], double alpha, double beta) throws MatrixException {
		HiddenUnit h_cell;
		MatrixObj phi = new MatrixObj(NumberOfLearningSamples, hidden_unit.size()+1); // with bias
		MatrixObj T[] = new MatrixObj[this.NumberOfOutputs];
		MatrixObj c;
		MatrixObj c_result[] = new MatrixObj[this.NumberOfOutputs];
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
				T[o] =  new MatrixObj(NumberOfLearningSamples, 1);
		}
		
		for (int p=0; p<NumberOfLearningSamples; p++) {
			phi.set_data(p, 0, 1); // bias
			Enumeration<HiddenUnit> e = hidden_unit.elements();
			int m=0;
			while (e.hasMoreElements()) {
				h_cell = (HiddenUnit)e.nextElement();
				phi.set_data(p, m+1, h_cell.calculate_output(inputs[p]));
				m++;
			}
			//phi.display("phi");
//			System.out.println("p=" + p);
			//System.out.println("target[0][" + p + "]=" + target[p][0]);
			for (int o=0; o<this.NumberOfOutputs; o++){
				T[o].set_data(p, 0, target[p][o]);
			}
		}
		this.PHI = phi;//pseudo input生成に使う		
		if (this.DEBUG) {
			//T.display("RBFNN:LeastSquare():T");
			phi.display("RBFNN:LeastSquare():phi");
		}
		c = phi.Transport().multiply(phi);//phi^{T} phi
		DiagonalMatrixObj alpha_beta_I = new DiagonalMatrixObj(c.getL(),0);
		//alpha_beta_I.display("alphabetaI");
		c = c.Add(alpha_beta_I);

		c = c.inverse();// (phi^{T} phi + (alpha/beta) I)^{-1}

		c = c.multiply(phi.Transport());// (phi^{T} phi + (alpha/beta)I)^{-1} phi^{T}
		phi.display("phi");
		for (int o=0; o<this.NumberOfOutputs; o++) {
			c_result[o] = c.multiply(T[o]); // (phi^{T} phi+alpha/beta I)^{-1} phi^{T} t
		}
		if (this.DEBUG) {
			c.display("RBFNN:LeastSquare():Wmap");
		}
		
		//得られたW_{MP}を大域変数にコピーする
		for (int cell=0; cell<hidden_unit.size()+1; cell++) {
			for (int o=0; o<this.NumberOfOutputs; o++) {
				this.C[cell][o] = c_result[o].getData(cell, 0);
			}
		}
		System.out.println("RBFNN:LeastSquare");
	}
	
	
	//getting matrix A = beta * phi^{T} phi + alpha I 

	MatrixObj get_A(Vector<HiddenUnit> hidden_unit, int NumberOfLearningSamples, double inputs[][], double alpha, double beta) {
		MatrixObj phi = new MatrixObj(NumberOfLearningSamples, hidden_unit.size());
		HiddenUnit h_cell;
		MatrixObj c;
		
		//setup phi
		for (int p=0; p<NumberOfLearningSamples; p++) {
			Enumeration<HiddenUnit> e = hidden_unit.elements();
			int m=0;
			while (e.hasMoreElements()) {
				h_cell = (HiddenUnit)e.nextElement();
				phi.set_data(p, m, h_cell.calculate_output(inputs[p]));
				m++;
			}
		}
		
		c = phi.Transport().multiply(phi);//phi^{T} phi
		c.multiply(beta);//beta * phi^{T} phi
		DiagonalMatrixObj alpha_I = new DiagonalMatrixObj(c.getL(),alpha); // alpha I
		
		c = c.Add(alpha_I);//beta * phi^{T} phi + alpha I
		return c;
	}


	MatrixObj get_A(Vector<HiddenUnit> hidden_unit, MinMoutWeightVariableSizedBuffer buffer, double alpha, double beta) {
		MatrixObj phi = new MatrixObj(buffer.getSize(), hidden_unit.size());
		HiddenUnit h_cell;
		MatrixObj c;
		
		//setup phi
		for (int p=0; p<buffer.getSize(); p++) {
			Enumeration<HiddenUnit> e = hidden_unit.elements();
			int m=0;
			while (e.hasMoreElements()) {
				h_cell = (HiddenUnit)e.nextElement();
				phi.set_data(p, m, h_cell.calculate_output(buffer.getInput(p)));
				m++;
			}
		}
		
		c = phi.Transport().multiply(phi);//phi^{T} phi
		c.multiply(beta);//beta * phi^{T} phi
		DiagonalMatrixObj alpha_I = new DiagonalMatrixObj(c.getL(),alpha); // alpha I
		
		c = c.Add(alpha_I);//beta * phi^{T} phi + alpha I
		return c;
	}
		
	/**
	 * @param numberOfHiddenUnits the numberOfHiddenUnits to set
	 */
	public void setNumberOfHiddenUnits(int numberOfHiddenUnits) {
		this.NumberOfHiddenUnits = numberOfHiddenUnits;
	}

	public double get_sqare_error(double[] inputs, double target_outputs[]) {
		// TODO Auto-generated method stub
		double outputs[] = this.getOutputs(inputs);
		double error=0D;
		for (int o=0; o<this.NumberOfOutputs; o++) {
			error += Math.pow(target_outputs[o]-outputs[o], 2D);
		}
		return error;
	}
	
	public boolean get_accuracy(double[] inputs, double target_outputs[]) {
		// TODO Auto-generated method stub
		double outputs[] = this.getOutputs(inputs);
		double max_output=0;
		int max_out_index=-1;
		boolean is_ok=true;

		for (int o=0; o<this.NumberOfOutputs; o++) {
			if (max_output < outputs[o]) {
				max_out_index = o;
				max_output = outputs[o];
			}
		}
		if (target_outputs[max_out_index]<0.5) {
			is_ok = false;
		}
		
		return is_ok;
	}
	
    //STSMによってQ*の値を返す。

	public double CalculateSTSM(double mean_x[], double sigma_x[], double diff3[], double diff4[], double RANGE, double a, double possible_MSE, double eta, double NumberOfSamples, double Remp) {
		RBF hc_obj=null;
		double A, B, c, Q=0;
		double min_Q=1000;
		double sum_V=0, sum_eta=0;
		int cell;
		
		double epsilon = possible_MSE * Math.sqrt(-Math.log(eta)/(2*(double)NumberOfSamples));
		c = 3*Math.pow(Math.sqrt(a-epsilon)-Math.sqrt(Remp)-RANGE, 2);
		for (int o=0; o<this.NumberOfOutputs; o++) {
			Enumeration<HiddenUnit> e = this.HiddenUnits.elements();
			cell=0;
			while (e.hasMoreElements()) {
				hc_obj = (RBF)e.nextElement();
				hc_obj.calculate_mean_Var_s(mean_x, sigma_x, diff3, diff4, this.C[cell][o]);
				sum_V += hc_obj.getV();
				sum_eta += hc_obj.getEta();
				cell++;
			}
			System.out.println("RBFNN:CalculateSTSM(): o=" +o + " sum_V=" + sum_V + " sum_eta= " + sum_eta);		
			A = (0.2/3)*this.NumberOfInputs * sum_eta;
			B = sum_V;
			System.out.println("RBFNN:CalculateSTSM(): o=" +o + " A=" + A + " B= " + B + " C= " + c);
			Q = - (B/(2*A)) + Math.sqrt((B*B)/(4*A*A) + c/A);
			if (min_Q > Q) {
				min_Q = Q;
			}
		}

		return Math.sqrt(min_Q);
	}
	
    //STSMによってQ*の値を返す。

	public double CalculateSTSM(MinMoutWeightVariableSizedBuffer buffer, double mean_x[], double sigma_x[], double RANGE, double a, double possible_MSE, double eta, double Remp) {
		RBF hc_obj=null;
		double A, B, c, Q, min_Q=1000;
		double sum_V=0, sum_eta=0;
		int cell;
		
		double epsilon = possible_MSE * Math.sqrt(-Math.log(eta)/(2*(double)buffer.getSize()));
		c = 3*Math.pow(Math.sqrt(a-epsilon)-Math.sqrt(Remp)-RANGE, 2);
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			Enumeration<HiddenUnit> e = this.HiddenUnits.elements();
			cell=0;
			while (e.hasMoreElements()) {
				hc_obj = (RBF)e.nextElement();
				hc_obj.calculate_mean_Var_s(buffer, mean_x, sigma_x, this.C[cell][o]);
				sum_V += hc_obj.getV();
				sum_eta += hc_obj.getEta();
				cell++;
			}
			System.out.println("RBFNN:CalculateSTSM(): sum_V=" + sum_V + " sum_eta= " + sum_eta);		
			A = (0.2/3)*this.NumberOfInputs * sum_eta;
			B = sum_V;
			System.out.println("RBFNN:CalculateSTSM(): A=" + A + " B= " + B + " C= " + c + " epsilon=" + epsilon);
			Q = - (B/(2*A)) + Math.sqrt((B*B)/(4*A*A) + c/A);
			if (min_Q > Q) {
				min_Q = Q;
			}
		}
		return Math.sqrt(min_Q);
	}

	public double[] generate_pseudo_input(int index, RBFNet old_RBFNN) {
		//最も大きな出力を持つ中間ユニットを探す
		double max_output = -9999999D;
		int winner_cell = -1;
		//this.PHI.display("PHI");
		for (int j=1; j<old_RBFNN.getNumberOfHiddenUnits(); j++) {//1からはじまるのは閾値をのぞくため
			if (max_output < this.PHI.getData(index, j)) {
				max_output = this.PHI.getData(index, j);
				winner_cell = j;
			}
		}
		
		//疑似入力の初期化
		double pseudo_x[];
		HiddenUnit h_cell = (HiddenUnit)old_RBFNN.getHiddenUnits().elementAt(winner_cell-1);
		pseudo_x = h_cell.getCenter();
		
		//疑似入力の更新
		double[] Delta_x = new double[this.NumberOfInputs];
		double gain, hidden_output, err=99999D, prev_err = 999999D;
		do {
			prev_err = err;
			err = 0;
			for (int h=0; h<old_RBFNN.getNumberOfHiddenUnits(); h++) {
				h_cell = (HiddenUnit)old_RBFNN.getHiddenUnits().elementAt(h);
				hidden_output = h_cell.calculate_output(pseudo_x);
				gain = hidden_output * (hidden_output - old_RBFNN.getPHI().getData(index, h+1));
				for (int i=0; i<this.NumberOfInputs; i++) {
					Delta_x[i] += 2 * this.rbfnn_parameters.get_inverse_map_eta() * ((pseudo_x[i] - h_cell.getCenter()[i])/h_cell.getVariance()) * gain;
				}
				err += Math.pow(hidden_output - old_RBFNN.getPHI().getData(index, h+1), 2);
			}
			//疑似入力の更新
			for (int i=0; i<this.NumberOfInputs; i++) {
				pseudo_x[i] += Delta_x[i];
				Delta_x[i] = 0;
			}			
		}while (Math.abs(err-prev_err) > this.rbfnn_parameters.get_inverse_map_stopCondition());
		
		return	pseudo_x;
	}	
	
	//WinnerCell,TargetOutputダケを使う疑似データ生成法
	public double[] generate_pseudo_input2(int index, RBFNet old_RBFNN) {
		HiddenUnit h_cell;
		//最も大きな出力を持つ中間ユニットを得る
		int winner_cell = old_RBFNN.getWinnerCell()[index];
		
		//疑似入力の初期化
		double pseudo_x[];
		h_cell = (HiddenUnit)old_RBFNN.getHiddenUnits().elementAt(winner_cell);
		pseudo_x = h_cell.getCenter();
		for (int i=0; i<this.NumberOfInputs; i++) {//小さな摂動を与えておく
			pseudo_x[i] += 0.001 * (double)2*(Math.random()-0.5);
		}
		
		//疑似入力の更新
		double[] Delta_x = new double[this.NumberOfInputs];
		double gain, gain2, err=Double.MAX_VALUE, prev_err=Double.MAX_VALUE;
		do {
			prev_err = err;
			err = 0;
			gain = 2 * (this.TargetOutputs.getData(index, 0)-old_RBFNN.getOutputs(pseudo_x)[0]);
			for (int h=0; h<old_RBFNN.getNumberOfHiddenUnits(); h++) {
				h_cell = (HiddenUnit)old_RBFNN.getHiddenUnits().elementAt(h);

				gain2 = this.C[h][0] * h_cell.calculate_output(pseudo_x) / Math.pow(h_cell.getVariance(),2D);
				for (int i=0; i<this.NumberOfInputs; i++) {
					Delta_x[i] -= gain * gain2 * this.rbfnn_parameters.get_inverse_map_eta() * ((pseudo_x[i] - h_cell.getCenter()[i]));
				}
				err += Math.pow((this.TargetOutputs.getData(index, 0)-old_RBFNN.getOutputs(pseudo_x)[0]), 2);
			}
			//疑似入力の更新
			for (int i=0; i<this.NumberOfInputs; i++) {
				pseudo_x[i] += Delta_x[i];
				Delta_x[i]=0;
			}			
			//System.out.println("RBFNN:generate_pseudo_input(): err=" + err + " g_x=" + pseudo_x[0]);
		}while (Math.abs(err-prev_err) > this.rbfnn_parameters.get_inverse_map_stopCondition());
		
		return	pseudo_x;
	}	
	
	int[] WinnerCell;
	MatrixObj TargetOutputs;
	void CompressRecordPastData() {
		if (this.PHI.getL()>0) {
			this.WinnerCell = new int[this.PHI.getL()];
			this.TargetOutputs = new MatrixObj(this.PHI.getL(), this.NumberOfOutputs);
		}
		//最も大きな出力を持つ中間ユニットを探す
		//this.PHI.display("PHI");
		int winner_cell = -1;
		double max_output = -Double.MAX_VALUE;
		for (int index=0; index<this.PHI.getL(); index ++) {
			max_output = -Double.MAX_VALUE;
			winner_cell = -1;
			for (int j=1; j<this.PHI.getM(); j++) {//バイアスがあるので1から始まる
				if (max_output < this.PHI.getData(index, j)) {
					max_output = this.PHI.getData(index, j);
					winner_cell = j;
				}
			}
			if (winner_cell != -1) {
				this.WinnerCell[index] = winner_cell;
			}
		}
		this.TargetOutputs = this.PHI.multiply(this.C_result[0]);
	}	
	
	public int getNumberOfLearnedSamples() {
		return this.PHI.getL();
	}
	
	/**
	 * @return the numberOfInputs
	 */
	public int getNumberOfInputs() {
		return NumberOfInputs;
	}

	/**
	 * @return the numberOfOutputs
	 */
	public int getNumberOfOutputs() {
		return NumberOfOutputs;
	}	
	
	public int getNumberOfHiddenUnits() {
		return this.HiddenUnits.size();
	}

	/**
	 * @return the alpha
	 */
	public double getAlpha() {
		return alpha;
	}

	/**
	 * @return the beta
	 */
	public double[] getBeta() {
		return beta;
	}

	@Override
	public MatrixObj getHat() {
		// TODO Auto-generated method stub
		return null;
	}


	
	public int getPhiSizeL() {
		return this.PHI.getL();
	}
	
	public int getPhiSizeM() {
		return this.PHI.getM();
	}


	@Override
	public Vector<HiddenUnit> getHiddenUnits() {
		// TODO Auto-generated method stub
		return this.HiddenUnits;
	}

	@Override
	public MatrixObj getPHI() {
		// TODO Auto-generated method stub
		return this.PHI;
	}


	@Override
	public int[] getWinnerCell() {
		// TODO Auto-generated method stub
		return this.WinnerCell;
	}

	@Override
	public boolean IncLearning(MinMoutWeightVariableSizedBuffer buffer,
			int AddNumberOfHiddenUnits, boolean UseWeightedKmeans,
			double threshold, RBFNN old_RBFNN) {
		// TODO Auto-generated method stub
		return true;
		
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatrixObj getTargetOutputs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatrixObj getWnew() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public MatrixObj getWold() {
		// TODO Auto-generated method stub
		return null;
	}
	
}
