package KernelPerceptron;



import java.util.ArrayList;

import mgrnn.Cell;
import mgrnn.LgrnnLearningStatus;
import org.w3c.dom.Node;

import VectorFunctions.*;
//import ActorCriticInterfaces.*;


public class KernelPerceptronLRFU extends Forgetron4Regression{
	final boolean DEBUG = true;
	protected double lambda = 0.1;
    protected double CBThreshold = 0.1;
    
	public KernelPerceptronLRFU(Node nd) {
		super(nd);
		// TODO Auto-generated constructor stub
		this.lambda = this.pkp.getLambda();
	}
	
	
	
	public double[] output(double inputs[], double ImportanceWeight, boolean UpdateLRFUStatus) {
		//System.out.println("KernelPerceptron:output() number of output = " + this.NumberOfOutputs);
		double sum[] = new double[this.NumberOfOutputs];
		Kernel NearestKernel = this.getNearestKernel(inputs); 
		double each_outputs[];
		for (Kernel cell: this.HiddenUnits) {
			each_outputs = cell.output(inputs);
			for (int i=0; i<this.getNumberOfOutputs(); i++) {
				sum[i] += each_outputs[i];
			}
		}
		if (UpdateLRFUStatus) {
			//Modify the status parameters of CellLRFU, whose center is the closet to the input.
			
			if (this.HiddenUnits.size()>0) {
				Kernel4LRFU NearestCellLRFU = (Kernel4LRFU)NearestKernel;//cast: Cell -> CellLRFU
				NearestCellLRFU.SetActivate(ImportanceWeight);//modify the status parameters.
			
				for (Kernel each_cell : this.HiddenUnits) {
					Kernel4LRFU each_cellLRFU = (Kernel4LRFU)each_cell;
					if (!each_cellLRFU.equals(NearestCellLRFU)) {//for all CellLRFU except for the nearest unit.
						each_cellLRFU.InActive();
					}
				}
			}
		}
		if (this.DEBUG) {
			System.out.println("KernelPerceptron.output(): sum[0]=" + sum[0]);
		}
		return sum;
	}
	
	
	Kernel getNearestKernel(double inputs[]) {
		double distance;
		Kernel NearestKernel = null;
		double MinDistance = Double.MAX_VALUE;
		for (Kernel each_kernel : this.HiddenUnits) {
			distance = VectorFunctions.getSqureNorm(VectorFunctions.diff(each_kernel.getCenter(), inputs));
			if (distance < MinDistance) {
				NearestKernel = each_kernel;
			}
		}
		return NearestKernel;
	}
	
	
	
	public void learn(double inputs[], double outputs[], double ImportanceWeight, double CbThreshold) {
		double provisional_outputs[] = this.output(inputs, ImportanceWeight, true);
		
		double errors[] = VectorFunctions.diff(outputs, provisional_outputs);
		double error = VectorFunctions.getSqureNorm(errors);
		double dummy[] = new double[this.getNumberOfOutputs()];
		
		//System.out.println("KernelPerceptron4Regression.learn(): learning process is occured.");		
		if (error > this.getErrorThreshold()) {
			this.Log("learn() : learning process is occured!");
			if (this.getNumberOfHiddenUnits() < this.pkp.getUpperLimitOfHiddenUnits()) {
				Kernel4LRFU new_unit = new Kernel4LRFU(this.getNumberOfInputs(), this.getNumberOfOutputs(), this.getDefaultSigma(), this.getLambda());
				new_unit.setCenter(inputs);
				new_unit.setDesiredOutputs(errors);// note: the desired output should be the error for regression.
				//new_unit.setDesiredOutputs(outputs);
				this.addHiddenUnit(new_unit);
			}else{
				Kernel4LRFU target_unit = this.getMostIneffectiveKernel();
				if (target_unit.getCb() < CbThreshold) {
					target_unit.setCenter(inputs);
					target_unit.setDesiredOutputs(dummy);
					target_unit.resetAge();
					target_unit.InitCb(ImportanceWeight);
					//provisional_outputs = this.output(inputs, ImportanceWeight, false);
					provisional_outputs = this.output(inputs, ImportanceWeight, true);
					errors = VectorFunctions.diff(outputs, provisional_outputs);
					target_unit.setDesiredOutputs(errors);
					//target_unit.setDesiredOutputs(outputs);
				}
			}
		}
	}	
	
	
	public double getLambda() {
		return lambda;
	}

	private void Log(String log) {
		if (this.DEBUG) {
			System.out.println("KernelPerceptronLRFU." + log);
		}
	}


	public LgrnnLearningStatus learning(double[] inputs, double[] outputs,
			double UtilityFunction) {
		// TODO Auto-generated method stub
		//System.out.println(outputs[3]);
		this.learn(inputs, outputs, UtilityFunction, this.CBThreshold);
		return null;
	}



	public int getOutputSize() {
		// TODO Auto-generated method stub
		return this.getNumberOfOutputs();
	}




	public double[] calculate_outputs(double[] input) {
		// TODO Auto-generated method stub
		return this.output(input);
	}



	public double[][] display_all_kernels() {
		// TODO Auto-generated method stub
		return null;
	}



	public ArrayList<Kernel> getHidden_units() {
		// TODO Auto-generated method stub
		return this.HiddenUnits;
	}



	public Cell getNearestUnit(double[] inputs) {
		// TODO Auto-generated method stub
		return this.getNearestUnit(inputs);
	}

	protected Kernel4LRFU getMostIneffectiveKernel() {
		double min_cb = Double.MAX_VALUE;
		Kernel4LRFU most_ineffective=null;
		for (Kernel each_cell : this.HiddenUnits){
			Kernel4LRFU each_kernel = (Kernel4LRFU)each_cell;
			if (each_kernel.getCb()<min_cb) {
				min_cb = each_kernel.getCb();
				most_ineffective = each_kernel;
			}
		}
		return most_ineffective;
	}



	public void setCBThreshold(double cBThreshold) {
		CBThreshold = cBThreshold;
	}


}
