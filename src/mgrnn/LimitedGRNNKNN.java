package mgrnn;


import java.util.Scanner;

import matrix.MatrixException;
import matrix.MatrixObj;

import org.w3c.dom.Node;

import VectorFunctions.VectorFunctions;

public class LimitedGRNNKNN extends LimitedGRNN {
	final boolean DEBUG = true;
	int UpperLimitOfNearestNeighbors = 3;
	
	public LimitedGRNNKNN(Node nd) {
		super(nd);
	}
	
	void init_parameters(Node nd) {
		LGRNN_parameters lgp = new LGRNN_parameters();
		lgp.getParameter(nd);
		this.UpperLimitOfHiddenUnits = lgp.getNumberOfUpperLimitOfUnits();
		this.NumberOfInputs = lgp.NumberOfInputs;
		this.NumberOfOutputs = lgp.NumberOfOutputs;
		this.epsilon = lgp.epsilon;
		this.variance = lgp.default_variance;
		this.ImportanceWeight4NewSample = lgp.getImportanceWeight4NewSample();
		this.UseIgnore = lgp.isUseIgnore();
		this.UseModify = lgp.isUseModify();
		this.UsePrune = lgp.isUsePrune();
		this.UseSubstitution = lgp.isUseSubstitution();
		this.UseEditing = lgp.isUseEditing();
		this.UpperLimitOfHiddenUnits = lgp.getUpperLimitOfNearestNeighbors();
		this.isCumulativeErrorEstimation = lgp.IsEstimatedByCumulativeError();
		System.out.println(UseIgnore + " " + UseModify + " " + UsePrune + " " + UseSubstitution+" variance=" + this.variance
				);
		
		this.K = new MatrixObj(0,0);
		//this.alpha = new MatrixObj(0,0);
		this.clear_check_flag();
	}	
		
	public LgrnnLearningStatus learning(double inputs[], double outputs[]) {

		try {
			return this.LGRNNKNN_learning(inputs, outputs, this.UpperLimitOfHiddenUnits, this.ImportanceWeight4NewSample, this.UpperLimitOfNearestNeighbors, this.UsePseudoInverse);

		} catch (MatrixException e) {
			Scanner sc = new Scanner(System.in);
			// TODO Auto-generated catch block
			e.printStackTrace();

			if (this.DEBUG) {
				sc.next();
			}
			return null;
		}
	}
	
