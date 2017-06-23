package MixtureOfDistributions;

import matrix.DiagonalMatrixObj;
import matrix.MatrixException;
import VectorFunctions.VectorFunctions;

//kernel�ؿ���studentT ��Ž���դ��뤿��Τ��
//�濴���֤ϸ��ꡣconstructor��ƤӽФ��ʳ����濴���֤򥻥åȤ��롣
public class StudentT4kernel {
	boolean DEBUG = false;
	int InputDim;
	double center[];
	double averaged_diff = 0;
	double NumberOfInstances = 0;
	StudentTdistributionD ST;
	NormalDistributionD ND;
	DiagonalMatrixObj Sigma;
	
	public StudentT4kernel(double center[], double min_distance) {
		this.InputDim = center.length;
		this.center = new double[this.InputDim];
		for (int i=0; i<this.InputDim; i++) {
			this.center[i] = center[i];
		}
		this.Sigma = new DiagonalMatrixObj(this.InputDim, min_distance * min_distance);
		this.averaged_diff = min_distance * min_distance; //�������Ȥɤ�����ǽ����������ʬ�˾������ͤ˥��åȤ���Ȥ������ȤǤ���м����ʤΤ���..
		this.NumberOfInstances = 1;
	}//constructor

	public StudentT4kernel(double center[]) {
		this.InputDim = center.length;
		this.NumberOfInstances = 0;
		this.center = new double[this.InputDim];
		for (int i=0; i<this.InputDim; i++) {
			this.center[i] = center[i];
		}
		this.Sigma = new DiagonalMatrixObj(this.InputDim, 0D);
	}//constructor
	
	//x���Ф����б�����kernel��winner�Ȥʤä��Ȥ���
	//ʬ�����ͤ򹹿�����
	public void updateSigma(double x[]) throws MatrixException {
		double each_diff = VectorFunctions.getSqureNorm(
				VectorFunctions.diff(this.center,x));
		this.NumberOfInstances +=1D;
		this.averaged_diff += (1D/this.NumberOfInstances) * (each_diff - this.averaged_diff);
		for (int i=0; i<this.InputDim; i++) {//�ºݤˤ϶�ʬ������ǤϤʤ������顼��ɸ���к�����Ѥ���
			this.Sigma.set_data(i, this.averaged_diff);//��󥻥åȤ���
		}
		if (this.DEBUG) {
			this.Sigma.display("StudentT4kernel.updateSigma():Sigma");
			System.out.println("StudentT4kernel.updateSigma() NumberOfInstances=" + this.NumberOfInstances);
		}
		this.ND.SetSigma(this.Sigma);

	}//updateSigma()
	
	//ʬ�����ͤ򹹿�����(�ؽ���������ץ���򥯥饹���ǥ�����Ȥ�����ˤ��褦���롣���������double�Ǥ��뤳�Ȥ���ա�
	public void updateSigma(double x[], double NumberOfLearnedSamples) throws MatrixException {
		double each_diff = VectorFunctions.getSqureNorm(
				VectorFunctions.diff(this.center,x));
		NumberOfLearnedSamples += 1D;
		this.NumberOfInstances = NumberOfLearnedSamples;
		this.averaged_diff += (1D/NumberOfLearnedSamples) * (each_diff - this.averaged_diff);
		for (int i=0; i<this.InputDim; i++) {//�ºݤˤ϶�ʬ������ǤϤʤ������顼��ɸ���к�����Ѥ���
			this.Sigma.set_data(i, this.averaged_diff);//��󥻥åȤ���
		}
		if (this.DEBUG) {
			this.Sigma.display("StudentT4kernel.updateSigma():Sigma");
			System.out.println("StudentT4kernel.updateSigma() NumberOfInstances=" + this.NumberOfInstances);
		}
		this.ND.SetSigma(this.Sigma);

	}//updateSigma()	
	
	public void AggregagteSigma(double Dependency, double center[], double sigma, double NumberOfSamples) {
		double revised_sigma = this.Sigma.getData(0,0);
		double diff = VectorFunctions.getSqureNorm(
				VectorFunctions.diff(this.center,center));		
		revised_sigma += Dependency * (diff *  NumberOfSamples + sigma);
		for (int i=0; i<this.InputDim; i++) {
			this.Sigma.set_data(i, revised_sigma);
		}
	}
	
	public double getSigma() {
		double sigma = this.Sigma.getData(0,0);
		return sigma;
	}
	
	//���Υ����ͥ���б���������ʬ�ۤ����դ���
	public void PrepareCorrespondingDistribution() throws MatrixException {
		this.ND = new NormalDistributionD(this.InputDim);//�ޤ��б���������ʬ��(Gaussian Distribution)����

		this.ND.SetCenter(this.center);//����ʬ�ۥ��饹ND���濴���֤򥻥åȤ���
		this.ND.SetSigma(this.Sigma);//����ʬ�ۥ��饹ND���б����붦ʬ������򥻥å�

	}//getStudentTDistribution()
	
	//����ʬ�ۥ��饹���̤���student tʬ��q(x)���ꡢ����ʬ��p(x)�Ȥ��桧(q(x)/p(x))^r�򻻽Ф��롣
	//UppdrLimitOfFreedom��ʸ���̤꼫ͳ�٤ξ��
	public double getImportanceWeight(int UpperLimitOfFreedom, double x[], double r) {
		this.ND.setFreedomeness(this.NumberOfInstances);
		this.ND.CreateStudenTdistribution(UpperLimitOfFreedom, true);
		return Math.pow(this.ND.q_div_p(x), r);
	}

	/**
	 * @return the numberOfInstances
	 */
	public double getNumberOfInstances() {
		return NumberOfInstances;
	}
	
}
