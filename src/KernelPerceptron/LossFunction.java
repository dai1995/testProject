package KernelPerceptron;

public interface LossFunction {
	abstract double getLoss(double actual_outputs[], double desired_outputs[]);
}
