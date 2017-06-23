/*
 * Created on 2008/07/24
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
public class MinMoutWeightVariableSizedBuffer extends
		MinMoutSerrVariableSizedBuffer {

	/**
	 * @param NumberOfInputs
	 * @param NumberOfOutputs
	 */
	public MinMoutWeightVariableSizedBuffer(int NumberOfInputs,
			int NumberOfOutputs) {
		super(NumberOfInputs, NumberOfOutputs);
		// TODO Auto-generated constructor stub
	}
	
	public void push_data(double inputs[], double outputs[]) {
		cell cellobj = new cellw(inputs, outputs, 0, 1, 1);
		buffer.addElement(cellobj);
	}
	public void push_data(double inputs[], int outputClass) {
		cell cellobj = new cellw(inputs, outputClass, 0, 1, 1);
		buffer.addElement(cellobj);
	}	
	
	public void push_data(double inputs[], double outputs[], double source_weight, double actual_weight) {
		cell cellobj = new cellw(inputs, outputs, 0, source_weight, actual_weight);
		buffer.addElement(cellobj);
	}	
	public void push_data(double inputs[], int outputClass, double source_weight, double actual_weight) {
		cell cellobj = new cellw(inputs, outputClass, 0, source_weight, actual_weight);
		buffer.addElement(cellobj);
	}	
	
	public double[] getInput(int index) {
		cell cellobj;
		cellobj = (cell)this.buffer.get(index);
		return cellobj.getInputs();
	}
	
	public double[] getOutput(int index) {
		cell cellobj;
		cellobj = (cell)this.buffer.get(index);
		return cellobj.getOutputs();
	}
	
	
	public void resetError(int index, double init_error) {
		cell cellobj;
		cellobj = (cell)this.buffer.get(index);
		cellobj.setError(init_error);
	}
	
	public void setError(int index, double error) {
		cell cell_obj;
		cell_obj = (cell)this.buffer.get(index);
		cell_obj.setError(error);
	}
	
	public void setActualWeight(int index, double Weight) {
		cellw cell_obj;
		cell_obj = (cellw)this.buffer.get(index);
		cell_obj.setActual_weight(Weight);
	}
	
	public void setSourceWeight(int index, double Weight) {
		cellw cell_obj;
		cell_obj = (cellw)this.buffer.get(index);
		cell_obj.setSource_weight(Weight);
	}
	
	public double getActualWeight(int index) {
		cellw cell_obj;
		cell_obj = (cellw)this.buffer.get(index);
		return cell_obj.getActual_weight();
	}
	
	public double getSourceWeight(int index) {
		cellw cell_obj;
		cell_obj = (cellw)this.buffer.get(index);
		return cell_obj.getSource_weight();
	}
	
	public int getSize() {
		return this.buffer.size();
	}
	
	public int getOutputClass(int index) {
		cellw cell_obj;
		cell_obj = (cellw)this.buffer.get(index);
		return cell_obj.getOutputClass();
	}
}
