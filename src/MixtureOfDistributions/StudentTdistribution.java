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
 * 
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StudentTdistribution implements Distribution {
	MatrixCalc mc;
	MatrixObj SIGMA;
	MatrixObj INV_SIGMA;
	MatrixObj U;
	double UpperLimitOfFreedom=50;
	int dim_x;
	double N;
	double Gain;
	double dotproduct;
	MatrixObj eigenVector[];
	double eigenValue[];
	boolean is_SMD;
	/**
	 * @throws MatrixException 
	 * 
	 */
	public StudentTdistribution(MatrixObj center, MatrixObj sigma, double N, double upperLimitOfFreedom2, boolean SMD, boolean CorrectTDistribution) throws MatrixException {
		this.UpperLimitOfFreedom = upperLimitOfFreedom2;
		this.N = N-1;//自由度
		
		this.is_SMD = SMD;
		
		if (this.N <= 2) this.N = 2.1;
		this.dim_x = center.getL();
		
		this.eigenVector = new MatrixObj[this.dim_x];
		this.eigenValue = new double[this.dim_x];
		
		//System.out.println("Student t dim(x)=" + this.dim_x+ " sigma.det=" + sigma.det() + " N=" + N);
		//System.exit(1);
		this.SIGMA = sigma;
		if (CorrectTDistribution) {
			this.SIGMA = this.SIGMA.multiply((double)(this.N-2)/(double)this.N);//この部分が抜けていたらしい2010/1/5
		}
		this.INV_SIGMA = this.SIGMA.inverse();
		this.U = center;
		this.Gain = MyMath.gamma((double)(N+dim_x)/2D)
	     / ( Math.pow((double)N*Math.PI, (double)dim_x/2D) * 
	     		MyMath.gamma((double)N/2D));
				//* Math.sqrt(this.SIGMA.det())); 
		/* this.Gain = MyMath.gamma((double)(N+dim_x)/2D)
	     / ( Math.pow((double)(N), (double)dim_x/2D) * 
	     		MyMath.gamma((double)(N)/2D));*/

	}

	/* (non-Javadoc)
	 * @see CovariateShift.Distribution#P(double[])
	 */
	public double P(double[] x) {
		// TODO Auto-generated method stub
		//System.out.println("Student t freedom is " + this.N + this.dim_x);
		if ((this.N+this.dim_x) > this.UpperLimitOfFreedom) {
			return -1D;
		}else{
			if (this.is_SMD) {
				this.dotproduct = this.getSimplifiedMaharanobisDistance(x);
			}else{
				this.dotproduct = this.getMaharanobisDistance(x);
			}
		
			return this.Gain * Math.pow(this.dotproduct, -(double)(this.N+this.dim_x)/2) /  Math.sqrt(this.SIGMA.det()); 
		}
	}
	
	public double P() {
		//System.out.println("Student t freedom is " + this.N + this.dim_x);
		if ((this.N+this.dim_x) > this.UpperLimitOfFreedom) {
			return -1D;
		}else{
			return this.Gain * Math.pow(this.dotproduct, -(double)(this.N+this.dim_x)/2) /  Math.sqrt(this.SIGMA.det());
		}
	}

	public double OmitDetSigmaP(double x[]) {
		if ((this.N+this.dim_x) > this.UpperLimitOfFreedom) {
			return -1D;
		}else{
			if (this.is_SMD) {
				this.dotproduct = this.getSimplifiedMaharanobisDistance(x);
			}else{
				this.dotproduct = this.getMaharanobisDistance(x);
			}
			//この部分の計算にも誤りがあったようだ2010/1/5
			this.Gain = Math.pow((this.N-2)/this.N, -this.dim_x/2) * MyMath.gamma((double)(N+dim_x)/2D)
		     / ( Math.pow((double)N*Math.PI, (double)dim_x/2D) * 
		     		MyMath.gamma((double)N/2D));		
			return this.Gain * Math.pow(this.dotproduct, -(double)(this.N+this.dim_x)/2);
		}		
	}

	public void MP(MinNoOutputsVariableSizedBuffer buffer) {
		//なにもしない。
	}

	
	public double getMaharanobisDistance(double x[]) {
		MatrixObj X = new MatrixObj(this.dim_x, x, 1);
		X.display("Student t X");
		X = X.Diff(this.U);
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
	
	public double getSimplifiedMaharanobisDistance(double[] x) {
		int M=0;
		double s[], E1, E2, sum_eigenvalue, S=0, lambda=0, SMD=0;
		
		MatrixObj X = new MatrixObj(x.length, x, 1); //縦ベクトルを作る。
		X=mc.DiffMatrix_M(X,U);
		
		if (this.dim_x>10) {
			M = (int)((double)this.dim_x/2);
			s = new double[M];
			sum_eigenvalue = 0;
			for (int m=0; m<M; m++) {
				s[m] = Math.pow(this.eigenVector[m].multiply(X).getData(0,0),2D);
				sum_eigenvalue += this.eigenValue[m];  
			}
			E1 = E2 = 0;
			for (int m=0; m<M; m++) {
				E1 += s[m]/this.eigenValue[m];
				E2 += s[m];
			}
			try {
				S = this.SIGMA.getTrace();
			}catch(MatrixException me) {
				me.printStackTrace();
				System.exit(1);
			}
			lambda = (S-sum_eigenvalue)/(this.dim_x - M);
			SMD = E1 + (this.getDistance(x) - E2)/lambda;
			this.dotproduct = SMD;
			return SMD;
		}else{
			MatrixObj Dotproduct = mc.MultiplyMatrix_M(mc.MultiplyMatrix_M(X.Transport(), this.INV_SIGMA),X);
			this.dotproduct = Dotproduct.getData(0,0);
		}
		return 0;
	}
		
	/* (non-Javadoc)
	 * @see CovariateShift.Distribution#SetCenter(double[])
	 */
	public double SetCenter(double[] center) {
		// TODO Auto-generated method stub
		this.U = new MatrixObj(dim_x, center, 1);//縦ベクトルで作る
		return 0;
	}
	public void setSigma(MatrixObj sigma) throws MatrixException {
		this.SIGMA = sigma;
		this.INV_SIGMA = this.SIGMA.inverse();
		
		// re-calculate Gain
		this.Gain = MyMath.gamma((double)(N+dim_x)/2D)
	     / ( Math.pow((double)N*Math.PI, (double)dim_x/2D) * 
	     		MyMath.gamma((double)N/2D))
				* Math.sqrt(this.SIGMA.det()); 		
	}
	public void setSigma(DiagonalMatrixObj sigma, double N) throws MatrixException {
		for (int i=0; i<this.dim_x; i++) {
			this.SIGMA.set_data(i,i, sigma.getData(i,i));
		}
		this.SIGMA = this.SIGMA.multiply((N-2)/N);//この部分が抜けていたらしい2010.1.5
		
		this.INV_SIGMA = this.SIGMA.inverse();
		
		// re-calculate Gain
		this.Gain = MyMath.gamma((double)(N+dim_x)/2D)
	     / ( Math.pow((double)N*Math.PI, (double)dim_x/2D) * 
	     		MyMath.gamma((double)N/2D))
				* Math.sqrt(this.SIGMA.det()); 				
	}

	/* (non-Javadoc)
	 * @see CovariateShift.Distribution#GetCenter()
	 */
	public MatrixObj GetCenter() {
		return this.U;
	}
	
	public double getDistance(double[] x) {
		MatrixObj X = new MatrixObj(x.length, x, 1); //縦ベクトルを作る。
		X=mc.DiffMatrix_M(X,U);
		MatrixObj dotproduct = X.Transport().multiply(X);
		return dotproduct.getData(0,0);
	}

	/* (non-Javadoc)
	 * @see CovariateShift.Distribution#getSigma()
	 */
	public MatrixObj getSigma() {
		return this.SIGMA;
	}
	public DiagonalMatrixObj getSigmaD() {
		System.err.println("StudentTdistribution: DiagonalMatrixObj getSigmaD(): Note: This method is not implemented!");												
		return null;
	}
	
	public double getEta(MinNoOutputsVariableSizedBuffer buffer) {
		System.err.println("StudentTdistribution: double getEta(): Note: This method is not implemented!");										
		return 0;
	}

	

	@Override
	public void push_center(double[] inputs, double weight) {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistribution:void push_center(): Note: This method is not implemented!");								
	}
	
	public void init_parameters() {
		System.err.println("StudentTdistribution:void init_parameters(): Note: This method is not implemented!");
	}

	@Override
	public double SetSigma(MatrixObj sigma) throws MatrixException {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistribution:double SetSigma(): Note: This method is not implemented!");		
		return 0;
	}

	@Override
	public double calculate_variance_covariance_matrix(double min_sigma)
			throws MatrixException {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistribution:double calculate_variance_covariance_matrix(): Note: This method is not implemented!");				
		return 0;
	}

	@Override
	public void reset_parameters() {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistribution:void reset_parameters(): Note: This method is not implemented!");				
	}

	@Override
	public double calculate_center() throws MatrixException {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistribution:double calculate_center(): Note: This method is not implemented!");						
		return 0;
	}

	
	@Override
	public void resetNumberOfMember() {
		// TODO Auto-generated method stub
		this.N = 0;
	}

	@Override
	public double getTvalue(double[] input) {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistribution:double getTvalue(): Note: This method is not implemented!");		
		return 0;
	}

	@Override
	public double q_div_p(double[] input) {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistribution:double q_div_p(): Note: This method is not implemented!");				
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
		System.err.println("StudentTdistribution:double getExp(): Note: This method is not implemented!");				
		return 0;
	}

	@Override
	public String getPlot() {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistribution:String getPlot(): Note: This method is not implemented!");
		return null;
	}

	@Override
	public double getPAI() {
		// TODO Auto-generated method stub
		System.err.println("StudentTdistribution:String getPAI(): Note: This method is not implemented!");
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
