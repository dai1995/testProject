package KernelPerceptron;

public class Kernel4LRFU extends kernel4Forgetron {
	private double gain, Cb=0D;
	public Kernel4LRFU(int NumberOfInputs, int NumberOfOutputs, double sigma, double lambda) {
		super(NumberOfInputs, NumberOfOutputs, sigma);
		// TODO Auto-generated constructor stub
		this.gain = Math.pow(0.5, lambda);
	}
	
	public void SetActivate(double importanceWeight) {
		this.Cb = importanceWeight + this.gain * this.Cb;
	}
	
	public void InActive() {
		this.Cb *= this.gain;
	}

	public double getCb() {
		return Cb;
	}
	
	public void InitCb(double importance_weight) {
		this.Cb = importance_weight;
	}

}
