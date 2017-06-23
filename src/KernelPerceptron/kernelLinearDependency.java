package KernelPerceptron;

import matrix.MatrixException;
//import matrix.MatrixObj;

public class kernelLinearDependency extends kernelOrg implements Kernel {
	//private final boolean DEBUG = false;
	private double NumberOfLearnedSamples = 0;
	private double ImportanceWeight = 1D;
	dependency dep;
	
	public kernelLinearDependency(int NumberOfInputs, int NumberOfOutputs,
			double r) {
		super(NumberOfInputs, NumberOfOutputs, r);
		// TODO Auto-generated constructor stub
	}
	
	
	// This method is used for replace process.
	public void learn(double input[], double output[]) throws MatrixException {
		for (int i=0; i<this.getNumberOfInputs(); i++) {
			this.u[i] = input[i];
		}
		for (int j=0; j<this.getNumberOfOutputs(); j++) {
			this.Y[j] = output[j];
		}
		this.NumberOfLearnedSamples = 1;
	}
	
	//for Projection process! Incremental learning of new outputs based on the LinearDependency
	public void Increment(double y[], double LinearDependency, double NumberOfSamples) {
		this.NumberOfLearnedSamples += NumberOfSamples;
		for (int o=0; o<this.getNumberOfOutputs(); o++) {
			this.Y[o] += y[o] * LinearDependency;
		}
	}
	
	//This method is the modified version of the above original one.
	//Here, tau denotes the learning ratio, which is optimized in the Projectron++.
	public void Increment(double y[], double LinearDependency, double tau, double NumberOfSamples) {
		this.NumberOfLearnedSamples += tau * NumberOfSamples;
		for (int o=0; o<this.getNumberOfOutputs(); o++) {
			this.Y[o] += y[o] * tau * LinearDependency;
		}
	}	
	
	@Override
	public double getNumberOfLearnedSamples() {
		// TODO Auto-generated method stub
		return this.NumberOfLearnedSamples;
	}
	
	/**
	 * @param numberOfLearnedSamples the numberOfLearnedSamples to set
	 */
	public void setNumberOfLearnedSamples(double numberOfLearnedSamples) {
		NumberOfLearnedSamples = numberOfLearnedSamples;
	}


	@Override
	public void setDependency(dependency dep) {
		// TODO Auto-generated method stub
		this.dep = dep;
	}	
	
	@Override
	public double getDependency(int i) {
		// TODO Auto-generated method stub
		return this.dep.alpha.getData(i, 0);
	}


	/**
	 * @return the importanceWeight
	 */
	public double getImportanceWeight() {
		return ImportanceWeight;
	}


	/**
	 * @param importanceWeight the importanceWeight to set
	 */
	public void setImportanceWeight(double importanceWeight) {
		if (importanceWeight >= 0) {
			ImportanceWeight = importanceWeight;
		}else{
			System.err.println("KernelLinearDependency.setImportanceWeight(): error! importance weight should be positive value!! Provisionally, the value was set to 1.");
			ImportanceWeight=1D;
		}
	}

	
}
