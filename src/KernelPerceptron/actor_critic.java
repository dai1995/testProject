package KernelPerceptron;

import java.util.ArrayList;


import mgrnn.Cell;
import mgrnn.LgrnnLearningStatus;

public interface actor_critic {
	public void learning(double inputs[], double outputs[], double UtilityFunction, boolean flag);
	public int getOutputSize();
	public double[] calculate_outputs(double input[]);
	public void setNumberOfOutputs(int numberOfOutputs);
	public void setNumberOfInputs(int numberOfInputs);
	public double[][] display_all_kernels();
	public ArrayList<Cell> getHidden_units();
	public Cell getNearestUnit(double inputs[]);
}
