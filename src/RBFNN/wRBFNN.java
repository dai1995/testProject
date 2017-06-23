package RBFNN;

import java.util.Enumeration;
import java.util.Vector;

import matrix.MatrixException;
import matrix.MatrixObj;

import org.w3c.dom.Node;


import FIFO.MinMoutWeightVariableSizedBuffer;
import MixtureOfDistributions.IncMixtureOfDistributions;
import MixtureOfDistributions.MixtureOfNormalDistributions;


public class wRBFNN extends RBFNN implements RBFNet, Cloneable {
	//String name;
	
	
	boolean DEBUG=false;

	MatrixObj Wold, Wnew;
	MatrixObj Hat;//Hat matrix

	public wRBFNN(Node nd) {
		super(nd);
		// TODO Auto-generated constructor stub
	}
	
	public boolean Learning(int NumberOfLearningSamples, 
			double inputs[][], double target_outputs[][], double weights[], double MinSigma, double MaxSigma, double Overlap, boolean UseWeightedKmeans) throws MatrixException {
		//init hidden centers
		this.init_hidden_centers(this.HiddenUnits, inputs);

		//setup hidden units
		if (this.k_means(this.HiddenUnits,
				this.rbfnn_parameters.getMinDistance(),
				NumberOfLearningSamples, 
				this.rbfnn_parameters.getKMeansChangeThreshold(), inputs,
				MinSigma, MaxSigma, Overlap, this.rbfnn_parameters.getKMeansMaxIteration())) {
			if (this.DEBUG) {
				this.display_hidden_centers(this.HiddenUnits);
			}
		
			//setup output connections
			this.wLeastSquare(this.HiddenUnits, NumberOfLearningSamples, inputs, target_outputs, weights);
			return true;
		}else{
			return false;
		}
	}	
	
	public boolean Learning(MinMoutWeightVariableSizedBuffer buffer, boolean UseWeightedKmeans) throws MatrixException {
		//this.DEBUG = true;
		if (this.DEBUG) {
			System.out.println("wRBFNN:Learning()");
		}
			
		this.init_hidden_centers(this.HiddenUnits, buffer);
		//int AddNumberOfHiddenUnits = this.rbfnn_parameters.getNumberOfHiddenUnits();
		//if (this.HiddenUnits.size()>0) this.HiddenUnits.removeAllElements();
		//this.init_newhidden_centers(this.HiddenUnits, AddNumberOfHiddenUnits, buffer);//これを使うと何故かNaNが多発*/
		
		if (k_means(this.HiddenUnits,
				this.rbfnn_parameters.getMinDistance(),
				this.rbfnn_parameters.getKMeansChangeThreshold(), buffer,
				this.rbfnn_parameters.getMinSigma(), this.rbfnn_parameters.getMaxSigma(), 
				this.rbfnn_parameters.getOverlap(), 
				UseWeightedKmeans, this.rbfnn_parameters.getKMeansMaxIteration())) {
			if (this.DEBUG) {
				this.display_hidden_centers(this.HiddenUnits);
			}
			wLeastSquare(this.HiddenUnits, buffer);
			//this.DEBUG = false;
			//学習データを圧縮保存
			this.CompressRecordPastData(buffer, null);
			return true;
		}else{
			return false;
		}
	}
	

	/*public void RemoveRelatedHiddenUnits(MinMoutWeightVariableSizedBuffer buffer) {
		HiddenUnit h_cell;
		//再構築対象となる中間ユニットのフラグを立てる
		this.check_isVariable(buffer, this.rbfnn_parameters.getMinActivationThreshold());

		//再構築対象となる細胞を一旦消す.

		int cell=0;
		while (cell < this.HiddenUnits.size()) {
			h_cell = (HiddenUnit)this.HiddenUnits.elementAt(cell);   
			if (!h_cell.isFixed()) {
				this.HiddenUnits.removeElementAt(cell);
			}else{
				cell++;
			}
		}
		
	}*/

