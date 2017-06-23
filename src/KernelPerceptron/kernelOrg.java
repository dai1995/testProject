package KernelPerceptron;

public class kernelOrg implements Kernel {
	private final boolean DEBUG = false;
	private int NumberOfInputs=0;
	private int NumberOfOutputs = 0;
	private double sigma = 0;
	private double distance= 0D;
	private String name;
	public double u[];
	private double exp_out;
	public double Y[];
	double outputs[];
	private boolean Enable = true; // If this flag is true, this Kernel is to be calculated. This flag is used in the parent class.

	public kernelOrg(int NumberOfInputs, int NumberOfOutputs, double r) {
		this.NumberOfInputs = NumberOfInputs;
		this.NumberOfOutputs = NumberOfOutputs;
		this.setStandardDeviation(r);// r is the mimimum distance between the neighbors.
		u = new double[this.NumberOfInputs];// kernel center
		Y = new double[this.NumberOfOutputs]; //desired outputs
		this.outputs = new double[this.NumberOfOutputs];
		this.Enable = true;
	}

	public void setCenter(double inputs[]) {
		for (int i=0; i<this.NumberOfInputs; i++) {
			this.u[i] = inputs[i];
 		}
	}

	public void setDesiredOutputs(double y[]) {
		for (int j=0; j<this.NumberOfOutputs; j++) {
			this.Y[j] = y[j];
		}
	}

	public double exp_output(double inputs[]) {
		this.distance = VectorFunctions.VectorFunctions.getSqureNorm(VectorFunctions.VectorFunctions.diff(inputs, this.u));
		this.exp_out = Math.exp(-this.distance/(2*this.sigma));
		if (this.DEBUG) {
			System.out.println("kernel.exp_output(): exp_out = " + this.exp_out + " distance = " +  distance + " sigma = " + sigma);
		}
		return this.exp_out;
	}

	public double[] output(double inputs[]) {
		double exp_out = exp_output(inputs);
		for (int j=0; j<this.NumberOfOutputs; j++) {
			outputs[j] = Y[j] * exp_out;
		}
		if (this.DEBUG) {
			System.out.println("kernel.output(): Y[0]=" + Y[0] + " exp_out=" + exp_out);
		}
		return outputs;
	}

	/**
	 * @return the exp_out
	 */
	public double exp_output() {
		return exp_out;
	}

	/**
	 * @return the numberOfInputs
	 */
	public int getNumberOfInputs() {
		return NumberOfInputs;
	}

	/**
	 * @return the numberOfOutputs
	 */
	public int getNumberOfOutputs() {
		return NumberOfOutputs;
	}

	@Override
	public double[] getCenter() {
		// TODO Auto-generated method stub
		return this.u;
	}



	@Override
	public double[] getDesiredOutputs() {
		// TODO Auto-generated method stub
		return this.Y;
	}

	@Override
	public double getNumberOfLearnedSamples() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void setDependency(dependency dep) {
		// TODO Auto-generated method stub
		//no implemented
	}

	@Override
	public double getDependency(int i) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isEnable() {
		// TODO Auto-generated method stub
		return this.Enable;
	}

	/**
	 * @param enable the enable to set
	 */
	public void setEnable(boolean enable) {
		Enable = enable;
	}

	/**
	 * @return the distance
	 */
	public double getDistance() {
		return distance;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	public void setStandardDeviation(double r) {
		this.sigma=Math.pow(0.3*r, 2.0);
	}

	/**
	 * @return the sigma
	 */
	public double getSigma() {
		return sigma;
	}



}
