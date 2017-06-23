package MixtureOfDistributions;

import matrix.MatrixException;
import matrix.MatrixObj;

public class IncNormalDistribution extends NormalDistribution implements IncDistribution {

	private boolean IsFix = false;
	
	public IncNormalDistribution(int NumberOfInputs, boolean SMD) {
		super(NumberOfInputs, SMD);
		// TODO Auto-generated constructor stub
	}

	public IncNormalDistribution(double[] Center, MatrixObj sigma, boolean SMD) throws MatrixException {
		super(Center, sigma, SMD);
		// TODO Auto-generated constructor stub
	}

	public double calculate_center() throws MatrixException {
		if (!this.IsFix) {
			for (int i=0; i<this.dim_input; i++) {
				this.s_center[i] /= this.sum_weight;
			}
		}
		//πだけはFixフラグが立っていたとしても更新する
		this.Pi = this.sum_weight/(double)this.buffer.getSize();
		if (!this.IsFix) {
			return this.SetCenter(this.s_center);			
		}else{
			return 0D;
		}
	}	

	/**
	 * @return the isFix
	 */
	public boolean isFix() {
		return IsFix;
	}

	/**
	 * @param isFix the isFix to set
	 */
	public void setFix(boolean isFix) {
		IsFix = isFix;
	}
	public void setPi(double pi) {
		this.Pi = pi;
	}

	public IncNormalDistribution clone() {
		IncNormalDistribution myclone=null;
		myclone = new IncNormalDistribution(this.dim_input,this.is_SMD);
		myclone.SIGMA = this.SIGMA.clone();
		myclone.INVSIGMA = this.INVSIGMA.clone();
		myclone.U = this.U.clone();
		return myclone;			
	}

	/*@Override
	public void incrementClassMember() {
		// TODO Auto-generated method stub
		
	}*/		
}
