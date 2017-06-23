/*
 * Created on 2005/09/06
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package FIFO;

import java.util.*;

import VectorFunctions.VectorFunctions;
/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MoutVariableSizedBuffer {
	Vector<MultiDimCell> buffer = new Vector<MultiDimCell>();
	
	public MoutVariableSizedBuffer() {

	}
	
	public void push_data(double data[]) {
		MultiDimCell cellobj = new MultiDimCell(data);
		buffer.addElement(cellobj);
	}
	
	
	public double[] getOutput(int index) {
		MultiDimCell cellobj;
		cellobj = (MultiDimCell)this.buffer.get(index);
		return cellobj.getData();
	}
	
	public void setData(int index, double data[]) {
		MultiDimCell cellobj;
		cellobj = (MultiDimCell)this.buffer.get(index);
		cellobj.setData(data);
	}
	
	public double getMeanSquare() {
		MultiDimCell cell_obj;
		int number_of_instances=this.buffer.size();
		double sum=0D;
		Enumeration<MultiDimCell> e = this.buffer.elements();
		while (e.hasMoreElements()) {
			cell_obj = (MultiDimCell)e.nextElement();
			sum += VectorFunctions.getNorm(cell_obj.getData());
		}
		sum /= number_of_instances;
		return sum;
	}
	
	
	public int getSize() {
		return this.buffer.size();
	}
}
