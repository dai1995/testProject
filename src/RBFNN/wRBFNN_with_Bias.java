package RBFNN;

import java.util.Enumeration;
import java.util.Vector;

import matrix.MatrixException;
import matrix.MatrixObj;

import org.w3c.dom.Node;

import FIFO.MinMoutWeightVariableSizedBuffer;
import MixtureOfDistributions.MixtureOfNormalDistributions;

public class wRBFNN_with_Bias extends RBFNN_with_Bias {
	boolean DEBUG=false;
	MatrixObj Hat;//Hat matrix
	public wRBFNN_with_Bias(Node nd) {
		super(nd);
		// TODO Auto-generated constructor stub
	}
	
	public void Learning(int NumberOfLearningSamples, 
			double inputs[][], double target_outputs[][], double weights[], double MinSigma, double MaxSigma, double Overlap, boolean UseWeightedKmeans) throws MatrixException {
		//init hidden centers
		this.init_hidden_centers(this.HiddenUnits, inputs);
		
		//setup hidden units
		this.k_means(this.HiddenUnits, NumberOfLearningSamples, this.rbfnn_parameters.getKMeansChangeThreshold(), inputs,
				MinSigma, MaxSigma, Overlap);
		this.display_hidden_centers(this.HiddenUnits);
		
		//setup output connections
		this.wLeastSquare(this.HiddenUnits, NumberOfLearningSamples, inputs, target_outputs, weights);
	}	
	
	public void Learning(MinMoutWeightVariableSizedBuffer buffer, boolean UseWeightedKmeans) throws MatrixException {
		//this.DEBUG = true;
		if (this.DEBUG) {
			System.out.println("wRBFNN:Learning()");
		}
		this.init_hidden_centers(this.HiddenUnits, buffer);
		k_means(this.HiddenUnits, this.rbfnn_parameters.getKMeansChangeThreshold(), buffer,
				this.rbfnn_parameters.getMinSigma(), this.rbfnn_parameters.getMaxSigma(), this.rbfnn_parameters.getOverlap(), UseWeightedKmeans);
		this.display_hidden_centers(this.HiddenUnits);
		wLeastSquare(this.HiddenUnits, buffer);
		//this.DEBUG = false;
	}

	//MixtureOfGaussianからHiddenUnitCenterをインポートする場合(kmeansだと孤立点が１つ増えるだけで不安定になるのでEMにした）
	public void Learning(MinMoutWeightVariableSizedBuffer buffer, MixtureOfNormalDistributions mn) throws MatrixException {
		//this.DEBUG = true;
		if (this.DEBUG) {
			System.out.println("wRBFNN:Learning()");
		}
		this.init_hidden_centers(this.HiddenUnits, buffer);
		this.ImportHiddenCenters(this.HiddenUnits, mn, this.rbfnn_parameters.getMinSigma(),
				this.rbfnn_parameters.getMaxSigma(), 
				this.rbfnn_parameters.getOverlap());
		this.display_hidden_centers(this.HiddenUnits);
		wLeastSquare(this.HiddenUnits, buffer);
		//this.DEBUG = false;
	}	
	

	void wLeastSquare(Vector<HiddenUnit> hidden_unit, int NumberOfLearningSamples, double inputs[][], double target[][], double weights[]) throws MatrixException {
		HiddenUnit h_cell;
		MatrixObj phi = new MatrixObj(NumberOfLearningSamples, hidden_unit.size()+1);
		MatrixObj T[] = new MatrixObj[this.NumberOfOutputs]; 
		MatrixObj W = new MatrixObj(NumberOfLearningSamples, NumberOfLearningSamples);
		MatrixObj c;
		MatrixObj c_result[] = new MatrixObj[this.NumberOfOutputs];

		for (int o=0; o<this.NumberOfOutputs; o++) {
			T[o] = new MatrixObj(NumberOfLearningSamples, 1);
		}
		
		for (int p=0; p<NumberOfLearningSamples; p++) {
			W.set_data(p, p, weights[p]);
			phi.set_data(p, 0, 1.0D);//bias
			Enumeration<HiddenUnit> e = hidden_unit.elements();
			int m=1;
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
		c = c.inverse(); // (phi^{T} W^{T} phi)^{-1}
		c = c.multiply(phi.Transport()); // (phi^{T} W^{T} phi)^{-1} phi^{T}
		c = c.multiply(W); //(phi^{T} W^{T} phi)^{-1} phi^{T} W
		this.Hat = new MatrixObj(c.getL(), c.getM(), c.getMatrix()); // hat matrix;
		this.Hat = phi.multiply(this.Hat);
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			c_result[o] = c.multiply(T[o]);// (phi^{T} W^{T} phi)^{-1} phi^{T} T
		}
		
		if (this.DEBUG) {
			c.display("wRBFNN:wLeastSquare():Wmap");
		}

		for (int o=0; o<this.NumberOfOutputs; o++) {
			for (int cell=0; cell<hidden_unit.size()+1; cell++) {
				this.C[cell][o] = c_result[o].getData(cell, 0);
				System.out.println("C[cell=" + cell + "][" + o + "]=" + this.C[cell][o]);
			}
		}
	}

	

