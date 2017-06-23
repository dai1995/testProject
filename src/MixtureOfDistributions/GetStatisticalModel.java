package MixtureOfDistributions;

import matrix.MatrixException;
import DataLoad.dataload;
import ParameterReader.ParameterReader;

public class GetStatisticalModel {
	MixtureOfDistributionParameters parameter;

	double init_sigma_value;
	int NumberOfInputs;
	int NumberOfOutputs; // only for loading dataset
	MixtureOfNormalDistributions MN[];
	double min_AIC = Double.MAX_VALUE;
	double previous_AIC = Double.MAX_VALUE;
	int best_model=-1, previous_best_model=-1;
	
	public GetStatisticalModel(ParameterReader pr) {
		this.parameter = new MixtureOfDistributionParameters();
		this.parameter.getParameter(pr.Reader("MixtureOfNormalDistribution"));
		this.NumberOfInputs = this.parameter.getNumberOfInputs();
		this.NumberOfOutputs = this.parameter.getNumberOfOutputs();

		//this.init_sigma_value = this.parameter.get
	}
	
	public MixtureOfNormalDistributions constructBestModelPlain(dataload dl) {
		this.MN = new MixtureOfNormalDistributions[this.parameter.getMaxNumberOfModels()];
		this.min_AIC = Double.MAX_VALUE;//この初期化を忘れると、再度呼び出したときに正しくモデル選択が行われない！
		this.previous_AIC = Double.MAX_VALUE;//この初期化を忘れると、再度呼び出したときに正しくモデル選択が行われない！
		for (int m=0; m<this.parameter.getMaxNumberOfModels(); m++) {
			this.MN[m] = new MixtureOfNormalDistributions(dl, m+1, this.NumberOfInputs, this.parameter.getActivation_threshold(),
					this.parameter.getInit_sigma(), this.parameter.isDIAGONAL(), this.parameter.isSMD(), this.parameter.isRANInitMode());
		}		
		try {
			//for (int m=0; m<this.parameter.getMaxNumberOfModels(); m++) {
			
			for (int m=0; m<this.parameter.getMaxNumberOfModels(); m++) {
				this.previous_AIC = this.min_AIC;
				this.previous_best_model = this.best_model;
				this.MN[m].EM(dl, this.parameter.getStop_threshold(), this.parameter.getMaxNumberOfIterations(), this.parameter.getMin_sigma());
				System.out.println("LogLikelihood: " + m + " " + this.MN[m].LogLikelihood(dl));
				double each_AIC = this.MN[m].AIC(dl, this.parameter.isDIAGONAL());
				System.out.println("GetStatisticalModel.constructBestModel(): AIC of " + (m+1) + " th model = " + each_AIC);
				if (this.min_AIC > each_AIC) {
					this.best_model = m;
					this.min_AIC = each_AIC;
				}
			}
			
			if (this.best_model>-1) {//best_modelが存在
				System.out.println("GetStatisticalModel.constructBestModel(): best model is " + (this.best_model+1) + " minAIC = " + this.min_AIC);
				this.MN[this.best_model].EstimateNumberOfClassMember(dl);
				this.MN[this.best_model].CreateStudentT(this.parameter.getUpperLimitOfFreedom(), 
						this.parameter.isSMD(), this.parameter.isCorrectTDistribution());
				return this.MN[this.best_model];
			}else{
				return null;
			}

		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public MixtureOfNormalDistributions constructBestModel(dataload dl) {
		this.MN = new MixtureOfNormalDistributions[this.parameter.getMaxNumberOfModels()];
		this.min_AIC = Double.MAX_VALUE;//この初期化を忘れると、再度呼び出したときに正しくモデル選択が行われない！
		this.previous_AIC = Double.MAX_VALUE;//この初期化を忘れると、再度呼び出したときに正しくモデル選択が行われない！
		for (int m=0; m<this.parameter.getMaxNumberOfModels(); m++) {
			this.MN[m] = new MixtureOfNormalDistributions(dl, m+1, this.NumberOfInputs, this.parameter.getActivation_threshold(),
					this.parameter.getInit_sigma(), this.parameter.isDIAGONAL(), this.parameter.isSMD(), this.parameter.isRANInitMode());
		}		
		try {
			//for (int m=0; m<this.parameter.getMaxNumberOfModels(); m++) {
			int m=0;
			do {
				this.previous_AIC = this.min_AIC;
				this.previous_best_model = this.best_model;
				this.MN[m].EM(dl, this.parameter.getStop_threshold(), this.parameter.getMaxNumberOfIterations(), this.parameter.getMin_sigma());
				System.out.println("LogLikelihood: " + m + " " + this.MN[m].LogLikelihood(dl));
				double each_AIC = this.MN[m].AIC(dl, this.parameter.isDIAGONAL());
				System.out.println("GetStatisticalModel.constructBestModel(): AIC of " + (m+1) + " th model = " + each_AIC);
				if (this.min_AIC > each_AIC) {
					this.best_model = m;
					this.min_AIC = each_AIC;
				}
				m++;
			} while (this.isTermination(this.parameter, this.previous_AIC, this.min_AIC, m));
			
			if (this.previous_AIC > this.min_AIC) {
				this.min_AIC = this.previous_AIC;
				this.best_model = this.previous_best_model;
			}
			if (this.best_model>-1) {//best_modelが存在
				System.out.println("GetStatisticalModel.constructBestModel(): best model is " + (this.best_model+1) + " minAIC = " + this.min_AIC);
				this.MN[this.best_model].EstimateNumberOfClassMember(dl);
				this.MN[this.best_model].CreateStudentT(this.parameter.getUpperLimitOfFreedom(), 
						this.parameter.isSMD(), this.parameter.isCorrectTDistribution());
				return this.MN[this.best_model];
			}else{
				return null;
			}

		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	boolean isTermination(MixtureOfDistributionParameters parameter, double previous_AIC, double min_AIC, int m) {
		if (parameter.isQuickMode()) {
			if (previous_AIC > min_AIC && m < parameter.getMaxNumberOfModels()) {
				return true;
			}else{
				return false;
			}
		}else{
			if (m<parameter.getMaxNumberOfModels()) {
				return true;
			}else{
				return false;
			}
		}
	}
	
	public MixtureOfNormalDistributions constructModel(dataload dl, int NumberOfModels) {
		MixtureOfNormalDistributions mn;
		mn = new MixtureOfNormalDistributions(dl, NumberOfModels, this.NumberOfInputs, this.parameter.getActivation_threshold(),
					this.parameter.getInit_sigma(), this.parameter.isDIAGONAL(), this.parameter.isSMD(), this.parameter.isRANInitMode());

		try {
			mn.EM(dl, this.parameter.getStop_threshold(), this.parameter.getMaxNumberOfIterations(), this.parameter.getMin_sigma());
			mn.CreateStudentT(this.parameter.getUpperLimitOfFreedom(), this.parameter.isSMD(), this.parameter.isCorrectTDistribution());			
		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mn;
	}	
	
}
