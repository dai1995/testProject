/*
 * Created on 2005/09/06
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package FIFO;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class cellnormal implements cell {
	protected double inputs[], outputs[];
	protected int outputClass;
	protected double error;

	public cellnormal(double inputs[], double outputs[], double error) {
		this.inputs = inputs;
		this.outputs = outputs;
		this.error = error;
	}
	public cellnormal(double inputs[], int outputClass, double error) {
		this.inputs = inputs;
		this.outputClass = outputClass;
		this.error = error;
	}	
	public double[] getInputs() {
		return this.inputs;
	}
	public double[] getOutputs() {
		return this.outputs;
	}
	public int getOutputClass() {
		return this.outputClass;
	}	
	
	public double getError() {
		return this.error;
	}
	public void setError(double error) {
		this.error = error;
	}
}