	void wLeastSquare(Vector<HiddenUnit> hidden_unit, MinMoutWeightVariableSizedBuffer buffer) throws MatrixException {
		HiddenUnit h_cell;
		MatrixObj phi = new MatrixObj(buffer.getSize(), hidden_unit.size()+1);
		MatrixObj T[] = new MatrixObj[this.NumberOfOutputs];
		MatrixObj W = new MatrixObj(buffer.getSize(), buffer.getSize());
		MatrixObj c;
		MatrixObj c_result[] = new MatrixObj[this.NumberOfOutputs];
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			T[o] = new MatrixObj(buffer.getSize(), 1);
		}
		
		for (int p=0; p<buffer.getSize(); p++) {
			W.set_data(p, p, buffer.getActualWeight(p));
			phi.set_data(p, 0, 1D);
			Enumeration<HiddenUnit> e = hidden_unit.elements();
			int m=1;
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
		c = c.inverse(); // (phi^{T} W^{T} phi)^{-1}
		c = c.multiply(phi.Transport()); // (phi^{T} W^{T} phi)^{-1} phi^{T}
		c = c.multiply(W); //(phi^{T} W^{T} phi)^{-1} phi^{T} W
		this.Hat = new MatrixObj(c.getL(), c.getM(), c.getMatrix()); // hat matrix;
		this.Hat = phi.multiply(this.Hat);

		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			c_result[o] = c.multiply(T[o]); // (phi^{T} W^{T} phi)^{-1} phi^{T} T
		}
		
		if (this.DEBUG) {
			c.display("wRBFNN:wLeastSquare():Wmap");
		}

		for (int o=0; o<this.NumberOfOutputs; o++) {		
			for (int cell=0; cell<hidden_unit.size()+1; cell++) {
				this.C[cell][o] = c_result[o].getData(cell, 0);
				System.out.println("C[cell=" + cell + "][" + o + "]=" + this.C[cell][o]);				
			}
		}
	}
	

	void k_means(Vector<HiddenUnit> hidden_units, double change_threshold, MinMoutWeightVariableSizedBuffer buffer, double MinSigma, double MaxSigma, double Overlap, boolean UseWeightedSamples) {
		RBF h_cell=null;
		double total_change, changes = 10000;
		double sumOfHiddenOutputs;
		Enumeration<HiddenUnit> e = hidden_units.elements();
		while (changes > change_threshold) {
		
			total_change = 0;
			while (e.hasMoreElements()) {
				h_cell = (RBF)e.nextElement();
				h_cell.init_M_step();
				for (int p=0; p<buffer.getSize(); p++) {
					sumOfHiddenOutputs = this.getSumOfHiddenOutputs(buffer.getInput(p));
					if (this.DEBUG) {
						System.out.println("wRBFNN:k_means(): sumOutput=" + sumOfHiddenOutputs);
					}
					if (sumOfHiddenOutputs < 1.0E-50) {
						sumOfHiddenOutputs = 0.0000001;
					}
					if (UseWeightedSamples) { 
						h_cell.w_M_step_add(buffer.getInput(p), sumOfHiddenOutputs, buffer.getActualWeight(p));
					}else{
						h_cell.M_step_add(buffer.getInput(p), sumOfHiddenOutputs);
					}
				}
				total_change += h_cell.M_step_update_center();
			}
			this.set_variance(hidden_units, MinSigma, MaxSigma, Overlap);
			changes = total_change/hidden_units.size();
		}
	}	
	

	void ImportHiddenCenters(Vector<HiddenUnit> hidden_units, MixtureOfNormalDistributions mn, double MinSigma, double MaxSigma, double Overlap) {
		RBF h_cell=null;
		int index=0;
		Enumeration<HiddenUnit> e = hidden_units.elements();
		while (e.hasMoreElements()) {
			h_cell = (RBF)e.nextElement();
			h_cell.setCenter(mn.getDistributionCenter(index));
			index++;
		}
		this.set_variance(hidden_units, MinSigma, MaxSigma, Overlap);
	}
	
	// input[][] -> buffer
	void init_hidden_centers(Vector<HiddenUnit> hidden_units, MinMoutWeightVariableSizedBuffer buffer) {
		Enumeration<HiddenUnit> e = hidden_units.elements();
		HiddenUnit h_cell;
		int p=0;
		while (e.hasMoreElements()) {
			h_cell = (HiddenUnit)e.nextElement();
			h_cell.setCenter(buffer.getInput(p));
			p++;
		}
	}
	
	public double get_sqare_error(double[] inputs, double[] target_output, double weight) {
		// TODO Auto-generated method stub
		double output[] = this.getOutputs(inputs);
		double error=0D;
		for (int o=0; o<this.NumberOfOutputs; o++) {
			error += weight * Math.pow(target_output[o]-output[o], 2D);
		}
		return error;
	}	
	
	public double get_hidden_output(double[] inputs, int cell) {
		HiddenUnit h_cell;
		h_cell = (HiddenUnit)this.HiddenUnits.get(cell);
		return h_cell.calculate_output(inputs);
	}

	/**
	 * @return the hat
	 */
	public MatrixObj getHat() {

		return Hat;
	}
	
	
}
