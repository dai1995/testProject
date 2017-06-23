package KernelPerceptron;

import org.w3c.dom.Node;

public class KernelPerceptron4Regression extends KernelPerceptron {
	private final boolean DEBUG = false;
	public KernelPerceptron4Regression(Node nd) {
		super(nd);
	}
	
	public void learn(double inputs[], double outputs[]) {
		double provisional_outputs[] = this.output(inputs);
		if (this.min_distance(inputs)<Double.MIN_VALUE) return;//If there is the same kernel, do nothing!.
		double error = this.LossFunc.getLoss(provisional_outputs, outputs);
		//System.out.println("KernelPerceptron4Regression.learn(): learning process is occured.");		
		if (error > this.getErrorThreshold()) {
			if (this.DEBUG) {
				System.out.println("KernelPerceptron4Regression.learn(): learning process is occured.");
			}
			kernelOrg new_unit = new kernelOrg(this.getNumberOfInputs(), this.getNumberOfOutputs(), this.getDefaultSigma());
			new_unit.setCenter(inputs);
			if (this.UseHindgeLossFunction) {
				new_unit.setDesiredOutputs(outputs);
			}else{
				double errors[] = VectorFunctions.VectorFunctions.diff(outputs, provisional_outputs);
				new_unit.setDesiredOutputs(errors);// note: the desired output should be the error for regression.
			}
			this.addHiddenUnit(new_unit);
		}
	}
}
