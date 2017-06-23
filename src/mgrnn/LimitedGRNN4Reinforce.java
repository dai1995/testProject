package mgrnn;

import org.w3c.dom.Node;

import VectorFunctions.VectorFunctions;
import matrix.MatrixException;


public class LimitedGRNN4Reinforce extends LimitedGRNN {
	final boolean DEBUG = false;
	public LimitedGRNN4Reinforce(Node nd) {
		super(nd);
		// TODO Auto-generated constructor stub
	}
	
	
	//Attention!! Please set importance_weight properly. Especially, if the actor falls into 
	// the area, where the reward is negative, the imporatnce_weight should be set a large value.
	//
	// If you want to use 'UseEditing=true', please note that other four options
	// should be false.
	// Editing option can be used separately. (UseEditing + Ignore is ok!)
	
	public LgrnnLearningStatus learning(double inputs[], double outputs[], double importance_weight) {
		this.Log_method("LgrnnLearningStatus learning---------------");
		try {
			return this.LGRNN_learning(inputs, outputs, this.UpperLimitOfHiddenUnits,
					importance_weight, this.UsePseudoInverse);
		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	
	LgrnnLearningStatus LGRNN_learning(double inputs[], double outputs[],
			int UpperLimitOfHiddenUnits, double importance_weight, boolean UsePseudoInverse)
			throws MatrixException {
		CellLinearDependency each_cell, new_cell;
		double MinInterference = Double.MAX_VALUE;
		LgrnnLearningStatus result = new LgrnnLearningStatus();
		int Action = -1;
		//final int SUBSTITUTION = 1, PRUNING = 2, MODIFY = 3, IGNORE = 4, AGGREGATE = 5;
		double[] error;
		error = VectorFunctions.diff(outputs, this.calculate_outputs(inputs));
		result.residual_error = VectorFunctions.getSqureNorm(error);
		System.out.println("U = " + importance_weight);
		//If there are the same center, the learning is canceled!
		if (this.IsExistTheSameKernel(this.hidden_units, inputs) != null) {
			result.LearningOption = LgrnnLearningStatus.IGNORE;
			return result; 
		}
		
		if (this.hidden_units.size() < UpperLimitOfHiddenUnits) {// only adding
																	// new units
			new_cell = new CellLinearDependency(this.NumberOfInputs,
					this.NumberOfOutputs);
			new_cell.setStandardDeviation(this.variance);
			new_cell.learn(inputs, outputs, 1D);
			new_cell.set_check(false);// DO NOT FORGET!
			this.hidden_units.add(new_cell);
			result.LearningOption = LgrnnLearningStatus.GRNN;
			System.out.println("LGRNN:learning():Append: number of hiddenunits: "
							+ hidden_units.size());
			if (this.isVariableSigma && this.hidden_units.size() == UpperLimitOfHiddenUnits) {
				OptimizeSigma();
			}
		} else {// substitution & replace, only replace(pruning), modification
				// and ignore.
				// find the most useless unit in terms of ALD
			UselessCell uselesscell_LD = this.getMostUselessCellLD(UsePseudoInverse);
			// find the most useless unit in terms of Editing (for comparison)
			UselessCell uselesscell_EDIT = null;
			if (this.UseEditing) {
				uselesscell_EDIT = this.getMostUselessCellEdit();
			}
			// obtain the linear dependency of the new input
			dependency dep_new = this.Calculate_LinearDependency(inputs); // this.alpha
																			// is
																			// also
																			// calculated
			// estimate the expected loss
			ExpectedLoss eloss = this.EstimateExpectedLoss(inputs, outputs,
					importance_weight, uselesscell_LD, uselesscell_EDIT,
					dep_new);
			//this.Log("LGRNN_learning() keyscanner");
			//this.keyScanner.next();
			
			if (this.UseEditing && eloss.mag_editing < eloss.mag_ignore) {
				// Editing option can be used separately. (only UseEditing +
				// Ignore is ok!)
				uselesscell_EDIT.target_cell.learn(inputs, outputs, 1D);
			} else {
				// get Min interference
				//replaceをfalseにすると学習しない理由↓ 
				if (this.UseSubstitution
						&& (eloss.mag_substitution < eloss.mag_pruning) 
						&& (eloss.mag_substitution < eloss.mag_modification)) {// substitution
																																// replace
					MinInterference = eloss.mag_substitution;
					Action = LgrnnLearningStatus.SUBSTITUTION;
				} else if (this.UsePrune
						&& (eloss.mag_pruning < eloss.mag_modification)
						&& (eloss.mag_pruning < eloss.mag_substitution)) {
					MinInterference = eloss.mag_pruning;
					Action = LgrnnLearningStatus.PRUNING;
				} else if (this.UseModify
						&& (eloss.mag_modification < eloss.mag_pruning)
						&& (eloss.mag_modification < eloss.mag_substitution)) {
					MinInterference = eloss.mag_modification;
					Action = LgrnnLearningStatus.MODIFY;
				}
				if (this.UseIgnore && eloss.mag_ignore <= MinInterference) {
					Action = LgrnnLearningStatus.IGNORE;
				}
				result.LearningOption = Action;
				
				switch (Action) {
				case LgrnnLearningStatus.SUBSTITUTION:
					int index = 0;
					// double[] A = new double[this.NumberOfOutputs];
					System.out.println("========substitution & replace==========");
					
					for (Cell cell : this.hidden_units) {
						each_cell = (CellLinearDependency)cell;
						if (!each_cell.equals(uselesscell_LD.target_cell)) {// each_cell��replace_cell�ǤϤʤ����

							each_cell.Increment(
									uselesscell_LD.target_cell.get_normalized_t_alpha(),
									uselesscell_LD.target_cell.getAlpha() *	uselesscell_LD.alpha.getData(index, 0),
									uselesscell_LD.relative_number(index));
							index++;
						}
					}
					// replace cell learns the new sample
					uselesscell_LD.target_cell.learn(inputs, outputs, 1D);
					
					uselesscell_LD.target_cell.NumberOfLearnedSamples = importance_weight;//<--The novel point!! (January 8, 2016)
			
					if (this.isVariableSigma) this.OptimizeSigma();
					break;

				case LgrnnLearningStatus.PRUNING:
					System.out.println("======== replace ==========");
					CellLinearDependency closest_unit = (CellLinearDependency) this.getNearestUnit(uselesscell_LD.target_cell);
					closest_unit.IncrementNumberOfLearnedSamples(uselesscell_LD.target_cell.getNumberOfLearnedSamples());
					uselesscell_LD.target_cell.learn(inputs, outputs, 1D);
					
					
					uselesscell_LD.target_cell.NumberOfLearnedSamples = importance_weight;//<--The novel point!! (January 8, 2016)
					
					// uselesscell_EDIT.target_cell.learn(inputs, outputs);
					if (this.isVariableSigma) this.OptimizeSigma();
						break;

				case LgrnnLearningStatus.MODIFY:
					System.out.println("========Modify=========");
					int index2 = 0;
					// double[] A = new double[this.NumberOfOutputs];
					double total_a =0; 
					for (int i=0; i<dep_new.alpha.getL(); i++) {
						//total_a += Math.abs(dep_new.alpha.getData(i, 0));
						total_a += Math.abs(dep_new.alpha.getData(i, 0));
					}					

					for (Cell cell : this.hidden_units) {
						each_cell = (CellLinearDependency)cell;
						each_cell.Increment(outputs,
								dep_new.alpha.getData(index2, 0),
								//dep_new.alpha.getData(index2, 0)/total_a);
								Math.abs(dep_new.alpha.getData(index2, 0))/total_a);
						index2++;
					}
					break;

				case LgrnnLearningStatus.IGNORE:
					// do nothing!
					System.out.println("=====IGNORE=====");
					break;
				}

			}
		}
		return result;
		// System.out.println("grnn:learn() number of hidden units:" +
		// this.hidden_units.size());
	}
	
	void Log(String log) {
		if (this.DEBUG) {
			System.out.println("LimitedGRNN4Reinforce." + log);
		}
	}
}
