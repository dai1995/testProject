/*
 * Created on 2008/08/21
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package MixtureOfDistributions;
import matrix.*;
import FIFO.*;

/**
 * @author yamauchi
 *
 * 注意：ここではq(x)/p(x)を計算するため、|Σ|の項は計算しない。そのまま使うと間違いになるので注意せよ
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StudentTdistributionD implements Distribution {
	MatrixCalc mc;
	DiagonalMatrixObj SIGMA;
	DiagonalMatrixObj INV_SIGMA;
	MatrixObj U;
	double UpperLimitOfFreedom=50;	
	int dim_x;
	double N;
	double Gain;
	double dotproduct;
	boolean CorrectTDistribution=true;
	/**
	 * 
	 */
	public StudentTdistributionD(MatrixObj center, DiagonalMatrixObj sigma, double N, double UpperLimitOfFreedom, boolean CorrectTDistribution) {
		this.N = (double)N-1D;//自由度
		if (this.N <= 2D) this.N = 2.1D;
		this.UpperLimitOfFreedom = UpperLimitOfFreedom;
		this.dim_x = center.getL();
		this.CorrectTDistribution = CorrectTDistribution;//T分布のスケールマトリックスを使うか、それとも分散共分散行列を使用するのか。（スケールマトリックスが正しい）
		//System.out.println("Student t dim(x)=" + this.dim_x+ " sigma.det=" + sigma.det() + " N=" + N);
		//System.exit(1);
		this.SIGMA = sigma;
		//この部分間違っていたらしい2010/1/5 !!
		if (CorrectTDistribution) {
			this.SIGMA = this.SIGMA.MultiplyMatrix((double)(this.N-2D)/(double)this.N);
		}
		
		this.INV_SIGMA = this.SIGMA.inverse_d();
		this.U = center;
		/*this.Gain = MyMath.gamma((double)(N+dim_x)/2D)
	     / ( Math.pow((double)N*Math.PI, (double)dim_x/2D) * 
	     		MyMath.gamma((double)N/2D) * Math.sqrt(this.SIGMA.det()));*/
		/*System.out.println("N=" + N);
		System.out.println("gamma((N+p)/2)=" + MyMath.gamma((double)(N+2)/2D));
		System.out.println("gamma((N)/2)=" + MyMath.gamma((double)(N)/2D));		
		System.out.println("g=" + Math.pow((2D/(double)N),(double)2/2D) * MyMath.gamma((double)(N+2)/2D) /MyMath.gamma((double)(N)/2D));*/ 
		/*this.Gain = MyMath.gamma((double)(N+dim_x)/2D)
	     / ( Math.pow((double)N, (double)dim_x/2D) * 
	     		MyMath.gamma((double)N/2D));*/
		//この部分の計算にも誤りがあったようだ。2010/1/5
		if (CorrectTDistribution) {
			this.Gain = Math.pow((double)(this.N-2)/(double)this.N, -(double)this.dim_x/2D) * MyMath.gamma((double)(N+dim_x)/2D)
				/ ( Math.pow((double)N*Math.PI, (double)dim_x/2D) * 
						MyMath.gamma((double)N/2D));
		}else{
			this.Gain = MyMath.gamma((double)(N+dim_x)/2D)
		     / ( Math.pow((double)N, (double)dim_x/2D) * 
		     		MyMath.gamma((double)N/2D));			
		}
				//* Math.sqrt(this.SIGMA.det())); 		
	}

	/* (non-Javadoc)
	 * @see CovariateShift.Distribution#P(double[])
	 */
	public double P(double[] x) {
		// TODO Auto-generated method stub
		if ((this.N+this.dim_x) > this.UpperLimitOfFreedom) {
			return -1D;
		}else{
			this.dotproduct = this.getMaharanobisDistance(x);
			return this.Gain * Math.pow(this.dotproduct, -(double)(this.N+this.dim_x)/2) / Math.sqrt(this.SIGMA.det()); 		
		}
	}
	
	public double P() {
		if ((this.N+this.dim_x) > this.UpperLimitOfFreedom) {
			return -1D;
		}else{
			return this.Gain * Math.pow(this.dotproduct, -(double)(this.N+this.dim_x)/(double)2)  
			/ Math.sqrt(this.SIGMA.det());
		}
	}
	
	public double OmitDetSigmaP(double x[]) {
		if ((this.N+this.dim_x) > this.UpperLimitOfFreedom) {
			return -1D;
		}else{
			this.dotproduct = this.getMaharanobisDistance(x);
			if (this.CorrectTDistribution) {
				this.Gain = Math.pow((double)(this.N-2)/(double)this.N, -(double)this.dim_x/2D) 
				    * MyMath.gamma((double)(N+dim_x)/2D)
					/ ( Math.pow((double)N*Math.PI, (double)dim_x/2D) * 
							MyMath.gamma((double)N/2D));
			}else{			
				this.Gain = MyMath.gamma((double)(N+dim_x)/2D)
					/ ( Math.pow((double)N*Math.PI, (double)dim_x/2D) * 
							MyMath.gamma((double)N/2D));
			}
			//System.out.println("StudentT D: OmitDetSigmaP():" + this.dotproduct + " p= " + (double)(this.N+this.dim_x)/2 + " Gain is " + this.Gain + " MyMath.gamma((double)N/2D)=" + MyMath.gamma((double)N/2D) + " MyMath.gamma((double)(N+dim_x)/2D)=" + MyMath.gamma((double)(N+dim_x)/2D) );
			return this.Gain * Math.pow(this.dotproduct, -(double)(this.N+this.dim_x)/2);
		}		
	}
	
	public void MP(MinNoOutputsVariableSizedBuffer buffer) {
		//なにもしない。
	}

	
	public double getMaharanobisDistance(double x[]) {
		MatrixObj X = new MatrixObj(this.dim_x, 1, x);
		//X.display("Student t X");
		X = X.Diff(this.U);
		//U.display("Mean");
		//X.display("Student t X-U");
		//this.INV_SIGMA.display("INVSigma");
		MatrixObj sdash = X.Transport().multiply(this.INV_SIGMA);
		//sdash.display("sdash");
		//X.display("X");
		MatrixObj S = sdash.multiply(X);// (X-U)^T inv_sigma (X-U)
		//System.out.println("Student t gain="+this.Gain);
		double sum = 1D + S.getData(0,0)/(double)(this.N);
		//System.out.println("sum = " + sum + " nu+dim(x)="+(double)(this.N+this.dim_x)/2);
		this.dotproduct = sum;
		return sum;
	}
	public double getMaharanobisDistance(MatrixObj x) {
		MatrixObj X;
		//X.display("Student t X");
		X = x.Diff(this.U);
		//U.display("Mean");
		//X.display("Student t X-U");
		MatrixObj sdash = mc.MultiplyMatrix_M(X.Transport(), this.INV_SIGMA);
		//sdash.display("sdash");
		//X.display("X");
		MatrixObj S = mc.MultiplyMatrix_M(sdash, X);// (X-U)^T inv_sigma (X-U)
		//System.out.println("Student t gain="+this.Gain);
		double sum = 1D + S.getData(0,0)/(double)(this.N);
		//System.out.println("sum = " + sum + " nu+dim(x)="+(double)(this.N+this.dim_x)/2);
		this.dotproduct = sum;
		return sum;
	}
	
	public double getDistance(double[] x) {
		MatrixObj X = new MatrixObj(x.length, x, 1); //縦ベクトルを作る。
		X=mc.DiffMatrix_M(X,U);
		MatrixObj dotproduct = X.Transport().multiply(X);
		return dotproduct.getData(0,0);
	}
	
	/* (non-Javadoc)
	 * @see CovariateShift.Distribution#SetCenter(double[])
	 */
	public double SetCenter(double[] center) {
		// TODO Auto-generated method stub
		this.U = new MatrixObj(dim_x, center, 1);//縦ベクトルで作る
		return 0;
	}
	
	
		
	public void setSigma(DiagonalMatrixObj sigma, int N) {
		for (int i=0; i<this.dim_x; i++) {
			this.SIGMA.set_data(i,i, sigma.getData(i,i));
		}
		if (this.CorrectTDistribution) {
			this.SIGMA = this.SIGMA.MultiplyMatrix((double)(N-2)/(double)N);//この部分間違っていたらしい2010/1/5
		}
		this.INV_SIGMA = this.SIGMA.inverse_d();
		// re-calculate Gain
		if (this.CorrectTDistribution) {
			this.Gain = Math.pow((double)(this.N-2)/(double)this.N, -(double)this.dim_x/2D) * MyMath.gamma((double)(N+dim_x)/2D)
				/ ( Math.pow((double)N*Math.PI, (double)dim_x/2D) * 
						MyMath.gamma((double)N/2D))
						* Math.sqrt(this.SIGMA.det());
		}else{		
			this.Gain = MyMath.gamma((double)(N+dim_x)/2D)
				/ ( Math.pow((double)N*Math.PI, (double)dim_x/2D) * 
						MyMath.gamma((double)N/2D))
						* Math.sqrt(this.SIGMA.det());
		}
	}	


	public MatrixObj GetCenter() {
		return this.U;
	}

	@Override
	public double calculate_center() {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistributionD:calculate_center(): Note: This method is not implemented!");				
		return 0;
	}

	@Override
	public void push_center(double[] inputs, double weight) {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistributionD:push_center(double[] inputs, double weight): Note: This method is not implemented!");		
		
	}

	public void init_parameters() {
		System.err.println("StudentTdistributionD:init_parameters(): Note: This method is not implemented!");
	}

	@Override
	public double SetSigma(MatrixObj sigma) throws MatrixException {
		System.err.println("StudentTdistributionD:SetSigma(MatrixObj): Note: This method is not implemented!");		
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double calculate_variance_covariance_matrix(double min_sigma)
			throws MatrixException {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public void resetNumberOfMember() {
		// TODO Auto-generated method stub
		this.N = 0;
	}

	@Override
	public void reset_parameters() {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistributionD:reset_parameters(): Note: This method is not implemented!");		
	}

	@Override
	public MatrixObj getSigma() {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistributionD:MatrixObj getSigma(): Note: This method is not implemented!");				
		return null;
	}

	@Override
	public double getTvalue(double[] input) {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistributionD:double getTvalue(): Note: This method is not implemented!");						
		return 0;
	}

	@Override
	public double q_div_p(double[] input) {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistributionD:double q_div_p(): Note: This method is not implemented!");						
		return 0;
	}

	@Override
	public void AddFreedomeness(double free) {
		// TODO Auto-generated method stub
		this.N += free;
	}

	@Override
	public void setFreedomeness(double free) {
		// TODO Auto-generated method stub
		this.N = free;
	}

	@Override
	public double getExp(double[] x) {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistributionD:double getExp(): Note: This method is not implemented!");								
		return 0;
	}

	@Override
	public String getPlot() {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistributionD:String getPlot(): Note: This method is not implemented!");
		return null;
	}

	@Override
	public double getPAI() {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistributionD:String getPAI(): Note: This method is not implemented!");
		return 0;
	}	
	
	MinMoutWeightVariableSizedBuffer data_buffer;
	//対応するデータを貯める
	public void push_corresponding_data(double x[], double outputs[]) {
		if (this.data_buffer==null) {
			this.data_buffer = new MinMoutWeightVariableSizedBuffer(x.length, outputs.length);
		}else if (this.data_buffer.getSize()==0) { 
			this.data_buffer = new MinMoutWeightVariableSizedBuffer(x.length, outputs.length);
		}
		this.data_buffer.push_data(x, outputs);
	}/* push_corresponding_data() */	
	
	public MinMoutWeightVariableSizedBuffer get_corresponding_data() {
		return this.data_buffer;
	}/* get_corresponding_data() */	
}
