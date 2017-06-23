/*
 * Created on 2005/08/10
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
public class multiple_dataOutput {
	String head_filename;
    FileWriter wfp;
    int NumberOfSegment = 0;
    boolean isEnable = true;
    
	public multiple_dataOutput(Node nd) {
    	datalogger_parameter dp = new datalogger_parameter();
    	dp.getParameter(nd);
    	head_filename = dp.filename;
    	if (head_filename.equals("NC")) this.isEnable = false;
    	init_additional_parameter(dp);
	}
	
	// If you  want to use ID_str, please use this constructor.
	// Note: ID_str is used for dividing the log file from the other parallel executed program's log files.
	public multiple_dataOutput(Node nd, String ID_str) {
    	datalogger_parameter dp = new datalogger_parameter();
    	dp.getParameter(nd);
    	this.Log("multiple_dataOutput(Node, String) ");
    	head_filename = dp.filename + ID_str;
    	this.Log("multiple_dataOutput() head_file_name = " + head_filename);
    	if (head_filename.contains("NC")) this.isEnable = false;
    	init_additional_parameter(dp);
	}
	
	void Log(String log) {
		System.out.println("multiple_dataOutput." + log);
	}
	
	void init_additional_parameter(datalogger_parameter dp) {
		//���̥��饹�ǥ����С��饤��
	}
	
	public void open() {
		if (this.isEnable) {
			String filename = this.head_filename + this.NumberOfSegment + ".dat";
			try {
				wfp = new FileWriter(filename);
				this.NumberOfSegment ++;
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
    
       
    //���顼�С�
    public void put(double x, double y, double ylow, double yhigh) {
    	if (this.isEnable) {
    		try {
    			wfp.write(String.valueOf(x));
    			wfp.write(" ");
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
    
    public void put(String str, double data) {
    	if (this.isEnable) {
    		try {
    			wfp.write(str);
    			wfp.write(" ");
    			wfp.write(String.valueOf(data));
    			wfp.write("\n");
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		
    		}
    	}
    }    
    
    public void put(double x){
    	if (this.isEnable) {
    		try {
    			wfp.write(String.valueOf(x));
    			wfp.write("\n");
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }
    
    public void put(String str) {
    	if (this.isEnable) {
    		try {
    			wfp.write("#"+str);
    			wfp.write(" ");
    			wfp.write("\n");
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		
    		}
    	}
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
    
    public void put(double x[], double data[]) {
    	if (this.isEnable) {
    		try {
    			for (int i=0; i<x.length; i++) {
    				wfp.write(String.valueOf(x[i]));
    				wfp.write(" ");
    			}
    			for (int j=0; j<data.length; j++) {
    				wfp.write(String.valueOf(data[j]));
    				wfp.write(" ");
    			}
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
    
    public void put(double x[][]) {
    	if (this.isEnable) {
    		try {
    			for(int n = 0; n < x.length; n++){
    				
    				for (int i=0; i<x[n].length; i++) {
    					wfp.write(String.valueOf(x[n][i]));
    					wfp.write(" ");
    				}
    				wfp.write("\n");
    			}
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }
    
    public void close() {
    	if (this.isEnable) {
    		try {
    			wfp.close();
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }
}