	LgrnnLearningStatus LGRNNKNN_learning(double inputs[], double outputs[], int UpperLimitOfHiddenUnits, double importance_weight, int UpperLimitOfNearestNeighbors, boolean UsePseudoInverse) throws MatrixException {
		CellLinearDependency new_cell, each_cell;
		double MinInterference = Double.MAX_VALUE;
		int Action=-1;
		LgrnnLearningStatus result = new LgrnnLearningStatus();
		
		double[] error = VectorFunctions.diff(outputs, this.calculate_outputs(inputs));
		result.residual_error = VectorFunctions.getSqureNorm(error);
		
		if (this.hidden_units.size() < UpperLimitOfHiddenUnits) {// only adding new units
			new_cell = new CellLinearDependency(this.NumberOfInputs, this.NumberOfOutputs);
			new_cell.setStandardDeviation(this.variance);
			new_cell.learn(inputs, outputs);
			this.hidden_units.add(new_cell);
			System.out.println("LGRNN:learning():Append: number of hiddenunits: " + hidden_units.size());			
		}else{//substitution & replace, only replace(pruning), modification and ignore.
			// find the most useless unit in terms of ALD
			UselessCell uselesscell_LD = this.getMostUselessCellLD(UsePseudoInverse);
			// find the most useless unit in terms of Editing (for comparison)
			UselessCell uselesscell_EDIT=null;
			if (this.UseEditing) {
				uselesscell_EDIT = this.getMostUselessCellEdit();
			}
			// obtain the linear dependency of the new input
			dependency dep_new = this.Calculate_LinearDependency(inputs); //this.alpha is also calculated
			// estimate the expected loss
			ExpectedLoss eloss = this.EstimateExpectedLoss(inputs, outputs, importance_weight, uselesscell_LD, uselesscell_EDIT, dep_new);

			if (this.UseEditing && eloss.mag_editing < eloss.mag_ignore) {
				//Editing option can be used separately. (only UseEditing + Ignore is ok!)
				uselesscell_EDIT.target_cell.learn(inputs, outputs);
			}else{
				//get Min interference
				if (this.UseSubstitution && (eloss.mag_substitution < eloss.mag_pruning && eloss.mag_substitution < eloss.mag_modification)) {//substitution and replace
					MinInterference = eloss.mag_substitution;
					Action = LgrnnLearningStatus.SUBSTITUTION;
				}else if (this.UsePrune && (eloss.mag_pruning < eloss.mag_modification)) {
					MinInterference = eloss.mag_pruning;
					Action = LgrnnLearningStatus.PRUNING;
				}else if (this.UseModify) {
					MinInterference = eloss.mag_modification;
					Action = LgrnnLearningStatus.MODIFY;
				}
				
				if (this.UseIgnore && eloss.mag_ignore<=MinInterference) {
					Action = LgrnnLearningStatus.IGNORE;
				}
				
				switch(Action) {
				case LgrnnLearningStatus.SUBSTITUTION:
					
					int index=0;
					//double[] A = new double[this.NumberOfOutputs]; 
					System.out.println("========substitution & replace==========");
					for (Cell cell : this.hidden_units) {
						each_cell = (CellLinearDependency)cell;
						if (!each_cell.equals(uselesscell_LD.target_cell)) {//each_cellがreplace_cellではない場合
							
							each_cell.Increment(uselesscell_LD.target_cell.get_normalized_t_alpha(), 
									uselesscell_LD.target_cell.getAlpha()*uselesscell_LD.alpha.getData(index, 0));
							index++;
						}
					}
					//replace cell learns the new sample
					uselesscell_LD.target_cell.learn(inputs, outputs);
					result.LearningOption = LgrnnLearningStatus.SUBSTITUTION;
					break;
					
				case LgrnnLearningStatus.PRUNING:
					System.out.println("======== replace ==========");
					uselesscell_LD.target_cell.learn(inputs, outputs);
					//uselesscell_EDIT.target_cell.learn(inputs, outputs);
					result.LearningOption = LgrnnLearningStatus.PRUNING;
					break;
					
				case LgrnnLearningStatus.MODIFY:
					
					int index2=0;
					//double[] A = new double[this.NumberOfOutputs]; 
					System.out.println("========Modify=========");
					for (Cell cell : this.hidden_units) {
						each_cell = (CellLinearDependency)cell;
						each_cell.Increment(outputs, dep_new.alpha.getData(index2, 0));
						index2++;
					}
					result.LearningOption = LgrnnLearningStatus.MODIFY;
					break;
					
				case LgrnnLearningStatus.IGNORE:
					// do nothing!
					System.out.println("=====IGNORE=====");
					result.LearningOption = LgrnnLearningStatus.IGNORE;
					break;
				}

			}
		}
		return result;
	}/*LGRNNKNN_learning() */
	
	
	
	dependency Calculate_LinearDependency(CellLinearDependency target_cell, boolean UsePseudoInverse) throws MatrixException {
		CellLinearDependency cell;
		//double OutputWeightNorm = VectorFunctions.getSqureNorm(target_cell.get_normalized_t_alpha());
		dependency dep = new dependency();
		if (this.hidden_units ==null) {
			dep.delta = Double.MAX_VALUE;
			dep.alpha = null;
			return dep;
		}

		this.K = this.matrix_K(hidden_units, target_cell);
		
		//k_sを作る
		MatrixObj k_s = new MatrixObj(hidden_units.size()-1,1);
		
		int i=0;
		for (Cell scell : this.hidden_units) {
			cell = (CellLinearDependency)scell;
			if (!cell.equals(target_cell)) {
				k_s.set_data(i, 0, cell.exp_output(target_cell.getT()));
				i++;
			}
		}
		if (this.UsePseudoInverse) {
			dep.alpha = K.PseudoInverse().multiply(k_s);
		}else{
			dep.alpha = K.inverse().multiply(k_s);
		}
		dep.alpha.display("LimitedGRNN:Calculate_LinearDependency(): alpha"); 
		//K.display("K");
		//alpha.display("grnnLinearDependency:Calculate_LinearDependency()");
		//double delta = OutputWeightNorm * (1 - k_s.Transport().multiply(alpha).getData(0, 0));
		dep.delta = (1 - k_s.Transport().multiply(dep.alpha).getData(0, 0));
		System.out.println("delta is " + dep.delta);
		return dep;
	}	
	
}
