package MixtureOfDistributions;

import java.util.Enumeration;
import java.util.Vector;

import matrix.MatrixException;
import matrix.MatrixObj;
import DataLoad.dataload;
import FIFO.MinMoutWeightVariableSizedBuffer;
import RBFNN.RBFNet;

public class IncWMixtureOfDistributions extends wMixtureOfNormalDistributions implements Cloneable {
	protected boolean DEBUG = false;
	protected int AddNumberOfModels;
	protected int target_date;
	protected double AIC_value=0;
	
	public IncWMixtureOfDistributions(MinMoutWeightVariableSizedBuffer buffer, int NumberOfModels,
			int NumberOfInputs, double activation_threshold, double init_sigma_value, boolean isDiagonal,
			boolean SMD, boolean RANInitMode) {

		super(buffer, NumberOfModels, NumberOfInputs, activation_threshold, init_sigma_value, isDiagonal,
				SMD, RANInitMode);
		if (this.DEBUG) {
			System.out.println("IncMixtureOfDistributions: # of models=" + this.HiddenNodes.size());
		}

		// TODO Auto-generated constructor stub
	}
	
		

	public double EM(MinMoutWeightVariableSizedBuffer buffer, 
			RBFNet rbfnn, 
			double Min_sigma, 
			double Stop_Threshold, 
			int MaxNumberOfIterations,
			double lambda
			) throws MatrixException {
		//FIFO.MinNoOutputsVariableSizedBuffer buffer = CorrectDatasets(dl, target_date, rbfnn, parameter.GammaThreshold, 2);//関連するデータのみを想起して貯める
		int NumberOfIterations=0;

		reset_variance_covariance_matrix(Min_sigma);
		do {
			init_parameters();			
			E_step(buffer, rbfnn, lambda);
			NumberOfIterations++;
		}while(M_step(Min_sigma)>Stop_Threshold &&
				NumberOfIterations < MaxNumberOfIterations);
		System.out.println("IncMixtureOfIdistribution: EM(): Number of EM iterations:" + NumberOfIterations);
		this.EstimateNumberOfClassMember(buffer, rbfnn);
		this.AIC_value = this.AIC(buffer, this.isDiagonal, rbfnn);
		if (this.DEBUG) {

			System.out.println("IncMixtureOfDistribution: EM() total number of members " + this.getTotalNumberOfMembers());
			/*Enumeration<Object> e = this.HiddenNodes.elements();
			while (e.hasMoreElements()) {
				Distribution dis = (Distribution)e.nextElement();
				dis.getSigma().display("Sigma");
				dis.GetCenter().display("center");
			}*/
		}
		return this.AIC_value;
	}

	// 過去のサンプルはバッファにたまったデータを使う
	public double EM(MinMoutWeightVariableSizedBuffer buffer, 
			double Min_sigma, 
			double Stop_Threshold, 
			int MaxNumberOfIterations,
			double lambda
			) throws MatrixException {
		//FIFO.MinNoOutputsVariableSizedBuffer buffer = CorrectDatasets(dl, target_date, rbfnn, parameter.GammaThreshold, 2);//関連するデータのみを想起して貯める
		int NumberOfIterations=0;

		reset_variance_covariance_matrix(Min_sigma);
		do {
			init_parameters();			
			E_step(buffer, lambda);
			NumberOfIterations++;
		}while(M_step(Min_sigma)>Stop_Threshold &&
				NumberOfIterations < MaxNumberOfIterations);
		System.out.println("IncMixtureOfIdistribution: EM(): Number of EM iterations:" + NumberOfIterations);
		this.EstimateNumberOfClassMember(buffer);
		this.AIC_value = this.AIC(buffer, this.isDiagonal);
		if (this.DEBUG) {

			System.out.println("IncMixtureOfDistribution: EM() total number of members " + this.getTotalNumberOfMembers());
			/*Enumeration<Object> e = this.HiddenNodes.elements();
			while (e.hasMoreElements()) {
				Distribution dis = (Distribution)e.nextElement();
				dis.getSigma().display("Sigma");
				dis.GetCenter().display("center");
			}*/
		}
		return this.AIC_value;
	}
	
	

	void E_step(MinMoutWeightVariableSizedBuffer buffer, RBFNet old_rbfnn, double lambda) {
		IncDistribution N;
		double gamma[], pseudo_x[];
		int m;
		for (int p=0; p<buffer.getSize(); p++) {
			gamma = this.calculate_gamma(buffer.getInput(p));
			m=0;
			Enumeration<Object> e = this.HiddenNodes.elements();
			while (e.hasMoreElements()) {
				N = (IncDistribution)e.nextElement();
				N.push_center(buffer.getInput(p), buffer.getActualWeight(p) * gamma[m]);
					//System.out.println("gamma[" + m + "]=" + gamma[m]);
				m++;
			}
		}
		if (old_rbfnn != null) {
			for (int q=0; q<old_rbfnn.getNumberOfLearnedSamples(); q++) {
				pseudo_x = old_rbfnn.generate_pseudo_input2(q, old_rbfnn);
				gamma = this.calculate_gamma(pseudo_x);
				m=0;
				double weight = Math.pow(old_rbfnn.getWold().getData(q,q), lambda);
				Enumeration<Object> e = this.HiddenNodes.elements();
				while (e.hasMoreElements()) {
					N = (IncDistribution)e.nextElement();
					N.push_center(pseudo_x,	weight * gamma[m]);
					//System.out.println("gamma[" + m + "]=" + gamma[m]);
					m++;
				}			
			}
		}
	}

