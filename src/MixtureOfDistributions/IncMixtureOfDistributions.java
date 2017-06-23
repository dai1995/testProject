package MixtureOfDistributions;

import java.util.Enumeration;
import java.util.Vector;

import matrix.MatrixException;
import matrix.MatrixObj;
import DataLoad.dataload;
import FIFO.MinMoutWeightVariableSizedBuffer;
import RBFNN.RBFNet;

public class IncMixtureOfDistributions extends MixtureOfNormalDistributions implements Cloneable {
	protected boolean DEBUG = false;
	protected int AddNumberOfModels;
	protected int target_date;
	protected double AIC_value=0;
	
	public IncMixtureOfDistributions(dataload dl, int NumberOfModels,
			int NumberOfInputs, double activation_threshold, double init_sigma_value, boolean isDiagonal,
			boolean SMD, boolean RANInitMode) {

		super(dl, NumberOfModels, NumberOfInputs, activation_threshold, init_sigma_value, isDiagonal,
				SMD, RANInitMode);
		if (this.DEBUG) {
			System.out.println("IncMixtureOfDistributions: # of models=" + this.HiddenNodes.size());
		}

		// TODO Auto-generated constructor stub
	}
	
		

	public double EM(dataload dl, int target_date, RBFNet rbfnn, MixtureOfDistributionParameters parameter) throws MatrixException {
		//FIFO.MinNoOutputsVariableSizedBuffer buffer = CorrectDatasets(dl, target_date, rbfnn, parameter.GammaThreshold, 2);//関連するデータのみを想起して貯める
		int NumberOfIterations=0;

		reset_variance_covariance_matrix(parameter.getMin_sigma());
		do {
			init_parameters();			
			E_step(dl, target_date, rbfnn);
			NumberOfIterations++;
		}while(M_step(parameter.getMin_sigma())>parameter.getStop_threshold() &&
				NumberOfIterations < parameter.getMaxNumberOfIterations());
		System.out.println("IncMixtureOfIdistribution.EM(): Number of EM iterations:" + NumberOfIterations);
		this.EstimateNumberOfClassMember(dl, target_date, rbfnn);
		this.AIC_value = this.AIC(dl, target_date, this.isDiagonal, rbfnn);
		if (this.DEBUG) {

			System.out.println("IncMixtureOfDistribution.EM() total number of members " + this.getTotalNumberOfMembers());

			Enumeration<Object> e = this.HiddenNodes.elements();
			while (e.hasMoreElements()) {
				Distribution dis = (Distribution)e.nextElement();
				dis.getSigma().display("Sigma");
				dis.GetCenter().display("center");
			}
		}
		return this.AIC_value;
	}

