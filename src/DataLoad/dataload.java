/*
 * Created on 2005/08/04
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package DataLoad;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.w3c.dom.Node;
import MyRandom.MyRandom;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class dataload {
    public final int BOOLEAN=0;
    public final int DOUBLE = 1;
    public final int INT = 2;
    public final int IMAGE = 3;
    
    int max_date=0;
    int NumberOfPracticesPerDay=0;
    int NumberOfTestSamples=0;
    int NumberOfLearningSamples=0;
    int NumberOfInputs = 0;
    int NumberOfOutputs = 0;
    String token_str;
    int data_type = this.DOUBLE;
    int Step=1;
    //ノイズを意図的に載せる場合の変数
    java.util.Random R;
    boolean AddInputRandom_LearningData = false;
    boolean AddOutputRandom_LearningData = false;
    boolean AddInputRandom_TestData = false;
    boolean AddOutputRandom_TestData = false;
    double InputRandomVariance = 0;
    double OutputRandomVariance = 0;
    MyRandom MyR;

    String filename;
    public double learning_input_patterns[][][];//[MAX_DATE][PRACTICE_NO][dimension]
    public double learning_desired_patterns[][][];
    public double test_input_patterns[][];//[PRACTICE_NO][dimension]
    public double test_desired_patterns[][];
    int d; //readData()で使われる日付のループ変数。dataLoadでの例外処理に使用するためグローバルにした。

    
    //コンストラクタ1 学習データ テストデータ両方とも読み込む
    public dataload(String learningfilename, String testfilename,
			String datatype, String token_str, 
			int NumberOfInputs, int NumberOfOutputs, 
			int MaxDates, int NumPracticesPerDay, int NumberOfTestSamples, int Step) {

      this.max_date = MaxDates;
      this.NumberOfPracticesPerDay = NumPracticesPerDay;
      this.NumberOfInputs = NumberOfInputs;
      this.NumberOfOutputs = NumberOfOutputs;
      this.token_str = token_str;
      this.Step = Step;
      
      //データタイプを読み込む
      if (datatype.equals("double")) {
        this.data_type = this.DOUBLE;
        System.out.println("dataLoad: datatype is double");
      }else{
        if (datatype.equals("boolean")) {
          this.data_type = this.BOOLEAN;
          System.out.println("dataLoad: datatype is boolean");
        }else{
          if (datatype.equals("integer")) {
            this.data_type = this.INT;
            System.out.println("dataLoad: datatype is integer");
          
          }else{
        	  if (datatype.equals("image")) {
        		  this.data_type = this.IMAGE;
                  System.out.println("dataLoad: datatype is image");        		  
        	  }else{
        		  System.err.println("Invarid data type!!: the datatypes are:");
        		  System.err.println(" integer, double, boolean.");
        		  System.exit(1);
        	  }
          }
        }
      }
      this.filename = learningfilename;
      this.read_learning_data();
      this.filename = testfilename;
      this.read_test_data();
    }

    //コンストラクタ2  XMLよりパラメータを読み込んで簡素化
    public dataload(Node node, int NumberOfInputs, int NumberOfOutputs) {
    	dataloadParameter dp = new dataloadParameter();
    	dp.getParameter(node);
    	this.max_date = dp.getMaxDates();
    	this.NumberOfPracticesPerDay = dp.getNumPracticesPerDay();
    	this.NumberOfInputs = NumberOfInputs;
    	this.NumberOfOutputs = NumberOfOutputs;
    	this.token_str = dp.getTokenStr();
    	this.NumberOfTestSamples = dp.getNumTestSamples();
    	this.Step = dp.getStep();
      
      //データタイプを読み込む
      if (dp.getDatatype().equals("double")) {
        this.data_type = this.DOUBLE;
        System.out.println("dataLoad: datatype is double");
      }else{
        if (dp.getDatatype().equals("boolean")) {
          this.data_type = this.BOOLEAN;
          System.out.println("dataLoad: datatype is boolean");
        }else{
          if (dp.getDatatype().equals("integer")) {
            this.data_type = this.INT;
            System.out.println("dataLoad: datatype is integer");
          
          }else{
          	  if (dp.getDatatype().equals("image")) {
        		  this.data_type = this.IMAGE;
                  System.out.println("dataLoad: datatype is image");        		  
        	  }else{
        		  System.err.println("Invarid data type!!: the datatypes are:");
        		  System.err.println(" integer, double, boolean.");
        		  System.exit(1);
        	  }
          }
        }
      }
      if (dp.isAddInputRandom_LearningData || dp.isAddOutputRandom_LearningData || dp.isAddInputRandom_TestData || dp.isAddOutputRandom_TestData) {
    	  this.R = new java.util.Random();
    	  this.R.setSeed(dp.getRandom_seed());
    	  this.InputRandomVariance = dp.getInputRandom_gain();
    	  this.OutputRandomVariance = dp.getOutput_random_gain();
    	  this.MyR = new MyRandom();
    	  this.AddInputRandom_LearningData = dp.isAddInputRandom_LearningData;
    	  this.AddOutputRandom_LearningData = dp.isAddOutputRandom_LearningData;
    	  this.AddInputRandom_TestData = dp.isAddInputRandom_TestData;
    	  this.AddOutputRandom_TestData = dp.isAddOutputRandom_TestData;
      }
      this.init_function(dp, null);
    }
    
    
    //コンストラクタ3  XMLよりパラメータを読み込んで簡素化 ヘッダパスを明示的に与える
    public dataload(Node node, int NumberOfInputs, int NumberOfOutputs, String head_path) {
    	dataloadParameter dp = new dataloadParameter();
    	dp.getParameter(node);
    	this.max_date = dp.getMaxDates();
    	this.NumberOfPracticesPerDay = dp.getNumPracticesPerDay();
    	this.NumberOfInputs = NumberOfInputs;
    	this.NumberOfOutputs = NumberOfOutputs;
    	this.token_str = dp.getTokenStr();
    	this.NumberOfTestSamples = dp.getNumTestSamples();
    	this.Step = dp.getStep();
      
      //データタイプを読み込む
      if (dp.getDatatype().equals("double")) {
        this.data_type = this.DOUBLE;
        System.out.println("dataLoad: datatype is double");
      }else{
        if (dp.getDatatype().equals("boolean")) {
          this.data_type = this.BOOLEAN;
          System.out.println("dataLoad: datatype is boolean");
        }else{
          if (dp.getDatatype().equals("integer")) {
            this.data_type = this.INT;
            System.out.println("dataLoad: datatype is integer");
          
          }else{
        	System.err.println("Invarid data type!!: the datatypes are:");
        	System.err.println(" integer, double, boolean.");
        	System.exit(1);
          }
        }
      }
      if (dp.isAddInputRandom_LearningData || dp.isAddOutputRandom_LearningData || dp.isAddInputRandom_TestData || dp.isAddOutputRandom_TestData) {
    	  this.R = new java.util.Random();
    	  this.R.setSeed(dp.getRandom_seed());
    	  this.InputRandomVariance = dp.getInputRandom_gain();
    	  this.OutputRandomVariance = dp.getOutput_random_gain();
    	  this.MyR = new MyRandom();
    	  this.AddInputRandom_LearningData = dp.isAddInputRandom_LearningData;
    	  this.AddOutputRandom_LearningData = dp.isAddOutputRandom_LearningData;
    	  this.AddInputRandom_TestData = dp.isAddInputRandom_TestData;
    	  this.AddOutputRandom_TestData = dp.isAddOutputRandom_TestData;
      }      
      this.init_function(dp, head_path);
    }
    //コンストラクタ3  XMLよりパラメータを読み込んで簡素化 ヘッダパスを明示的に与える
    //ID_strは並行処理するシミュレータのログがバッティングしないようにするための追加文字列
    public dataload(Node node, int NumberOfInputs, 
    		int NumberOfOutputs, String head_path, String ID_str) {
    	dataloadParameter dp = new dataloadParameter();
    	dp.getParameter(node);
    	this.max_date = dp.getMaxDates();
    	this.NumberOfPracticesPerDay = dp.getNumPracticesPerDay();
    	this.NumberOfInputs = NumberOfInputs;
    	this.NumberOfOutputs = NumberOfOutputs;
    	this.token_str = dp.getTokenStr();
    	this.NumberOfTestSamples = dp.getNumTestSamples();
    	this.Step = dp.getStep();
      
      //データタイプを読み込む
      if (dp.getDatatype().equals("double")) {
        this.data_type = this.DOUBLE;
        System.out.println("dataLoad: datatype is double");
      }else{
        if (dp.getDatatype().equals("boolean")) {
          this.data_type = this.BOOLEAN;
          System.out.println("dataLoad: datatype is boolean");
        }else{
          if (dp.getDatatype().equals("integer")) {
            this.data_type = this.INT;
            System.out.println("dataLoad: datatype is integer");
          
          }else{
        	System.err.println("Invarid data type!!: the datatypes are:");
        	System.err.println(" integer, double, boolean.");
        	System.exit(1);
          }
        }
      }
      if (dp.isAddInputRandom_LearningData || dp.isAddOutputRandom_LearningData || dp.isAddInputRandom_TestData || dp.isAddOutputRandom_TestData) {
    	  this.R = new java.util.Random();
    	  this.R.setSeed(dp.getRandom_seed());
    	  this.InputRandomVariance = dp.getInputRandom_gain();
    	  this.MyR = new MyRandom();
    	  this.AddInputRandom_LearningData = dp.isAddInputRandom_LearningData;
    	  this.AddOutputRandom_LearningData = dp.isAddOutputRandom_LearningData;
    	  this.AddInputRandom_TestData = dp.isAddInputRandom_TestData;
    	  this.AddOutputRandom_TestData = dp.isAddOutputRandom_TestData;
      }      
      this.init_function(dp, head_path, ID_str);
    }    
    //コンストラクタ4  XMLよりパラメータを読み込んで簡素化 ヘッダパスを明示的に与える
    //入力次元数、出力次元数をこのパラメータファイルより読む。
    public dataload(Node node) {
    	dataloadParameter dp = new dataloadParameter();
    	dp.getParameter(node);
    	this.max_date = dp.getMaxDates();
    	this.NumberOfPracticesPerDay = dp.getNumPracticesPerDay();
    	this.NumberOfInputs = dp.getInputDim();
    	this.NumberOfOutputs = dp.getOutputDim();
    	this.token_str = dp.getTokenStr();
    	this.NumberOfTestSamples = dp.getNumTestSamples();
    	this.Step = dp.getStep();
      
      //データタイプを読み込む
      if (dp.getDatatype().equals("double")) {
        this.data_type = this.DOUBLE;
        System.out.println("dataLoad: datatype is double");
      }else{
        if (dp.getDatatype().equals("boolean")) {
          this.data_type = this.BOOLEAN;
          System.out.println("dataLoad: datatype is boolean");
        }else{
          if (dp.getDatatype().equals("integer")) {
            this.data_type = this.INT;
            System.out.println("dataLoad: datatype is integer");
          
          }else{
        	System.err.println("Invarid data type!!: the datatypes are:");
        	System.err.println(" integer, double, boolean.");
        	System.exit(1);
          }
        }
      }
      if (dp.isAddInputRandom_LearningData || dp.isAddOutputRandom_LearningData || dp.isAddInputRandom_TestData || dp.isAddOutputRandom_TestData) {
    	  this.R = new java.util.Random();
    	  this.R.setSeed(dp.getRandom_seed());
    	  this.InputRandomVariance = dp.getInputRandom_gain();
    	  this.MyR = new MyRandom();
    	  this.AddInputRandom_LearningData = dp.isAddInputRandom_LearningData;
    	  this.AddOutputRandom_LearningData = dp.isAddOutputRandom_LearningData;
    	  this.AddInputRandom_TestData = dp.isAddInputRandom_TestData;
    	  this.AddOutputRandom_TestData = dp.isAddOutputRandom_TestData;
      }      
      this.init_function(dp, null);
    }
 

	//head_pathは明示的に与えたいときのみ与える。それ以外はnullを与える。
    void init_function(dataloadParameter dp, String head_path) {
    	if (head_path==null) {
    		this.filename = dp.LearningDataFilename;
    	}else{
    		this.filename = head_path + dp.LearningDataFilename;
    	}
    	
        this.read_learning_data();
        this.filename = dp.TestDataFilename;
        this.read_test_data();
    }
    
    void init_function(dataloadParameter dp, String head_path, String ID_str) {
    	if (head_path==null) {
    		this.filename = dp.LearningDataFilename;
    		if (ID_str != null) {
    			this.filename += ID_str;
    		}
    	}else{
    		this.filename = head_path + dp.LearningDataFilename;
    		if (ID_str != null) {
    			this.filename += ID_str;
    		}
    	}
    	
        this.read_learning_data();
        this.filename = dp.TestDataFilename;
        this.read_test_data();
    }    
    void read_learning_data() {
        System.out.println("dataLoad: Learning data filename is " + filename);
        this.learning_input_patterns = new double[this.max_date][this.NumberOfPracticesPerDay][this.NumberOfInputs];
        this.learning_desired_patterns = new double[this.max_date][this.NumberOfPracticesPerDay][this.NumberOfOutputs];
        try {
          BufferedReader fp = new BufferedReader(new FileReader(filename));
          if (data_type == IMAGE) {
        	  this.readLearningImageData(fp, token_str);
          }else{
        	  readLearningData(fp, data_type, token_str);
          }
          //System.out.println("Learning data reader: totalnumber = " + share.TestNum + "days " + share.MaxDates);
        }catch(IOException ioe) {
          System.err.println("datafile " + filename);
          ioe.printStackTrace();
        }
    }
    
    void read_test_data() {
        this.test_input_patterns = new double[this.NumberOfTestSamples][this.NumberOfInputs];
        this.test_desired_patterns = new double[this.NumberOfTestSamples][this.NumberOfOutputs];
        try {
          System.out.println("Test data filename is " + filename);
          BufferedReader fp = new BufferedReader(new FileReader(filename));
          if (data_type == IMAGE) {
        	  this.readTestImageData(fp, token_str);
          }else{
        	  readTestData(fp, data_type, token_str);
          }
          //System.out.println("Test Pattern Reader: totalnumber = " + share.TestNum + "days " + share.MaxDates);
        }catch(IOException ioe) {
          System.err.print(filename + " cannot be opend!");
          ioe.printStackTrace();
        }
    }

    //学習データの読み込み
    void readLearningData(BufferedReader fp, int data_type, String token_str) throws IOException {
      String line, sub_str;
      int p, k, o;
      this.NumberOfLearningSamples=0;      
      for (d=0; d<this.max_date; d++) {
        //System.out.println("date = " + d);
        for (p=0; p<this.NumberOfPracticesPerDay; p++) {
          for (int s=0; s<this.Step-1; s++) {
          	fp.readLine(); //読み飛ばし
          }
          if ((line = fp.readLine()) == null) {
            this.max_date = d+1; //日付の最大値をdにセットする。(データサイズに合わせる)
            break;
          }
          //System.out.println(line.toString());
          if (line.toString().getBytes()[0] != '#') {
          	this.NumberOfLearningSamples++;
          	StringTokenizer tk = new StringTokenizer(line, this.token_str);
          	try {
          		//入力側の読み込み
          		for (k=0; k<this.NumberOfInputs; k++) {
          			sub_str = tk.nextToken();
          			//System.out.println(sub_str.trim());
          			switch(data_type) {
          			case BOOLEAN:
          				if (Boolean.valueOf(sub_str.trim()).booleanValue()) {
          					learning_input_patterns[d][p][k] = 0.5D;
          				}else{
          					learning_input_patterns[d][p][k] = -0.5D;
          				}
          				break;
          			case DOUBLE:
          				learning_input_patterns[d][p][k] =
          					Double.valueOf(sub_str.trim()).doubleValue();
          				if (this.AddInputRandom_LearningData) {
          					learning_input_patterns[d][p][k] += this.getRandomValue(0, this.InputRandomVariance);
          				}
          				System.out.print(" " + learning_input_patterns[d][p][k]);
          				break;
          			case INT:
          				learning_input_patterns[d][p][k] =
                      	Double.valueOf(sub_str.trim()).intValue();
          				break;
          			}
          			//System.out.print(" : " + learning_input_patterns[d][p][k]);
          		}/* for k */
          		//System.out.println(" ");
          		//出力側の読み込み
          		for (o=0; o<this.NumberOfOutputs; o++) {
          			sub_str = tk.nextToken();
          			switch(data_type) {
          			case BOOLEAN:
          				if (Boolean.valueOf(sub_str.trim()).booleanValue()) {
          					learning_desired_patterns[d][p][o] = 0.5D;
          				}else{
          					learning_desired_patterns[d][p][o] = -0.5D;
          				}
          				break;
          			case DOUBLE:
          				learning_desired_patterns[d][p][o] = Double.valueOf(sub_str.trim()).doubleValue();
          				if (this.AddOutputRandom_LearningData) {
          					learning_desired_patterns[d][p][o] += this.getRandomValue(0, this.OutputRandomVariance);
          				}
          				System.out.print(" " + learning_input_patterns[d][p][o]);
          				break;
          			case INT:
          				learning_desired_patterns[d][p][o] = Integer.valueOf(sub_str.trim()).intValue();
          				break;
          			}
          			//System.out.print(" : " + learning_desired_patterns[d][p][o]);
          		}
          		System.out.println("\n");
          	}catch(NoSuchElementException ex) {
          		ex.printStackTrace();
          	}
          }
        }
      }
    }
    
    String getNextDataStr(BufferedReader fp, StringTokenizer sk,  String token_str, String line) {

    	if (sk==null) {
    		if (line != null) {
    			sk = new StringTokenizer(line, this.token_str);
    		}else{
    	   		do {
        			try {
						line = fp.readLine();
					} catch (IOException e) {
						e.printStackTrace();
						return null;
					}
        		}while(line.toString().getBytes()[0] != '#' && line != null);
        		if (line == null) return null;
        		sk = new StringTokenizer(line, this.token_str);
    		}
    	}
    	try {
    		return sk.nextToken();
    	}catch(NoSuchElementException ex) {
       		do {
    			try {
					line = fp.readLine();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
    		}while(line.toString().getBytes()[0] != '#' && line != null);
    		if (line == null) return null;
    		sk = new StringTokenizer(line, token_str);
    		return sk.nextToken();
    	}
    }

  //学習データの読み込み
    void readLearningImageData(BufferedReader fp, String token_str) throws IOException {
      String line=null, sub_str=null;
      StringTokenizer sk=null;
      int p, k, o;
      this.NumberOfLearningSamples=0;      
      for (d=0; d<this.max_date; d++) {
        //System.out.println("date = " + d);
        for (p=0; p<this.NumberOfPracticesPerDay; p++) {
      		for (k=0; k<this.NumberOfInputs; k++) {
       			if ((sub_str = this.getNextDataStr(fp, sk, token_str, line))==null) break;
       			learning_input_patterns[d][p][k] =
       				Double.valueOf(sub_str.trim()).doubleValue();
       			if (this.AddInputRandom_LearningData) {
       				learning_input_patterns[d][p][k] += this.getRandomValue(0, this.InputRandomVariance);
       			}
       			System.out.print(" " + learning_input_patterns[d][p][k]);
      		}/* for input */
      		for (o=0; o<this.NumberOfOutputs; o++) {      		
       			if ((sub_str = this.getNextDataStr(fp, sk, token_str, line))==null) break;
          		//System.out.println(" ");
          		//出力側の読み込み

       			learning_desired_patterns[d][p][o] = Double.valueOf(sub_str.trim()).doubleValue();
       			if (this.AddOutputRandom_LearningData) {
       				learning_desired_patterns[d][p][o] += this.getRandomValue(0, this.OutputRandomVariance);
       			}
       			System.out.print(" " + learning_input_patterns[d][p][o]);
      		}// for output
        }// for p
      }// for date
    }
    //学習データの読み込み
    void readTestImageData(BufferedReader fp, String token_str) throws IOException {
      String line=null, sub_str=null;
      StringTokenizer sk=null;
      int p, k, o;
      this.NumberOfLearningSamples=0;      

        //System.out.println("date = " + d);
        for (p=0; p<this.NumberOfPracticesPerDay; p++) {
      		for (k=0; k<this.NumberOfInputs; k++) {
       			if ((sub_str = this.getNextDataStr(fp, sk, token_str, line))==null) break;
       			this.test_input_patterns[p][k] =
       				Double.valueOf(sub_str.trim()).doubleValue();
       			if (this.AddInputRandom_TestData) {
       				test_input_patterns[p][k] += this.getRandomValue(0, this.InputRandomVariance);
       			}
       			System.out.print(" " + test_input_patterns[p][k]);
      		}/* for input */
      		for (o=0; o<this.NumberOfOutputs; o++) {      		
       			if ((sub_str = this.getNextDataStr(fp, sk, token_str, line))==null) break;
          		//System.out.println(" ");
          		//出力側の読み込み

       			test_desired_patterns[p][o] = Double.valueOf(sub_str.trim()).doubleValue();
       			if (this.AddOutputRandom_TestData) {
       				test_desired_patterns[p][o] += this.getRandomValue(0, this.OutputRandomVariance);
       			}
       			System.out.print(" " + test_input_patterns[p][o]);
      		}// for output
        }// for p

    }
     
    //テストデータの読み込み
    void readTestData(BufferedReader fp, int data_type, String token_str) throws IOException {
      String line, sub_str;
      int k, o;
      for (d=0; d<this.NumberOfTestSamples; d++) {
        for (int s=0; s<this.Step-1; s++) {
          	fp.readLine(); //読み飛ばし
        }
      	
        if ((line = fp.readLine()) == null) {
          this.NumberOfTestSamples = d+1; //最大値をdにセットする。(データサイズに合わせる)
          //System.out.println("========================Test Data Size is " + this.NumberOfTestSamples);
        }else{
          StringTokenizer tk = new StringTokenizer(line, this.token_str);
          try {
            for (k = 0; k < this.NumberOfInputs; k++) {
              sub_str = tk.nextToken();
              switch (data_type) {
                case BOOLEAN:
                  if (Boolean.valueOf(sub_str.trim()).booleanValue()) {
                    test_input_patterns[d][k] = 0.5D;
                  }
                  else {
                    test_input_patterns[d][k] = -0.5D;
                  }

                  break;
                case DOUBLE:
                  test_input_patterns[d][k] = Double.valueOf(sub_str.trim()).
                      doubleValue();
                  if (this.AddInputRandom_TestData) {
                	  test_input_patterns[d][k] += this.getRandomValue(0, this.InputRandomVariance);
                  }
                  break;
                case INT:
                  test_input_patterns[d][k] = Integer.valueOf(sub_str.trim()).intValue();
                  break;
              }
              //System.out.print( " : " + test_input_patterns[d][k]);
            }//for k

            //System.out.println(" ");
            for (o = 0; o < this.NumberOfOutputs; o++) {
              sub_str = tk.nextToken();
              switch (data_type) {
                case BOOLEAN:
                  if (Boolean.valueOf(sub_str.trim()).booleanValue()) {
                    test_desired_patterns[d][o] = 0.5D;
                  }
                  else {
                    test_desired_patterns[d][o] = -0.5D;
                  }

                  break;
                case DOUBLE:
                  test_desired_patterns[d][o] = Double.valueOf(sub_str.trim()).
                      doubleValue();
                  if (this.AddOutputRandom_TestData) {
                	  test_desired_patterns[d][o] += this.getRandomValue(0, this.OutputRandomVariance);
                  }
                  break;
                case INT:
                  test_desired_patterns[d][o] = Integer.valueOf(sub_str.trim()).
                      intValue();
                  break;
              }
              //System.out.print(" -> " + test_desired_patterns[d][o]);
            }
            //System.out.println("\n");
          }catch (NoSuchElementException ex) {
            ex.printStackTrace();
          }
        }// fi line is not null

      }//for d
    }// readTestData()
    
    boolean is_divide_able(int a, int b) {
    	if ((a-(a/b)*b)==0) {
    		return true;
    	}else{
    		return false;
    	}
    }
    
    public int getActualNumberOfTestSamples() {
    	return this.NumberOfTestSamples;
    }
    
    public int getMaxDates() {
    	return this.max_date;
    }
    
    public int getNumberOfPracticePerDay() {
    	return this.NumberOfPracticesPerDay;
    }
    
    public int getActualNumberOfLearningSamples() {
    	return this.NumberOfLearningSamples;
    }
    
    public double getRandomValue(double mean_value, double variance) {
    	double result = variance * MyR.nrnd(this.R) + mean_value;
    	System.out.println("dataload.getRandomValue():random=" + result);
    	return result;
    }

    /**
 	 * @return the numberOfInputs
 	 */
 	public int getNumberOfInputs() {
 		return NumberOfInputs;
 	}

 	/**
 	 * @return the numberOfOutputs
 	 */
 	public int getNumberOfOutputs() {
 		return NumberOfOutputs;
 	}

	/**
	 * @param inputRandomVariance the inputRandomVariance to set
	 */
	public void setInputRandomVariance(double inputRandomVariance) {
		InputRandomVariance = inputRandomVariance;
	}

	/**
	 * @param outputRandomVariance the outputRandomVariance to set
	 */
	public void setOutputRandomVariance(double outputRandomVariance) {
		OutputRandomVariance = outputRandomVariance;
	}	
 	
}
