package KernelPerceptron;

import java.util.ArrayList;

import matrix.MatrixException;
import matrix.MatrixObj;

import org.w3c.dom.Node;

import VectorFunctions.VectorFunctions;
//import ActorCriticInterfaces.actor_critic;

public class KernelLeastSquareLRFU extends KernelPerceptronLRFU{
	final boolean DEBUG = true;
	MatrixObj K, Y[];
	ArrayList<Double> Desired[];
	public KernelLeastSquareLRFU(Node nd) {
		super(nd);
		// TODO Auto-generated constructor stub
		this.K =  null;

		this.LossFunc = new SquaredErrorFunction();
	}
	
	private void initDesired() {
		this.Y = new MatrixObj[this.NumberOfOutputs];
		this.Desired = new ArrayList[this.NumberOfOutputs];
		for (int o=0; o<this.NumberOfOutputs; o++) {
			this.Desired[o] = new ArrayList<Double>();
		}
	}
	
	public void learn(double inputs[], double outputs[], double ImportanceWeight) {
		double provisional_outputs[] = this.output(inputs, ImportanceWeight, true);
		
		double errors[] = VectorFunctions.diff(outputs, provisional_outputs);
		double error = VectorFunctions.getSqureNorm(errors);
		double dummy[] = new double[this.getNumberOfOutputs()];
		
		
		//System.out.println("KernelPerceptron4Regression.learn(): learning process is occured.");		
		if (error > this.getErrorThreshold()) {
			//System.out.println("(" +outputs[0] +"," + outputs[1] +")");
			for (int o=0; o<this.NumberOfOutputs; o++) {
				this.Desired[o].add(outputs[o]);
			}
			this.displayDesOutput(Desired);
			
			this.Log("learn() : learning process is occured!");
			if (this.getNumberOfHiddenUnits() < this.pkp.getUpperLimitOfHiddenUnits()) {
				
				
				Kernel4LRFU new_unit = new Kernel4LRFU(this.getNumberOfInputs(), this.getNumberOfOutputs(), this.getDefaultSigma(), this.getLambda());
				new_unit.setCenter(inputs);
				//new_unit.setDesiredOutputs(errors);// note: the desired output should be the error for regression.
				//new_unit.setDesiredOutputs(outputs);
				this.addHiddenUnit(new_unit);

			}else{
				//LRFU pruning
				Kernel4LRFU target_unit = this.getMostIneffectiveKernel();
				for (int j=0; j<this.HiddenUnits.size(); j++) {
					Kernel each_kernel = this.HiddenUnits.get(j);
					if (target_unit.equals((Kernel4LRFU)each_kernel)) {
						for (int o=0; o<this.NumberOfOutputs; o++) {
							this.Desired[o].remove(j);
						}
						this.Log("learning() pruning j=" + j);
						this.HiddenUnits.remove(j);
						break;
					}
				}
				
				Kernel4LRFU new_unit =new Kernel4LRFU(this.getNumberOfInputs(), this.getNumberOfOutputs(), this.getDefaultSigma(), this.getLambda());
				new_unit.setCenter(inputs);
				new_unit.setDesiredOutputs(dummy);
				new_unit.resetAge();
				new_unit.InitCb(ImportanceWeight);
				this.HiddenUnits.add(new_unit);
				//provisional_outputs = this.output(inputs, ImportanceWeight, false);
				//provisional_outputs = this.output(inputs, ImportanceWeight, true);
				//errors = VectorFunctions.diff(outputs, provisional_outputs);
				//target_unit.setDesiredOutputs(errors);
				//target_unit.setDesiredOutputs(outputs);
			}
			
			//LeastSquare
			this.LeastSquare(this.Desired, HiddenUnits);
		}
	}	
	
	
	void displayDesOutput(ArrayList<Double>[] desired) {
		this.Log("displayDesired()");
		for (ArrayList<Double> each : desired) {
			for (int i=0; i<this.NumberOfOutputs; i++) {
				System.out.print(" " + each);
			}
			System.out.println();
		}
	}
	
	private void LeastSquare(ArrayList<Double>[] desired, ArrayList<Kernel> HiddenUnits) {
		MatrixObj W[] = new MatrixObj[this.NumberOfOutputs];
		//LeastSquare
		for (int o=0; o<this.NumberOfOutputs; o++) {
			this.Y[o] = new MatrixObj(this.Desired[o]);
		}
		//this.Y.display("Y");
		this.K = this.getK(HiddenUnits);
	
		try {
			for (int o=0; o<this.NumberOfOutputs; o++) {
				W[o] = (this.Y[o].Transport().multiply(K.Transport().multiply((K.multiply(K.Transport())).PseudoInverse())));
			}
			//W.display("W");
			double[] des = new double[this.NumberOfOutputs];
			for (int h=0; h<this.HiddenUnits.size(); h++) {
				Kernel target = this.HiddenUnits.get(h);
				for (int o=0; o<this.NumberOfOutputs; o++) {
					des[o] = W[o].getData(0, h);
				}
				target.setDesiredOutputs(des);
			}
		} catch (MatrixException e) {
			this.Log("learn() matrix exception is occured!!!");
			// TODO Auto-generated catch block
			this.Y[0].display("Y[0]");
			e.printStackTrace();
		}
	}
	
	MatrixObj getK(ArrayList<Kernel> kernels) {
		double data=0;
		this.K = new MatrixObj(kernels.size(), kernels.size());
		for (int l=0; l<kernels.size(); l++) {
			Kernel each_kernel_l = kernels.get(l);
			for (int m=l; m<kernels.size(); m++) {
				Kernel each_kernel_m = kernels.get(m);
				if (l!=m) {
					data = each_kernel_l.exp_output(each_kernel_m.getCenter());
				}else{
					data = 1D;//Diagonal element is 1.
				}
				K.set_data(l, m, data);
				K.set_data(m, l, data);
			}
 		}
		return K;
	}
	

	
	void Log(String log) {
		if (this.DEBUG) {
			System.out.println("KernelLeastSquareLRFU." + log);
		}
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
		this.initDesired();
	}
}
