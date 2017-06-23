package MixtureOfDistributions;

import matrix.MatrixException;
import DataLoad.dataload;
import FIFO.MinMoutWeightVariableSizedBuffer;
import ParameterReader.ParameterReader;
import RBFNN.RBFNet;

public class IncGetStatisticalModel extends GetStatisticalModel {
	boolean DEBUG = false;
	IncMixtureOfDistributions IncMN[];	
	
	public IncGetStatisticalModel(ParameterReader pr) {
		super(pr);
		// TODO Auto-generated constructor stub
	}
	
	//target_dateで表される新規サンプルのみで最良のモデルを構築する, 
	//rbfnnは疑似サンプルを生成させるためのもの。
	//新しく生成した入力の時のerrの変化量がこの閾値を下回ったら終了する重要任務を帯びたパラメータ。
	public IncMixtureOfDistributions constructBestModel(dataload dl, int target_date, 
			IncMixtureOfDistributions prev_best_model, RBFNet rbfnn) throws MatrixException {
		this.IncMN = new IncMixtureOfDistributions[this.parameter.getMaxNumberOfModels()];
		this.min_AIC = Double.MAX_VALUE;//この初期化を忘れると、再度呼び出したときに正しくモデル選択が行われない！
		this.previous_AIC = Double.MAX_VALUE;//この初期化を忘れると、再度呼び出したときに正しくモデル選択が行われない!
		int NumberOfPreviousGaussians=0;
		if (prev_best_model == null) {//最初の１回目
			System.out.println("IncGetStatisticalModel:constructBestModel(): 1st EM");
			for (int m=0; m<this.parameter.getMaxNumberOfModels(); m++) {
				this.IncMN[m] = new IncMixtureOfDistributions(dl, m+1, this.NumberOfInputs,
						this.parameter.getActivation_threshold(),
						this.parameter.getInit_sigma(), this.parameter.isDIAGONAL(), this.parameter.isSMD(), this.parameter.isRANInitMode());

				//デバッグのため中心位置の表示
				if (this.DEBUG) {
					System.out.println("IncGetStatisticalModel:constructBestModel(): Model:" + m);
					this.IncMN[m].display_unit_centers(true);
				}
			}
		    NumberOfPreviousGaussians = 0;
		}else{//二回目以降
			System.out.println("IncGesStatisticalModel:constructBestModel(): Incremental EM");
			
			//System.out.println("# of Old samples is " + rbfnn.getNumberOfLearnedSamples());
			//System.out.println("# of new samples is " + dl.getActualNumberOfLearningSamples());]
			this.IncMN[0] = prev_best_model.clone(this.parameter.isDIAGONAL());//0番は１つ前のベストモデル
			for (int m=1; m<this.parameter.getMaxNumberOfModels()-prev_best_model.getNumberOfModels(); m++) {
				this.IncMN[m] = prev_best_model.clone(this.parameter.isDIAGONAL());
				this.IncMN[m].Append_init_models(m, target_date, this.parameter.getActivation_threshold(), dl, prev_best_model.getInit_sigma_value(), true, false);
				System.out.println("the " + m + " th model: Number of models " + this.IncMN[m].getNumberOfHiddenUnits());
				if (this.DEBUG) {
					this.IncMN[m].display_unit_centers(true);
					//new IncMixtureOfDistributions(dl, target_date, m, prev_best_model);
				}
			}
		    NumberOfPreviousGaussians = prev_best_model.getNumberOfModels();
		    if (this.DEBUG) {
		    	for (int i=0; i<this.IncMN.length; i++) {
		    		if (this.IncMN[i] != null) {
		    			System.out.println(i+ "number of class members: " + this.IncMN[i].getTotalNumberOfMembers());
		    		}
		    	}
		    }
		}
		try {
			//for (int m=0; m<this.parameter.getMaxNumberOfModels(); m++) {
			int m=0;
			this.best_model = -1;
			do {
				this.previous_AIC = this.min_AIC;
				this.previous_best_model = this.best_model;//ここのprevious_best_modelはprev_best_modelとは違うので注意
				System.out.println("m=" + m);
				double each_AIC = this.IncMN[m].EM(dl, target_date, rbfnn, this.parameter);
				if (this.DEBUG) {
					System.out.println("IncGesStatisticalModel:constructBestModel(): LogLikelihood: " + m + " " + this.IncMN[m].LogLikelihood(dl));
				}
				//double each_AIC = this.IncMN[m].AIC(dl, target_date, this.parameter.isDIAGONAL());
				//if (this.DEBUG) {
					System.out.println("IncGesStatisticalModel:constructBestModel(): AIC: " + m + " " + each_AIC + " best one is " + this.min_AIC);
				//}
				if (this.min_AIC > each_AIC) {
					this.best_model = m;
					this.min_AIC = each_AIC;
				}
				if (this.DEBUG) {
					for (int i=0; i<this.IncMN.length; i++) {
						if (this.IncMN[i] != null) {
							System.out.println(i+ "number of class members: " + this.IncMN[i].getTotalNumberOfMembers());
						}
					}
				}
				m++;
			} while (this.isTermination(this.parameter, this.previous_AIC, this.min_AIC, m+NumberOfPreviousGaussians));
			
			System.out.println("IncGetStatisticalModel: constructBestModel() best model is " + this.IncMN[this.best_model].getNumberOfModels());
			System.out.println("IncGetStatisticalModel: constructBestModel() best model corrsponding inputs " + this.IncMN[this.best_model].getTotalNumberOfMembers());
			//System.out.println("IncGetStatisticalModel: constructBestModel() best model index is " + this.best_model);
			if (this.DEBUG) {
				for (m=0; m<this.IncMN.length; m++) {
					if (this.IncMN[m]!=null) {
						System.out.println(m+ "number of class members: " + this.IncMN[m].getTotalNumberOfMembers());
					}
				}
			}
			if (this.previous_AIC < this.min_AIC) {
				this.min_AIC = this.previous_AIC;
				this.best_model = this.previous_best_model;
			}
			
			System.out.println("IncGetStatisticalModel: constructBestModel() best model index is " + this.best_model);
			if (this.best_model>-1) {//best_modelが存在
				System.out.println("IncGesStatisticalModel:constructBestModel(): best model is " + (this.best_model) + " minAIC = " + this.min_AIC);
				//this.IncMN[this.best_model].EstimateNumberOfClassMember(dl);//EMで既にやっている
				this.IncMN[this.best_model].CreateStudentT(this.parameter.getUpperLimitOfFreedom(), 
						this.parameter.isSMD(), this.parameter.isCorrectTDistribution());
				return this.IncMN[this.best_model];
			}else{
				return null;
			}

		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public IncMixtureOfDistributions constructBestModel(
			dataload dl, int target_date, 
			IncMixtureOfDistributions prev_best_model, 
			MinMoutWeightVariableSizedBuffer old_pattern_buffer) throws MatrixException {
		this.IncMN = new IncMixtureOfDistributions[this.parameter.getMaxNumberOfModels()];
		this.min_AIC = Double.MAX_VALUE;//この初期化を忘れると、再度呼び出したときに正しくモデル選択が行われない！
		this.previous_AIC = Double.MAX_VALUE;//この初期化を忘れると、再度呼び出したときに正しくモデル選択が行われない!
		int NumberOfPreviousGaussians=0;
		
		//モデル候補の準備
		if (prev_best_model == null) {//最初の１回目
			System.out.println("IncGetStatisticalModel:constructBestModel(): 1st EM");
			for (int m=0; m<this.parameter.getMaxNumberOfModels(); m++) {
				this.IncMN[m] = new IncMixtureOfDistributions(dl, m+1, this.NumberOfInputs,
						this.parameter.getActivation_threshold(),
						this.parameter.getInit_sigma(), this.parameter.isDIAGONAL(), this.parameter.isSMD(), this.parameter.isRANInitMode());
				//デバッグのため中心位置の表示
				if (this.DEBUG) {
					System.out.println("IncGetStatisticalModel:constructBestModel(): Model:" + m);
					this.IncMN[m].display_unit_centers(true);
				}
			}
		    NumberOfPreviousGaussians = 0;
		}else{//二回目以降
			System.out.println("IncGesStatisticalModel:constructBestModel(): Incremental EM");
			
			//System.out.println("# of Old samples is " + rbfnn.getNumberOfLearnedSamples());
			//System.out.println("# of new samples is " + dl.getActualNumberOfLearningSamples());]
			this.IncMN[0] = prev_best_model.clone(this.parameter.isDIAGONAL());//0番は１つ前のベストモデル
			for (int m=1; m<this.parameter.getMaxNumberOfModels()-prev_best_model.getNumberOfModels(); m++) {
				this.IncMN[m] = prev_best_model.clone(this.parameter.isDIAGONAL());
				this.IncMN[m].Append_init_models(m, target_date, this.parameter.getActivation_threshold(), dl, prev_best_model.getInit_sigma_value(), true, false);
				
				if (this.DEBUG) {
					System.out.println("the " + m + " th model: Number of models " + this.IncMN[m].getNumberOfHiddenUnits());
					this.IncMN[m].display_unit_centers(true);
					//new IncMixtureOfDistributions(dl, target_date, m, prev_best_model);
				}
			}
		    NumberOfPreviousGaussians = prev_best_model.getNumberOfModels();
		    if (this.DEBUG) {
		    	for (int i=0; i<this.IncMN.length; i++) {
		    		if (this.IncMN[i] != null) {
		    			System.out.println(i+ "number of class members: " + this.IncMN[i].getTotalNumberOfMembers());
		    		}
		    	}
		    }
		}//モデル候補の準備
		
		//学習
		try {
			//for (int m=0; m<this.parameter.getMaxNumberOfModels(); m++) {
			int m=0;
			this.best_model = -1;
			do {
				this.previous_AIC = this.min_AIC;
				this.previous_best_model = this.best_model;//ここのprevious_best_modelはprev_best_modelとは違うので注意
				System.out.println("m=" + m);
				//EMアルゴリズムを実行。AICの値を得る
				double each_AIC = this.IncMN[m].EM(dl, target_date, old_pattern_buffer, this.parameter);
				if (this.DEBUG) {
					System.out.println("IncGesStatisticalModel:constructBestModel(): LogLikelihood: " + m + " " + this.IncMN[m].LogLikelihood(dl));
				}
				if (this.DEBUG) {
					System.out.println("IncGesStatisticalModel:constructBestModel(): AIC: " + m + " " + each_AIC + " best one is " + this.min_AIC);
				}
				if (this.min_AIC > each_AIC) {
					this.best_model = m;
					this.min_AIC = each_AIC;
				}
				if (this.DEBUG) {
					for (int i=0; i<this.IncMN.length; i++) {
						if (this.IncMN[i] != null) {
							System.out.println(i+ "IncGetStatisticalModel.constructBestModel(): number of class members: " + this.IncMN[i].getTotalNumberOfMembers());
						}
					}
				}
				m++;
			} while (this.isTermination(this.parameter, this.previous_AIC, this.min_AIC, m+NumberOfPreviousGaussians));
			
			System.out.println("IncGetStatisticalModel.constructBestModel() best model is " + this.IncMN[this.best_model].getNumberOfModels());
			System.out.println("IncGetStatisticalModel.constructBestModel() best model corrsponding inputs " + this.IncMN[this.best_model].getTotalNumberOfMembers());
			//System.out.println("IncGetStatisticalModel: constructBestModel() best model index is " + this.best_model);
			if (this.DEBUG) {
				for (m=0; m<this.IncMN.length; m++) {
					if (this.IncMN[m]!=null) {
						System.out.println(m+ "IncGetStatisticalModel.constructBestModel().number of class members: " + this.IncMN[m].getTotalNumberOfMembers());
					}
				}
			}
			if (this.previous_AIC < this.min_AIC) {
				this.min_AIC = this.previous_AIC;
				this.best_model = this.previous_best_model;
			}
			
			System.out.println("IncGetStatisticalModel: constructBestModel() best model index is " + this.best_model);
			if (this.best_model>-1) {//best_modelが存在
				System.out.println("IncGesStatisticalModel:constructBestModel(): best model is " + (this.best_model) + " minAIC = " + this.min_AIC);
				//this.IncMN[this.best_model].EstimateNumberOfClassMember(dl);//EMで既にやっている
				this.IncMN[this.best_model].CreateStudentT(this.parameter.getUpperLimitOfFreedom(), 
						this.parameter.isSMD(), this.parameter.isCorrectTDistribution());
				return this.IncMN[this.best_model];
			}else{
				return null;
			}

		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
}
