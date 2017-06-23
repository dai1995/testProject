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
public class MinMoutSerrVariableSizedBuffer {
	Vector<Object> buffer = new Vector<Object>();
	int NumberOfInputs, NumberOfOutputs;
	
	public MinMoutSerrVariableSizedBuffer(int NumberOfInputs, int NumberOfOutputs) {
		this.NumberOfInputs = NumberOfInputs;
		this.NumberOfOutputs = NumberOfOutputs;
	}
	
	public void push_data(double inputs[], double outputs[], double error) {
		cell cellobj = new cellnormal(inputs, outputs, error);
		buffer.addElement(cellobj);
	}
	public void push_data(double inputs[], int outputClass, double error) {
		cell cellobj = new cellnormal(inputs, outputClass, error);
		buffer.addElement(cellobj);
	}
	public void push_data(double inputs[], double outputs[]) {
		cell cellobj = new cellnormal(inputs, outputs, 0);
		buffer.addElement(cellobj);
	}
	public void push_data(double inputs[], int outputClass) {
		cell cellobj = new cellnormal(inputs, outputClass, 0);
		buffer.addElement(cellobj);
	}	
	
	public double[] getInput(int index) {
		cell cellobj;
		cellobj = (cell)this.buffer.get(index);
		return cellobj.getInputs();
	}
	
	public double getWeight(int index) {
		cell cellobj;
		cellobj = (cell)this.buffer.get(index);
		return cellobj.getError();
	}
	
	public double[] getOutput(int index) {
		cell cellobj;
		cellobj = (cell)this.buffer.get(index);
		return cellobj.getOutputs();
	}
	public int getOutputClass(int index) {
		cell cellobj;
		cellobj = (cell)this.buffer.get(index);
		return cellobj.getOutputClass();
	}	
	public void resetError(int index, double err) {
		cell cellobj;
		cellobj = (cell)this.buffer.get(index);
		cellobj.setError(err);
	}
	
	public double getMeanSquareError() {
		cell cell_obj;
		int number_of_instances=this.buffer.size();
		double sum=0D;
		Enumeration<Object> e = this.buffer.elements();
		while (e.hasMoreElements()) {
			cell_obj = (cell)e.nextElement();
			sum += cell_obj.getError();
		}
		sum /= number_of_instances;
		return sum;
	}
	
	public void setError(int index, double error) {
		cell cell_obj;
		cell_obj = (cell)this.buffer.get(index);
		cell_obj.setError(error);
	}
	
	public int getSize() {
		return this.buffer.size();
	}
	
	public void shuffle() {
		int a;
		for (int i=0; i<this.getSize(); i++) {
			a = (int)((double)i*Math.random());
			this.swap(i, a);
		}
	}
	
	void swap(int i, int j) {
		Object s_obj;
		s_obj = this.buffer.get(i);
		this.buffer.set(i, this.buffer.get(j));
		this.buffer.set(j, s_obj);
	}
	
	public void dispose() {
		this.buffer.removeAllElements();
	}
}
