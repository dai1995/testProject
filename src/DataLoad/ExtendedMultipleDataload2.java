/*
 * Created on 2006/11/24
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package DataLoad;


//未完成なので注意		//未完成なので注意		//未完成なので注意
//未完成なので注意		//未完成なので注意		//未完成なので注意
//未完成なので注意		//未完成なので注意		//未完成なので注意
//未完成なので注意		//未完成なので注意		//未完成なので注意
//未完成なので注意		//未完成なので注意		//未完成なので注意
//未完成なので注意		//未完成なので注意		//未完成なので注意
//未完成なので注意		//未完成なので注意		//未完成なので注意
//未完成なので注意		//未完成なので注意		//未完成なので注意

import org.w3c.dom.Node;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ExtendedMultipleDataload2 extends ExtendedMultipleDataload {
    public final int BOOLEAN=0;
    public final int DOUBLE = 1;
    public final int INT = 2;
    int previous_d;

	/**
	 * @param nd
	 * @param NumberOfInputs
	 * @param NumberOfOutputs
	 * @param HeadPath
	 */
	//Path ヘッダを外から明示的に与える場合 <TestSampleHeadName>conv</TestSampleHeadName>にはファイル名本体を書く。	   
	public ExtendedMultipleDataload2(Node nd, int NumberOfInputs,
			int NumberOfOutputs, String HeadPath) {
		super(nd, NumberOfInputs, NumberOfOutputs, HeadPath);
		//未完成なので注意
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param nd
	 * @param NumberOfInputs
	 * @param NumberOfOutputs
	 */
	//Path ヘッダを設定ファイルから得る場合 	   <TestSampleHeadName>/home/yamauchi/work...</TestSampleHeadName>にフルパスで書く	   
	public ExtendedMultipleDataload2(Node nd, int NumberOfInputs,
			int NumberOfOutputs) {
		super(nd, NumberOfInputs, NumberOfOutputs);
		// TODO Auto-generated constructor stub
	}

	
    //テストデータの読み込み(オーバライドする)
    // "d   data1 data2 data3..." の行を読み込むが、dの値を読み込み test_input[index][d][k]にデータを代入
	// つまり、dに抜けがあっても良いようになっている。このデータの抜けはthreadでデータを吐き出さ
	//せたときに頻発する問題。
    void readTestData(BufferedReader fp, int index, int data_type, String token_str) throws IOException {
      String line, sub_str;
      int k, o;

      while ((line=fp.readLine()) != null) {
        StringTokenizer tk = new StringTokenizer(line, this.token_str);      	
        sub_str = tk.nextToken();
        try {
        	d = Integer.valueOf(sub_str.trim()).intValue()-1; //まず d を読み込む(配列の規則上-1することに注意)
            for (k = 0; k < this.NumberOfInputs; k++) { //次にデータ部分を読み込む
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
        }// while line is not null

    }// readTestData()
    
    
    void interpolation(int index, int d) {
    	int sd;
		//未完成なので注意 ここ変です!
    	if (this.previous_d < d-1) {
    		for (sd=previous_d+1; sd<=d-1; sd++) {
    			for (int k=0; k<this.NumberOfInputs; k++) {//previous_dの入力で補間
    				this.test_input_patterns[index][sd][k] =
        				this.test_desired_patterns[index][previous_d][k];
    			}
    			for (int o=0; o<this.NumberOfOutputs; o++) {//previous_dの出力で補間
    				this.test_desired_patterns[index][sd][o] =
        				this.test_desired_patterns[index][previous_d][o];
    			}
    		}
    		this.previous_d = d;
    	}
    }
	
}
