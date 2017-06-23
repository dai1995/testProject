package RBFNN;

import java.util.Enumeration;
import java.util.Vector;
import org.w3c.dom.Node;

import FIFO.MinMoutWeightVariableSizedBuffer;


import matrix.DiagonalMatrixObj;
import matrix.Jacobi;
import matrix.MatrixException;
import matrix.MatrixObj;

public class RBFNN implements RBFNet, Cloneable {
	final boolean DEBUG = false;
	
	Vector<HiddenUnit> HiddenUnits;
	double C[][];//output connection strength
	MatrixObj C_result[];
	int NumberOfInputs, NumberOfOutputs, NumberOfHiddenUnits;
	double init_sigma;
	public RBFNN_parameters rbfnn_parameters;
	double alpha=0;// for ARD
	double beta[];// for ARD 将来の拡張のため出力時減数分用意
	protected MatrixObj PHI, Teacher[];
	boolean WithinRegion = true;
	boolean isFirstLearning = true; //最初の学習フラグ
	String name = " ";
	
	
	/*public RBFNN(int NumberOfInputs, int NumberOfOutputs, int NumberOfHiddenUnits, double init_sigma) {
		this.NumberOfInputs = NumberOfInputs;
		this.NumberOfOutputs = NumberOfOutputs;	
		if (this.NumberOfOutputs>1) this.NumberOfOutputs=1;
		this.NumberOfHiddenUnits = NumberOfHiddenUnits;
		this.HiddenUnits = new Vector<HiddenUnit>();
		this.init_sigma = init_sigma;
		this.reset_parameters(NumberOfInputs, NumberOfHiddenUnits);
		beta = new double[this.NumberOfOutputs];
	}*/
	

	public RBFNN(Node nd) {
		if (this.DEBUG) {
			System.out.println("RBFNN.RBFNN()");
		}

		ReadParameters(nd);
	}
	
	public void ReadParameters(Node nd) {
		int NumberOfHiddenUnits;
		this.rbfnn_parameters = new RBFNN_parameters();
		this.rbfnn_parameters.getParameter(nd);
		NumberOfHiddenUnits = this.rbfnn_parameters.getNumberOfHiddenUnits();
		this.NumberOfInputs = this.rbfnn_parameters.getNumberOfInputs();
		this.NumberOfOutputs = this.rbfnn_parameters.getNumberOfOutputs();
		this.init_sigma = this.rbfnn_parameters.getInit_sigma();
		//下位クラスにて細胞数が可変になることが多く、init_parameters()を学習プロセスのと中で複数回呼ぶ
		//ために以下のように細胞数を引数で渡す定義となっている。
		this.init_parameters(NumberOfHiddenUnits);
	}
	
	public void init_parameters(int NumberOfHddenUnits) {
		if (this.DEBUG) {
			System.out.println("RBFNN.init_parameters()");
		}
	
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
	}
	

	public void reset_parameters(int AddNumberOfHiddenUnits) {
		this.isFirstLearning = true; //最初の学習フラグ
		this.setNumberOfHiddenUnits(this.NumberOfHiddenUnits+AddNumberOfHiddenUnits);
		if (this.HiddenUnits.size()>0) {
			this.HiddenUnits.removeAllElements();

		}
		for (int cell=0; cell<this.NumberOfHiddenUnits; cell++) {
			this.HiddenUnits.addElement(new RBF(this.NumberOfInputs, this.init_sigma));
		}
		this.C = new double[this.NumberOfHiddenUnits][this.NumberOfOutputs];//output connections			
	}	

	

	public double[] getOutputs(double inputs[]) {
		double sum[];
		double MinDistance = Double.MAX_VALUE;
		double each_distance = 0;
		int cell; // loop variable
		HiddenUnit h_cell, nearest_cell=null;
		Enumeration<HiddenUnit> e = this.HiddenUnits.elements();
		sum = new double[this.NumberOfOutputs];
		cell=0;
		//System.out.println("RBFNN: getOutput(): hidden unit size = " + HiddenUnits.size());
		while (e.hasMoreElements()) {
			h_cell = (HiddenUnit)e.nextElement();
			for (int o=0; o<this.NumberOfOutputs; o++) {
				sum[o] += this.C[cell][o] * h_cell.calculate_output(inputs);
			}
			each_distance = h_cell.getDistance(inputs);
			if (MinDistance > each_distance) {
				MinDistance = each_distance;
				nearest_cell = h_cell;
			}
			//sum += h_cell.calculate_output(inputs);
			cell++;
		}
		if (nearest_cell!=null) {
			this.WithinRegion = nearest_cell.WithinRegion(inputs);
		}
		return sum;
	}
	

	public double getSumOfHiddenOutputs(Vector<HiddenUnit> hiddenUnits, double inputs[]) {
		double sum;

		HiddenUnit h_cell;
		Enumeration<HiddenUnit> e = hiddenUnits.elements();
		sum = 0D;

		while (e.hasMoreElements()) {
			h_cell = (HiddenUnit)e.nextElement();
			sum += h_cell.calculate_output(inputs);
		}
		sum += Double.MIN_NORMAL;//NaNを防ぐため
		return sum;		
	}
	

