package MixtureOfDistributions;

import FIFO.MinMoutWeightVariableSizedBuffer;
import FIFO.MinNoOutputsVariableSizedBuffer;
import matrix.DiagonalMatrixObj;
import matrix.MatrixException;
import matrix.MatrixObj;

public class NormalDistributionD implements Distribution {
	DiagonalMatrixObj SIGMA;
	MatrixObj INVSIGMA;
	MatrixObj U;
	int dim_input;
	double Gain, PAI;
	double NumberOfMember=0;
	boolean DEBUG=false;
	double dotProduct;
	double Pi;
	StudentTdistributionD ST=null; //StudentTdistribution whose center and variance-covariance matrix are the same as those of used in this class
	
	MatrixObj eigenVector[];
	double eigenValue[];
   
	
	// for calculating center
	double s_center[];
	double sum_weight=0;	
	
	// data buffer
	MinNoOutputsVariableSizedBuffer buffer;
	/**
	 * 
	 */
	public NormalDistributionD(int NumberOfInputs) {

		this.dim_input = NumberOfInputs;
		this.Gain = 1; //とりあへず
		this.Pi = 1;
		this.buffer = new MinNoOutputsVariableSizedBuffer(NumberOfInputs);
		this.s_center = new double[this.dim_input];
		this.SIGMA = new DiagonalMatrixObj(this.dim_input, this.dim_input);
		this.INVSIGMA = new MatrixObj(this.dim_input, this.dim_input);
	}
	
