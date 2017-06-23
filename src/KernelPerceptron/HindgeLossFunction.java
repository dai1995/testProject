package KernelPerceptron;

public class HindgeLossFunction implements LossFunction {

		@Override
	public  double getLoss(double[] actual_outputs, double[] desired_outputs) {
		// TODO Auto-generated method stub
		if (actual_outputs.length > 1 || desired_outputs.length > 1) {
			System.err.println("HidgeLossFuntion can support actual_outputs[1] and desired_outputs[1]");
			System.exit(1);
		}
		return 1-desired_outputs[0] * actual_outputs[0];
	}

}
