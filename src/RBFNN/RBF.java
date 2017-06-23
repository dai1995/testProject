package RBFNN;

import matrix.MatrixObj;
import FIFO.MinMoutWeightVariableSizedBuffer;

public class RBF implements HiddenUnit {
	boolean DEBUG = false;
	int NumberOfInputs;
	double u[];
	double sigma=0.1, sigma2=0.01;
	double w_eta, w_V, w_phi;
	int NumberOfSamples;
	double Output;
	//protected boolean IsFixed = false;
	
	double sum_inputs[], total_weights;
	
	public RBF(int NumberOfInputs, double init_sigma) {
		this.NumberOfInputs = NumberOfInputs;
		this.u = new double[NumberOfInputs];
		this.sum_inputs = new double[NumberOfInputs];
		this.sigma = init_sigma;
		this.sigma2 = Math.pow(this.sigma, 2D);
	}

	public double calculate_output(double[] input) {
		// TODO Auto-generated method stub
		double sum;
		sum = this.get_square_norm(input, this.u);
		//System.out.println("RBF: calculate_output sum=" +sum);
		this.Output = Math.exp(-sum/(2*this.sigma2));
		
		//this.w_phi += Math.pow(this.Output, 2D) / (double)this.NumberOfSamples;//for getting average
		this.NumberOfSamples++;
		
		return this.Output;
	}
	public String getPlot() {
		String result= "exp(";
		if (this.NumberOfInputs == 1) {
			result += "-((x-(" + this.u[0] + "))**2)/(" + (2*this.sigma2) + "))";
			return result;
		}else{
			return "NumberOfInputs must be less than 2 \n";
		}
	}
	
	public double getOutput() {
		return this.Output;
	}
	
	public void reset_average() {
		this.w_phi = 0;
		this.NumberOfSamples = 1;
	}

	public void calculate_mean_Var_s(MinMoutWeightVariableSizedBuffer buffer, double[] mean_x, double[] sigma_x, double c) {
		double sum=0, weight_sum = 0D;
		double w_mean_s=0;
		for (int p=0; p<buffer.getSize(); p++) {
			sum += buffer.getSourceWeight(p) * Math.pow(this.calculate_output(buffer.getInput(p)),2D);
			weight_sum += buffer.getSourceWeight(p);
		}
		sum /= weight_sum;
		sum *= Math.pow(c, 2D);
		this.w_phi = sum;
		for (int i=0; i<mean_x.length; i++) {
	  		w_mean_s += sigma_x[i] + Math.pow(mean_x[i] - this.u[i], 2D);//P.1297			
		}
		this.w_V = this.w_phi * w_mean_s / Math.pow(this.sigma, 4D);
		this.w_eta = this.w_phi / Math.pow(this.sigma, 4D);
	  	if (this.DEBUG) {
	  		System.out.println("RBF:calculate_mean_Var_s(): w_eta = " + this.w_eta + " w_V = " + this.w_V); 
	  	} 	 		
	}
	
	public void calculate_mean_Var_s(double[] mean_x, double[] sigma_x,
			double[] diff3, double[] diff4, double c) {
		// TODO Auto-generated method stub	
	  	double w_mean_s = 0D;
	  	double w_var_s = 0D;

	  	this.DEBUG = true;
	  	for (int i=0; i<mean_x.length; i++) {
	  		if (this.DEBUG) {
	  			System.out.println("RBF:calculate_mean_Var_s(): sigma_x[" + i +"] = " + sigma_x[i]);
	  			System.out.println("RBF:calculate_mean_Var_s(): mean_x[" + i +"] = " + mean_x[i]);
	  		}
	  		w_mean_s += sigma_x[i] + Math.pow(mean_x[i] - this.u[i], 2D);//P.1297
	  		w_var_s += diff4[i] - Math.pow(sigma_x[i], 2D) + 4 * sigma_x[i] * Math.pow(mean_x[i]-u[i], 2D)
				+ 4*diff3[i]*(mean_x[i]-u[i]);
	  	}
	  	
	  	double sum = w_var_s /(2*Math.pow(this.sigma, 4D)) - w_mean_s/Math.pow(this.sigma, 2D);
	  	if (this.DEBUG) {
	  		System.out.println("RBF:calculate_mean_Var_s(): w_mean_s = " + w_mean_s + " w_var_s = " + w_var_s);
	  		System.out.println("RBF:calculate_mean_Var_s(): sum = " + sum);
	  	}
	  	

	  	this.w_phi=Math.pow(c,2D) * Math.exp(w_var_s /(2*Math.pow(this.sigma, 4D)) - w_mean_s/Math.pow(this.sigma, 2D));
	  	//this.w_phi *= Math.pow(c, 2D);
	  	
	  	if (this.DEBUG) {
	  		System.out.println("RBF:calculate_mean_Var_s(): w_phi = " + w_phi + " sigma = " + this.sigma); 
	  	}
	  	
	  	this.w_eta = this.w_phi / Math.pow(this.sigma, 4D);
	  	this.w_V = this.w_phi * w_mean_s / Math.pow(this.sigma, 4D);
	  	if (this.DEBUG) {
	  		System.out.println("RBF:calculate_mean_Var_s(): w_eta = " + this.w_eta + " w_V = " + this.w_V); 
	  	} 	  	
	  	this.DEBUG = false;		
	}
	
