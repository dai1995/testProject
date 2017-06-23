package KernelPerceptron;

public interface Kernel {
	public void setCenter(double inputs[]);
	public void setDesiredOutputs(double y[]);
	public double[] getDesiredOutputs();
	public double exp_output(double inputs[]);
	public double exp_output();
	public double[] output(double inputs[]);
	public double[] getCenter();
	public int getNumberOfInputs();
	public int getNumberOfOutputs();
	public double getNumberOfLearnedSamples();
	public void setDependency(dependency dep);
	public double getDependency(int i);
	public boolean isEnable();
	public void setEnable(boolean flag);
	public double getDistance(); // Before getting the distance, you have to exectute output();.
	public void setStandardDeviation(double r);
}
