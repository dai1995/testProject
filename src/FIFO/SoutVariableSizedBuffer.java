/*
 * Created on 2005/09/06
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package FIFO;

import java.util.*;
/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class SoutVariableSizedBuffer {
	Vector<OneDimCell> buffer = new Vector<OneDimCell>();
	
	public SoutVariableSizedBuffer() {

	}
	
	public void push_data(double data) {
		OneDimCell cellobj = new OneDimCell(data);
		buffer.addElement(cellobj);
	}
	
	
	public double getOutput(int index) {
		OneDimCell cellobj;
		cellobj = (OneDimCell)this.buffer.get(index);
		return cellobj.getData();
	}
	
	public void setData(int index, double data) {
		OneDimCell cellobj;
		cellobj = (OneDimCell)this.buffer.get(index);
		cellobj.setData(data);
	}
	
	public double getMeanSquare() {
		OneDimCell cell_obj;
		int number_of_instances=this.buffer.size();
		double sum=0D;
		Enumeration<OneDimCell> e = this.buffer.elements();
		while (e.hasMoreElements()) {
			cell_obj = (OneDimCell)e.nextElement();
			sum += cell_obj.getData();
		}
		sum /= number_of_instances;
		return sum;
	}
	
	
	public int getSize() {
		return this.buffer.size();
	}
}