	//新規データの重みはbufferの中に入ってくる。
	//既知データの重みはWoldの中にsource weightとして保持されている。既知データは古いdesign matrixからinverse mappingで想起する。
	//kappa(=λ)もパラメータとして受け取り、既知データの重みを改めて作ってdesign matrixを再構成する

	public boolean IncLearning(MinMoutWeightVariableSizedBuffer buffer, 
			int AddNumberOfHiddenUnits, 
			boolean UseWeightedKmeans, 
			double kappa, wRBFNN old_RBFNN, IncMixtureOfDistributions MN, 
			double UpperLimitOfWeight, double LowerLimitOfWeight) throws MatrixException {
		//if (this.DEBUG) {
			if (this.PHI!=null) System.out.println("wRBFNN:IncLearning()" + " Phi(" + this.PHI.getL() +", " +this.PHI.getM() + ")");
		//}
		if (this.isFirstLearning) {//最初の学習は従来通りの学習法
			//if (this.DEBUG) {
				System.out.println("wRBFNN: IncLearning(): 1st Learning");
				System.out.println("wRBFNN:IncLearning()" + " HiddenUnits = " + this.HiddenUnits.size());
			//}
			if (this.Learning(buffer, UseWeightedKmeans)) {
				this.isFirstLearning = false;
				return true;
			}else{
				return false;
			}
			//this.Wold.display("first Wold");

		}else{//２回目以降は以下を実行
			//if (this.DEBUG) {
				System.out.println("wRBFNN: IncLearning(): Incremental Learning");
			//}
			
			//再構築対象となる中間ユニットのフラグを立て、関係する細胞を一旦消す(これをやると、
			//細胞数の少ない領域（例えば1個）で疑似データに信頼性が無くなりICwが限度無
			//く小さくなっていき止まらなくなる場合あり
			//this.RemoveRelatedHiddenUnits(buffer);
			
			//再構築対象となる中間ユニットのフラグを立てる
			//this.check_isVariable(buffer, this.rbfnn_parameters.getMinActivationThreshold());			
			
			System.out.println("Hidden unit size =" + this.HiddenUnits.size() );
			System.out.println("Add unit size =" + AddNumberOfHiddenUnits);
			//中間ユニットを新しく割り付けてパラメータをbufferのサンプルで初期化する
			int ActualAddedUnits = this.init_newhidden_centers(this.HiddenUnits, AddNumberOfHiddenUnits, buffer);
			//int ActualAddedUnits = 0;
			
			if (this.DEBUG) {
				System.out.println("wRBFNN:IncLearning()" + " HiddenUnits = " + this.HiddenUnits.size());
			}
			
			//新しく割り付けた中間ユニット、再構成するユニットをkMeans法で中心位置を決める
			if (
					this.Inc_k_means(this.HiddenUnits,
							this.rbfnn_parameters.getMinDistance(),
							buffer, this.rbfnn_parameters.getKMeansChangeThreshold(), 
							this.rbfnn_parameters.getMinSigma(), 
							this.rbfnn_parameters.getMaxSigma(), 
							this.rbfnn_parameters.getOverlap(),
							UseWeightedKmeans,
							kappa, old_RBFNN,
							this.rbfnn_parameters.getKMeansMaxIteration()) ) {//中間ユニットのセットアップが成功
				
				if (this.DEBUG) {
					this.display_hidden_centers(this.HiddenUnits);
				}
			
				//design matrixの再構築(縦方向に新規データ分、横方向に追加するユニット数分増やす)
				int old_L=this.PHI.getL();//新旧区別するために後で使う
				int old_M=this.PHI.getM();//新旧区別するために後で使う
				if (this.DEBUG) {
					System.out.println("PHI is (" + old_L + ", " + old_M + ")");
					System.out.println("PHIOLD is (" + old_RBFNN.getPHI().getL() + ", " + old_RBFNN.getPHI().getM() + ")");
				}
				this.Wnew = new MatrixObj(old_L+buffer.getSize(), old_L+buffer.getSize());			
				this.PHI.increase_allocate_data(old_L+buffer.getSize(), old_M+ActualAddedUnits);

				//Woldの新しいサンプルに対応する領域を追加する
				this.Wold.increase_allocate_data(old_L + buffer.getSize(), old_L + buffer.getSize());
				//新規パターンに対する重みをWnewにセット
				//ついでにWoldにもsource weightを追加セットしておく
				for (int b=0; b<buffer.getSize(); b++) {
					this.Wnew.set_data(b+old_L, b+old_L, buffer.getActualWeight(b));
					this.Wold.set_data(b+old_L, b+old_L, buffer.getSourceWeight(b));
				}
				//新規データに対する教師信号の領域をTeacherに追加セット
				for (int o=0; o<this.NumberOfOutputs; o++) {
					this.Teacher[o].increase_allocate_data(old_L+buffer.getSize(), 1);
				}


				//新規データ部分のdesign matrixをセット
				HiddenUnit h_cell;
				for (int p=0; p<buffer.getSize(); p++) {
					Enumeration<HiddenUnit> e = this.HiddenUnits.elements();
					int cell=0;
					while (e.hasMoreElements()) {
						h_cell = (HiddenUnit)e.nextElement();
						//System.out.println("PHI set (" + (old_L + p) + ", " + cell);
						this.PHI.set_data(old_L+p, cell, 
								h_cell.calculate_output(buffer.getInput(p)));
						cell++;
					}
					//Teaching signal行列をセット
					for (int o=0; o<this.NumberOfOutputs; o++) {			
						this.Teacher[o].set_data(old_L+p, 0, buffer.getOutput(p)[o]);
					}
				}

			
				//既知データ部分の新しく割り付けた中間ユニット部分をセット
				for (int p=0; p<old_L; p++) {
					double[] old_x = old_RBFNN.generate_pseudo_input2(p, old_RBFNN); //疑似サンプルの想起
					//VectorFunctions.VectorFunctions.display_vector("wRBFNN:IncLearning() pseudo sample", old_x);
					//既知パターンに対する重みを新しいkappa(=λ)で再計算してWnewにセットする
					double weight = Math.pow(MN.q_div_p(old_x), kappa);
					if (weight < LowerLimitOfWeight) weight = LowerLimitOfWeight;
					if (weight > UpperLimitOfWeight) weight = UpperLimitOfWeight;
					this.Wnew.set_data(p, p, weight); 

					for (int m=0; m<this.PHI.getM(); m++) {
						//System.out.println("PHI.m="+this.PHI.getM()+"#of hidden units=" +this.HiddenUnits.size());
						h_cell = (HiddenUnit)this.HiddenUnits.elementAt(m);
						//if (!h_cell.isFixed()) {
						this.PHI.set_data(p, m, h_cell.calculate_output(old_x));
						//}
					}
				
					double[] outputs = old_RBFNN.getOutputs(old_x);
					for (int o=0; o<this.NumberOfOutputs; o++) {
						this.Teacher[o].set_data(p, 0, outputs[o]); 
					}
				}
			
				//incWLeastSqure法
				this.IncWLeastSquare(this.HiddenUnits, this.PHI, this.Teacher, this.Wnew);
				if (this.PHI!=null) System.out.println("wRBFNN:IncLearning()" + " Phi(" + this.PHI.getL() +", " +this.PHI.getM() + ")");
			
				//学習データを圧縮保存
				this.CompressRecordPastData(buffer, old_RBFNN);
				return true;
			}else{
				return false;
			}
		}// 2回目以降の学習ならば
	}

