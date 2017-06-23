package KernelPerceptron;

import java.util.ArrayList;

import matrix.MatrixException;
import matrix.MatrixObj;

import org.w3c.dom.Node;


class dependency {
	double delta;
	MatrixObj alpha;
}

public class Projectron4Regression extends KernelPerceptron4Regression {
	private boolean DEBUG = false;
	private MatrixObj K;
	
	public Projectron4Regression(Node nd) {
		super(nd);
		// TODO Auto-generated constructor stub
	}
	
	public void init_parameters(Node nd) {
		this.pkp = new ParameterKernelPerceptron();
		this.pkp.getParameter(nd);
		this.HiddenUnits = new ArrayList<Kernel>();
		this.NumberOfInputs = this.pkp.getNumberOfInputs();//初期設定はこのように。後から変えても良い
		this.NumberOfOutputs = this.pkp.getNumberOfOutputs();
		this.UseHindgeLossFunction = this.pkp.isUseHindgeLossFunction();
		this.IsCumulativeErrorEstimation = this.pkp.isCumulativeErrorEstimation();
		if (this.UseHindgeLossFunction) {
			this.LossFunc = new HindgeLossFunction();
		}else{
			this.LossFunc = new SquaredErrorFunction();
		}
	}
	
	public void learn(double inputs[], double outputs[]) {
		double provisional_outputs[] = this.output(inputs);
		if (this.min_distance(inputs)<Double.MIN_VALUE) return;//If there is the same kernel, do nothing!.
		//double errors[] = VectorFunctions.VectorFunctions.diff(outputs, provisional_outputs);
		double error = this.LossFunc.getLoss(provisional_outputs, outputs);
		//System.out.println("KernelPerceptron4Regression.learn(): learning process is occured.");		
		if (error > this.getErrorThreshold()) {
			if (this.DEBUG) {
				System.out.println("Projectron4Regression.learn(): learning process is occured.");
			}
		
			final boolean UsePseudoInverse = false;
			dependency each_LD=null;
			try {
				each_LD = this.Calculate_LinearDependency(inputs, UsePseudoInverse);
			} catch (MatrixException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				return;
			}
				
			//Projection process
			if (each_LD.delta < this.pkp.getLDThreshold()) {
				this.Log("learn(): dependency is lower than the threshold");
				int index=0;
				for (Kernel cell : this.HiddenUnits) {
					//System.out.println("i=" + i + " size of cells : " + this.HiddenUnits.size());
					kernelLinearDependency cellLD = (kernelLinearDependency)cell;
					cellLD.Increment(outputs,each_LD.alpha.getData(index, 0),	1);
				}
			}else{
				if (this.getNumberOfHiddenUnits() < this.pkp.getUpperLimitOfHiddenUnits()) { 
					kernelLinearDependency new_unit = new kernelLinearDependency(this.getNumberOfInputs(), this.getNumberOfOutputs(), this.getDefaultSigma());
					new_unit.setCenter(inputs);
					if (this.UseHindgeLossFunction) {
						new_unit.setDesiredOutputs(outputs);
					}else{
						double errors[] = VectorFunctions.VectorFunctions.diff(outputs, provisional_outputs);
						new_unit.setDesiredOutputs(errors);// note: the desired output should be the error for regression.
					}
					this.addHiddenUnit(new_unit);
				}
			}
		}
	}
	
	
	
	//target_cellの冗長度δを算出する。行列式の逆行列を算出する時に一般逆行列を使用する時はUsePseduoInverse=trueとする。
	//普段は使用しなくて良い。(faluseにする)
	dependency Calculate_LinearDependency(double input[], boolean UsePseudoInverse)
			throws MatrixException {
		// double OutputWeightNorm =
		// VectorFunctions.getSqureNorm(target_cell.get_normalized_t_alpha());
		dependency dep = new dependency();
		if (this.HiddenUnits.size() == 0) {
			dep.delta = Double.MAX_VALUE;
			dep.alpha = null;
			return dep;
		}
		//this.Log("Calculate_LinearDependency(): # of hidden units =" + this.HiddenUnits.size());
		this.K = this.matrix_K(this.HiddenUnits);


		// k_sを作る
		MatrixObj k_s = new MatrixObj(this.HiddenUnits.size(), 1);
		
		int i = 0;
		for (Kernel cell: this.HiddenUnits) {
			
				k_s.set_data(i, 0, cell.exp_output(input));
				i++;
			
		}
		
		if (UsePseudoInverse) {
			dep.alpha = K.PseudoInverse().multiply(k_s);
		}else{
			dep.alpha = K.inverse().multiply(k_s);
		}
		 //dep.alpha.display("LimitedGRNN:Calculate_LinearDependency(): alpha");
		//K.display("K");
		// alpha.display("grnnLinearDependency:Calculate_LinearDependency()");
		// double delta = OutputWeightNorm * (1 -
		// k_s.Transport().multiply(alpha).getData(0, 0));
		dep.delta = (1 - k_s.Transport().multiply(dep.alpha).getData(0, 0));
		//System.out.println("LimitedGRNN: Calculate_LinearDependency():delta is " + dep.delta);
		return dep;
	}
	
	
	//冗長度を求める時に使用する行列Kを準備するメソッド
	MatrixObj matrix_K(ArrayList<Kernel> hidden_units) {
		int size;
		if (hidden_units == null) {
			size = 0;
		} else {
			size = hidden_units.size();
		}
		MatrixObj K = null;
		
		double each_output;

		K = new MatrixObj(size, size);
		int index_i = 0, index_j = 0;
		for (Kernel h_cell_i : hidden_units) {
			
			
				index_j = 0;
				for (Kernel h_cell_j: hidden_units) {
					
						each_output = h_cell_j.exp_output(h_cell_i.getCenter());
						K.set_data(index_i, index_j, each_output);
						K.set_data(index_j, index_i, each_output);
						index_j++;
					
				}
				index_i++;
			
		}
		return K;
	}
	
	void Log(String log) {
		System.out.println("Projectron4Regression." + log);
	}

}