	//過去のサンプルはbufferのサンプルを使うもの
	void E_step(MinMoutWeightVariableSizedBuffer buffer, double lambda) {
		IncDistribution N;
		double gamma[];
		int m;
		for (int p=0; p<buffer.getSize(); p++) {
			gamma = this.calculate_gamma(buffer.getInput(p));
			m=0;
			Enumeration<Object> e = this.HiddenNodes.elements();
			while (e.hasMoreElements()) {
				N = (IncDistribution)e.nextElement();
				N.push_center(buffer.getInput(p), buffer.getActualWeight(p) * gamma[m]);
					//System.out.println("gamma[" + m + "]=" + gamma[m]);
				m++;
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

	//target_dateのデータセット(新規データ)のみでAICを計算
	public double AIC(MinMoutWeightVariableSizedBuffer buffer, boolean isDiagonal, RBFNet old_RBFNN) {
		double likelihood_new_samples = this.LogLikelihood(buffer);
		double likelihood_old_samples = this.LogLikelihood(old_RBFNN);
		System.out.println("IncMixtureOfDistribution: AIC(): Likelihood for new samples = " + likelihood_new_samples);
		System.out.println("IncMixtureOfDistribution: AIC(): Likelihood for old samples = " + likelihood_old_samples);		
		double AIC_value = -2D*(likelihood_new_samples + likelihood_old_samples) + 2D*(double)this.getNumberOfParameters(isDiagonal);
		return AIC_value;
	}

	//target_dateのデータセット(新規データ)のみでAICを計算(古いサンプルは全部bufferに含まれるものとする)
	public double AIC(MinMoutWeightVariableSizedBuffer buffer, boolean isDiagonal) {
		double likelihood_all_samples = this.LogLikelihood(buffer);
		System.out.println("IncMixtureOfDistribution: AIC(): Likelihood for all samples = " + likelihood_all_samples);
		double AIC_value = -2D*(likelihood_all_samples) + 2D*(double)this.getNumberOfParameters(isDiagonal);
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
	


	//各々のクラスターに含まれるサンプル数を数え、これを自由度とする。（Student Tの計算に使う)
	public void EstimateNumberOfClassMember(MinMoutWeightVariableSizedBuffer buffer, RBFNet old_rbfnn) {
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
		for (int p=0; p<buffer.getSize(); p++) {
			//winner_model = this.getWinnerCell(dl.learning_input_patterns[target_date][p]);
			this.gamma = this.calculate_gamma(buffer.getInput(p));
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
	}//	EstimateNumberOfClassMember()	

	//各々のクラスターに含まれるサンプル数を数え、これを自由度とする。（Student Tの計算に使う)
	//古いサンプルは全部bufferに含まれるものとする
	public void EstimateNumberOfClassMember(MinMoutWeightVariableSizedBuffer buffer) {
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
		for (int p=0; p<buffer.getSize(); p++) {
			//winner_model = this.getWinnerCell(dl.learning_input_patterns[target_date][p]);
			this.gamma = this.calculate_gamma(buffer.getInput(p));
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
		System.out.println("total number of members : " + this.getTotalNumberOfMembers());
	}//	EstimateNumberOfClassMember()
	

	public void init_models(MinMoutWeightVariableSizedBuffer buffer, int NumberOfModels, double init_sigma_value, boolean isDiagonal, boolean SMD) throws MatrixException {
		this.HiddenNodes = new Vector<Object>();
		for (int m=0; m<NumberOfModels; m++) {
			MatrixObj init_sigma = new MatrixObj(this.NumberOfInputs, this.NumberOfInputs);
			this.init_sigma(init_sigma, init_sigma_value);

			if (isDiagonal) {
				IncNormalDistributionD nd = new IncNormalDistributionD(buffer.getInput(m), init_sigma);
				this.HiddenNodes.addElement(nd);
			}else{
				IncNormalDistribution  nd = new IncNormalDistribution(buffer.getInput(m), init_sigma, SMD);
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
	

	public IncWMixtureOfDistributions clone(boolean isDiagonal) {
	    IncWMixtureOfDistributions myclone=null;
		try {
			myclone = (IncWMixtureOfDistributions)super.clone();
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
