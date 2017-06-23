package datalogger;
import java.io.*;
import org.w3c.dom.Node;

public class errorOutput extends cellNumberOutput {
    public errorOutput(String filename) {
	super(filename);
    }
    
    public errorOutput(Node nd) {
    	super(nd);
    }
    public errorOutput(Node nd, String ID_str) {
    	super(nd, ID_str);
    }

    public void put(int timeIndex, double error) {
    	if (this.isEnable) {
    		try {
    			wfp.write(String.valueOf(timeIndex));
    			wfp.write(" ");
    			wfp.write(String.valueOf(error));
    			wfp.write("\n");
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }
}
