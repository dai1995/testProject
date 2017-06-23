/*
 * Created on 2006/11/24
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package DataLoad;


//̤�����ʤΤ����		//̤�����ʤΤ����		//̤�����ʤΤ����
//̤�����ʤΤ����		//̤�����ʤΤ����		//̤�����ʤΤ����
//̤�����ʤΤ����		//̤�����ʤΤ����		//̤�����ʤΤ����
//̤�����ʤΤ����		//̤�����ʤΤ����		//̤�����ʤΤ����
//̤�����ʤΤ����		//̤�����ʤΤ����		//̤�����ʤΤ����
//̤�����ʤΤ����		//̤�����ʤΤ����		//̤�����ʤΤ����
//̤�����ʤΤ����		//̤�����ʤΤ����		//̤�����ʤΤ����
//̤�����ʤΤ����		//̤�����ʤΤ����		//̤�����ʤΤ����

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
	//Path �إå��򳰤�������Ū��Ϳ������ <TestSampleHeadName>conv</TestSampleHeadName>�ˤϥե�����̾���Τ�񤯡�	   
	public ExtendedMultipleDataload2(Node nd, int NumberOfInputs,
			int NumberOfOutputs, String HeadPath) {
		super(nd, NumberOfInputs, NumberOfOutputs, HeadPath);
		//̤�����ʤΤ����
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param nd
	 * @param NumberOfInputs
	 * @param NumberOfOutputs
	 */
	//Path �إå�������ե����뤫�������� 	   <TestSampleHeadName>/home/yamauchi/work...</TestSampleHeadName>�˥ե�ѥ��ǽ�	   
	public ExtendedMultipleDataload2(Node nd, int NumberOfInputs,
			int NumberOfOutputs) {
		super(nd, NumberOfInputs, NumberOfOutputs);
		// TODO Auto-generated constructor stub
	}

	
    //�ƥ��ȥǡ������ɤ߹���(�����Х饤�ɤ���)
    // "d   data1 data2 data3..." �ιԤ��ɤ߹��ब��d���ͤ��ɤ߹��� test_input[index][d][k]�˥ǡ���������
	// �Ĥޤꡢd��ȴ�������äƤ��ɤ��褦�ˤʤäƤ��롣���Υǡ�����ȴ����thread�ǥǡ������Ǥ��Ф�
	//�����Ȥ�����ȯ�������ꡣ
    void readTestData(BufferedReader fp, int index, int data_type, String token_str) throws IOException {
      String line, sub_str;
      int k, o;

      while ((line=fp.readLine()) != null) {
        StringTokenizer tk = new StringTokenizer(line, this.token_str);      	
        sub_str = tk.nextToken();
        try {
        	d = Integer.valueOf(sub_str.trim()).intValue()-1; //�ޤ� d ���ɤ߹���(����ε�§��-1���뤳�Ȥ����)
            for (k = 0; k < this.NumberOfInputs; k++) { //���˥ǡ�����ʬ���ɤ߹���
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
		//̤�����ʤΤ���� �����ѤǤ�!
    	if (this.previous_d < d-1) {
    		for (sd=previous_d+1; sd<=d-1; sd++) {
    			for (int k=0; k<this.NumberOfInputs; k++) {//previous_d�����Ϥ����
    				this.test_input_patterns[index][sd][k] =
        				this.test_desired_patterns[index][previous_d][k];
    			}
    			for (int o=0; o<this.NumberOfOutputs; o++) {//previous_d�ν��Ϥ����
    				this.test_desired_patterns[index][sd][o] =
        				this.test_desired_patterns[index][previous_d][o];
    			}
    		}
    		this.previous_d = d;
    	}
    }
	
}