	public boolean Learning(int NumberOfLearningSamples, 
			double inputs[][], double target_outputs[][]) throws MatrixException {
		//init hidden centers
		this.init_hidden_centers(this.HiddenUnits, inputs);

		
		//setup hidden units
		if (this.k_means(this.HiddenUnits,
				this.rbfnn_parameters.getMinDistance(),
				NumberOfLearningSamples, 
				this.rbfnn_parameters.getKMeansChangeThreshold(), inputs,
				this.rbfnn_parameters.getMinSigma(), 
				this.rbfnn_parameters.getMaxSigma(), 
				this.rbfnn_parameters.getOverlap(),
				this.rbfnn_parameters.getKMeansMaxIteration()) ) {
			if (this.DEBUG) {
				this.display_hidden_centers(this.HiddenUnits);
			}
		
			//setup output connections
			this.LeastSquare(this.HiddenUnits, NumberOfLearningSamples, inputs, target_outputs);
			return true;
		}else{
			return false;
		}
	}

	
	public boolean Learning(MinMoutWeightVariableSizedBuffer buffer) throws MatrixException {
		//this.DEBUG = true;
		if (this.DEBUG) {
			System.out.println("RBFNN:Learning()");
		}
		this.init_hidden_centers(this.HiddenUnits, buffer);
		if (k_means(this.HiddenUnits,
				this.rbfnn_parameters.getMinDistance(),
				this.rbfnn_parameters.getKMeansChangeThreshold(), buffer,
				this.rbfnn_parameters.getMinSigma(), this.rbfnn_parameters.getMaxSigma(), 
				this.rbfnn_parameters.getOverlap(), this.rbfnn_parameters.getKMeansMaxIteration())) {
			if (this.DEBUG) {
				this.display_hidden_centers(this.HiddenUnits);
			}
			LeastSquare(this.HiddenUnits, buffer);
			//this.DEBUG = false;
			this.CompressRecordPastData(buffer, null);
			return true;
		}else{
			return false;
		}
	}	
	
	//追記学習




	public boolean IncLearning(FIFO.MinMoutWeightVariableSizedBuffer buffer,	//bufferは新規サンプルバッファ 
			int AddNumberOfHiddenUnits,	//AddNumberOfHiddenUnitsは追加するユニット数 
			boolean UseWeightedKmeans, //重みつきkmeansを使用するかどうか
			double threshold, //thresholdは再構築対象となる中間ユニットの出力の最小値 
			RBFNN old_RBFNN) throws MatrixException  { //疑似サンプル生成用の前回までの暫定１位モデル
		HiddenUnit h_cell; //中間ユニット
		if (this.DEBUG) {
			System.out.println("wRBFNN:IncLearning()");
		}
		if (this.HiddenUnits.size()==0) {//最初は従来通りの学習法
				this.Learning(buffer);
				return true;
		}else{//二回目以降は以下を実行
			//再構築対象となる中間ユニットのフラグを立てる
			//this.check_isVariable(buffer, threshold);
		
			//新しく割り付けた中間ユニットのパラメータを初期化する
			this.init_newhidden_centers(this.HiddenUnits, 
				AddNumberOfHiddenUnits, buffer);

			//新しく割り付けた中間ユニット、再構成するユニットをkMeans法で中心位置を決める
			if (this.Inc_k_means(this.HiddenUnits,
					this.rbfnn_parameters.getMinDistance(),
					buffer, this.rbfnn_parameters.getKMeansChangeThreshold(), 
					this.rbfnn_parameters.getMinSigma(), 
					this.rbfnn_parameters.getMaxSigma(), 
					this.rbfnn_parameters.getOverlap(),
					old_RBFNN, 
					this.rbfnn_parameters.getKMeansMaxIteration())) {//中間ユニット割り付け成功（縮退無し）
				
				//design matrixの再構築(縦方向に新規データ分、横方向に追加するユニット数分増やす)
				int old_L=this.PHI.getL();//新旧区別するために後で使う
				int old_M=this.PHI.getM();//新旧区別するために後で使う
				this.PHI.increase_allocate_data(this.PHI.getL()+buffer.getSize(), this.PHI.getM()+AddNumberOfHiddenUnits);
				for (int o=0; o<this.NumberOfOutputs; o++) {
					this.Teacher[o].increase_allocate_data(old_M+buffer.getSize(), 1);
				}
		
				//新規データ部分のdesign matrixをセット
				for (int p=0; p<buffer.getSize(); p++) {
					Enumeration<HiddenUnit> e = this.HiddenUnits.elements();
					int cell=0;
					while (e.hasMoreElements()) {
						h_cell = (HiddenUnit)e.nextElement();
						this.PHI.set_data(old_L+p, cell, 
								h_cell.calculate_output(buffer.getInput(p)));
					}
					//Teaching signal行列をセット
					for (int o=0; o<this.NumberOfOutputs; o++) {			
						this.Teacher[o].set_data(old_L+p, 0, buffer.getOutput(p)[o]);
					}
				}
		
				//既知データ部分の新しく割り付けた中間ユニット部分をセット
				for (int p=0; p<old_L; p++) {
					double[] old_x = this.generate_pseudo_input2(p, old_RBFNN); //疑似サンプルの想起
					for (int m=0; m<this.PHI.getM(); m++) {
						h_cell = (HiddenUnit)this.HiddenUnits.elementAt(0);
						//if (!h_cell.isFixed()) {
						this.PHI.set_data(p, m, h_cell.calculate_output(old_x));
						//}
					}
				}
		
				//LeastSqure法
				this.IncLeastSquare(this.HiddenUnits, this.PHI, this.Teacher);
				//学習データを圧縮保存
				this.CompressRecordPastData(buffer, old_RBFNN);
				return true;
			}else{
				return false;//学習失敗信号
			}
		}
	}
	
