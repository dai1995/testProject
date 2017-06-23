/*
 * Created on 2009/02/04
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
public class cellw implements cell {
	protected double actual_weight;
	protected double source_weight;
	protected double inputs[], outputs[];
	protected int outputClass;
	protected double error;	
	/**
	 * @param inputs
	 * @param outputs
	 * @param error
	 */
	public cellw(double[] inputs, double[] outputs, double error, double actual_weight, double source_weight) {
		this.inputs = inputs;
		this.outputs = outputs;
		this.error = error;
		this.actual_weight = actual_weight;
		this.source_weight = source_weight;
	}
	public cellw(double[] inputs, int outputClass, double error, double actual_weight, double source_weight) {
		this.inputs = inputs;
		this.outputClass = outputClass;
		this.error = error;
		this.actual_weight = actual_weight;
		this.source_weight = source_weight;
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
	/**
	 * @return Returns the actual_weight.
	 */
	public double getActual_weight() {
		return actual_weight;
	}
	/**
	 * @param actual_weight The actual_weight to set.
	 */
	public void setActual_weight(double actual_weight) {
		this.actual_weight = actual_weight;
	}
	/**
	 * @return Returns the source_weight.
	 */
	public double getSource_weight() {
		return source_weight;
	}
	/**
	 * @param source_weight The source_weight to set.
	 */
	public void setSource_weight(double source_weight) {
		this.source_weight = source_weight;
	}
}