	public NormalDistributionD(double Center[], MatrixObj sigma) {
		this.dim_input = Center.length;
		try {
			this.SetCenter(Center);
		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.SIGMA = new DiagonalMatrixObj(this.dim_input, this.dim_input);
		this.INVSIGMA = new MatrixObj(this.dim_input, this.dim_input);
		

		//this.invsigma = this.sigma.inverse_d();
		try {
			this.SetSigma(sigma);
		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		// TODO Auto-generated method stub
		this.dotProduct = getMaharanobisDistance(x);

		//System.out.println("NormalDistribution:P(x): MaharanobisDist = " + this.dotProduct);
		double result = Math.exp(-this.dotProduct/2D);
		//if (result == 0.0) {
			//System.out.println("dot product is " + this.dotProduct);
			//result = Double.MIN_NORMAL;
		//}
		this.Gain = Math.pow(2*Math.PI, (double)this.dim_input/2) * Math.sqrt(this.SIGMA.det());
		//System.out.println("NormalDistributionD:P(x): dotproduct = " + this.dotProduct);
		//System.out.println("NormalDistributionD:P(x): Gain = " + this.Gain + " exp= " + result);
		//System.out.println("NormalDistribution:P(x): Pi = " + this.Pi);
		this.PAI = this.Pi/this.Gain;
		result *= this.PAI;
		/*if (result == 0.0) {
			System.out.println("NormalDistributionD:P():dot product is " + this.dotProduct);
		}*/		
		return result;
	}
	
	public double OmitDetSigmaP(double[] x) {
		this.dotProduct = getMaharanobisDistance(x);
		double result = Math.exp(-this.dotProduct/2D);
		this.Gain = Math.pow(2*Math.PI, -(double)this.dim_input/2D); //Note: det(Σ) is not calculated. 
		//System.out.println("NormalDistribution:OmitDetSigmaP(x): Gain = " + this.Gain);
		result *= this.Gain;
		return result;		
	}
	

	public double q_div_p(double[] x) {
		double q = this.ST.OmitDetSigmaP(x);
		double p = this.OmitDetSigmaP(x);
		double qp = q /p;
		//System.out.println("NormalDistributionD: q_div_p(): qp = " + qp + " p=" + p + " q=" +q);		
		return qp;
	}	
	
	
	
	public double getExp(double[] x) {
		this.dotProduct = getMaharanobisDistance(x);
		double result = Math.exp(-this.dotProduct/2D);
		return result;
	}
	
	public double getGain() {
		this.Gain = Math.pow(2*Math.PI, (double)this.dim_input/2D) * Math.sqrt(this.SIGMA.det());
		double result = this.Pi/this.Gain;
		return result;
	}
	
	public double getEachGain(int p) {
		double gain = 
			Math.sqrt(2D * Math.PI) * Math.sqrt(this.SIGMA.getData(p, p));
		double result = 1 / gain;
		return result;
	}
	
	public double getEachP(int p, double x[]) {
		double distance = 
			Math.pow((x[p] - this.U.getData(p, 0)), 2D)/
			(2*this.SIGMA.getData(p,p));
		double result = this.getEachGain(p) * Math.exp(-distance);
		return result;
	}
	
	public double P() {
		double result = Math.exp(-this.dotProduct/2);
		result *= this.Gain * this.Pi;
		return result;
	}
	
	public double getMaharanobisDistance(double[] x) {
		MatrixObj X = new MatrixObj(x.length, x, 1); //縦ベクトルを作る。
		X=X.Diff(this.U);
		//this.INVSIGMA.display("INVSIGMA");
		//X.display("X");
		MatrixObj XT = X.Transport();
		//XT.display("XT");
		MatrixObj XTI = XT.multiply(this.INVSIGMA);
		
		MatrixObj Dotproduct =XTI.multiply(X); 
		//MatrixObj Dotproduct = mc.MultiplyMatrix_M(mc.MultiplyMatrix_M(X.Transport(), this.INVSIGMA),X);
		this.dotProduct = Dotproduct.getData(0,0);
		
		return Dotproduct.getData(0,0);
	}
	

	//ユークリッドノルムの2乗を与える
	public double getDistance(double[] x) {
		MatrixObj X = new MatrixObj(x.length, x, 1); //縦ベクトルを作る。
		X=X.Diff(this.U);
		MatrixObj dotproduct = X.Transport().multiply(X);
		return dotproduct.getData(0,0);
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
	
	public DiagonalMatrixObj getSigmaD() {
		return null;
	}
		
	
	public void reset_parameters() {
		for (int i=0; i<this.dim_input; i++) {
			this.s_center[i] = 0;//あくまで中心計算用の配列で一時的に使用する.本物の中心位置はUに入っている。
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
		if (this.DEBUG) {
			System.out.println("NormalDistribution:calculate_center(): sum_weight = " + this.sum_weight);
		}
		for (int i=0; i<this.dim_input; i++) {
			this.s_center[i] /= this.sum_weight;
		}
		this.Pi = this.sum_weight/(double)this.buffer.getSize();
		return this.SetCenter(this.s_center);
	}
	
	public double calculate_variance_covariance_matrix(double min_sigma) throws MatrixException {
		MatrixObj s_sigma = new MatrixObj(this.dim_input, this.dim_input);
		double each_value;
		MatrixObj X, Y;
		for (int p=0; p<this.buffer.getSize(); p++) {
			X = new MatrixObj(this.dim_input, 1, this.buffer.getInput(p));
			Y = X.Diff(this.U);
			//Y.display("NormalDistributionD:calculate_variance_covariance_matrix(): X-U");
			for (int i=0; i<this.dim_input; i++) {
				each_value = s_sigma.getData(i, i);
				each_value += Math.pow(Y.getData(i, 0),2D) * this.buffer.getWeight(p);
				s_sigma.set_data(i, i, each_value);
			}
		}
		for (int i=0; i<this.dim_input; i++) {
			each_value = s_sigma.getData(i, i);
			each_value /= this.sum_weight;
			s_sigma.set_data(i, i, each_value);
		}		
		this.CheckShigma(s_sigma, min_sigma);
		//s_sigma.display("NormalDistributionD:calculate_variance_covariance_matrix(): s_sigma");
		//System.out.println("det s_sigma = " + s_sigma.det());
		//this.CheckShigma(s_sigma, 0.0001);
		return this.SetSigma(s_sigma);
	}

	void CheckShigma(MatrixObj sigma, double small_value) {
		for (int i=0; i<sigma.getL(); i++) {
			if (sigma.getData(i,i)<small_value) {
				sigma.set_data(i,i,small_value);
			}
		}
	}
	

	/**
	 * @param sigma The sigma to set.
	 * @throws MatrixException 
	 */
	public double SetSigma(MatrixObj sigma) throws MatrixException {
		MatrixObj Diff = this.SIGMA.Diff(sigma);

		for (int l=0; l<sigma.getL(); l++) {
			this.SIGMA.set_data(l, sigma.getData(l, l));
		}

		for (int l=0; l<sigma.getL(); l++) {
			this.INVSIGMA.set_data(l, l, 1/this.SIGMA.getData(l, l));
		}
		return Diff.getNorm();
	}

	public void CreateStudenTdistribution(int UpperLimitOfFreedom, boolean CorrectTDistribution) {
		if (this.NumberOfMember <= 2.0) {
			this.NumberOfMember = 2.1D;
		}
		System.out.println("NormalDistributionD:CreateStudentTdistribution():NumberOfMember=" + this.NumberOfMember );
		
		this.ST = new StudentTdistributionD(this.U, this.SIGMA, (double)this.NumberOfMember, (double)UpperLimitOfFreedom, CorrectTDistribution);
	}

	public double getTvalue(double x[]) {
		if (this.ST==null) {
			System.err.println("Before using this method, you have to initialize StudentTdistribution using CreateStudenTdistribution(int UpperLimitOfFreedom, boolean SMD) !");
			System.exit(1);
		}
		return this.Pi * this.ST.P(x);
		//return this.ST.P(x);
	}
	
	/**
	 * @return the numberOfMember
	 */
	public double getNumberOfMember() {
		return NumberOfMember;
	}
	

	@Override
	public void resetNumberOfMember() {
		// TODO Auto-generated method stub
		this.NumberOfMember = 0;
	}
	@Override
	public void AddFreedomeness(double free) {
		// TODO Auto-generated method stub
		this.NumberOfMember += free;
	}

	@Override
	public void setFreedomeness(double free) {
		// TODO Auto-generated method stub
		this.NumberOfMember = free;
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
	
	public NormalDistributionD clone() {
		NormalDistributionD myclone=null;

		myclone = new NormalDistributionD(this.dim_input);
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