	//MixtureOfGaussianからHiddenUnitCenterをインポートする場合(kmeansだと孤立点が１つ増えるだけで不安定になるのでEMにした）
	public void Learning(MinMoutWeightVariableSizedBuffer buffer, MixtureOfNormalDistributions mn) throws MatrixException {
		//this.DEBUG = true;
		if (this.DEBUG) {
			System.out.println("wRBFNN:Learning()");
		}
		this.init_hidden_centers(this.HiddenUnits, buffer);
		this.ImportHiddenCenters(this.HiddenUnits,
				this.rbfnn_parameters.getMinDistance(),
				mn, this.rbfnn_parameters.getMinSigma(),
				this.rbfnn_parameters.getMaxSigma(), 
				this.rbfnn_parameters.getOverlap());
		if (this.DEBUG) {
			this.display_hidden_centers(this.HiddenUnits);
		}
		wLeastSquare(this.HiddenUnits, buffer);
		//this.DEBUG = false;
		this.CompressRecordPastData(buffer, null);
	}	
	
	
	//こちらのwLeastSquareはIncrementalLearning仕様にはなっていないので注意。

	void wLeastSquare(Vector<HiddenUnit> hidden_unit, int NumberOfLearningSamples, double inputs[][], double target[][], double weights[]) throws MatrixException {
		HiddenUnit h_cell;
		MatrixObj phi = new MatrixObj(NumberOfLearningSamples, hidden_unit.size());
		MatrixObj T[] = new MatrixObj[this.NumberOfOutputs]; 
		this.Wnew = new MatrixObj(NumberOfLearningSamples, NumberOfLearningSamples);
		MatrixObj c;
		MatrixObj c_result[] = new MatrixObj[this.NumberOfOutputs];
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			T[o] = new MatrixObj(NumberOfLearningSamples, 1);
		}

