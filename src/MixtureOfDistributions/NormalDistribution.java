/*
 * Created on 2008/07/20
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package MixtureOfDistributions;

/**
 * @author yamauchi
 *
  * Window - Preferences - Java - Code Style - Code Templates
 */

import FIFO.MinMoutWeightVariableSizedBuffer;
import FIFO.MinNoOutputsVariableSizedBuffer;
import matrix.*;

public class NormalDistribution implements Distribution {
	
	MatrixCalc mc;
	MatrixObj SIGMA;
	MatrixObj INVSIGMA;
	MatrixObj U;
	StudentTdistribution ST=null; //StudentTdistribution whose center and variance-covariance matrix are the same as those of used in this class
	int dim_input;
	double NumberOfMember=0;
	double Gain, PAI;
	boolean isFirstTime = false;
	boolean DEBUG=false;
	double dotProduct;
	double Pi;
	
	MatrixObj eigenVector[];
	double eigenValue[];
	boolean is_SMD=false;//use simplified maharabinos distance
	
	// for calculating center
	double s_center[];
	double sum_weight=0;	
	
	// data buffer
	MinNoOutputsVariableSizedBuffer buffer;
	
	// このクラスに属するデータをためるため
	MinMoutWeightVariableSizedBuffer data_buffer;
	/**
	 * 
	 */
	public NormalDistribution(int NumberOfInputs, boolean SMD) {
		this.mc = new MatrixCalc();
		this.dim_input = NumberOfInputs;
		this.eigenVector = new MatrixObj[NumberOfInputs];
		this.eigenValue = new double[NumberOfInputs];
		this.Gain = 1; //とりあへず
		this.is_SMD = SMD;
		this.Pi = 1;
		this.buffer = new MinNoOutputsVariableSizedBuffer(NumberOfInputs);

		this.s_center = new double[this.dim_input];
	}
	
