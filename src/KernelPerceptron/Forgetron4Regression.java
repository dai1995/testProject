package KernelPerceptron;

import org.w3c.dom.Node;

public class Forgetron4Regression extends KernelPerceptron4Regression {
	private final boolean DEBUG = false;
	private double U;
	public Forgetron4Regression(Node nd) {
		super(nd);
		// TODO Auto-generated constructor stub
		this.U = 0.25 * Math.sqrt((double)(this.pkp.getUpperLimitOfHiddenUnits()+1)
				/Math.log((double)(this.pkp.getUpperLimitOfHiddenUnits()+1)));
	}
	
	public void learn(double inputs[], double outputs[]) {
		double provisional_outputs[] = this.output(inputs);
		
		double errors[] = VectorFunctions.VectorFunctions.diff(outputs, provisional_outputs);
		double error = VectorFunctions.VectorFunctions.getSqureNorm(errors);
		double dummy[] = new double[this.getNumberOfOutputs()];
		
		//System.out.println("KernelPerceptron4Regression.learn(): learning process is occured.");		
		if (error > this.getErrorThreshold()) {
			if (this.DEBUG) {
				System.out.println("Forgetron4Regression.learn(): learning process is occured.");
			}
			if (this.getNumberOfHiddenUnits() < this.pkp.getUpperLimitOfHiddenUnits()) {
				kernel4Forgetron new_unit = new kernel4Forgetron(this.getNumberOfInputs(), this.getNumberOfOutputs(), this.getDefaultSigma());
				new_unit.setCenter(inputs);
				new_unit.setDesiredOutputs(errors);// note: the desired output should be the error for regression.
				this.addHiddenUnit(new_unit);
			}else{
				kernel4Forgetron target_unit = this.getOldestKernel();
				target_unit.setCenter(inputs);
				target_unit.setDesiredOutputs(dummy);
				target_unit.resetAge();
				provisional_outputs = this.output(inputs);
				errors = VectorFunctions.VectorFunctions.diff(outputs, provisional_outputs);
				target_unit.setDesiredOutputs(errors);
			}
		}
	}	
	
	public void learn_with_shrink(double inputs[], double outputs[]) {
		double provisional_outputs[] = this.output(inputs);
		
		double errors[] = VectorFunctions.VectorFunctions.diff(outputs, provisional_outputs);
		double error = VectorFunctions.VectorFunctions.getSqureNorm(errors);
		
		
		//System.out.println("KernelPerceptron4Regression.learn(): learning process is occured.");		
		if (error > this.getErrorThreshold()) {
			if (this.DEBUG) {
				System.out.println("Forgetron4Regression.learn(): learning process is occured.");
			}
			kernel4Forgetron new_unit = new kernel4Forgetron(this.getNumberOfInputs(), this.getNumberOfOutputs(), this.getDefaultSigma());
			new_unit.setCenter(inputs);
			new_unit.setDesiredOutputs(errors);// note: the desired output should be the error for regression.
			this.addHiddenUnit(new_unit);
			//int ProvisionalNewUnitIndex = this.HiddenUnits.indexOf(new_unit);
			double f_dash_norm = this.getNormOfCurrentFunction();
			double phi_t = Math.min(Math.pow((double)(this.pkp.getUpperLimitOfHiddenUnits()+1), -1/(2*(double)(this.pkp.getUpperLimitOfHiddenUnits()+1))), this.U /f_dash_norm );
			for (Kernel cell : this.HiddenUnits) {
				kernel4Forgetron s_cell = (kernel4Forgetron)cell;
				s_cell.modifyGain(phi_t);
				s_cell.incrementAge();
			}
			if (this.getNumberOfHiddenUnits() >= this.pkp.getUpperLimitOfHiddenUnits()) {
				this.HiddenUnits.remove(this.getOldestKernel());
			}
		}
	}	
	
	kernel4Forgetron getOldestKernel() {
		int oldestAge = -1;
		kernel4Forgetron oldestKernel = null;
		for (Kernel cell: this.HiddenUnits) {
			kernel4Forgetron target_cell = (kernel4Forgetron)cell;
			if (oldestAge < target_cell.getAge()) {
				oldestKernel = target_cell;
				oldestAge = target_cell.getAge();
			}
		}
		return oldestKernel;
	}
	
	double getNormOfCurrentFunction() {
		double sum = 0;
		for (Kernel cell_i : this.HiddenUnits) {
			for (Kernel cell_j : this.HiddenUnits) {
				sum += cell_i.getDesiredOutputs()[0] * cell_j.getDesiredOutputs()[0] * 
						cell_i.exp_output(cell_j.getCenter());
 			}
		}
		return sum;
	}

}