	public void ARD(int NumberOfLearningSamples, double inputs[][], double target_outputs[][], double termination_threshold) throws MatrixException {
		double previous_alpha;
		double previous_beta[];
		//init hidden centers
		this.init_hidden_centers(this.HiddenUnits, inputs);
		previous_beta = new double[this.NumberOfOutputs];
		//init alpha, beta;
		this.alpha = previous_alpha = 10;
		this.beta[0] = previous_beta[0] = 1; //今は１次元なので０に入れる
		
		//setup hidden units
		this.k_means(this.HiddenUnits,
				this.rbfnn_parameters.getMinDistance(),
				NumberOfLearningSamples, this.rbfnn_parameters.getKMeansChangeThreshold(), inputs,
				this.rbfnn_parameters.getMinSigma(), this.rbfnn_parameters.getMaxSigma(), this.rbfnn_parameters.getOverlap(),
				this.rbfnn_parameters.getKMeansMaxIteration());
		if (this.DEBUG) {
			this.display_hidden_centers(this.HiddenUnits);
		}
		
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
	
	void init_hidden_centers(Vector<HiddenUnit> hidden_units, FIFO.MinMoutWeightVariableSizedBuffer buffer) {
		Enumeration<HiddenUnit> e = hidden_units.elements();
		boolean isExist = false;
		RBF target_h_cell, h_cell;

		int p=0;
		int init_cell_number=0; //初期化した細胞数
		System.out.println("RBFNN:init_hidden_centers()");
		

		while (e.hasMoreElements()) {
			isExist = false;//初期化
			//発火した細胞があるかどうかを調べる
				this.getOutputs(buffer.getInput(p));

			Enumeration<HiddenUnit> e1 = this.HiddenUnits.elements();
			for (int q=0; q<init_cell_number && e1.hasMoreElements(); q++) {
				h_cell = (RBF)e1.nextElement();
				if (h_cell.getOutput()>this.rbfnn_parameters.getMinActivationThreshold()) {//発火している
					//フラグを立てる
					isExist = true;
				}
			}
			if (!isExist) {
				target_h_cell = (RBF)e.nextElement();
				target_h_cell.setCenter(buffer.getInput(p));
				init_cell_number ++; //初期化した細胞数
				//if (this.DEBUG) {
					System.out.println("RBFNN:init_hidden_center(): buffer.getInput(" + p + ")=" + buffer.getInput(p)[0]);
				//}
			}
			p++;
		}
	}	
	// input[][] -> buffer
	//追記学習時に使う。AddNumberOfHiddenUnitsは追加する細胞数
	//bufferは新規サンプルバッファ
	int init_newhidden_centers(Vector<HiddenUnit> hidden_units,//中間細胞のベクター 
			int AddNumberOfHiddenUnits, //新しく割り付ける中間ユニット数 
			MinMoutWeightVariableSizedBuffer buffer) {//新規サンプルバッファ
		RBF h_cell;//中間ユニット
		boolean isExist = false;//発火している細胞がある場合はTrue
		int cell=0;//追加した中間ユニット数を数えるためのループ変数
		
		if (AddNumberOfHiddenUnits==0) return 0;
		for (int p=0; p<buffer.getSize(); p++) {
			//まずはfalseに初期化しておく
			isExist = false;
			//p番目のデータにたいする出力を計算する
			this.getOutputs(buffer.getInput(p));
			
			//発火した細胞があるかどうかを調べる
			Enumeration<HiddenUnit> e = this.HiddenUnits.elements();
			while (e.hasMoreElements()) {
				h_cell = (RBF)e.nextElement();
				if (h_cell.getOutput()>this.rbfnn_parameters.getMinActivationThreshold()) {//発火している
					//フラグを立てる
					isExist = true;
				}
			}
			
			if (!isExist) {//発火している細胞が無いなら
				//新く中間ユニットを割り付ける
				h_cell = new RBF(NumberOfInputs, this.init_sigma);
				//新く割り付けた中間ユニットの中心位置をp版目のデータとする
				h_cell.setCenter(buffer.getInput(p));
				//新く割り付けた中間ユニットをリストに含める
				this.HiddenUnits.addElement(h_cell);
				//中間ユニット数の変数this.NumberOfHiddenUnitsをインクリメントする
				this.setNumberOfHiddenUnits(this.NumberOfHiddenUnits+1);
				//中間ユニットから出力ユニットへのコネクションを作りなおす
				this.C = new double[this.NumberOfHiddenUnits][this.NumberOfOutputs];//output connections
				//ループ変数もインクリメント
				cell++;
			}
			//ループ変数が指定した細胞数に達したら終了する
			if (cell == AddNumberOfHiddenUnits) break;
		}
		//追加した細胞数を返す。(場合によっては指定した細胞数よりも少なくなることがあるから)
		return cell;
	}//	init_newhidden_center()	
	
	

	void display_hidden_centers(Vector<HiddenUnit> hidden_units) {
		Enumeration<HiddenUnit> e = hidden_units.elements();
		HiddenUnit h_cell;
		int p=0;
		while (e.hasMoreElements()) {
			h_cell = (HiddenUnit)e.nextElement();
			System.out.print("RBFNN:display_hidden_center():HiddenCenter(" + p + ")= " );
			for (int i=0; i<this.NumberOfInputs; i++) {
				System.out.print(" " + h_cell.getCenter()[i]);
			}
			System.out.println(" ");
			p++;
		}		
	}

	//k-means法のためのパラメータの初期化。E-stepの前に実行する
	void ResetMstepParameters(Vector<HiddenUnit> hidden_units) {
		HiddenUnit h_cell=null;
		Enumeration<HiddenUnit> e1 = hidden_units.elements();
		//reset internal parameters
		while (e1.hasMoreElements()) {
			h_cell = (HiddenUnit)e1.nextElement();
			h_cell.init_M_step();
		}		
	}

	//k-means法のためのM-step
	double UpdateCenter(Vector<HiddenUnit> hidden_units) {
		double total_change_in_parameters = 0D;
		HiddenUnit h_cell;
		//update center(M-Step)
		Enumeration<HiddenUnit> e = hidden_units.elements();		
		while (e.hasMoreElements()) {
			h_cell = (HiddenUnit)e.nextElement();			
			total_change_in_parameters += h_cell.M_step_update_center();
		}	
		return total_change_in_parameters/(double)hidden_units.size();
	}
	
	
	boolean k_means(Vector<HiddenUnit> hidden_units, double MinDistance, int NumberOfLearningSamples, double change_threshold, double input[][],  double MinSigma, double MaxSigma, double Overlap, int KMeansMaxIteration) {
		HiddenUnit h_cell=null;
		double changes = Double.MAX_VALUE;
		double sumOfHiddenOutputs;
		int iteration=0;
		while (changes > change_threshold && iteration < KMeansMaxIteration) {
			changes = 0;
			//E-step用パラメータの初期化
			this.ResetMstepParameters(hidden_units);
			//E-step
			for (int p=0; p<NumberOfLearningSamples; p++) {
				sumOfHiddenOutputs = this.getSumOfHiddenOutputs(hidden_units, input[p]);
				Enumeration<HiddenUnit> e2 = hidden_units.elements();		
				while (e2.hasMoreElements()) {
					h_cell = (HiddenUnit)e2.nextElement();
					h_cell.M_step_add(input[p], sumOfHiddenOutputs);
				}
			}
			//M-step パラメータの変化量も計測
			changes = this.UpdateCenter(hidden_units);
			iteration++;
		}
		//最近傍距離に比例するvarianceをセット
		return this.set_variance(hidden_units, MinDistance, MinSigma, MaxSigma, Overlap);		
	}
	

	boolean k_means(Vector<HiddenUnit> hidden_units, double MinDistance, double change_threshold, MinMoutWeightVariableSizedBuffer buffer, double MinSigma, double MaxSigma, double Overlap, int KMeansMaxIteration) {
		HiddenUnit h_cell=null;
		double changes = Double.MAX_VALUE;
		double sumOfHiddenOutputs;
		int iteration=0;
		
		while (changes > change_threshold && iteration < KMeansMaxIteration) {
			
			changes = 0D;
			this.ResetMstepParameters(hidden_units);
			
			for (int p=0; p<buffer.getSize(); p++) {
				sumOfHiddenOutputs = this.getSumOfHiddenOutputs(hidden_units, buffer.getInput(p));
				Enumeration<HiddenUnit> e2 = hidden_units.elements();						
				while (e2.hasMoreElements()) {
					h_cell = (RBF)e2.nextElement();
					if (this.DEBUG) {
						System.out.println("RBFNN:k_means(): sumOutput=" + sumOfHiddenOutputs);
					}
					/*if (sumOfHiddenOutputs < 1.0E-50) {
						sumOfHiddenOutputs = 0.0000001;
					}*/
					h_cell.M_step_add(buffer.getInput(p), sumOfHiddenOutputs);
				}
			}
			
			changes = this.UpdateCenter(hidden_units);
			iteration++;
		}
		return this.set_variance(hidden_units, MinDistance, MinSigma, MaxSigma, Overlap);		
	}	

	

	public boolean Inc_k_means(Vector<HiddenUnit> hidden_units, 
			double MinDistance,//中間ユニット同士の距離がこの値以下になったら失敗とする 
			FIFO.MinMoutWeightVariableSizedBuffer buffer, //新規サンプルバッファ
			double change_threshold, //パラメータ変化量の合計がこの値以下となったら止める
			double MinSigma, //分散の最小値
			double MaxSigma, //分散の最大値 
			double Overlap,  //オーバラップファクター
			RBFNN old_RBFNN, //疑似サンプル生成用の以前の暫定１位モデル
			int KMeansMaxIteration) { //繰り返し計算の最大回数
		HiddenUnit h_cell=null;
		double changes = Double.MAX_VALUE;
		double sumOfHiddenOutputs;
		int iteration=0;

		while (changes > change_threshold && iteration < KMeansMaxIteration) {
			changes = 0D;
			//Mstepのためのパラメータを初期化する
			this.ResetMstepParameters(hidden_units);
			
			//新規サンプルの学習
			for (int p=0; p<buffer.getSize(); p++) {
				sumOfHiddenOutputs = this.getSumOfHiddenOutputs(hidden_units, buffer.getInput(p));
				Enumeration<HiddenUnit> e = hidden_units.elements();		
				while (e.hasMoreElements()) {
					h_cell = (HiddenUnit)e.nextElement();
					//if (!h_cell.isFixed()) {
					h_cell.M_step_add(buffer.getInput(p), sumOfHiddenOutputs);
				}
			}
			//既知サンプル（疑似サンプル）
			if (old_RBFNN!=null) {
				for (int q=0; q<old_RBFNN.getNumberOfLearnedSamples(); q++) {
					double[] old_x = old_RBFNN.generate_pseudo_input(q,old_RBFNN);
					Enumeration<HiddenUnit> e = hidden_units.elements();
					while (e.hasMoreElements()) {
						h_cell = (HiddenUnit)e.nextElement();
						sumOfHiddenOutputs = this.getSumOfHiddenOutputs(hidden_units, old_x);
						h_cell.M_step_add(old_x, sumOfHiddenOutputs);
					}
				}

			}
			
			//Update center
			changes = this.UpdateCenter(hidden_units);
			iteration ++;
		}
		return this.Inc_set_variance(hidden_units, MinDistance, MinSigma, MaxSigma, Overlap);		
	}	
	
//	@SuppressWarnings("unchecked")
	/*public void check_isVariable(FIFO.MinMoutWeightVariableSizedBuffer buffer, double threshold) {
		for (int p=0; p<buffer.getSize(); p++) {
			Enumeration e = this.HiddenUnits.elements();
			while (e.hasMoreElements()) {
				HiddenUnit h_cell = (HiddenUnit)e.nextElement();
				if (h_cell.calculate_output(buffer.getInput(p))>threshold) {
					h_cell.setFixed(false);
				}else{
					//h_cell.setFixed(true);
					h_cell.setFixed(false);
				}
			}
		}
	}*/
	
	boolean set_variance(Vector<HiddenUnit> hidden_units, double MinDistance, double MinSigma, double MaxSigma, double Overlap) {
		Enumeration<HiddenUnit> e1 = hidden_units.elements();
		HiddenUnit h_cell, t_cell;
		double result;
		while (e1.hasMoreElements()) {
			double min_distance = Double.MAX_VALUE;
			h_cell = (HiddenUnit)e1.nextElement();
			Enumeration<HiddenUnit> e2 = hidden_units.elements();
			while (e2.hasMoreElements()) {
				t_cell = (HiddenUnit)e2.nextElement();
				if (t_cell != h_cell) {
					min_distance = Math.min(min_distance, h_cell.getDistance(t_cell.getCenter()));
				}
			}

			//if (this.DEBUG) {
				System.out.println("RBFNN:set_variance: min_distance = " + min_distance);
			//}
			if (min_distance < MinDistance) return false; //失敗：縮退が発生した
			result = Overlap * Math.sqrt(min_distance);
			if (result > MaxSigma) result = MaxSigma;
			if (result < MinSigma) result = MinSigma;
			h_cell.setVariance(result);
		}
		return true;
	}

	//近傍距離から\sigmaをセットする
	public boolean Inc_set_variance(Vector<HiddenUnit> hidden_units, double MinDistance, double MinSigma, double MaxSigma, double Overlap) {
		Enumeration<HiddenUnit> e1 = hidden_units.elements();
		HiddenUnit h_cell, t_cell;
		double result;
		System.out.println("RBFNN.Inc_set_variance(): # of hidden units: " + hidden_units.size());
		while (e1.hasMoreElements()) {
			double min_distance = Double.MAX_VALUE;
			h_cell = (HiddenUnit)e1.nextElement();
			//if (!h_cell.isFixed()) {
				Enumeration<HiddenUnit> e2 = hidden_units.elements();
				while (e2.hasMoreElements()) {
					t_cell = (HiddenUnit)e2.nextElement();
					if (!t_cell.equals(h_cell)) {
						min_distance = Math.min(min_distance, h_cell.getDistance(t_cell.getCenter()));
					}
				}

				if (min_distance<MinDistance) return false;
				result = Overlap * Math.sqrt(min_distance);
				if (this.DEBUG) {
					System.out.println("RBFNN:set_variance: min_distance = " + min_distance + " result=" + result);
				}				
				if (result > MaxSigma) result = MaxSigma;
				if (result < MinSigma) result = MinSigma;
				h_cell.setVariance(result);
			//}
		}
		return true;
	}
	
	void reset_variance(Vector<HiddenUnit> hidden_units, double MinSigma) {
		HiddenUnit h_cell;
		Enumeration<HiddenUnit> e = hidden_units.elements();
		while (e.hasMoreElements()) {
			h_cell = (HiddenUnit)e.nextElement();
			h_cell.setVariance(MinSigma);
		}
	}
	

	void LeastSquare(Vector<HiddenUnit> hidden_unit, int NumberOfLearningSamples, double inputs[][], double target[][]) throws MatrixException {
		HiddenUnit h_cell;
		MatrixObj phi = new MatrixObj(NumberOfLearningSamples, hidden_unit.size());
		MatrixObj T[] = new MatrixObj[this.NumberOfOutputs];
		MatrixObj c;
		MatrixObj c_result[] = new MatrixObj[this.NumberOfOutputs];
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
				T[o] =  new MatrixObj(NumberOfLearningSamples, 1);
		}
		
		for (int p=0; p<NumberOfLearningSamples; p++) {
			Enumeration<HiddenUnit> e = hidden_unit.elements();
			int m=0;
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
		this.Teacher = T;
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
		this.C_result = c_result;//レコードに利用される		
		if (this.DEBUG) {
			c.display("RBFNN:LeastSquare():Wmap");
		}

		//得られたW_{MP}を大域変数にコピーする
		for (int cell=0; cell<hidden_unit.size(); cell++) {
			for (int o=0; o<this.NumberOfOutputs; o++) {
				this.C[cell][o] = c_result[o].getData(cell, 0);
			}
		}
		System.out.println("RBFNN:LeastSquare");
	}
	
	
	//圧縮された形でデータを保存する方法
	int[] WinnerCell;
	MatrixObj TargetOutputs;
	void CompressRecordPastData(FIFO.MinMoutWeightVariableSizedBuffer buffer, 
			RBFNet old_RBFNN) {
		//データの初期化
		if (this.PHI.getL()>0) {
			this.TargetOutputs = null;
		}

		this.RecordNearestCell(buffer, old_RBFNN);
		
		//最も大きな出力を持つ中間ユニットを探す(これではうまくいかないことがある)
		//int winner_cell = -1;
		/*double max_output = -Double.MAX_VALUE;
		for (int index=0; index<this.PHI.getL(); index ++) {
			max_output = -Double.MAX_VALUE;
			winner_cell = -1;
			for (int j=0; j<this.PHI.getM(); j++) {//バイアスが無いので０から始まる
				if (max_output < this.PHI.getData(index, j)) {
					max_output = this.PHI.getData(index, j);
					winner_cell = j;
				}
			}
			if (winner_cell != -1) {
				this.WinnerCell[index] = winner_cell;
			}
		}*/
		
		//this.PHI.display("RBFNN.CompressRecordPastData(): PHI");
		//this.C_result[0].display("RBFNN.CompressRecordPastData(): C_result[0]");
		
		this.TargetOutputs = this.PHI.multiply(this.C_result[0]);
		if (this.DEBUG) {
			this.TargetOutputs.display("RBFNN:CompressRecordPastData(): TargetOutputs");
		}
	}
	
	void RecordNearestCell(FIFO.MinMoutWeightVariableSizedBuffer buffer, 
			RBFNet old_RBFNN) {
		HiddenUnit h_cell;
		double min_distance = Double.MIN_VALUE;
		double each_distance;
		int cell_index, offset=0;
		
		if (old_RBFNN == null) {
			this.WinnerCell = new int[buffer.getSize()];
			offset = 0;
		}else{
			this.WinnerCell = new int[buffer.getSize()+old_RBFNN.getNumberOfLearnedSamples()];
			offset = old_RBFNN.getNumberOfLearnedSamples();
		}

		if (old_RBFNN != null) {
			//先に既知サンプルに対する最近傍細胞を探す
			for (int p=0; p<old_RBFNN.getNumberOfLearnedSamples(); p++) {
				min_distance = Double.MAX_VALUE;
				Enumeration<HiddenUnit> e = this.HiddenUnits.elements();
				cell_index = 0;
				while (e.hasMoreElements()) {
					h_cell = (HiddenUnit)e.nextElement();
					each_distance =  h_cell.getDistance(old_RBFNN.generate_pseudo_input2(p, old_RBFNN));
					//System.out.println("(cellindex=" + cell_index + ", distance=" + each_distance);
					if (each_distance < min_distance) {
						min_distance = each_distance;
						this.WinnerCell[p] = cell_index; 
						//System.out.println("(nearest cell=" + cell_index);
					}
					cell_index++;
				}
			}
		}
		
		//新規サンプルに対する最近傍細胞を探す
		for (int n=0; n<buffer.getSize(); n++) {
			min_distance = Double.MAX_VALUE;
			Enumeration<HiddenUnit> e = this.HiddenUnits.elements();
			cell_index = 0;
			while (e.hasMoreElements()) {
				h_cell = (HiddenUnit)e.nextElement();
				each_distance =  h_cell.getDistance(buffer.getInput(n));
				if (each_distance < min_distance) {
					min_distance = each_distance;
					this.WinnerCell[offset+n] = cell_index; 
				}
				cell_index++;
			}
		}		
	}
	
	void LeastSquare(Vector<HiddenUnit> hidden_unit, FIFO.MinMoutWeightVariableSizedBuffer buffer) throws MatrixException {
		HiddenUnit h_cell;
		MatrixObj phi = new MatrixObj(buffer.getSize(), hidden_unit.size());
		MatrixObj T[] = new MatrixObj[this.NumberOfOutputs];
		MatrixObj c;
		MatrixObj c_result[] = new MatrixObj[this.NumberOfOutputs];
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
				T[o] =  new MatrixObj(buffer.getSize(), 1);
		}
		
		for (int p=0; p<buffer.getSize(); p++) {
			Enumeration<HiddenUnit> e = hidden_unit.elements();
			int m=0;
			while (e.hasMoreElements()) {
				h_cell = (HiddenUnit)e.nextElement();
				phi.set_data(p, m, h_cell.calculate_output(buffer.getInput(p)));
				m++;
			}
//			System.out.println("p=" + p);
			for (int o=0; o<this.NumberOfOutputs; o++){
				T[o].set_data(p, 0, buffer.getOutput(p)[o]);
			}
		}
		this.PHI = phi;//pseudo input生成に使う
		this.Teacher = T;
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
		this.C_result = c_result;//レコードに利用される
		if (this.DEBUG) {
			c.display("RBFNN:LeastSquare():Wmap");
		}

		//得られたW_{MP}を大域変数にコピーする
		for (int cell=0; cell<hidden_unit.size(); cell++) {
			for (int o=0; o<this.NumberOfOutputs; o++) {
				this.C[cell][o] = c_result[o].getData(cell, 0);
			}
		}
		System.out.println("RBFNN:LeastSquare");
	}
	
