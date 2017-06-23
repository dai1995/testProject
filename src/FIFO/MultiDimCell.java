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
public class MultiDimCell {
	protected double data[];

	public MultiDimCell(double data[]) {
		this.data = data;
	}

	public double[] getData() {
		return this.data;
	}
	public void setData(double data[]) {
		this.data = data;
	}
}
