/*
 * Created on 2005/08/05
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
public class dataOutput extends errorOutput {

	/**
	 * 
	 */
	public dataOutput(String filename) {
		super(filename);
		// TODO Auto-generated constructor stub
	}
	
	public dataOutput(Node nd) {
		super(nd);
	}
	public dataOutput(Node nd, String ID_str) {
		super(nd, ID_str);
	}
	
   public void put(String data) {
	    	if (this.isEnable) {
	    		try {
	    			wfp.write("#" + data);
	    			wfp.write("\n");
	    		}catch(IOException ioex) {
	    			ioex.printStackTrace();
	    		}
	    	}
    }
   
    public void put(double x, double data) {
    	if (this.isEnable) {
    		try {
    			wfp.write(String.valueOf(x));
    			wfp.write(" ");
    			wfp.write(String.valueOf(data));
    			wfp.write("\n");
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }

    public void put(double x, double data1, double data2) {
    	if (this.isEnable) {
    		try {
    			wfp.write(String.valueOf(x));
    			wfp.write(" ");
    			wfp.write(String.valueOf(data1));
    			wfp.write(" ");
    			wfp.write(String.valueOf(data2));
    			wfp.write("\n");
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }
    public void put(double x, double data1, double data2, double data3) {
    	if (this.isEnable) {
    		try {
    			wfp.write(String.valueOf(x));
    			wfp.write(" ");
    			wfp.write(String.valueOf(data1));
    			wfp.write(" ");
    			wfp.write(String.valueOf(data2));
    			wfp.write(" ");
    			wfp.write(String.valueOf(data3));
    			wfp.write("\n");
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }
    
    public void put(int x, int data) {
    	if (this.isEnable) {
    		try {
    			wfp.write(String.valueOf(x));
    			wfp.write(" ");
    			wfp.write(String.valueOf(data));
    			wfp.write("\n");
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }
    public void put(int x, int data1, int data2) {
    	if (this.isEnable) {
    		try {
    			wfp.write(String.valueOf(x));
    			wfp.write(" ");
    			wfp.write(String.valueOf(data1));
    			wfp.write(" ");
    			wfp.write(String.valueOf(data2));
    			wfp.write("\n");
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }

}
