package mgrnn;

import org.w3c.dom.Node;

import VectorFunctions.VectorFunctions;

public class LimitedGRNNLRU extends LimitedGRNNFIFO {

	public LimitedGRNNLRU(Node nd) {
		super(nd);
		// TODO Auto-generated constructor stub
	}

	public LgrnnLearningStatus learning(double inputs[], double outputs[]) {
		return this.LRU_learning(inputs, outputs, this.UpperLimitOfHiddenUnits);
	}
	
	LgrnnLearningStatus LRU_learning(double inputs[], double outputs[], int UpperLimitOfHiddenUnits) {
		CellFIFO new_cell, each_cell;
		LgrnnLearningStatus result = new LgrnnLearningStatus();
		
		CellFIFO nearest_kernel = this.getNearestUnit(inputs);
		double[] error = VectorFunctions.diff(outputs, this.calculate_outputs(inputs));
		result.residual_error = VectorFunctions.getSqureNorm(error);
		
		if (nearest_kernel != null) nearest_kernel.resetAge();
		if (this.hidden_units.size() < UpperLimitOfHiddenUnits) {// only adding
																	// new units
			new_cell = new CellFIFO(this.getNumberOfInputs(),
					this.getNumberOfOutputs());
			new_cell.set_default_variance(this.getVariance(), 1);
			new_cell.learn(inputs, outputs);
			new_cell.set_check(false);// DO NOT FORGET!
			this.hidden_units.add(new_cell);
			System.out
					.println("LGRNN:learning():Append: number of hiddenunits: "
							+ hidden_units.size());
			if (this.isVariableSigma && this.hidden_units.size() == UpperLimitOfHiddenUnits) {
				OptimizeSigma();
			}
		} else {// substitution & replace, only replace(pruning), modification

				System.out.println("======== replace ==========");
				CellFIFO oldest_kernel = this.getOldestKernel();
				
				oldest_kernel.learn(inputs, outputs);
				oldest_kernel.resetAge();
					// uselesscell_EDIT.target_cell.learn(inputs, outputs);
				if (this.isVariableSigma) this.OptimizeSigma();
			
		}
		for (Cell cell : this.hidden_units) {
			each_cell = (CellFIFO)cell;
			each_cell.incrementalAge();
		}
		// System.out.println("grnn:learn() number of hidden units:" +
		// this.hidden_units.size());
		return result;
	}	
	
	CellFIFO getNearestUnit(double input[]) {
		double min_distance = Double.MAX_VALUE;
		CellFIFO nearest_unit = null;
		for (Cell cell : this.hidden_units) {
			CellFIFO each_cell = (CellFIFO)cell;

			double each_distance = VectorFunctions.getSqureNorm(
						VectorFunctions.diff(input, each_cell.getT())
						);
			if (each_distance < min_distance) {
				nearest_unit = each_cell;
				min_distance = each_distance;
			}

		}
		return nearest_unit;
	}	
}
