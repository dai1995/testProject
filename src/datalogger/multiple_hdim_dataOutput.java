/*
 * Created on 2007/11/04
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
public class multiple_hdim_dataOutput extends multiple_dataOutput {
	int NumberOfInputs;
	
	/**
	 * @param nd
	 */
	public multiple_hdim_dataOutput(Node nd) {
		super(nd);
		// TODO Auto-generated constructor stub
	}
	

    public void put(double x[], double data) {
    	if (this.isEnable) {
    		try {
    			for (int i=0; i<x.length; i++) {
    				wfp.write(String.valueOf(x[i]));
    				wfp.write(" ");
    			}
    			wfp.write(String.valueOf(data));
    			wfp.write("\n");
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }
    
    //for put errorbar
    public void put(double x[], double y, double ylow, double yhigh) {
    	if (this.isEnable) {
    		try {
    			for (int i=0; i<x.length; i++) {
    				wfp.write(String.valueOf(x[i]));
    				wfp.write(" ");
    			}
    			wfp.write(String.valueOf(y));
				wfp.write(" ");
    			wfp.write(String.valueOf(ylow));
				wfp.write(" ");    			
    			wfp.write(String.valueOf(yhigh));				
    			wfp.write("\n");
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }
    public void put(int x[], int data) {
    	if (this.isEnable) {
    		try {
    			for (int i=0; i<x.length; i++) {
    				wfp.write(String.valueOf(x[i]));
    				wfp.write(" ");
    			}
    			wfp.write(String.valueOf(data));
    			wfp.write("\n");
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }


	public void put(double x[], double y, double ylow, double yhigh, double other) {
    	if (this.isEnable) {
    		try {
    			for (int i=0; i<x.length; i++) {
    				wfp.write(String.valueOf(x[i]));
    				wfp.write(" ");
    			}
    			wfp.write(String.valueOf(y));
				wfp.write(" ");
    			wfp.write(String.valueOf(ylow));
				wfp.write(" ");    			
    			wfp.write(String.valueOf(yhigh));
				wfp.write(" ");    			
    			wfp.write(String.valueOf(other));				
    			wfp.write("\n");
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}		// TODO Auto-generated method stub
		
	}
    
}
