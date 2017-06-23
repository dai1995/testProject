package datalogger;
import java.io.*;
import org.w3c.dom.Node;


public class cellNumberOutput {
    FileWriter wfp;
    String delimiter = " ";
    datalogger_parameter dp;
    boolean isEnable=true;

    public cellNumberOutput(Node nd) {
    	dp = new datalogger_parameter();
    	dp.getParameter(nd);
    	if (dp.filename.equals("NC")) { //ファイル名に"NC"が入るとなにもしない！　ログファイルの抑制に使える2008/12/24
    		this.isEnable = false;
    	}
    	
    	if (this.isEnable) {
    		try {
    			wfp = new FileWriter(dp.filename);
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }
    //If you want to divide the shared log filename, please add ID_str.
    //Then, the corresponding filename is changed to ID_str + dp.filename.
    //This can be used for using parallel computing of the test.
    public cellNumberOutput(Node nd, String ID_str) {
    	String filename;
    	dp = new datalogger_parameter();
    	dp.getParameter(nd);
    	if (dp.filename.equals("NC")) { //ファイル名に"NC"が入るとなにもしない！　ログファイルの抑制に使える2008/12/24
    		this.isEnable = false;
    	}
    	filename = dp.filename + ID_str;
    	if (this.isEnable) {
    		try {
    			wfp = new FileWriter(filename);
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }    
    public cellNumberOutput(String filename) {
    	if (this.isEnable) {
    		try {
    			wfp = new FileWriter(filename);
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }

    public void put(int timeIndex, int numberOfCells) {
    	if (this.isEnable) {
    		try {
    			wfp.write(String.valueOf(timeIndex));
    			wfp.write(this.delimiter);
    			wfp.write(String.valueOf(numberOfCells));
    			wfp.write("\n");
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }
    public void put(int timeIndex, double numberOfCells) {
    	if (this.isEnable) {
    		try {
    			wfp.write(String.valueOf(timeIndex));
    			wfp.write(this.delimiter);
    			wfp.write(String.valueOf(numberOfCells));
    			wfp.write("\n");
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }

    public void put(double Index, double numberOfCells) {
    	if (this.isEnable) {
    		try {
    			wfp.write(String.valueOf(Index));
    			wfp.write(this.delimiter);
    			wfp.write(String.valueOf(numberOfCells));
    			wfp.write("\n");
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }
    
    public void set_delimiter(String new_delimiter) {
    	this.delimiter = new_delimiter;
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
