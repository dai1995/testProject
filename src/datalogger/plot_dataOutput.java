/*
 * Created on 2006/01/31
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package datalogger;
import java.io.*;
import org.w3c.dom.Node;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class plot_dataOutput extends errorOutput {

	public double inputs[];//ľ�����Ϥ��Ƥ��礦����
	public double outputs[];
	int NumberOfInputs, NumberOfOutputs;
	/**
	 * 
	 */
	public plot_dataOutput(String filename, int NumberOfInputs, int NumberOfOutputs) {
		super(filename);
		this.NumberOfInputs = NumberOfInputs;
		this.NumberOfOutputs = NumberOfOutputs;
		this.int_proc();
		// TODO Auto-generated constructor stub
	}
	
	public plot_dataOutput(Node nd, int NumberOfInputs, int NumberOfOutputs) {
		super(nd);
		this.NumberOfInputs = NumberOfInputs;
		this.NumberOfOutputs = NumberOfOutputs;
		this.int_proc();		
		// TODO Auto-generated constructor stub
	}
	
	public void set_data(double inputs[], double outputs[]) {
		this.inputs = inputs;
		this.outputs = outputs;
	}

	//���� �ޤ� set_data�¹ԤΤ���
    public void vector_put() {
    	try {
    		//���Ϥν񤭹���
    		for (int i=0; i<this.NumberOfInputs; i++) {
    			wfp.write(String.valueOf(this.inputs[i]));
    			wfp.write(" ");
    		}
    		//���Ϥν񤭹���    		
    		for (int o=0; o<this.NumberOfOutputs; o++) {
        	    wfp.write(String.valueOf(this.outputs[o]));
        	    wfp.write(" ");
    		}
    	    wfp.write("\n");
    	}catch(IOException ioex) {
    	    ioex.printStackTrace();
    	}
    }
	
	
	void int_proc() {
		this.inputs = new double[this.NumberOfInputs];
		this.outputs = new double[this.NumberOfOutputs];
	}

}
