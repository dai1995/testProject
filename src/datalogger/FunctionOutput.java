/*
 * Created on 2005/10/11
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
 * gnuplotで、直接関数を書き出すためのもの put(string data)のdataには例えば" exp(-(x-1)*(x-1))と文字列で関数を出力する
 * close()の実行を忘れるとデータが出力されないので注意。
 */
public class FunctionOutput extends multiple_dataOutput {
	boolean is_first = true;
	String command = "plot";
	String output_str;
	
	public FunctionOutput(Node nd) {
		super(nd);
		output_str = null;
		//this.command = dp.getFirst_command();
	}

	public void put(String data) {
		if (this.is_first) {
			this.output_str = this.command + " " + data + "\\\n";
			this.is_first = false;
		}else{
			this.output_str += ", " + data + " \\\n";
		}
	}
	
    public void close() {
    	if (this.isEnable) {
    		try {
    			//System.out.println("outstr = " + this.output_str);
    			if (this.output_str != null) {//なぜか途中でnullになっちゃうことがある
    				wfp.write(this.output_str.toCharArray());
    			}
    			wfp.close();
    			this.output_str = null;//文字列をクリア。
    			this.is_first = true;
    		}catch(IOException ioex) {
    			ioex.printStackTrace();
    		}
    	}
    }
	
}