		for (int p=0; p<NumberOfLearningSamples; p++) {
			this.Wnew.set_data(p, p, weights[p]);
			Enumeration<HiddenUnit> e = hidden_unit.elements();
			int m=0;
			while (e.hasMoreElements()) {
				h_cell = (HiddenUnit)e.nextElement();
				phi.set_data(p, m, h_cell.calculate_output(inputs[p]));
				m++;
			}
//			System.out.println("p=" + p);
			//System.out.println("target[0][" + p + "]=" + target[p][0]);
			for (int o=0; o<this.NumberOfOutputs; o++) {
				T[o].set_data(p, 0, target[p][o]);				
			}
		}
		this.PHI = phi;//pseudo input生成に使う
		this.Teacher = T;
		
		if (this.DEBUG) {
			//T.display("wRBFNN:wLeastSquare():T");
			phi.display("wRBFNN:wLeastSquare():phi");
		}
		c = phi.Transport().multiply(this.Wnew.Transport()); //phi^{T} W^{T}
		c = c.multiply(phi); //phi^{T} W^{T} phi
		c = c.inverse(); // (phi^{T} W^{T} phi)^{-1}
		c = c.multiply(phi.Transport()); // (phi^{T} W^{T} phi)^{-1} phi^{T}
		c = c.multiply(this.Wnew); //(phi^{T} W^{T} phi)^{-1} phi^{T} W
		this.Hat = new MatrixObj(c.getL(), c.getM(), c.getMatrix()); // hat matrix;
		this.Hat = phi.multiply(this.Hat);
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			c_result[o] = c.multiply(T[o]);// (phi^{T} W^{T} phi)^{-1} phi^{T} T
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
			//T.display("wRBFNN:wLeastSquare():T");
			phi.display("wRBFNN:wLeastSquare():phi");
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
	
	
	//Incremental wLeastSqure
	//最初の一回目はwLeastSqureを使うが二回目以降はこちらを使う。

