package KernelPerceptron;
import VectorFunctions.VectorFunctions;
public class SquaredErrorFunction implements LossFunction {
	public double getLoss(double[] actual_outputs, double[] desired_outputs) {
		// TODO Auto-generated method stub
		return VectorFunctions.getSqureNorm(VectorFunctions.diff(desired_outputs, actual_outputs));
	}

}
