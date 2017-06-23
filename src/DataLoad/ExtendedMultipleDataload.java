/*
 * Created on 2005/08/19
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package DataLoad;

import org.w3c.dom.Node;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExtendedMultipleDataload extends MultipleDataload {
    public final int BOOLEAN=0;
    public final int DOUBLE = 1;
    public final int INT = 2;
	
	String HeadPath;
	String Head_filename;
	dataloadParameter dp;
	public double[][][] test_input_patterns;
	public double[][][] test_desired_patterns;
	
	//Path ヘッダを外から明示的に与える場合 <TestSampleHeadName>conv</TestSampleHeadName>にはファイル名本体を書く。	   
	public ExtendedMultipleDataload(Node nd, int NumberOfInputs, int NumberOfOutputs, String HeadPath) {
		super(nd, NumberOfInputs, NumberOfOutputs, HeadPath);
		System.out.println("constructor 1 header path is " + HeadPath);		
		this.HeadPath = HeadPath;
	}
	
	public ExtendedMultipleDataload(Node nd, int NumberOfInputs, int NumberOfOutputs, String HeadPath, String ID_str) {
		super(nd, NumberOfInputs, NumberOfOutputs, HeadPath, ID_str);
		this.HeadPath = HeadPath;
	}	
	//Path ヘッダを設定ファイルから得る場合 	   <TestSampleHeadName>/home/yamauchi/work...</TestSampleHeadName>にフルパスで書く	   
	public ExtendedMultipleDataload(Node nd, int NumberOfInputs, int NumberOfOutputs) {
		super(nd, NumberOfInputs, NumberOfOutputs);
		//System.out.println("header path is null");
		//this.HeadPath = null;
	}
	
    void init_function(dataloadParameter dp, String header_path) {
    	
    	this.dp = dp;
    	System.out.println("init function header_path is " + header_path);
    	if (header_path!=null) {
    		this.Head_filename = header_path + dp.TestDataFileHeadName;
    	}else{
    		this.Head_filename = dp.TestDataFileHeadName;
    	}

        this.extention = dp.Extention;
        this.NumberOfDataSets = dp.NumberOfDataSets;
        this.test_input_patterns = new double[this.NumberOfDataSets][this.NumberOfTestSamples][this.NumberOfInputs];
        this.test_desired_patterns = new double[this.NumberOfDataSets][this.NumberOfTestSamples][this.NumberOfOutputs];
        for (int i=0; i<this.NumberOfDataSets; i++) {
        	this.read_test_data(i);
        }
    }
    
   void init_function(dataloadParameter dp, String header_path, String ID_str) {
    	this.dp = dp;
    	this.Log("init_function() with ID_str");

    	if (header_path!=null) {
    		this.Head_filename = header_path + dp.TestDataFileHeadName;
    	}else{
    		this.Head_filename = dp.TestDataFileHeadName;
    	}
    	
		if (ID_str != null) {
			this.Head_filename += ID_str;
		}
		
        this.extention = dp.Extention;
        this.NumberOfDataSets = dp.NumberOfDataSets;
        this.test_input_patterns = new double[this.NumberOfDataSets][this.NumberOfTestSamples][this.NumberOfInputs];
        this.test_desired_patterns = new double[this.NumberOfDataSets][this.NumberOfTestSamples][this.NumberOfOutputs];
        for (int i=0; i<this.NumberOfDataSets; i++) {
        	this.read_test_data(i);
        }
    }    

    void read_test_data(int i) {
        try {
        	this.filename = this.Head_filename + i + this.dp.Extention;
        	this.Log("read_test_data(" + i + ") filename=" + this.filename);
        	BufferedReader fp = new BufferedReader(new FileReader(filename));
        	readTestData(fp, i, data_type, token_str);
        	fp.close();
          System.out.println("Test Pattern Reader: totalnumber");
        }catch(IOException ioe) {
          System.err.print(filename + " cannot be opend!");
          ioe.printStackTrace();
        }
    }
    
    //テストデータの読み込み
    void readTestData(BufferedReader fp, int index, int data_type, String token_str) throws IOException {
      String line, sub_str;
      int k, o;
      for (d=0; d<this.NumberOfTestSamples; d++) {
        for (int s=0; s<this.Step-1; s++) {
          	fp.readLine(); //読み飛ばし
        }
        if ((line = fp.readLine()) == null) {
          this.NumberOfTestSamples = d+1; //最大値をdにセットする。(データサイズに合わせる)
          System.out.println("========================Test Data Size is " + this.NumberOfTestSamples);
        }else{
        	System.out.println(line.toString());
        	if (line.toString().getBytes()[0] != '#') {        	
        		StringTokenizer tk = new StringTokenizer(line, this.token_str);
        		try {
        			for (k = 0; k < this.NumberOfInputs; k++) {
        				sub_str = tk.nextToken();
        				switch (data_type) {
        				case BOOLEAN:
        					if (Boolean.valueOf(sub_str.trim()).booleanValue()) {
        						this.test_input_patterns[index][d][k] = 0.5D;
        					}
        					else {
        						this.test_input_patterns[index][d][k] = -0.5D;
        					}

        					break;
        				case DOUBLE:
        					this.test_input_patterns[index][d][k] = Double.valueOf(sub_str.trim()).
							doubleValue();
        					break;
        				case INT:
        					this.test_input_patterns[index][d][k] = Integer.valueOf(sub_str.trim()).intValue();
        					break;
        				}

        			}//for k

        			//System.out.println(" ");
        			for (o = 0; o < this.NumberOfOutputs; o++) {
        				sub_str = tk.nextToken();
        				switch (data_type) {
        				case BOOLEAN:
        					if (Boolean.valueOf(sub_str.trim()).booleanValue()) {
        						this.test_desired_patterns[index][d][o] = 0.5D;
        					}
        					else {
        						this.test_desired_patterns[index][d][o] = -0.5D;
        					}
        					break;
        				case DOUBLE:
        					this.test_desired_patterns[index][d][o] = Double.valueOf(sub_str.trim()).
							doubleValue();
        					break;
        				case INT:
        					this.test_desired_patterns[index][d][o] = Integer.valueOf(sub_str.trim()).
							intValue();
        					break;
        				}
        				//System.out.print(" -> " + test_desired_patterns[d][o]);
        			}
            
        			//System.out.println("\n");
        		}catch (NoSuchElementException ex) {
        			ex.printStackTrace();
        		}
        	}else{
        		d--;
        	}
        }// fi line is not null

      }//for d
    }// readTestData()
	
    boolean equalToPreviousData(double[] current, double[] previous) {
    	for (int i=0; i<current.length; i++) {
    		if (current[i] != previous[i]) return false;
    	}
    	return true;
    }
}