	void IncWLeastSquare(Vector<HiddenUnit> hidden_unit, MatrixObj phi, MatrixObj T[], MatrixObj W) throws MatrixException {
		MatrixObj c;
		MatrixObj c_result[] = new MatrixObj[this.NumberOfOutputs];
		
		if (this.DEBUG) {
			//T.display("RBFNN:LeastSquare():T");
			//phi.display("wRBFNN:IncLeastSquare():phi");
			W.display("wRBFNN: IncWLeastSquare(): W");
		}
		
		c = phi.Transport().multiply(W.Transport()); //phi^{T} W^{T}
		c = c.multiply(phi); //phi^{T} W^{T} phi
		//c.display("wRBFNN: IncWLeastSquare(): c");
		c = c.inverse(); // (phi^{T} W^{T} phi)^{-1}
		c = c.multiply(phi.Transport()); // (phi^{T} W^{T} phi)^{-1} phi^{T}
		c = c.multiply(W); //(phi^{T} W^{T} phi)^{-1} phi^{T} W
		this.Hat = new MatrixObj(c.getL(), c.getM(), c.getMatrix()); // hat matrix;
		this.Hat = phi.multiply(this.Hat);		
		

		for (int o=0; o<this.NumberOfOutputs; o++) {
			c_result[o] = c.multiply(T[o]); // (phi^{T} W^{T} phi)^{-1} phi^{T} T			
		}
		this.C_result = c_result;//レコードに利用される		
		if (this.DEBUG) {
			c.display("wRBFNN:IncWLeastSquare():Wmap");
		}
		
		//得られたW_{MP}を大域変数にコピーする
		this.C = new double[hidden_unit.size()][this.NumberOfOutputs];
		for (int cell=0; cell<hidden_unit.size(); cell++) {
			for (int o=0; o<this.NumberOfOutputs; o++) {
				this.C[cell][o] = c_result[o].getData(cell, 0);
			}
		}
		System.out.println("WRBFNN:IncWLeastSquare");
		//this.PHIOld = (MatrixObj)phi.clone();
	}	
	

	boolean k_means(Vector<HiddenUnit> hidden_units, double MinDistance, double change_threshold, MinMoutWeightVariableSizedBuffer buffer, double MinSigma, double MaxSigma, double Overlap, boolean UseWeightedSamples, int KMeansMaxIteration) {
		RBF h_cell=null;
		double changes = Double.MAX_VALUE;
		double sumOfHiddenOutputs;
		int NumOfIterations = 0;

		//まず全ての中間ユニットの分散をMinSigmaに初期化する
		this.reset_variance(hidden_units, MinSigma);
		//k-means法を実行
		while (changes > change_threshold && NumOfIterations < KMeansMaxIteration) {
			changes = 0D;
			//E-step用パラメータの初期化
			this.ResetMstepParameters(hidden_units);
			
			//新規サンプルから
			for (int p=0; p<buffer.getSize(); p++) {
				sumOfHiddenOutputs = this.getSumOfHiddenOutputs(hidden_units, buffer.getInput(p));
				Enumeration<HiddenUnit> e = hidden_units.elements();
				while (e.hasMoreElements()) {
					h_cell = (RBF)e.nextElement();
					if (this.DEBUG) {
						System.out.println("wRBFNN:k_means(): sumOutput=" + sumOfHiddenOutputs);
					}
					/*if (sumOfHiddenOutputs < 1.0E-50) {
						sumOfHiddenOutputs = 0.0000001;
					}*/
					if (UseWeightedSamples) { 
						h_cell.w_M_step_add(buffer.getInput(p), sumOfHiddenOutputs, buffer.getActualWeight(p));
					}else{
						h_cell.M_step_add(buffer.getInput(p), sumOfHiddenOutputs);
					}
				}
			}
			//update center
			changes = this.UpdateCenter(hidden_units);
			NumOfIterations ++;
		}
		return this.set_variance(hidden_units, MinDistance, MinSigma, MaxSigma, Overlap);		
	}	
	

