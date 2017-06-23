package KernelPerceptron;

import java.util.ArrayList;

import org.w3c.dom.Node;

import VectorFunctions.VectorFunctions;

public class KernelPerceptron {

	ParameterKernelPerceptron pkp;
	ArrayList<Kernel> HiddenUnits;
	private final boolean DEBUG = false;
	int NumberOfInputs;
	int NumberOfOutputs;
	boolean IsCumulativeErrorEstimation = false;
	boolean UseHindgeLossFunction = false;
	LossFunction LossFunc;


	public KernelPerceptron(Node nd) {
		this.init_parameters(nd);
	}


	public void init_parameters(Node nd) {
		this.pkp = new ParameterKernelPerceptron();
		this.pkp.getParameter(nd);
		this.HiddenUnits = new ArrayList<Kernel>();
		this.NumberOfInputs = this.pkp.getNumberOfInputs();//初期設定はこのように。後から変えても良い
		this.NumberOfOutputs = this.pkp.getNumberOfOutputs();
		this.IsCumulativeErrorEstimation = this.pkp.isCumulativeErrorEstimation();
		this.UseHindgeLossFunction = this.pkp.isUseHindgeLossFunction();
		if (this.UseHindgeLossFunction) {
			this.LossFunc = new HindgeLossFunction();
		}else{
			this.LossFunc = new SquaredErrorFunction();
		}
	}

	public double[] output(double inputs[]) {
		//System.out.println("KernelPerceptron:output() number of output = " + this.NumberOfOutputs);
		double sum[] = new double[this.NumberOfOutputs];
		double each_outputs[];
		for (Kernel cell: this.HiddenUnits) {
			each_outputs = cell.output(inputs);
			for (int i=0; i<this.getNumberOfOutputs(); i++) {
				sum[i] += each_outputs[i];
			}
		}
		if (this.DEBUG) {
			System.out.println("KernelPerceptron.output(): sum[0]=" + sum[0]);
		}
		return sum;
	}

	public void learn(double inputs[], double outputs[]) {
		double provisional_outputs[] = this.output(inputs);
		double error;
		if (this.min_distance(inputs)<Double.MIN_VALUE) return;//If there is the same kernel, do nothing!.
		error = this.LossFunc.getLoss(provisional_outputs, outputs);
		if (error > this.pkp.getErr_threshold()) {
			if (this.DEBUG) {
				System.out.println("KernelPerceptron.learn(): learning process is occured.");
			}
			kernelOrg new_unit = new kernelOrg(this.NumberOfInputs, this.NumberOfOutputs, this.pkp.getDefaultSigma());
			new_unit.setCenter(inputs);
			new_unit.setDesiredOutputs(outputs);
			this.HiddenUnits.add(new_unit);
		}
	}

	public double min_distance(double x[]) {
		double distance = Double.MAX_VALUE;
		double each_distance;
		for (Kernel each_cell: this.HiddenUnits) {
			each_distance = VectorFunctions.getSqureNorm(VectorFunctions.diff(x, each_cell.getCenter()));
			if (each_distance < distance) {
				distance = each_distance;
			}
		}
		return distance;
	}

	public int getNumberOfInputs() {
		return this.NumberOfInputs;
	}

	public int getNumberOfOutputs() {
		return this.NumberOfOutputs;
	}

	public int getNumberOfHiddenUnits() {
		return this.HiddenUnits.size();
	}

	public double getErrorThreshold() {
		return this.pkp.getErr_threshold();
	}

	public double getDefaultSigma() {
		return this.pkp.getDefaultSigma();
	}

	public void addHiddenUnit(Kernel cell) {
		this.HiddenUnits.add(cell);
	}

	/**
	 * @return the isCumulativeErrorEstimation
	 */
	public boolean isIsCumulativeErrorEstimation() {
		return IsCumulativeErrorEstimation;
	}


	public double residual_error(double inputs[], double outputs[]) {
		double[] a_output = this.output(inputs);
		System.out.println("KernelPerceptron.residual_error()");
		double err = this.LossFunc.getLoss(a_output, outputs);
		return err;
	}

	/**
	 * @param numberOfInputs the numberOfInputs to set
	 */
	public void setNumberOfInputs(int numberOfInputs) {
		NumberOfInputs = numberOfInputs;
	}

	/**
	 * @param numberOfOutputs the numberOfOutputs to set
	 */
	public void setNumberOfOutputs(int numberOfOutputs) {
		NumberOfOutputs = numberOfOutputs;
	}

	public void setUpperLimitOfKernels(int numberOfKernels) {
		this.pkp.setUpperLimitOfHiddenUnits(numberOfKernels);
	}

}
