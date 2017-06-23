package MixtureOfDistributions;

import FIFO.MinMoutWeightVariableSizedBuffer;
import matrix.MatrixException;
import matrix.MatrixObj;

public interface Distribution {
	double P(double x[]);
	double P();
	double SetCenter(double center[]) throws MatrixException; //return change in parameter
	double SetSigma(MatrixObj sigma) throws MatrixException ; //return change in parameter
	//void SetSigma(DiagonalMatrixObj sigma);
	//void MP(MinNoOutputsVariableSizedBuffer buffer);
	MatrixObj GetCenter();
	MatrixObj getSigma();
	void reset_parameters();
	void push_center(double[] ds, double d);
	double calculate_center() throws MatrixException;
	double calculate_variance_covariance_matrix(double min_sigma) throws MatrixException;
	void resetNumberOfMember();
	void setFreedomeness(double free);
	void AddFreedomeness(double free);
	//void CreateStudenTdistribution(int upperLimitOfFreedom, boolean smd);
	double getTvalue(double[] input);
	double q_div_p(double[] input);
	public double getExp(double[] x);
	public String getPlot();
	public double getPAI();//ガウシャンの高さ
	public void push_corresponding_data(double x[], double outputs[]);
	public MinMoutWeightVariableSizedBuffer get_corresponding_data();
}