	public double getEta() {
		return this.w_eta;
	}
	
	public double getV() {
		return this.w_V;
	}


	public double[] getCenter() {
		// TODO Auto-generated method stub
		return this.u;
	}


	public double getVariance() {
		return this.sigma;
	}


	public void setCenter(double[] u) {
		// TODO Auto-generated method stub
		for (int i=0; i<this.NumberOfInputs; i++) {
			this.u[i] = u[i];
		}
	}
	public void setCenter(MatrixObj U) {
		double matrix_u[][];
		matrix_u = U.getMatrix();
		for (int i=0; i<this.NumberOfInputs; i++) {
			this.u[i] = matrix_u[i][0];
		}
	}

	public void setVariance(double sigma) {
		// TODO Auto-generated method stub
		this.sigma = sigma;
		this.sigma2 = Math.pow(this.sigma, 2D);
	}

	//MStepの前に実行する。変数の初期化
	public void init_M_step() {
		for (int i=0; i<this.NumberOfInputs; i++) {
			this.sum_inputs[i] = 0D;
		}
		this.total_weights = 0D;
	}
	
	public void M_step_add(double inputs[], double sumOfHiddenOutputs) {
		double weight = this.calculate_output(inputs) / sumOfHiddenOutputs;
		this.total_weights += weight;
		for (int i=0; i<this.NumberOfInputs; i++) {
			this.sum_inputs[i] += weight * inputs[i];
		}
	}
	
	public void w_M_step_add(double inputs[], double sumOfHiddenOutputs, double CovShiftWeight) {
		double weight = CovShiftWeight * this.calculate_output(inputs) / sumOfHiddenOutputs;
		//System.out.println("weight = " + weight + " h_cell_output=" + this.getOutput() + " sum_output=" + sumOfHiddenOutputs);
		this.total_weights += weight;
		for (int i=0; i<this.NumberOfInputs; i++) {
			this.sum_inputs[i] += weight * inputs[i];
		}
		//System.out.println(" sum_inputs[0]=" + sum_inputs[0] + " total_weights=" + this.total_weights);
	}
	
	public double M_step_update_center() {
		double new_center[] = new double[this.NumberOfInputs];
		double diff;
		for (int i=0; i<this.NumberOfInputs; i++) {
			new_center[i] = this.sum_inputs[i] / this.total_weights;
		}
		diff = this.get_square_norm(new_center, this.u);
		for (int i=0; i<this.NumberOfInputs; i++) {
			this.u[i] = new_center[i];
		}
		//System.out.println("u=" + u[0] + " sum_inputs[0]=" + sum_inputs[0] + " total_weights=" + this.total_weights);
		return diff;
	}
	
	public double getDistance(double x[]) {
		return this.get_square_norm(this.u, x);
	}
	
	public boolean WithinRegion(double x[]) {
		double distance = this.get_square_norm(this.u, x);
		if (distance < this.sigma2) {
			return true;
		}else{
			return false;
		}
	}
	
	public double get_square_norm(double x[], double y[]) {
		double sum=0D;
		for (int i=0; i<this.NumberOfInputs; i++) {
			sum += Math.pow(x[i]-y[i], 2D);
		}
		return sum;
	}

	/**
	 * @return the isFixed
	 */
	/*public boolean isFixed() {
		return IsFixed;
	}*/

	/**
	 * @param isFixed the isFixed to set
	 */
	/*public void setFixed(boolean isFixed) {
		IsFixed = isFixed;
	}*/
	
	public RBF clone() {
		RBF myclone = new RBF(this.NumberOfInputs, this.sigma);
		for (int i=0; i<this.NumberOfInputs; i++) {
			myclone.u[i] = this.u[i];
		}
		return myclone;
	}
}
