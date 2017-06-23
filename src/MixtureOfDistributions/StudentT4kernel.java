package MixtureOfDistributions;

import matrix.DiagonalMatrixObj;
import matrix.MatrixException;
import VectorFunctions.VectorFunctions;

//kernel関数にstudentT を貼り付けるためのもの
//中心位置は固定。constructorを呼び出す段階で中心位置をセットする。
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
		this.averaged_diff = min_distance * min_distance; //これをやるとどうも性能が悪い。十分に小さな値にセットするということであれば自然なのだが..
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
	
	//xに対して対応するkernelがwinnerとなったときに
	//分散の値を更新する
	public void updateSigma(double x[]) throws MatrixException {
		double each_diff = VectorFunctions.getSqureNorm(
				VectorFunctions.diff(this.center,x));
		this.NumberOfInstances +=1D;
		this.averaged_diff += (1D/this.NumberOfInstances) * (each_diff - this.averaged_diff);
		for (int i=0; i<this.InputDim; i++) {//実際には共分散行列ではなくスカラーの標準偏差を使用する
			this.Sigma.set_data(i, this.averaged_diff);//毎回セットする
		}
		if (this.DEBUG) {
			this.Sigma.display("StudentT4kernel.updateSigma():Sigma");
			System.out.println("StudentT4kernel.updateSigma() NumberOfInstances=" + this.NumberOfInstances);
		}
		this.ND.SetSigma(this.Sigma);

	}//updateSigma()
	
	//分散の値を更新する(学習したサンプル数をクラス外でカウントする場合にしようする。第二引数はdoubleであることに注意）
	public void updateSigma(double x[], double NumberOfLearnedSamples) throws MatrixException {
		double each_diff = VectorFunctions.getSqureNorm(
				VectorFunctions.diff(this.center,x));
		NumberOfLearnedSamples += 1D;
		this.NumberOfInstances = NumberOfLearnedSamples;
		this.averaged_diff += (1D/NumberOfLearnedSamples) * (each_diff - this.averaged_diff);
		for (int i=0; i<this.InputDim; i++) {//実際には共分散行列ではなくスカラーの標準偏差を使用する
			this.Sigma.set_data(i, this.averaged_diff);//毎回セットする
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
	
	//このカーネルに対応する正規分布を割り付ける
	public void PrepareCorrespondingDistribution() throws MatrixException {
		this.ND = new NormalDistributionD(this.InputDim);//まず対応する正規分布(Gaussian Distribution)を作る

		this.ND.SetCenter(this.center);//正規分布クラスNDに中心位置をセットする
		this.ND.SetSigma(this.Sigma);//正規分布クラスNDに対応する共分散行列をセット

	}//getStudentTDistribution()
	
	//正規分布クラスを通してstudent t分布q(x)を作り、正規分布p(x)との比：(q(x)/p(x))^rを算出する。
	//UppdrLimitOfFreedomは文字通り自由度の上限
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
