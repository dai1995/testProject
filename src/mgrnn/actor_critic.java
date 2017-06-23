package mgrnn;

import java.util.ArrayList;

public interface actor_critic {
	public LgrnnLearningStatus learning(double inputs[], double outputs[], double UtilityFunction);
	public int getOutputSize();
	public double[] calculate_outputs(double input[]);
	public void setNumberOfOutputs(int numberOfOutputs);
	public void setNumberOfInputs(int numberOfInputs);
	public double[][] display_all_kernels();
	public ArrayList<Cell> getHidden_units();
	public Cell getNearestUnit(double inputs[]);
}
