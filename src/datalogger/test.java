/*
 * Created on 2005/08/05
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package datalogger;
import ParameterReader.*;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class test {
	public test(String parameterfile) {
		ParameterReader pr = new ParameterReader(parameterfile);
		dataOutput dataout1 = new dataOutput(pr.Reader("logfile1"));
		dataOutput dataout2 = new dataOutput(pr.Reader("logfile2"));		
		for (double x=0; x<10; x+=0.1D) {
			dataout1.put(x, Math.sin(x));
			dataout2.put(x, Math.cos(x));			
		}
		dataout1.close();
		dataout2.close();
	}
	
	public static void main(String[] args) {
		if (args.length<1) {
			System.err.println("Usage test [parameterfilename.xml]");
			System.exit(1);
		}
		new test(args[0]);
	}
}