	//過去のサンプルをブッファに貯めて利用するVersion
	public double EM(dataload dl, int target_date, MinMoutWeightVariableSizedBuffer old_pattern_buffer, MixtureOfDistributionParameters parameter) throws MatrixException {
		//FIFO.MinNoOutputsVariableSizedBuffer buffer = CorrectDatasets(dl, target_date, rbfnn, parameter.GammaThreshold, 2);//関連するデータのみを想起して貯める
		int NumberOfIterations=0;

		reset_variance_covariance_matrix(parameter.getMin_sigma());
		do {
			init_parameters();			
			E_step(dl, target_date, old_pattern_buffer);
			NumberOfIterations++;
		}while(M_step(parameter.getMin_sigma())>parameter.getStop_threshold() &&
				NumberOfIterations < parameter.getMaxNumberOfIterations());
		System.out.println("IncMixtureOfIdistribution: EM(): Number of EM iterations:" + NumberOfIterations);
		this.EstimateNumberOfClassMember(dl, target_date, old_pattern_buffer);
		this.AIC_value = this.AIC(dl, target_date, this.isDiagonal, old_pattern_buffer);
		if (this.DEBUG) {

			System.out.println("IncMixtureOfDistribution: EM() total number of members " + this.getTotalNumberOfMembers());

			Enumeration<Object> e = this.HiddenNodes.elements();
			while (e.hasMoreElements()) {
				Distribution dis = (Distribution)e.nextElement();
				dis.getSigma().display("Sigma");
				dis.GetCenter().display("center");
			}
		}
		return this.AIC_value;
	}
	
	
	/*@SuppressWarnings("unchecked")
	private MinNoOutputsVariableSizedBuffer CorrectDatasets(dataload dl,
			int target_date, RBFNet rbfnn, double gammaThreshold, int Repeats) {
		IncDistribution N;
		MinNoOutputsVariableSizedBuffer buffer = new MinNoOutputsVariableSizedBuffer(this.NumberOfInputs);
		if (rbfnn !=null) {
			System.out.println("IncMixtureOfDistribution:CorrectDatasets(): Phi(" + rbfnn.getPhiSizeL() + ", " + rbfnn.getPhiSizeM() + ")");
		}else{
			System.out.println("IncMixtureOfDistribution:CorrectDatasets(): Phi is null ");
		}
		for (int i=0; i<Repeats; i++) {
			buffer.dispose();
			//新規データをバッファに貯める
			for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
				buffer.push_data(dl.learning_input_patterns[target_date][p]);
			}
			if (i==0) {//初期化してフラグを建てなおす
				this.checkFixedUnits(buffer, target_date, gammaThreshold, true);
			}else{//初期化せずに追加的にフラグを立てる
				this.checkFixedUnits(buffer, target_date, gammaThreshold, false);				
			}

			//既知データを集める
			if (rbfnn != null) {
				System.out.println("IncMixtureOfDistribution:CorrectDatasets(): buffer size " + buffer.getSize() + " rbfnn learned samples : " + rbfnn.getNumberOfLearnedSamples());				
				for (int p=0; p<rbfnn.getNumberOfLearnedSamples(); p++) {
					double[] x = rbfnn.generate_pseudo_input(p,rbfnn);//疑似サンプル生成
					IncDistribution winner = this.getWinnerCell(x);
					double[] gamma = this.calculate_gamma(x);
					Enumeration e = this.HiddenNodes.elements();
					int cell=0;
					while (e.hasMoreElements()) {
						N = (IncDistribution)e.nextElement();
						if (gamma[cell]>0.01 && !N.isFix()) {
							buffer.push_data(x);
						}
					}
				}
				if (i<Repeats-1) {//最後でなければ追加的にフラグを立てる
					this.checkFixedUnits(buffer, target_date, gammaThreshold, false);
				}
			}
			System.out.println("IncMixtureOfDistribution:CorrectDatasets(): buffer size after recalling " + buffer.getSize());			
		}
		return buffer;
	}*/

	
	//過去のサンプルをrbfnnから想起するversion
	void E_step(dataload dl, int target_date, RBFNet old_rbfnn) {
		IncDistribution N;
		double gamma[], pseudo_x[];
		int m;
		for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
			gamma = this.calculate_gamma(dl.learning_input_patterns[target_date][p]);
			m=0;

			Enumeration<Object> e = this.HiddenNodes.elements();
			while (e.hasMoreElements()) {
				N = (IncDistribution)e.nextElement();
				N.push_center(dl.learning_input_patterns[target_date][p], gamma[m]);
					//System.out.println("gamma[" + m + "]=" + gamma[m]);
				m++;
			}
		}
		if (old_rbfnn != null) {
			for (int q=0; q<old_rbfnn.getNumberOfLearnedSamples(); q++) {
				pseudo_x = old_rbfnn.generate_pseudo_input2(q, old_rbfnn);
				gamma = this.calculate_gamma(pseudo_x);
				m=0;

				Enumeration<Object> e = this.HiddenNodes.elements();
				while (e.hasMoreElements()) {
					N = (IncDistribution)e.nextElement();
					N.push_center(pseudo_x, gamma[m]);
					//System.out.println("gamma[" + m + "]=" + gamma[m]);
					m++;
				}			
			}
		}
	}
	
	//bufferを使用するもの。この場合は当然ながら過去のサンプルを全部bufferに貯めておく必要がある。
	void E_step(dataload dl, int target_date, MinMoutWeightVariableSizedBuffer old_pattern_buffer) {
		IncDistribution N;
		double gamma[], x[];
		int m;
		for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
			gamma = this.calculate_gamma(dl.learning_input_patterns[target_date][p]);
			m=0;

			Enumeration<Object> e = this.HiddenNodes.elements();
			while (e.hasMoreElements()) {
				N = (IncDistribution)e.nextElement();
				N.push_center(dl.learning_input_patterns[target_date][p], gamma[m]);
					//System.out.println("gamma[" + m + "]=" + gamma[m]);
				m++;
			}
		}
		if (old_pattern_buffer != null) {
			for (int q=0; q<old_pattern_buffer.getSize(); q++) {
				x = old_pattern_buffer.getInput(q);
				gamma = this.calculate_gamma(x);
				m=0;

				Enumeration<Object> e = this.HiddenNodes.elements();
				while (e.hasMoreElements()) {
					N = (IncDistribution)e.nextElement();
					N.push_center(x, gamma[m]);
					//System.out.println("gamma[" + m + "]=" + gamma[m]);
					m++;
				}			
			}
		}
	}
		

	double M_step(double min_sigma) throws MatrixException {
		IncDistribution N;
		double change_in_parameters = 0D;

		Enumeration<Object> e = this.HiddenNodes.elements();
		while (e.hasMoreElements()) {
			N = (IncDistribution)e.nextElement();
			change_in_parameters += N.calculate_center();//ここはisFix()如何に係わらず実行。条件分岐はこのメソッドの中で定義		
			//if (!N.isFix()) {
				change_in_parameters += N.calculate_variance_covariance_matrix(min_sigma);
			//}
		}
		if (this.DEBUG) {
			System.out.println("IncMixtureOfNormalDistribution:M_step(): change_in_parameters=" + change_in_parameters);
		}
		return change_in_parameters;
	}
	
	//@SuppressWarnings("unchecked")
	/*void checkFixedUnits(dataload dl, int target_date, double gammaThreshold) {
		double gamma[];
		if (this.HiddenNodes==null) return;
		//一旦全ユニットパラメータ固定フラグを立てる
		Enumeration e = this.HiddenNodes.elements();
		while (e.hasMoreElements()) {
			IncDistribution ND = (IncDistribution)e.nextElement();
			ND.setFix(true);
		}
		
		//新規サンプルに対してgammaの値が閾値以上のユニットはパラメータ可変扱いにする
		//新規サンプルは dl.learning_input_patterns[target_date] のサンプルとする
		for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
			gamma = this.calculate_gamma(dl.learning_input_patterns[target_date][p]);
			for (int m=0; m<this.NumberOfModels; m++) {
				if (gamma[m]>gammaThreshold) {
					IncNormalDistributionD ND = (IncNormalDistributionD)this.HiddenNodes.elementAt(m);
					ND.setFix(false);//パラメータを可変扱いにする
				}
			}
		}
	}*/
	
	//@SuppressWarnings("unchecked")