	void LeastSquare_with_regularization(Vector<HiddenUnit> hidden_unit, int NumberOfLearningSamples, double inputs[][], double target[][], double alpha, double beta) throws MatrixException {
		HiddenUnit h_cell;
		MatrixObj phi = new MatrixObj(NumberOfLearningSamples, hidden_unit.size());
		MatrixObj T[] = new MatrixObj[this.NumberOfOutputs];
		MatrixObj c;
		MatrixObj c_result[] = new MatrixObj[this.NumberOfOutputs];
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
				T[o] =  new MatrixObj(NumberOfLearningSamples, 1);
		}
		
		for (int p=0; p<NumberOfLearningSamples; p++) {
			Enumeration<HiddenUnit> e = hidden_unit.elements();
			int m=0;
			while (e.hasMoreElements()) {
				h_cell = (HiddenUnit)e.nextElement();
				phi.set_data(p, m, h_cell.calculate_output(inputs[p]));
				m++;
			}
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
		//c = c.multiply(T);
		for (int o=0; o<this.NumberOfOutputs; o++) {
			c_result[o] = c.multiply(T[o]); // (phi^{T} phi)^{-1} phi^{T} t
		}
		this.C_result = c_result;//レコードに利用される
		if (this.DEBUG) {
			c.display("RBFNN:LeastSquare():Wmap");
		}
		
		//得られたW_{MP}を大域変数にコピーする
		for (int cell=0; cell<hidden_unit.size(); cell++) {
			for (int o=0; o<this.NumberOfOutputs; o++) {
				this.C[cell][o] = c_result[o].getData(cell, 0);
			}
		}
		System.out.println("RBFNN:LeastSquare");

	}
	