	public NormalDistribution(double Center[], MatrixObj sigma, boolean SMD) throws MatrixException {
		this.is_SMD = SMD;
		this.mc = new MatrixCalc();
		this.dim_input = Center.length;
		try {
			this.SetCenter(Center);
		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.eigenVector = new MatrixObj[this.dim_input];
		this.eigenValue = new double[this.dim_input];		
		this.SIGMA = new MatrixObj(this.dim_input, this.dim_input);
		this.INVSIGMA = new MatrixObj(this.dim_input, this.dim_input);
		try {
			this.SetSigma(sigma);
		} catch (MatrixException e) {

			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.SIGMA = this.INVSIGMA.inverse();

		
		//this.invsigma = this.sigma.inverse_d();

		//this.SIGMA.display("SIGMA");

		this.Gain = 1; //とりあへず
		// TODO Auto-generated constructor stub
		this.Pi = 1;
		this.buffer = new MinNoOutputsVariableSizedBuffer(Center.length);
		this.s_center = new double[this.dim_input];
	}

	/* (non-Javadoc)
	 * @see CovariateShift.Distribution#P(double[])
	 */
	public double P(double[] x) {
		double det = this.SIGMA.det();

		// TODO Auto-generated method stub
		if (this.is_SMD) {
			this.dotProduct = this.getSimplifiedMaharanobisDistance(x);
		}else{
			this.dotProduct = getMaharanobisDistance(x);
		}

		//System.out.println("NormalDistribution:P(x): MaharanobisDist = " + this.dotProduct);
		double result = Math.exp(-this.dotProduct/2);
		this.Gain = Math.pow(2*Math.PI, -(double)this.dim_input/2) * Math.pow(det, -0.5);
		this.PAI = this.Gain * this.Pi;
		//System.out.println("NormalDistribution:P(x): Gain = " + this.Gain);
		//System.out.println("NormalDistribution:P(x): Pi = " + this.Pi);		
		result *= this.PAI;

		return result;
	}
	
	public double getExp(double[] x) {
		this.dotProduct = getMaharanobisDistance(x);
		double result = Math.exp(-this.dotProduct/2);
		return result;
	}	
	
	public double OmitDetSigmaP(double[] x) {
		if (this.is_SMD) {
			this.dotProduct = this.getSimplifiedMaharanobisDistance(x);
		}else{
			this.dotProduct = getMaharanobisDistance(x);
		}
		double result = Math.exp(-this.dotProduct/2);
		this.Gain = Math.pow(2*Math.PI, -(double)this.dim_input/2); //Note: det(Σ) is not calculated. 
		//System.out.println("NormalDistribution:OmitDetSigmaP(x): Gain = " + this.Gain);
		result *= this.Gain;
		return result;		
	}
	
	public double P() {
		double result = Math.exp(-this.dotProduct/2);
		result *= this.Gain * this.Pi;
		return result;
	}
	
	public double q_div_p(double[] x) {
		double q = this.ST.OmitDetSigmaP(x);
		double p = this.OmitDetSigmaP(x);
		//double qp = q /p;
		//System.out.println("qp = " + qp);
		return q/p;
	}
	
	public double getMaharanobisDistance(double[] x) {
		MatrixObj X = new MatrixObj(x.length, x, 1); //縦ベクトルを作る。
		X=X.Diff(this.U);
		MatrixObj Dotproduct =((X.Transport()).multiply(this.INVSIGMA)).multiply(X); 
		//MatrixObj Dotproduct = mc.MultiplyMatrix_M(mc.MultiplyMatrix_M(X.Transport(), this.INVSIGMA),X);
		this.dotProduct = Dotproduct.getData(0,0);
		
		return Dotproduct.getData(0,0);
	}
	
	public double getSimplifiedMaharanobisDistance(double[] x) {
		int M=0;
		double s[], E1, E2, sum_eigenvalue, S=0, lambda=0, SMD=0;
		
		MatrixObj X = new MatrixObj(x.length, x, 1); //縦ベクトルを作る。
		X=mc.DiffMatrix_M(X,U);
		
		if (this.dim_input>4) {
			M = (int)((double)this.dim_input/2);
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
			lambda = (S-sum_eigenvalue)/(this.dim_input - M);
			SMD = E1 + (this.getDistance(x) - E2)/lambda;
			this.dotProduct = SMD;
		}else{
			MatrixObj Dotproduct = mc.MultiplyMatrix_M(mc.MultiplyMatrix_M(X.Transport(), this.INVSIGMA),X);
			this.dotProduct = Dotproduct.getData(0,0);
		}
		return this.dotProduct;
	}
	
	//ユークリッドノルムの2乗を与える
	public double getDistance(double[] x) {
		MatrixObj X = new MatrixObj(x.length, x, 1); //縦ベクトルを作る。
		X=mc.DiffMatrix_M(X,this.U);
		MatrixObj dotproduct = X.Transport().multiply(X);
		return dotproduct.getData(0,0);
	}

	
	
	/*public void MP(MinNoOutputsVariableSizedBuffer buffer) {
		this.M_step(buffer);//1回だけでOK.なぜならモデル数は１個だから
	}*/
	
	/*void M_step(MinNoOutputsVariableSizedBuffer buffer) {

		double[] Center = new double[this.dim_input];
		if (buffer.getSize()==0) {
			System.out.println("NormalDistribution.M_step(): warning! buffer size is 0!");
			return;
		}
		//reset center
		for (int j=0; j<this.dim_input; j++) {
			Center[j]=0D;
		}
		//for getting center
		for (int i=0; i<buffer.getSize(); i++) {
			for (int j=0; j<this.dim_input; j++) {
				Center[j] += buffer.getInput(i)[j];
			}
		}
		for (int j=0; j<this.dim_input; j++) {
			Center[j] /= (double)buffer.getSize();
		}
		try {
			this.SetCenter(Center);
		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}//ここでUが作られる
		//this.U.display("E(X)");
		this.SIGMA = new MatrixObj(this.dim_input, this.dim_input);
		//for getting sigma
		for (int i=0; i<buffer.getSize(); i++) {
			MatrixObj X = new MatrixObj(this.dim_input, buffer.getInput(i), 1); //縦ベクトルを作る。
			MatrixObj dash_X = X.multiply(X.Transport());//X XT
			this.SIGMA = this.SIGMA.Add(dash_X); //SIGMA<-SIGMA+XXT
		}
		this.SIGMA=mc.MultiplyMatrix_M(this.SIGMA, 1/(double)buffer.getSize());//E[XXT]
		this.SIGMA = this.SIGMA.Diff(this.U.multiply(this.U.Transport()));//E[XXT]-UUT
		/*this.CheckShigma(this.SIGMA, 0.0001);//ゼロとなる要素があれば対角要素に小さな値をセットする*/
		
		//固有値.固有ベクトルを得る*/
		/*Jacobi jac = new Jacobi(this.SIGMA.getL(), this.SIGMA.getMatrix());
		try {
			jac.jacobi();
			for (int i=0; i<this.dim_input; i++) {
				this.eigenValue[i] = jac.get_DescentOrderEigenValue(i);
				this.eigenVector[i] = new MatrixObj(this.dim_input, jac.get_DescentOrderEigenVector(i), 1);
			}
		}catch(MatrixException me) {
			me.printStackTrace();
		}
		
		if (this.DEBUG) this.SIGMA.display("SIGMA");
		if (this.DEBUG) System.out.println("det(SIGMA)=" + this.SIGMA.det());
		this.INVSIGMA = this.SIGMA.inverse();//Σ^{-1}を計算
		this.Gain = Math.pow((2*Math.PI), (double)this.dim_input/(double)2);
		//this.Gain = Math.pow(2, (double)this.dim_input/(double)2);
		//this.Gain *= Math.sqrt(this.SIGMA.det());
		this.Gain = 1D/this.Gain;
		if (this.DEBUG) System.out.println("Gain is " + this.Gain);
	}*/
	
	void CheckShigma(MatrixObj sigma, double small_value) {
		for (int i=0; i<sigma.getL(); i++) {
			if (sigma.getData(i,i)<small_value) {
				sigma.set_data(i,i,small_value);
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see CovariateShift.Distribution#SetCenter(double[])
	 */
	public double SetCenter(double[] center) throws MatrixException {
		// TODO Auto-generated method stub
		double difference=10;
		MatrixObj new_center = new MatrixObj(center.length, center, 1); // u
		if (this.U != null) {
			//this.U.display("NormalDistribution:SetCenter() before modify U");
			MatrixObj Diff = this.U.Diff(new_center);
			//Diff.display("Diff");
			difference = Diff.getNorm();
		}else{
			difference = 10;
		}
		this.U = new_center;
		//this.U.display("NormalDistribution:SetCenter(): U ");
		return difference;
	}

	/* (non-Javadoc)
	 * @see CovariateShift.Distribution#GetCenter()
	 */
	public MatrixObj GetCenter() {
		// TODO Auto-generated method stub
		return this.U;
	}

	/**
	 * @return Returns the sigma.
	 */
	public MatrixObj getSigma() {
		return this.SIGMA;
	}
	
	
	/**
	 * @param sigma The sigma to set.
	 * @throws MatrixException 
	 */
	public double SetSigma(MatrixObj sigma) throws MatrixException {
		MatrixObj Diff = this.INVSIGMA.Diff(sigma);
		this.INVSIGMA = sigma;
		//System.out.println(" matrix = " + sigma.getMatrix());
		//sigma.display("sigma");
		//固有値.固有ベクトルを得る for simplified maharanobis distance
		/*Jacobi jac = new Jacobi(this.SIGMA.getL(), this.SIGMA.getMatrix());
		try {
			jac.jacobi();
			for (int i=0; i<this.dim_input; i++) {
				this.eigenValue[i] = jac.get_DescentOrderEigenValue(i);
				this.eigenVector[i] = new MatrixObj(this.dim_input, jac.get_DescentOrderEigenVector(i), 1);
			}
			System.out.println("SetSigma");
			this.eigenVector[0].display("SetSigma eigenvector0");
		}catch(MatrixException me) {
			me.printStackTrace();
		}	*/					
		
		//this.INVSIGMA=this.SIGMA.inverse();
		return Diff.getNorm();
	}
	
	
	public void reset_parameters() {
		for (int i=0; i<this.dim_input; i++) {
			this.s_center[i] = 0;
		}
		this.sum_weight =0D;
		this.buffer.dispose();
	}
	
	public void push_center(double inputs[], double weight) {
		for (int i=0; i<this.dim_input; i++) {
			this.s_center[i] += weight * inputs[i];
		}
		this.sum_weight += weight;
		this.buffer.push_data(inputs, weight);
	}
	
	public double calculate_center() throws MatrixException {
		//if (this.DEBUG) {
			System.out.println("NormalDistribution:calculate_center(): sum_weight = " + this.sum_weight);
		//}

		for (int i=0; i<this.dim_input; i++) {
			this.s_center[i] /= this.sum_weight;
		}
		this.Pi = this.sum_weight/(double)this.buffer.getSize();
		return this.SetCenter(this.s_center);
	}
	
	public double calculate_variance_covariance_matrix(double min_sigma) throws MatrixException {
		MatrixObj s_sigma = new MatrixObj(this.dim_input, this.dim_input);
		MatrixObj X, Y;
		for (int p=0; p<this.buffer.getSize(); p++) {
			X = new MatrixObj(this.dim_input, 1, this.buffer.getInput(p));
			//X.display("NormalDistribution calculate_variance_covariance_matrix() X");
			Y = X.Diff(this.U);
			//Y.display("NormalDistribution calculate_variance_covariance_matrix() X-U");			
			s_sigma = s_sigma.Add(Y.multiply(Y.Transport()).multiply(this.buffer.getWeight(p)));
		}
		System.out.println("weight sum=" + this.sum_weight);
		s_sigma.display("before normalize s_sigma");		
		s_sigma = s_sigma.multiply(1/this.sum_weight);
		//s_sigma.display("s_sigma");		
		s_sigma = this.getTeplitz(s_sigma, min_sigma);
		this.SIGMA = s_sigma.inverse();
		s_sigma.display("NormalDistribution:calculate_variance_covariance_matrix():Teplitz");
		this.SIGMA.display("NormalDistribution:calculate_variance_covariance_matrix(): SIGMA");
		//MatrixObj test = this.SIGMA.multiply(s_sigma);
		//test.display("NormalDistribution:calculate_variance_covariance_matrix(): SIGMA*INVSIGMA");
		System.out.println("NormalDistribution:calculate_variance_covariance_matrix(): sqrt(det(SIGMA))=" + Math.sqrt(this.SIGMA.det()));
		//this.CheckShigma(s_sigma, min_sigma);

		//s_sigma.inverse().display("NormalDistribution:calculate_variance_covariance_matrix(): s_sigma");
		//this.CheckShigma(s_sigma, 0.0001);
		return this.SetSigma(s_sigma);
	}

	MatrixObj getTeplitz(MatrixObj sigma, double min_sigma) {
		MatrixObj INVgamma = new MatrixObj(sigma.getL(), sigma.getM());
		MatrixObj INVR = new MatrixObj(sigma.getL(), sigma.getM());
		double rho = this.CalculateRho(sigma, min_sigma);
		System.out.println("NormalDistribution.getTeplitz(): rho=" + rho);
		double gain = 1/(1-rho*rho);
		System.out.println("NormalDistribution.getTeplitz(): gain=" + gain);		
		for (int i=0; i<sigma.getL(); i++) {
			double each_sigma = Math.sqrt(sigma.getData(i, i));
			if (each_sigma < min_sigma) each_sigma = min_sigma;
			INVgamma.set_data(i, i, 1/each_sigma);
			if (i<sigma.getL()-1) {
				INVR.set_data(i+1, i, -rho*gain);
				INVR.set_data(i, i+1, -rho*gain);							
			}
			if (i==0 || i==sigma.getL()-1) {
				INVR.set_data(i, i, gain);
			}else{
				INVR.set_data(i, i, gain*(1+rho*rho));
			}
		}
		INVR.display("NormalDistribution.getTeplitz(): INVR");
		INVgamma.display("NormalDistribution.getTeplitz(): INVgamma");		
		MatrixObj result = (INVgamma.multiply(INVR)).multiply(INVgamma);
		return result;
	}
	
	double CalculateRho(MatrixObj sigma, double min_sigma) {
		double sum=0;
		double diagonal_element1, diagonal_element2, cor;
		for (int i=0; i<sigma.getL()-1; i++) {
			diagonal_element1 =Math.sqrt(sigma.getData(i, i));
			diagonal_element2 =Math.sqrt(sigma.getData(i+1, i+1));
			if (diagonal_element1 < min_sigma) diagonal_element1 = min_sigma;
			if (diagonal_element2 < min_sigma) diagonal_element2 = min_sigma;			
			//System.out.println("diagonal element1= " + diagonal_element1 + " diagonal element2=" + diagonal_element2);
			cor = sigma.getData(i, i+1);
			sum += cor / (diagonal_element1 * diagonal_element2);
		}
		sum /= (double)(sigma.getL()-1);
		return sum;
	}
	
	public void CreateStudenTdistribution(double UpperLimitOfFreedom, boolean SMD, boolean CorrectTDistribution) throws MatrixException {
		if (this.NumberOfMember < 3.0) {
			this.NumberOfMember = 4D;
		}
		this.ST = new StudentTdistribution(this.U, this.SIGMA, (double)this.NumberOfMember, UpperLimitOfFreedom, SMD, CorrectTDistribution);
	}

	public double getTvalue(double x[]) {
		if (this.ST==null) {
			System.err.println("Before using this method, you have to initialize StudentTdistribution using CreateStudenTdistribution(int UpperLimitOfFreedom, boolean SMD) !");
			System.exit(1);
		}
		return this.Pi * this.ST.P(x);
	}
	
	/*@Override
	public void incrementClassMember() {
		this.NumberOfMember ++;
	}*/

	/**
	 * @return the numberOfMember
	 */
	public double getNumberOfMember() {
		return NumberOfMember;
	}

	public void resetNumberOfMember() {
		this.NumberOfMember = 0;
	}
	public void setFreedomeness(double free) {
		this.NumberOfMember = free;
	}
	public void AddFreedomeness(double free) {
		this.NumberOfMember += free;
	}
	
	public MatrixObj getCenter() {
		return this.U;
	}
	public void setPi(double pi) {
		this.Pi = pi;
	}	
	
	public String getPlot() {
		String result= "exp(";
		if (this.dim_input == 1) {
			result += "-((x-(" + this.U.getData(0, 0) + "))**2)/(" + (2*this.SIGMA.getData(0, 0)) + "))";
			return result;
		}else{
			return "NumberOfInputs must be less than 2 \n";
		}
	}	
	
	public NormalDistribution clone() {
		NormalDistribution myclone=null;

		myclone = new NormalDistribution(this.dim_input, this.is_SMD);
		myclone.SIGMA = this.SIGMA.clone();
		myclone.INVSIGMA = this.INVSIGMA.clone();
		myclone.U = this.U.clone();
		return myclone;			

	}

	/**
	 * @return the pAI
	 */
	public double getPAI() {
		return PAI;
	}

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
	
