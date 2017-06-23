package KernelPerceptron;

public class kernel4Forgetron extends kernelOrg implements Kernel {
	private int age;
	private double gain = 1.0;
	private final boolean DEBUG = false;
	public kernel4Forgetron(int NumberOfInputs, int NumberOfOutputs,
			double sigma) {
		super(NumberOfInputs, NumberOfOutputs, sigma);
		// TODO Auto-generated constructor stub
		this.age = 0;
	}
	
	public double[] output(double inputs[]) {
		double exp_out = exp_output(inputs);
		for (int j=0; j<this.getNumberOfOutputs(); j++) {
			outputs[j] = this.gain * Y[j] * exp_out;
		}
		if (this.DEBUG) {
			System.out.println("kernel4Forgetron.output(): Y[0]=" + Y[0] + " exp_out=" + exp_out);
		}
		return outputs;
	}	
	/**
	 * @return the age
	 */
	public int getAge() {
		return age;
	}
	
	public void resetAge() {
		this.age = 0;
	}
	
	public void incrementAge() {
		this.age ++;
	}

	public void modifyGain(double data) {
		this.gain *= data;
	}
}
