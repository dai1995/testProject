package RBFNN;

import java.util.Vector;

import matrix.MatrixException;
import matrix.MatrixObj;

public interface RBFNet {
	public double get_sqare_error(double[] inputs, double target_outputs[]);
	public boolean get_accuracy(double[] inputs, double target_outputs[]);
	public double[] getOutputs(double inputs[]);
	
	public int getNumberOfInputs();
	public int getNumberOfOutputs();
	
	public MatrixObj getHat();
	//public double[] generate_pseudo_input(int index, RBFNet old_RBFNN);
	public double[] generate_pseudo_input2(int index, RBFNet old_RBFNN);	
	public int getNumberOfLearnedSamples();
	public String getName();
	public int getPhiSizeL();
	public int getPhiSizeM();
	public MatrixObj getPHI();

	public Vector<HiddenUnit> getHiddenUnits();
	public int getNumberOfHiddenUnits();
	public int[] getWinnerCell();
	public boolean IncLearning(FIFO.MinMoutWeightVariableSizedBuffer buffer, int AddNumberOfHiddenUnits, boolean UseWeightedKmeans, double threshold, RBFNN old_RBFNN) throws MatrixException;
	public MatrixObj getTargetOutputs();
	public MatrixObj getWold();
	public MatrixObj getWnew();

}