	void IncLeastSquare(Vector<HiddenUnit> hidden_unit, MatrixObj phi, MatrixObj T[]) throws MatrixException {
		MatrixObj c;
		MatrixObj c_result[] = new MatrixObj[this.NumberOfOutputs];
		
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
		this.C_result = c_result;//レコードに利用される
		if (this.DEBUG) {
			c.display("RBFNN:IncLeastSquare():Wmap");
		}

		//得られたW_{MP}を大域変数にコピーする
		for (int cell=0; cell<hidden_unit.size(); cell++) {
			for (int o=0; o<this.NumberOfOutputs; o++) {
				this.C[cell][o] = c_result[o].getData(cell, 0);
			}
		}
		System.out.println("RBFNN:IncLeastSquare");
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

	//デザインマトリックスをそのまま利用する疑似データ生成法（メモリを食う）
	public double[] generate_pseudo_input(int index, RBFNet old_RBFNN) {
		HiddenUnit h_cell;
		//最も大きな出力を持つ中間ユニットを探す
		//this.PHI.display("PHI");
		double max_output = -9999999D;
		int winner_cell = -1;
		for (int j=0; j<old_RBFNN.getPhiSizeM(); j++) {
			h_cell = (HiddenUnit)old_RBFNN.getHiddenUnits().elementAt(j);
			if (max_output < old_RBFNN.getPHI().getData(index, j)) {
				max_output = old_RBFNN.getPHI().getData(index, j);
				winner_cell = j;
			}
		}
		
		//疑似入力の初期化
		double pseudo_x[];
		h_cell = (HiddenUnit)old_RBFNN.getHiddenUnits().elementAt(winner_cell);
		pseudo_x = h_cell.getCenter();
		for (int i=0; i<this.NumberOfInputs; i++) {//小さな摂動を与えておく
			pseudo_x[i] += 0.001 * (double)2*(Math.random()-0.5);
		}
		
		//疑似入力の更新
		double[] Delta_x = new double[this.NumberOfInputs];
		double gain, hidden_output, err=Double.MAX_VALUE, prev_err=Double.MAX_VALUE;
		do {
			prev_err = err;
			err = 0;
			for (int h=0; h<old_RBFNN.getNumberOfHiddenUnits(); h++) {
				h_cell = (HiddenUnit)old_RBFNN.getHiddenUnits().elementAt(h);
				hidden_output = h_cell.calculate_output(pseudo_x);
				gain = hidden_output * (hidden_output - old_RBFNN.getPHI().getData(index, h));
				for (int i=0; i<this.NumberOfInputs; i++) {
					Delta_x[i] += 2 * this.rbfnn_parameters.get_inverse_map_eta() * ((pseudo_x[i] - h_cell.getCenter()[i])/
							Math.pow(h_cell.getVariance(),2D)) * gain;
				}
				err += Math.pow(hidden_output - old_RBFNN.getPHI().getData(index, h), 2);
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
	
	//WinnerCell,TargetOutputダケを使う疑似データ生成法
	public double[] generate_pseudo_input2(int index, RBFNet old_RBFNN) {
		HiddenUnit h_cell;
		//System.out.println("RBFNN:generate_pseudo_input2()");
		//最も大きな出力を持つ中間ユニットを得る
		int winner_cell = old_RBFNN.getWinnerCell()[index];
		//System.out.println("RBFNN: old_RBFNN is " + old_RBFNN.getName());
		//System.out.println("RBFNN: generate_pseudo_input2(): winner_cell is " + winner_cell);
		//疑似入力の初期化
		double pseudo_x[]= new double[this.NumberOfInputs];
		h_cell = (HiddenUnit)old_RBFNN.getHiddenUnits().elementAt(winner_cell);
		for (int i=0; i<this.NumberOfInputs; i++) {
			pseudo_x[i] = h_cell.getCenter()[i];
		}
		for (int i=0; i<this.NumberOfInputs; i++) {//小さな摂動を与えておく
			pseudo_x[i] += 0.01 * (double)2*(double)(Math.random()-0.5);
		}
		
		//疑似入力の更新
		double[] Delta_x = new double[this.NumberOfInputs];
		double gain, gain2, err=Double.MAX_VALUE;
		int NumberOfIterations=0;
		do {
			err = 0;
			//System.out.println("generate_pseudo_input2: targetOutput=" + this.TargetOutputs.getData(index, 0));
			gain = 2 * (this.TargetOutputs.getData(index, 0)-old_RBFNN.getOutputs(pseudo_x)[0]);
			for (int h=0; h<old_RBFNN.getNumberOfHiddenUnits(); h++) {
				h_cell = (HiddenUnit)old_RBFNN.getHiddenUnits().elementAt(h);

				gain2 = this.C[h][0] * h_cell.calculate_output(pseudo_x) / Math.pow(h_cell.getVariance(),2D);
				for (int i=0; i<this.NumberOfInputs; i++) {
					Delta_x[i]-= gain * gain2 * ((pseudo_x[i] - h_cell.getCenter()[i]));
					//Delta_x[i]+= gain * gain2 * this.rbfnn_parameters.get_inverse_map_eta() * ((pseudo_x[i] - h_cell.getCenter()[i]));
				}
			}
			//疑似入力の更新
			for (int i=0; i<this.NumberOfInputs; i++) {
				pseudo_x[i] += this.rbfnn_parameters.get_inverse_map_eta() * Delta_x[i];
				Delta_x[i]=0;
			}
			err = Math.pow((this.TargetOutputs.getData(index, 0)-old_RBFNN.getOutputs(pseudo_x)[0]), 2);						
			//System.out.println("RBFNN:generate_pseudo_input(): err=" + err + " g_x=" + pseudo_x[0]);
			NumberOfIterations ++;
		}while (NumberOfIterations < this.rbfnn_parameters.getMaxNumberOfInverseIterations() &&
				err > this.rbfnn_parameters.get_inverse_map_stopCondition());
		return	pseudo_x;
	}	
	
	public int getNumberOfLearnedSamples() {
		return this.PHI.getL();
	}
	
	
	public String getGnuPlot() {
		HiddenUnit h_cell;
		int cell_index = 0;
		String plot_str = "plot ";
		Enumeration<HiddenUnit> e = this.HiddenUnits.elements();
		while (e.hasMoreElements()) {
			h_cell = (HiddenUnit)e.nextElement();
			plot_str += this.C[cell_index][0] + " * " + h_cell.getPlot();
			cell_index ++;
			if (cell_index < this.HiddenUnits.size()) {
				plot_str += ", ";
			}
		}
		return plot_str;
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


	public Vector<HiddenUnit> getHiddenUnits() {
		return this.HiddenUnits;
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

	
	@SuppressWarnings("unchecked")
	public Object clone() {  
	    try {
	    	RBFNN myclone = (RBFNN)super.clone();
	    	myclone.C = C.clone();//output connection strength
	    	myclone.beta = beta.clone();// for ARD 将来の拡張のため出力時減数分用意
	    	myclone.PHI=PHI.clone();
	    	myclone.Teacher = Teacher.clone();
	    	myclone.HiddenUnits = (Vector<HiddenUnit>)HiddenUnits.clone();
	    	myclone.name = this.name;
	    	myclone.WinnerCell = this.WinnerCell;
	    	myclone.TargetOutputs = this.TargetOutputs.clone();
	    	Enumeration<HiddenUnit> e = this.HiddenUnits.elements();
	    	int i=0;
	    	while (e.hasMoreElements()) {
	    		RBF h_cell = (RBF)e.nextElement();
	    		myclone.HiddenUnits.setElementAt(h_cell.clone(), i);
	    		i++;
	    	}
	    	return myclone;
	    } catch (CloneNotSupportedException e) {  
	         return null;  
	    }  
	}

	/**
	 * @return the withinRegion
	 */
	public boolean isWithinRegion() {
		return WithinRegion;
	}

	/**
	 * @return the winnerCell
	 */
	public int[] getWinnerCell() {
		return WinnerCell;
	}

	/**
	 * @return the targetOutputs
	 */
	public MatrixObj getTargetOutputs() {
		return TargetOutputs;
	}

	/**
	 * @return the c
	 */
	public double[][] getC() {
		return C;
	}

	/**
	 * @return the pHI
	 */
	public MatrixObj getPHI() {
		return PHI;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
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
