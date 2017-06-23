package mgrnn;
import java.util.ArrayList;
import java.util.Iterator;

import matrix.MatrixException;

import MixtureOfDistributions.StudentT4kernel;
import VectorFunctions.VectorFunctions;



public class CellLinearDependencyAIW extends GRNNCell {
	//appended by k.yamauchi at 2010.6.30
	double alpha=1D; //Linear Dependency(分母側）
	double t_alpha[]; //Linear Dependency x t(分子側）
	private double NumberOfLearnedSamples=0D; //肩代わり分も含む
	int ID;
	StudentT4kernel ST4K;
	boolean isAllocateDistribution = false;//importance weight計算用の分布を準備したかどうか


	ArrayList<CellLinearDependencyAIW> Edge = new ArrayList<CellLinearDependencyAIW>();// the list of neighbors
	
	
	public CellLinearDependencyAIW(int NumberOfInput, int NumberOfOutput) {
		super(NumberOfInput, NumberOfOutput);
		this.t_alpha = new double[this.NumberOfOutput];
		this.NumberOfLearnedSamples =1D;
	}//constructor

	public double exp_output(double input[]) {
		double sum2=0D;
		for (int i=0; i<this.NumberOfInput; i++) {
			//sum += Math.pow(this.W[i]*(this.T[i]-input[i]),2D);
			sum2 += Math.pow((this.T[i]-input[i]),2D);		
		}
		sum2 /= 2*Math.pow(0.3*this.R,2D);
		this.exp_output_value = Math.exp(-sum2);
		//if (this.exp_output_value<0.1) this.exp_output_value = 0D;
		//this.actual_distance = Math.sqrt(sum);
		this.actual_distance = Math.sqrt(sum2);
		return this.exp_output_value;
	}
	
	//after calculating exp_output()
	public double responsiblity() {
		return this.alpha * this.exp_output_value;
	}
	
	public double[] output(double input[]) {
		double exp_out = this.exp_output(input);
		double[] outputs = new double[this.NumberOfInput];
		for (int i=0; i<this.NumberOfOutput; i++) {
			outputs[i] = this.t_alpha[i] * exp_out;
		}
		return outputs;
	}
	
	//initial learning
	public void learn(double input[], double output[], double alpha, double min_distance) throws MatrixException {
		for (int i=0; i<this.NumberOfInput; i++) {
			this.T[i] = input[i];
		}
		for (int j=0; j<this.NumberOfOutput; j++) {
			this.t_alpha[j] = this.C[j] = output[j];
		}
		this.alpha = alpha;
		this.NumberOfLearnedSamples = 1D;
		this.ST4K = new StudentT4kernel(this.getT(), min_distance/100D);//対応する分布も割り付ける
		this.ST4K.PrepareCorrespondingDistribution();		

		this.isAllocateDistribution = false; //これをセットしないと変なことになるはず
	}	
	
	//Incremental learning of new outputs based on the LinearDependency
	public void Increment(double t[], double LinearDependency, double NumberOfSamples) {
		this.NumberOfLearnedSamples += NumberOfSamples;
		this.alpha += LinearDependency;
		for (int o=0; o<this.NumberOfOutput; o++) {
			this.t_alpha[o] += t[o] * LinearDependency;
		}
	}
	//Incremental learning of new outputs based on the LinearDependency
	public void Increment(double LinearDependency, CellLinearDependencyAIW target_unit) {
		this.R += LinearDependency * VectorFunctions.getSqureNorm(VectorFunctions.diff(this.T, target_unit.getT())) * target_unit.getAlpha();
		this.R += LinearDependency * target_unit.getR();
		this.alpha += LinearDependency;
		for (int o=0; o<this.NumberOfOutput; o++) {
			this.t_alpha[o] += target_unit.getT()[o] * LinearDependency;
		}
	}
	
	//Decremental learning is also available
	public void Decrement(double t[], double LinearDependency) {
		this.alpha -= LinearDependency;
		for (int o=0; o<this.NumberOfOutput; o++) {
			this.t_alpha[o] -= t[o] * LinearDependency;
		}
	}


	/**
	 * @return the alpha
	 */
	public double getAlpha() {
		return alpha;
	}


	/**
	 * @param alpha the alpha to set
	 */
	public void setAlpha(double alpha) {
		this.alpha = alpha;
	}


	/**
	 * @return the t_alpha
	 */
	public double[] getT_alpha() {
		return t_alpha;
	}


	/**
	 * @param tAlpha the t_alpha to set
	 */
	public void setT_alpha(double[] tAlpha) {
		t_alpha = tAlpha;
	}
	
	
	public void setStandardDeviation(double r) {
		this.R = r;
	}
	
	//実質的な重みを返す(C[]を返すだけではダメで、ちゃんと以下の計算をしないと正しい出力に対応した重みが出ないことに注意）
	public double[] ActualWeight() {
		double actual_weight[] = new double[this.NumberOfOutput];
		for (int o=0; o<this.NumberOfOutput; o++) {
			actual_weight[o] = this.t_alpha[o]/this.alpha;
		}
		return actual_weight;
	}
	


	//Following functions are for manipulating Edges.
	// Answer whether or not the NeighborCandidate is included in the Edge.
	public boolean isConnected(CellLinearDependencyAIW NeighborCandidate) {
		CellLinearDependencyAIW target_unit;
		Iterator<CellLinearDependencyAIW> edge = this.Edge.iterator();
		while (edge.hasNext()) {
			target_unit = edge.next();
			if (target_unit.equals(NeighborCandidate)) {
				return true;
			}
		}
		return false;
	}

	//remove the specified edge, which corresponds to the Neighbor.
	public void removeEdge(CellLinearDependencyAIW Neighbor) {
		CellLinearDependencyAIW target_unit;
		for (int i=0; i<this.Edge.size(); i++) {
			target_unit = this.Edge.get(i);
			if (target_unit.equals(Neighbor)) {
				this.Edge.remove(i);
				break;
			}
		}
	}

	//remove all Edges
	public void removeAllEdges() {
		while (this.Edge.size()>0) {
			this.Edge.remove(this.Edge.size()-1);
		}
	}
	
	//importance weightを計算
	//対応する分布がまだ割付けられていない場合には割付けて、
	//近傍入力x[]を使って分散値を更新する。
	public double getQ_div_P(double x[], int UpperLimitOfWeight, int UpperLimitOfFreedom, double r) {
		double weight=0;
		try {
			weight = this.ST4K.getImportanceWeight(UpperLimitOfFreedom, x, r);
			this.ST4K.updateSigma(x);
			if (weight > (double)UpperLimitOfWeight) weight = (double)UpperLimitOfWeight;
			//else if (weight < 1D) weight = 1D;
			return weight;			
		}catch(MatrixException ex) {
			ex.printStackTrace();
		}
		return -1;
	}
	
	/**
	 * @return the iD
	 */
	public int getID() {
		return ID;
	}

	/**
	 * @param iD the iD to set
	 */
	public void setID(int iD) {
		ID = iD;
	}

	/**
	 * @return the numberOfLearnedSamples
	 */
	public double getNumberOfLearnedSamples() {
		return NumberOfLearnedSamples;
	}

	/**
	 * @param numberOfLearnedSamples the numberOfLearnedSamples to set
	 */
	public void IncrementNumberOfLearnedSamples(double AddNumberOfLearnedSamples) {
		NumberOfLearnedSamples += AddNumberOfLearnedSamples;
	}
	
	
}