/*	void checkFixedUnits(FIFO.MinNoOutputsVariableSizedBuffer buffer, int target_date, double gammaThreshold, boolean reset) {
		double gamma[];
		if (this.HiddenNodes==null) return;
		
		if (reset) {//resetフラグがtrueならば
			//一旦全ユニットパラメータ固定フラグを立てる
			Enumeration e = this.HiddenNodes.elements();
			while (e.hasMoreElements()) {
				IncDistribution ND = (IncDistribution)e.nextElement();
				//ND.setFix(true);
				ND.setFix(false);//今は全部更新
				ND.resetNumberOfMember();
			}
		}
		
		//新規サンプルに対してgammaの値が閾値以上のユニットはパラメータ可変扱いにする
		//新規サンプルは dl.learning_input_patterns[target_date] のサンプルとする
		for (int p=0; p<buffer.getSize(); p++) {
			gamma = this.calculate_gamma(buffer.getInput(p));
			for (int m=0; m<this.NumberOfModels; m++) {
				if (gamma[m]>gammaThreshold) {
					IncNormalDistributionD ND = (IncNormalDistributionD)this.HiddenNodes.elementAt(m);
					ND.setFix(false);//パラメータを可変扱いにする
				}
			}
		}
	}*/

	//target_dateのデータセット(新規データ)+oldデータセットでAICを計算する。
	//oldデータセットはRBFNNから想起する
	public double AIC(dataload dl, int target_date, boolean isDiagonal, RBFNet old_RBFNN) {
		double likelihood_new_samples = this.LogLikelihood(dl, target_date);
		double likelihood_old_samples = this.LogLikelihood(old_RBFNN);
		System.out.println("IncMixtureOfDistribution: AIC(): Likelihood for new samples = " + likelihood_new_samples);
		System.out.println("IncMixtureOfDistribution: AIC(): Likelihood for old samples = " + likelihood_old_samples);		
		double AIC_value = -2D*(likelihood_new_samples + likelihood_old_samples) + 2D*(double)this.getNumberOfParameters(isDiagonal);
		return AIC_value;
	}	
	
	//target_dateのデータセット(新規データ)+oldデータセットでAICを計算する
	//oldデータセットはbufferに貯めたものを使用する
	public double AIC(dataload dl, int target_date, boolean isDiagonal, MinMoutWeightVariableSizedBuffer old_pattern_buffer) {
		double likelihood_new_samples = this.LogLikelihood(dl, target_date);
		double likelihood_old_samples = this.LogLikelihood(old_pattern_buffer);
		System.out.println("IncMixtureOfDistribution: AIC(): Likelihood for new samples = " + likelihood_new_samples);
		System.out.println("IncMixtureOfDistribution.AIC(): Likelihood for old samples = " + likelihood_old_samples);		
		double AIC_value = -2D*(likelihood_old_samples + likelihood_new_samples) + 2D*(double)this.getNumberOfParameters(isDiagonal);
		return AIC_value;
	}

	//関連データセット(新規データを含む)のみでAICを計算
	public double AIC(FIFO.MinNoOutputsVariableSizedBuffer buffer, boolean isDiagonal) {
		double AIC_value = -2*this.LogLikelihood(buffer) + 2*this.getNumberOfParameters(isDiagonal);
		return AIC_value;
	}
	
	//target_dateのデータセットのみ(新規データのみ)で対数尤度を計る
	double LogLikelihood(dataload dl, int target_date) {
		double sum=0D;
		double each_p;
		for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
			each_p = this.P(dl.learning_input_patterns[target_date][p]);
			//System.out.println("IncMixtureOfDistribution: LogLikelihood() each P(x)=" + each_p);
			if (each_p != 0) {//これが無いと Infinityが起きる！
				sum += Math.log(each_p);
			}
		}
		return sum;
	}

	//関係するデータのみで対数尤度を計る
	double LogLikelihood(MinMoutWeightVariableSizedBuffer buffer) {
		double sum=0D;
		double each_p;
		for (int p=0; p<buffer.getSize(); p++) {
			each_p = this.P(buffer.getInput(p));
			if (each_p!=0) {
				sum += Math.log(each_p);
			}
		}
		return sum;
	}	
	
	//関係するデータのみで対数尤度を計る
	double LogLikelihood(FIFO.MinNoOutputsVariableSizedBuffer buffer) {
		double sum=0D;
		double each_p;
		for (int p=0; p<buffer.getSize(); p++) {
			each_p = this.P(buffer.getInput(p));
			if (each_p!=0) {
				sum += Math.log(each_p);
			}
		}
		return sum;
	}	
	
	double LogLikelihood(RBFNet old_RBFNN) {
		double sum = 0D;
		double each_p;
		if (old_RBFNN != null) {
			for (int p=0; p<old_RBFNN.getNumberOfLearnedSamples(); p++) {
				each_p = this.P(old_RBFNN.generate_pseudo_input2(p, old_RBFNN));
				if (each_p != 0) {
					sum += Math.log(each_p);
				}
			}
		
			if (Double.isInfinite(sum)) {
				for (int p=0; p<old_RBFNN.getNumberOfLearnedSamples(); p++) {
					double[] x = old_RBFNN.generate_pseudo_input2(p, old_RBFNN);
					for (int i=0; i<this.NumberOfInputs; i++) {
						System.out.printf(" " + x[i] + " ");
					}
					System.out.println(" ");
				}
			}
		}
		//System.out.println("log of old samples: " + sum);
		return sum;
	}
	

	public void EstimateNumberOfClassMember(FIFO.MinNoOutputsVariableSizedBuffer buffer) {
		IncDistribution nd;
		
		//reset NumberOfMember
		Enumeration<Object> e1 = this.HiddenNodes.elements();

		while (e1.hasMoreElements()) {
			nd = (IncNormalDistributionD)e1.nextElement();
			nd.resetNumberOfMember();
		}
		//estimation
		for (int p=0; p<buffer.getSize(); p++) {
			this.gamma = this.calculate_gamma(buffer.getInput(p));
			Enumeration<Object> e2 = this.HiddenNodes.elements();
			int cell=0;
			while (e2.hasMoreElements()) {
				nd = (IncNormalDistributionD)e2.nextElement();
				nd.AddFreedomeness(this.gamma[cell]);
				cell++;
			}

		}
	}


	//各々のモデルに属するサンプルの数を数えて、これを自由度として登録する。
	//これはStudentTを計算するときに使う。
	//過去のサンプルはold_rbfnnから想起するVersion
	public void EstimateNumberOfClassMember(dataload dl, int target_date, RBFNet old_rbfnn) {
		IncDistribution nd;
		//IncDistribution winner_model = null;
		int cell;
		
		//reset NumberOfMember

		Enumeration<Object> e1 = this.HiddenNodes.elements();
		while (e1.hasMoreElements()) {
			nd = (IncNormalDistributionD)e1.nextElement();
			nd.resetNumberOfMember();
		}
		
		//estimation for new learning samples
		for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
			//winner_model = this.getWinnerCell(dl.learning_input_patterns[target_date][p]);
			this.gamma = this.calculate_gamma(dl.learning_input_patterns[target_date][p]);
			e1 = this.HiddenNodes.elements();
			cell = 0;
			while (e1.hasMoreElements()) {
				IncDistribution h_cell = (IncDistribution)e1.nextElement();
				h_cell.AddFreedomeness(this.gamma[cell]);
				cell++;
			}
			/*if (winner_model != null) {
				winner_model.incrementClassMember();
				//System.out.print("+");				
			}else{
				System.out.println("IncMixtureOfDistribution: EstimateNumberOfClassMember : null!!!!");
			}*/
			
		}
		//estimation for old learning samples
		if (old_rbfnn != null) {
			for (int q=0; q<old_rbfnn.getNumberOfLearnedSamples(); q++) {
				//winner_model = this.getWinnerCell(old_rbfnn.generate_pseudo_input(q, old_rbfnn));
				this.gamma = this.calculate_gamma(old_rbfnn.generate_pseudo_input2(q, old_rbfnn));
				e1 = this.HiddenNodes.elements();
				cell=0;
				while (e1.hasMoreElements()) {
					IncDistribution h_cell = (IncDistribution)e1.nextElement();
					h_cell.AddFreedomeness(this.gamma[cell]);
					cell++;
				}
				/*if (winner_model != null) {
					winner_model.incrementClassMember();
					//System.out.print("o");									
				}else{
					System.out.println("IncMixtureOfDistribution: EstimateNumberOfClassMember : null!!!!");					
				}*/
			}
		}
		System.out.println("total number of members : " + this.getTotalNumberOfMembers());
	}	

	//各々のモデルに属するサンプルの数を数えて、これを自由度として登録する。
	//これはStudentTを計算するときに使う。
	//過去のサンプルはbufferに貯めたものを使うVersion
	public void EstimateNumberOfClassMember(dataload dl, int target_date, MinMoutWeightVariableSizedBuffer old_pattern_buffer) {
		IncDistribution nd;
		//IncDistribution winner_model = null;
		int cell;
		
		//reset NumberOfMember

		Enumeration<Object> e1 = this.HiddenNodes.elements();
		while (e1.hasMoreElements()) {
			nd = (IncNormalDistributionD)e1.nextElement();
			nd.resetNumberOfMember();
		}
		
		//estimation for new learning samples
		for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
			//winner_model = this.getWinnerCell(dl.learning_input_patterns[target_date][p]);
			this.gamma = this.calculate_gamma(dl.learning_input_patterns[target_date][p]);
			e1 = this.HiddenNodes.elements();
			cell = 0;
			while (e1.hasMoreElements()) {
				IncDistribution h_cell = (IncDistribution)e1.nextElement();
				h_cell.AddFreedomeness(this.gamma[cell]);
				cell++;
			}
		}
		//estimation for old learning samples
		if (old_pattern_buffer != null) {
			for (int q=0; q<old_pattern_buffer.getSize(); q++) {
				//winner_model = this.getWinnerCell(old_rbfnn.generate_pseudo_input(q, old_rbfnn));
				this.gamma = this.calculate_gamma(old_pattern_buffer.getInput(q));
				e1 = this.HiddenNodes.elements();
				cell=0;
				while (e1.hasMoreElements()) {
					IncDistribution h_cell = (IncDistribution)e1.nextElement();
					h_cell.AddFreedomeness(this.gamma[cell]);
					cell++;
				}
			}
		}
		System.out.println("total number of members : " + this.getTotalNumberOfMembers());
	}//EstimateNumberOfClassMember()
	

	public void init_models(dataload dl, int NumberOfModels, double init_sigma_value, boolean isDiagonal, boolean SMD) throws MatrixException {
		this.HiddenNodes = new Vector<Object>();
		for (int m=0; m<NumberOfModels; m++) {
			MatrixObj init_sigma = new MatrixObj(this.NumberOfInputs, this.NumberOfInputs);
			this.init_sigma(init_sigma, init_sigma_value);

			if (isDiagonal) {
				IncNormalDistributionD nd = new IncNormalDistributionD(dl.learning_input_patterns[this.target_date][m], init_sigma);
				this.HiddenNodes.addElement(nd);
			}else{
				IncNormalDistribution  nd = new IncNormalDistribution(dl.learning_input_patterns[this.target_date][m], init_sigma, SMD);
				this.HiddenNodes.addElement(nd);
			}
		}
	}	
	


	public int getTotalNumberOfMembers() {
		int sum = 0;
		IncDistribution h_cell;
		Enumeration<Object> e = this.HiddenNodes.elements();
		
		while (e.hasMoreElements()) {
			h_cell = (IncDistribution)e.nextElement();
			sum += h_cell.getNumberOfMember();
		}
		return sum;
	}
	

	public IncMixtureOfDistributions clone(boolean isDiagonal) {
	    IncMixtureOfDistributions myclone=null;
		try {
			myclone = (IncMixtureOfDistributions)super.clone();
			myclone.HiddenNodes = new Vector<Object>();
			//int i=0;
			Enumeration<Object> e = this.HiddenNodes.elements();
			if (isDiagonal) {
				while (e.hasMoreElements()) {
					IncNormalDistributionD h_cell = (IncNormalDistributionD)e.nextElement();
					myclone.HiddenNodes.addElement(h_cell.clone());
					//i++;
				}
			}else{
				while (e.hasMoreElements()) {
					IncNormalDistribution h_cell = (IncNormalDistribution)e.nextElement();
					myclone.HiddenNodes.addElement(h_cell.clone());
					//i++;
				}
			}
			return myclone;			
		} catch (CloneNotSupportedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return myclone;
	}	
}
