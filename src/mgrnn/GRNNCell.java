/*
 * Created on 2005/07/19
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mgrnn;


import VectorFunctions.VectorFunctions;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class GRNNCell implements Cell {
	int NumberOfInput, NumberOfOutput;
	double[] W;
	double[] T; //templete
	double[] C; //output connections
	
	double R = 1D; // variance;
	double exp_output_value=0D; //cellの出力値
	double actual_distance;
	double shortestDistance; // the distance from the nearest unit.
	double sum_of_outputs;
	boolean check=false;
	int NumberOfActivations = 1;
	
	public GRNNCell(int NumberOfInput, int NumberOfOutput) {
		this.NumberOfInput = NumberOfInput;
		this.NumberOfOutput = NumberOfOutput;
		this.W = new double[this.NumberOfInput];
		this.C = new double[this.NumberOfOutput];
		this.T = new double[this.NumberOfInput];
		for (int i=0; i<this.NumberOfInput; i++) {
			this.W[i] = 10D;
		}
	}//constructer
	
	public void set_variance(double nearest_point[], double lambda) {
		double distance;
		for (int i=0; i<this.NumberOfInput; i++) {
			distance =  lambda*Math.abs((nearest_point[i] - this.T[i]));
			
			/*if (this.W[i] < distance) {
				this.W[i] = distance;
			}*/
			this.R = distance;
		}
	}
	
	public void set_default_variance(double variance, double R) {
		for (int i=0; i<this.NumberOfInput; i++) {
			this.W[i] = variance;
		}
		this.R = R;
	}
	
	public void clean_check() {
		this.check = false;
	}
	
	public void inc_activation_number() {
		this.NumberOfActivations ++;
	}
	
	public int getNumberOfActivations() {
		return this.NumberOfActivations;
	}
	
	public void set_check(boolean value) {
		this.check = value;
	}
	
	public boolean is_check() {
		return this.check;
	}
	
	public double[] output(double input[]) {
		double exp_out = this.exp_output(input);
		double[] outputs = new double[this.NumberOfInput];
		for (int i=0; i<this.NumberOfOutput; i++) {
			outputs[i] = this.C[i] * exp_out;
		}
		return outputs;
	}

	
	public double exp_output(double input[]) {
		double sum2=0D;
		for (int i=0; i<this.NumberOfInput; i++) {
			//sum += Math.pow(this.W[i]*(this.T[i]-input[i]),2D);
			sum2 += Math.pow((this.T[i]-input[i]),2D)/(2*Math.pow(0.3*this.R,2D));	

		}
		//System.out.println("Cell:exp_output(): sum2=" + sum2 + " R=" + this.R);
		this.exp_output_value = Math.exp(-sum2);
		//if (this.exp_output_value<0.1) this.exp_output_value = 0D;
		//this.actual_distance = Math.sqrt(sum);
		this.actual_distance = Math.sqrt(sum2);
		return this.exp_output_value;
	}


	
	public double getActualDistance() {
		return this.actual_distance;
	}
	
	public double exp_output() {
		return this.exp_output_value;
	}
	
	
	// If output is null, the C[] is set to 0 vector.
	public void learn(double input[], double output[]) {
		for (int i=0; i<this.NumberOfInput; i++) {
			this.T[i] = input[i];
		}
		for (int j=0; j<this.NumberOfOutput; j++) {
			if (output == null) {
				this.C[j] = 0D;
			}else{
				this.C[j] = output[j];
			}
		}
	}
	
	public void EFuNNLearn(double input[], double desired_output[], double MaxVariance) {
		double eta =(double)1/((double)this.NumberOfActivations);
		double[] delta_T = new double[this.NumberOfInput];
		for (int i=0; i<this.NumberOfInput; i++) {
			this.T[i] += delta_T[i] = eta * (input[i]- this.T[i]);
		}
		double exp_value = this.exp_output(input);
		for (int j=0; j<this.NumberOfOutput; j++) {
			this.C[j] += eta * (desired_output[j]- this.C[j]*exp_value);
		}
		this.R += VectorFunctions.getNorm(delta_T);
		if (this.R > MaxVariance) this.R = MaxVariance;
		this.NumberOfActivations ++;
	}
	
	public double affect_EFuNNLearning(double input[], double desired_output[]) {
		double eta =(double)1/((double)this.NumberOfActivations);
		double[] delta_c = new double[this.NumberOfOutput];

		double exp_value = this.exp_output(input);
		for (int j=0; j<this.NumberOfOutput; j++) {
			delta_c[j] = eta * (desired_output[j]- this.C[j]*exp_value);
		}
		return VectorFunctions.getSqureNorm(delta_c) * (double)this.NumberOfActivations;
	}
	
	public String OutputHiddenUnitFunction() {
		String data;
		data = this.C[0] + " * exp(-(x - " + this.T[0] +")*(x-" + this.T[0] + ")/"+ 2*Math.pow(0.3*this.R,2D) + ")";
		return data;
	}
	
	/**
	 * @return the t
	 */
	public double[] getT() {
		return T;
	}
	public void setT(double new_center[]) {
		for (int i=0; i<this.NumberOfInput; i++) {
			this.T[i] = new_center[i];
		}
	}

	/**
	 * @return the c
	 */
	public double[] getC() {
		return C;
	}
	
	/**
	 * @param numberOfActivations the numberOfActivations to set
	 */
	public void setNumberOfActivations(int numberOfActivations) {
		NumberOfActivations = numberOfActivations;
	}

	/**
	 * @return the r
	 */
	public double getR() {
		return R;
	}

	public double getAlpha() {
		return 0;
	}
	/**
	 * @param r the r to set
	 */
	public void setR(double r) {
		R = r;
	}

	/**
	 * @param c the c to set
	 */
	public void setC(double[] c) {
		C = c;
	}

	
	//after calculating exp_output()
	public double responsiblity() {
		return this.exp_output_value;
	}

	/**
	 * @return the shortestDistance
	 */
	public double getShortestDistance() {
		return shortestDistance;
	}

	/**
	 * @param shortestDistance the shortestDistance to set
	 */
	public void setShortestDistance(double shortestDistance) {
		this.shortestDistance = shortestDistance;
	}


	public void setSumOfOutputs(double sum) {
		// TODO Auto-generated method stub
		this.sum_of_outputs = sum;
	}


	public double relativeResponsibility() {
		// TODO Auto-generated method stub
		return this.relativeResponsibility()/this.sum_of_outputs;
	}

	// dummy
	public double[] get_normalized_t_alpha() {
		return null;
	}


	public double getNumberOfLearnedSamples() {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public double[] getT_alpha() {
		// TODO Auto-generated method stub
		return null;
	}


	
}
