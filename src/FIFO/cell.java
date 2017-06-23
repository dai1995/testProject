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
public interface cell {
	public double[] getInputs();
	public double[] getOutputs();
	public int getOutputClass();
	public double getError();
	public void setError(double error);
}
