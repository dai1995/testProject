package RBFNN;

import java.util.Enumeration;
import java.util.Vector;

import matrix.DiagonalMatrixObj;
import matrix.Jacobi;
import matrix.MatrixException;
import matrix.MatrixObj;

import org.w3c.dom.Node;

import FIFO.MinMoutWeightVariableSizedBuffer;
import MixtureOfDistributions.MixtureOfNormalDistributions;

public class wRBFNN_with_VarianceOutput extends wRBFNN {

	//alpha, min_alpha, max_alpha, alpha_step;
	MatrixObj S=null;
	double MarginalLikelihood=0;
	
	public wRBFNN_with_VarianceOutput(Node nd) { 
			//double min_alpha, double max_alpha, double alpha_step) {
		super(nd);
		// TODO Auto-generated constructor stub
		
		/*this.alpha = min_alpha;
		this.min_alpha = min_alpha;
		this.max_alpha = max_alpha;
		this.alpha_step = alpha_step;*/
	}
	

	

	public double[] VarianceOutput(double x[]) {
		double[] variances=new double[this.NumberOfOutputs];
		MatrixObj phi = new MatrixObj(this.HiddenUnits.size(),1);		
		this.getOutputs(x);
		RBF hcell;
		Enumeration<HiddenUnit> e = this.HiddenUnits.elements();
		int cell=0;
		while (e.hasMoreElements()) {
			hcell = (RBF)e.nextElement();
			phi.set_data(cell, 0, hcell.getOutput());
			cell++;
		}
		if (this.DEBUG) {
			this.S.display("wRBFNN_with_VarianceOutput:VarianceOutput(): S");
			phi.display("wRBFNN_with_VarianceOutput:VarianceOutput(): phi");
		}
		double hidden_var = (((phi.Transport()).multiply(this.S)).multiply(phi)).getData(0, 0);
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			variances[o] = 1/this.beta[o] + hidden_var;
		}
		return variances;
	}	
	
	public boolean isChanged(double x[], double output_value) {
		double output[] = this.getOutputs(x);
		double variance[] = this.VarianceOutput(x);
		double max_value = output[0] + Math.sqrt(variance[0]);
		double min_value = output[0] - Math.sqrt(variance[0]);
		if (this.isWithinRegion() && (output_value > max_value || output_value <min_value)) {
			return true;
		}else{
			return false;
		}
	}
	
	public boolean Learning(int NumberOfLearningSamples, 
			double inputs[][], double target_outputs[][], double weights[], double MinSigma, double MaxSigma, double Overlap, boolean UseWeightedKmeans, double alpha) throws MatrixException {
		//init hidden centers
		this.init_hidden_centers(this.HiddenUnits, inputs);
		
		//setup hidden units
		if (this.k_means(this.HiddenUnits,
				this.rbfnn_parameters.getMinDistance(),
				NumberOfLearningSamples, 
				this.rbfnn_parameters.getKMeansChangeThreshold(), inputs,
				MinSigma, MaxSigma, Overlap, this.rbfnn_parameters.getKMeansMaxIteration())) {
			this.display_hidden_centers(this.HiddenUnits);
		
			//setup output connections
			this.wLeastSquare_with_regularization(this.HiddenUnits, NumberOfLearningSamples, inputs, target_outputs, weights, alpha, beta[0]);
			return true;
		}else{
			return false;
		}
	}	
	
	//Hidden Unit setting にk-means法を使う場合
	public boolean Learning(MinMoutWeightVariableSizedBuffer buffer, boolean UseWeightedKmeans, double alpha) throws MatrixException {
		//this.DEBUG = true;
		if (this.DEBUG) {
			System.out.println("wRBFNN:Learning()");
		}
		this.init_hidden_centers(this.HiddenUnits, buffer);
		if (k_means(this.HiddenUnits,
				this.rbfnn_parameters.getMinDistance(),
				this.rbfnn_parameters.getKMeansChangeThreshold(), buffer,
				this.rbfnn_parameters.getMinSigma(), this.rbfnn_parameters.getMaxSigma(), 
				this.rbfnn_parameters.getOverlap(), UseWeightedKmeans,
				this.rbfnn_parameters.getKMeansMaxIteration())) {
			this.display_hidden_centers(this.HiddenUnits);
			wLeastSquare_with_regularization(this.HiddenUnits, buffer, alpha, beta[0]);
			//this.DEBUG = false;
		
			//学習データを圧縮保存する
			this.CompressRecordPastData(buffer, null);
			return true;
		}else{
			return false;
		}
	}
	
	public void ARD(MinMoutWeightVariableSizedBuffer buffer, boolean UseWeightedKmeans, double termination_threshold) throws MatrixException {
		double previous_alpha;
		double previous_beta[];
		MatrixObj phiphi;
		//init hidden centers
		this.init_hidden_centers(this.HiddenUnits, buffer);
		previous_beta = new double[this.NumberOfOutputs];
		//init alpha, beta;
		this.alpha = previous_alpha = 1;
		this.beta[0] = previous_beta[0] = 100; //今は１次元なので０に入れる
		
		//setup hidden units
		k_means(this.HiddenUnits,
				this.rbfnn_parameters.getMinDistance(),
				this.rbfnn_parameters.getKMeansChangeThreshold(), buffer,
				this.rbfnn_parameters.getMinSigma(), this.rbfnn_parameters.getMaxSigma(), 
				this.rbfnn_parameters.getOverlap(), UseWeightedKmeans,
				this.rbfnn_parameters.getKMeansMaxIteration());				
		this.display_hidden_centers(this.HiddenUnits);

		do {
		//setup output connections
			previous_alpha = this.alpha;
			previous_beta[0] = this.beta[0];
			phiphi = this.wLeastSquare_with_regularization(this.HiddenUnits, buffer, this.alpha, this.beta[0]);
			double gamma = this.getGamma(phiphi, this.alpha);
			double norm_w = 0;
			for (int h=0; h<this.HiddenUnits.size(); h++) {
				norm_w += Math.pow(this.C[h][0],2D); 
			}
			this.alpha = gamma / norm_w;
			//this.alpha = 1;
			double err_sum = 0;
			for (int p=0; p<buffer.getSize(); p++) {
				err_sum += this.get_sqare_error(buffer.getInput(p), buffer.getOutput(p));
			}
			this.beta[0] = (buffer.getSize() - gamma)/err_sum;
			//this.beta[0] = 100;
			System.out.println("wRBFNN_with_variance: ARD() alpha=" + this.alpha + " beta=" + this.beta[0] + " gamma=" + gamma + " normW=" + norm_w); 
					//+ " marginal posterior= " + this.MarginalLikelihood(buffer));
		}while ((Math.abs(previous_alpha - this.alpha)+
				Math.abs(previous_beta[0]-this.beta[0]))>termination_threshold);
		this.S = phiphi.Add(new DiagonalMatrixObj(this.HiddenUnits.size(),alpha));
		this.S = this.S.inverse();//used for variance calculation		
	}

	//MixtureOfGaussianからHiddenUnitCenterをインポートする場合
	public boolean Learning(MinMoutWeightVariableSizedBuffer buffer, MixtureOfNormalDistributions mn, double alpha) throws MatrixException {
		//this.DEBUG = true;
		if (this.DEBUG) {
			System.out.println("wRBFNN:Learning()");
		}
		this.init_hidden_centers(this.HiddenUnits, buffer);
		if (this.ImportHiddenCenters(this.HiddenUnits,
				this.rbfnn_parameters.getMinDistance(),
				mn, this.rbfnn_parameters.getMinSigma(),
				this.rbfnn_parameters.getMaxSigma(), 
				this.rbfnn_parameters.getOverlap())) {
			this.display_hidden_centers(this.HiddenUnits);
			wLeastSquare_with_regularization(this.HiddenUnits, buffer, alpha, beta[0]);
			//this.DEBUG = false;
			//学習データを圧縮保存する
			this.CompressRecordPastData(buffer, null);
			return true;
		}else{
			return false;
		}
	}	
	
	

	MatrixObj wLeastSquare_with_regularization(Vector<HiddenUnit> hidden_unit, int NumberOfLearningSamples, double inputs[][], double target[][], double weights[], double alpha, double beta) throws MatrixException {
		HiddenUnit h_cell;
		MatrixObj phi = new MatrixObj(NumberOfLearningSamples, hidden_unit.size());
		MatrixObj T[] = new MatrixObj[this.NumberOfOutputs]; 
		MatrixObj W = new MatrixObj(NumberOfLearningSamples, NumberOfLearningSamples);
		MatrixObj c;
		MatrixObj c_result[] = new MatrixObj[this.NumberOfOutputs];
		MatrixObj phiphi;
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			T[o] = new MatrixObj(NumberOfLearningSamples, 1);
		}
		
		for (int p=0; p<NumberOfLearningSamples; p++) {
			W.set_data(p, p, weights[p]);
			Enumeration<HiddenUnit> e = hidden_unit.elements();
			int m=0;
			while (e.hasMoreElements()) {
				h_cell = (HiddenUnit)e.nextElement();
				phi.set_data(p, m, h_cell.calculate_output(inputs[p]));
				m++;
			}
//			System.out.println("p=" + p);
			//System.out.println("target[0][" + p + "]=" + target[p][0]);
			for (int o=0; o<this.NumberOfOutputs; o++) {
				T[o].set_data(p, 0, target[p][o]);				
			}
		}
		if (this.DEBUG) {
			//T.display("wRBFNN:wLeastSquare():T");
			phi.display("wRBFNN:wLeastSquare():phi");
		}
		this.PHI = phi;
		c = phi.Transport().multiply(W.Transport()); //phi^{T} W^{T}
		c = c.multiply(phi); //phi^{T} W^{T} phi
		phiphi = c.multiply(beta);
		c = c.Add(new DiagonalMatrixObj(this.HiddenUnits.size(),alpha/beta)); // phi^{T} W^{T} phi + (alpha/beta) I		
		c = c.inverse(); // (phi^{T} W^{T} phi + alpha/beta I)^{-1}
		c = c.multiply(phi.Transport()); // (phi^{T} W^{T} phi + alpha/beta I)^{-1} phi^{T}
		c = c.multiply(W); //(phi^{T} W^{T} phi + alpha/beta I)^{-1} phi^{T} W
		
		this.Hat = new MatrixObj(c.getL(), c.getM(), c.getMatrix()); // hat matrix;
		this.Hat = phi.multiply(this.Hat);
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			c_result[o] = c.multiply(T[o]);// (phi^{T} W^{T} phi)^{-1} phi^{T} T
		}
		this.C_result = c_result;//レコードに利用される		
		if (this.DEBUG) {
			c.display("wRBFNN:wLeastSquare():Wmap");
		}

		for (int o=0; o<this.NumberOfOutputs; o++) {
			for (int cell=0; cell<hidden_unit.size(); cell++) {
				this.C[cell][o] = c_result[o].getData(cell, 0);
				System.out.println("C[cell=" + cell + "][" + o + "]=" + this.C[cell][o]);
			}
		}
		return phiphi;
	}
	

	MatrixObj wLeastSquare_with_regularization(Vector<HiddenUnit> hidden_unit, MinMoutWeightVariableSizedBuffer buffer, double alpha, double beta) throws MatrixException {
		HiddenUnit h_cell;
		MatrixObj phi = new MatrixObj(buffer.getSize(), hidden_unit.size());
		MatrixObj T[] = new MatrixObj[this.NumberOfOutputs];
		MatrixObj W = new MatrixObj(buffer.getSize(), buffer.getSize());
		MatrixObj c;
		MatrixObj c_result[] = new MatrixObj[this.NumberOfOutputs];
		MatrixObj phiphi=null;
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			T[o] = new MatrixObj(buffer.getSize(), 1);
		}
		
		for (int p=0; p<buffer.getSize(); p++) {
			W.set_data(p, p, buffer.getActualWeight(p));
			Enumeration<HiddenUnit> e = hidden_unit.elements();
			int m=0;
			while (e.hasMoreElements()) {
				h_cell = (HiddenUnit)e.nextElement();
				phi.set_data(p, m, h_cell.calculate_output(buffer.getInput(p)));
				m++;
			}
//			System.out.println("p=" + p);
			//System.out.println("target[0][" + p + "]=" + target[p][0]);
			for (int o=0; o<this.NumberOfOutputs; o++) {
				T[o].set_data(p, 0, buffer.getOutput(p)[o]);
			}
		}
		if (this.DEBUG) {
			//T.display("wRBFNN:wLeastSquare():T");
			phi.display("wRBFNN:wLeastSquare():phi");
		}
		this.PHI = phi;
		c = phi.Transport().multiply(W.Transport()); //phi^{T} W^{T}
		c = c.multiply(phi); //phi^{T} W^{T} phi
		phiphi = c.multiply(beta);
		c = c.Add(new DiagonalMatrixObj(this.HiddenUnits.size(),alpha/beta)); // phi^{T} W^{T} phi + (alpha/beta) I
		c = c.inverse(); // (phi^{T} W^{T} phi + (alpha/beta) I)^{-1}
		c = c.multiply(phi.Transport()); // (phi^{T} W^{T} phi + (alpha/beta)I)^{-1} phi^{T}
		c = c.multiply(W); //(phi^{T} W^{T} phi +(alpha/beta)I)^{-1} phi^{T} W
		
		this.Hat = new MatrixObj(c.getL(), c.getM(), c.getMatrix()); // hat matrix;
		this.Hat = phi.multiply(this.Hat);
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			c_result[o] = c.multiply(T[o]); // (phi^{T} W^{T} phi+(alpha/beta)I)^{-1} phi^{T} T
		}
		this.C_result = c_result;//レコードに利用される		
		
		if (this.DEBUG) {
			c.display("wRBFNN:wLeastSquare():Wmap");
		}

		for (int o=0; o<this.NumberOfOutputs; o++) {		
			for (int cell=0; cell<hidden_unit.size(); cell++) {
				this.C[cell][o] = c_result[o].getData(cell, 0);
				//System.out.println("C[cell=" + cell + "][" + o + "]=" + this.C[cell][o]);				
			}
		}
		return phiphi;
	}

	public double MarginalLikelihood(MinMoutWeightVariableSizedBuffer buffer) {
		double result=0D;
		double sqr_err=0D;
		for (int p=0; p<buffer.getSize(); p++) {
			sqr_err += this.get_sqare_error(buffer.getInput(p), buffer.getOutput(p), buffer.getSourceWeight(p));
		}
		result = ((double)this.HiddenUnits.size()/2) * Math.log(this.alpha) + 
			((double)buffer.getSize()/2) * Math.log(this.beta[0]) - (1/2) * sqr_err - 
			(1/2)* Math.log(this.get_A(this.HiddenUnits, buffer, this.alpha, this.beta[0]).det()) 
			- ((double)buffer.getSize()/2) * Math.log(2*Math.PI);
		this.MarginalLikelihood = result;
		return result;
	}

	public double MarginalLikelihood() {
		return this.MarginalLikelihood;
	}
	
	
	double getGamma(MatrixObj phiphi, double alpha) throws MatrixException {
		MatrixObj A = phiphi;
		//A.display("A");
		Jacobi pj = new Jacobi(A.getL(), A.getMatrix());
		
		pj.jacobi();
		double sum=0;
		for (int p=0; p<A.getL(); p++) {
			double gamma_i = pj.get_DescentOrderEigenValue(p);
			sum += gamma_i / (gamma_i + alpha);
		}
		return sum;
	}		
		
}