	public boolean Inc_k_means(Vector<HiddenUnit> hidden_units, //中間ユニットのリスト 
			double MinDistance,//中間ユニットの最小値
			FIFO.MinMoutWeightVariableSizedBuffer buffer, //新規パターンバッファ 
			double change_threshold, //パラメータの変化量閾値（終了条件）
			double MinSigma, //HiddenUnitの分散最小値
			double MaxSigma, //分散最大値
			double Overlap, //オーバーラップファクター
			boolean UseWeightedSamples, //Weighted k-means法をつかうかどうか
			double lambda,//λの値
			RBFNet old_RBFNN, //pseudo sample想起のための前のRBFNN
			int KMeansMaxIteration) {//k-means法の繰り返し回数
		RBF h_cell=null;
		double changes = Double.MAX_VALUE;//パラメータ変化量
		double sumOfHiddenOutputs;
		int NumOfIterations = 0;
		System.out.println("wRBFNN:Inc_k_means(): start");
		
		//全ての中間ユニットの分散をMinSigmaに初期化する
		this.reset_variance(hidden_units, MinSigma);//これを忘れるな！
		while (changes > change_threshold && NumOfIterations < KMeansMaxIteration) {
			changes = 0D;
			this.ResetMstepParameters(hidden_units);
			
			for (int p=0; p<buffer.getSize(); p++) {
				//全ユニットの出力の総和を求める（isFixed() 如何に係わらず全部)
				sumOfHiddenOutputs = this.getSumOfHiddenOutputs(hidden_units, buffer.getInput(p));
				//System.out.println("wRBFNN:Inc_k_means():sumOfHiddenOutputs=" + sumOfHiddenOutputs);
				Enumeration<HiddenUnit> e2 = hidden_units.elements();		
				while (e2.hasMoreElements()) {
					h_cell = (RBF)e2.nextElement();
					//if (!h_cell.isFixed()) {

					//新規サンプルの学習
					if (UseWeightedSamples) { 
						h_cell.w_M_step_add(buffer.getInput(p), sumOfHiddenOutputs, buffer.getActualWeight(p));
					}else{
						h_cell.M_step_add(buffer.getInput(p), sumOfHiddenOutputs);
					}				
				}// for cell
			}// for p
			//既知サンプル（疑似サンプル）
			if (old_RBFNN != null) {
				for (int q=0; q<old_RBFNN.getNumberOfLearnedSamples(); q++) {
					//疑似サンプル生成
					double[] old_x = old_RBFNN.generate_pseudo_input2(q, old_RBFNN);
					//全ユニットの出力の総和を求める
					sumOfHiddenOutputs = this.getSumOfHiddenOutputs(hidden_units, old_x);
					//System.out.println("wRBFNN:Inc_k_means():sumOfHiddenOutputs=" + sumOfHiddenOutputs);
					
					Enumeration<HiddenUnit> e3 = hidden_units.elements();
					while (e3.hasMoreElements()) {
						h_cell = (RBF)e3.nextElement();
						//System.out.println("Ink_k_means(): Generate Pseudo Samples");
						//System.out.println("wRBFNN: Ink_k_means(): finish Generate Pseudo Samples");
						
						if (this.DEBUG) {
							System.out.print(	"wRBFNN:Inc_k_means(): " + q + " pseudo sample: " + NumOfIterations);							
							
							for (int i=0; i<this.NumberOfInputs; i++) {
								System.out.print(" " + old_x[i]);
							}
							System.out.println(" ");
						}


						//M_STEPを実行
						if (UseWeightedSamples) { 
							h_cell.w_M_step_add(old_x, sumOfHiddenOutputs, Math.pow(this.Wold.getData(q,q), lambda));
						}else{
							h_cell.M_step_add(old_x, sumOfHiddenOutputs);
						}

					}// for cell
				}// for q (old samples)

			}// if old_RBFNN != null
			
			//Δuを反映させる
			changes = this.UpdateCenter(hidden_units);
			NumOfIterations++;
		}
		System.out.println("wRBFNN:Inc_k_means(): finished");
		return this.Inc_set_variance(hidden_units, MinDistance, MinSigma, MaxSigma, Overlap);
	}	
	

	
	boolean ImportHiddenCenters(Vector<HiddenUnit> hidden_units, double MinDistance, MixtureOfNormalDistributions mn, double MinSigma, double MaxSigma, double Overlap) {
		RBF h_cell=null;
		int index=0;
		Enumeration<HiddenUnit> e = hidden_units.elements();
		while (e.hasMoreElements()) {
			h_cell = (RBF)e.nextElement();
			h_cell.setCenter(mn.getDistributionCenter(index));
			index++;
		}
		return this.set_variance(hidden_units, MinDistance, MinSigma, MaxSigma, Overlap);
	}
	


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
		wRBFNN myclone = (wRBFNN) super.clone();
	    myclone.C = this.C.clone();//output connection strength
		myclone.beta = this.beta.clone();// for ARD 将来の拡張のため出力時減数分用意
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
