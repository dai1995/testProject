package FIFO;

import java.util.Vector;

public class MinNoOutputsVariableSizedBuffer {
	Vector<Object> buffer = new Vector<Object>();
	int NumberOfInputs, NumberOfOutputs;

	public MinNoOutputsVariableSizedBuffer(int NumberOfInputs) {
		this.NumberOfInputs = NumberOfInputs;
		this.NumberOfOutputs = 0;
	}
	
	public void push_data(double inputs[]) {
		cellNoOutputs cellobj = new cellNoOutputs(inputs);
		buffer.addElement(cellobj);
	}
	public void push_data(int data) {
		cellNoOutputs cellobj = new cellNoOutputs(data);
		buffer.addElement(cellobj);
	}	
	public void push_data(double inputs[], double weight) {
		cellNoOutputs cellobj = new cellNoOutputs(inputs);
		cellobj.setError(weight);
		buffer.addElement(cellobj);
	}	
	public double[] getInput(int index) {
		cell cellobj;
		cellobj = (cell)this.buffer.get(index);
		return cellobj.getInputs();
	}
	
	public int getSingleInput(int index) {
		cellNoOutputs cellobj;
		cellobj = (cellNoOutputs)this.buffer.get(index);
		return cellobj.getInput();
	}
	public int getSize() {
		return this.buffer.size();
	}
	
	public void dispose() {
		this.buffer.removeAllElements();
	}
	
	public void setWeight(int index, double weight) {
		cell cellobj;
		cellobj = (cell)this.buffer.get(index);
		cellobj.setError(weight);
	}
	
	public double getWeight(int index) {
		cell cellobj;
		cellobj = (cell)this.buffer.get(index);
		return cellobj.getError();
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
}
