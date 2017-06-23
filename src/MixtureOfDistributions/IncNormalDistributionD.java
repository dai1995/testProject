package MixtureOfDistributions;

import matrix.MatrixException;
import matrix.MatrixObj;

public class IncNormalDistributionD extends NormalDistributionD implements IncDistribution {
	
	public IncNormalDistributionD(int NumberOfInputs) {
		super(NumberOfInputs);
		// TODO Auto-generated constructor stub
	}

	public IncNormalDistributionD(double[] Center, MatrixObj sigma) {
		super(Center, sigma);
		// TODO Auto-generated constructor stub
	}

	

	public double calculate_center() throws MatrixException {

		for (int i=0; i<this.dim_input; i++) {
			this.s_center[i] /= this.sum_weight;
		}

		//πだけはFixフラグが立っていたとしても更新する
		this.Pi = this.sum_weight/(double)this.buffer.getSize();

		return this.SetCenter(this.s_center);			

	}	
	
	
	
	public IncNormalDistributionD clone() {
		IncNormalDistributionD myclone=null;
		myclone = new IncNormalDistributionD(this.dim_input);
		myclone.SIGMA = this.SIGMA.clone();
		myclone.INVSIGMA = this.INVSIGMA.clone();
		myclone.U = this.U.clone();
		return myclone;			
	}			
}
