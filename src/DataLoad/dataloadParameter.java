/*
 * Created on 2005/08/05
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package DataLoad;

import org.w3c.dom.Node;

import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class dataloadParameter {
	String LearningDataFilename;
	String TestDataFilename;
	String TokenStr=" ";
	String datatype="double";
	String SelectedInputFeatures=null;
	String SelectedOutputFeatures=null;
	int SelectedInputIndex[];
	int SelectedOutputIndex[];	
	double NormalizeValues[];
    int MaxDates=1;
    int NumPracticesPerDay=0;
    int NumTestSamples=0;
    int NumLearningSamples=0;
    int InputDim=0, OutputDim=0;
    int Step=1;
    
    boolean isAddOutputRandom_LearningData = false;//�ؽ��ǡ����ν��Ϥ˥Υ�����ܤ��뤫�ɤ���
    boolean isAddInputRandom_LearningData = false; //�ؽ��ǡ��������Ϥ˥Υ�����ܤ��뤫�ɤ���
    boolean isAddOutputRandom_TestData = false; //�ƥ��ȥǡ����ν��Ϥ˥Υ�����ܤ��뤫�ɤ���
    boolean isAddInputRandom_TestData = false;	//�ƥ��ȥǡ��������Ϥ˥Υ�����ܤ��뤫�ɤ���
    
    long random_seed = 1L;//���ȯ��������ο�������
    double input_random_gain = 0.01D; //[-0.01,0.01] 
    double output_random_gain = 0.01D;//���ϤȽ��Ϥ�ʬ����gain�򥻥åȤǤ���褦�ˤ���
    
    //for multiple dataload
    int NumberOfDataSets;
    String LearningDataFileHeadName;
    String TestDataFileHeadName;
    String Extention;
    
    
    public dataloadParameter() {};
    
	void getParameter(Node node) {
		for (Node ch = node.getFirstChild(); ch != null; ch = ch
				.getNextSibling()) {
			if (ch.getNodeType() == Node.ELEMENT_NODE
					|| ch.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
				if (ch.getNodeName().equals("LearningSampleFile")) {
					//�ؽ�����ץ�ե�����̾
					System.out.println("DataLoad:LearningSampleFile:"
							+ getChildrenValue(ch));
					this.LearningDataFilename = getChildrenValue(ch);
				}else if (ch.getNodeName().equals("TestSampleFile")) {
					//�ƥ��ȥ���ץ�ե�����̾					
					System.out.println("DataLoad:TestSampleFile:"
							+ getChildrenValue(ch));
					this.TestDataFilename = getChildrenValue(ch);
				}else if (ch.getNodeName().equals("MaxDates")) {
					//���粿��ʬ����Τ�
					System.out.println("DataLoad:MaxDate:"
							+ getChildrenValue(ch));
					this.MaxDates = Integer.valueOf(getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("NumberOfPracticePerDay")) {
					//����������γؽ�����ץ��
					System.out.println("DataLoad:NumberOfPracticePerDay:"
							+ getChildrenValue(ch));
					this.NumPracticesPerDay = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("NumberOfTestSamples")) {
					//�ƥ����ѥ���ץ�ο�
					System.out.println("DataLoad:NumberOfTestSamples:"
							+ getChildrenValue(ch));
					this.NumTestSamples = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("NumberOfLearningSamples")) {
					//�ؽ��ѥ���ץ�ο�
					System.out.println("DataLoad:NumberOfLearningSamples:"
							+ getChildrenValue(ch));
					this.NumLearningSamples = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("Token")) {
					//�ȡ�����:�㤨�ж��򥹥ڡ���
					System.out.println("DataLoad:TokenStr:"
							+ getChildrenValue(ch));
					this.TokenStr = this.getChildrenValue(ch);
				}else if (ch.getNodeName().equals("datatype")) {
					//�ǡ����η�
					System.out.println("DataLoad:datatype:"
							+ getChildrenValue(ch));
					this.datatype = this.getChildrenValue(ch);
				}else if (ch.getNodeName().equals("StepSize")) {
					//���Ĥ������ɤ߹���Τ�
					System.out.println("DataLoad:stepsize:"
							+ getChildrenValue(ch));
					this.Step = Integer.valueOf(this.getChildrenValue(ch)).intValue();
// for multiple dataloader					
				}else if (ch.getNodeName().equals("NumberOfInputDim")) {
					//���ϼ�����
					System.out.println("DataLoad:NumberOfInputDim:"
							+ getChildrenValue(ch));
					this.InputDim = Integer.valueOf(this.getChildrenValue(ch)).intValue();
					this.SelectedInputIndex = new int[this.InputDim];
					this.NormalizeValues = new double[this.InputDim];
					for (int i=0; i<this.InputDim; i++) {
						this.SelectedInputIndex[i] = i;// default valut
						this.NormalizeValues[i] = 1D;// default value
					}
				}else if (ch.getNodeName().equals("NumberOfOutputDim")) {
					//���ϼ�����
					System.out.println("DataLoad:NumberOfOutputDim:"
							+ getChildrenValue(ch));
					this.OutputDim = Integer.valueOf(this.getChildrenValue(ch)).intValue();
					this.SelectedOutputIndex = new int[this.OutputDim];
					for (int o=0; o<this.OutputDim; o++) {
						this.SelectedOutputIndex[o] = o;
					}
				}else if (ch.getNodeName().equals("SelectedInputFeatures")) {
					System.out.println("DataLoad:SelectedInputFeatures:"
							+ getChildrenValue(ch));
					this.SelectedInputFeatures = getChildrenValue(ch);
					
					StringTokenizer tk = new StringTokenizer(this.SelectedInputFeatures," ");
					try {
						String substr;
						for (int k=0; k<this.InputDim; k++) {
							substr = tk.nextToken();
							this.SelectedInputIndex[k] = Integer.getInteger(substr).intValue();
						}
					}catch(NoSuchElementException nsex) {
						nsex.printStackTrace();
					}
				}else if (ch.getNodeName().equals("SelectedOutputFeatures")) {
					System.out.println("DataLoad:SelectedOutputFeatures:"
							+ getChildrenValue(ch));
					this.SelectedOutputFeatures = getChildrenValue(ch);
					
					StringTokenizer tk = new StringTokenizer(this.SelectedOutputFeatures," ");
					try {
						String substr;
						for (int k=0; k<this.OutputDim; k++) {
							substr = tk.nextToken();
							this.SelectedOutputIndex[k] = Integer.getInteger(substr).intValue();
						}
					}catch(NoSuchElementException nsex) {
						nsex.printStackTrace();
					}
				}else if (ch.getNodeName().equals("NormalizeValue")) {
					//̤����
					/*System.out.println("DataLoad:NormalizeValue:"
							+ getChildrenValue(ch));
					 this.SelectedInputFeatures = getChildrenValue(ch);*/
					
					/*StringTokenizer tk = new StringTokenizer(this.SelectedInputFeatures," ");
					try {
						String substr;
						for (int k=0; k<this.InputDim; k++) {
							substr = tk.nextToken();
							this.SelectedInputIndex[k] = Integer.getInteger(substr).intValue();
						}
					}catch(NoSuchElementException nsex) {
						nsex.printStackTrace();
					}*/
					
				}else if (ch.getNodeName().equals("NumberOfDatasets")) {
					//�ǡ������åȤο�
					System.out.println("DataLoad:NumberOfDatasets:"
							+ getChildrenValue(ch));
					this.NumberOfDataSets = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("LearningSampleHeadName")) {
					//�ؽ�����ץ�ǡ����ե�����Υإå�̾
					System.out.println("DataLoad:LearningSampleHeadName:"
							+ getChildrenValue(ch));
					if (getChildrenValue(ch).equals("NULL")) this.LearningDataFileHeadName = "";
					else this.LearningDataFileHeadName = getChildrenValue(ch);
				}else if (ch.getNodeName().equals("TestSampleHeadName")) {
					//�ƥ��ȥ���ץ�ե�����Υإå�̾
					System.out.println("DataLoad:TestSampleHeadName:"
							+ getChildrenValue(ch));
					if (getChildrenValue(ch).equals("NULL")) this.TestDataFileHeadName = "";					
					else this.TestDataFileHeadName = getChildrenValue(ch);
				}else if (ch.getNodeName().equals("AddInputRandomLearning")) {
					//���Ϥ˥Υ�����ܤ��뤫�ɤ���
					System.out.println("DataLoad: AddRandom to Input of Leanringdata: " + getChildrenValue(ch));
					this.isAddInputRandom_LearningData = Boolean.valueOf(getChildrenValue(ch));
				}else if (ch.getNodeName().equals("AddOutputRandomLearning")) {
					//���Ϥ˥Υ�����ܤ��뤫�ɤ���
					System.out.println("DataLoad: AddRandom to Input of Leanringdata: " + getChildrenValue(ch));
					this.isAddOutputRandom_LearningData = Boolean.valueOf(getChildrenValue(ch));
				}else if (ch.getNodeName().equals("AddOutputRandomTest")) {
					//���Ϥ˥Υ�����ܤ��뤫�ɤ���
					System.out.println("DataLoad: AddRandom to Output of Test data: " + getChildrenValue(ch));
					this.isAddOutputRandom_TestData = Boolean.valueOf(getChildrenValue(ch));
				}else if (ch.getNodeName().equals("AddInputRandomTest")) {
					//���Ϥ˥Υ�����ܤ��뤫�ɤ���
					System.out.println("DataLoad: AddRandom to Input of Test data: " + getChildrenValue(ch));
					this.isAddInputRandom_TestData = Boolean.valueOf(getChildrenValue(ch));					
				}else if (ch.getNodeName().equals("RandomSeed")) {
					//�Υ�����ܤ����祷������
					System.out.println("DataLoad:RandomSeed:" + getChildrenValue(ch));
					this.random_seed = Long.valueOf(getChildrenValue(ch)).longValue();
				}else if (ch.getNodeName().equals("InputRandomGain")) {
					//�Υ������礭��[-RandomGain, RandomGain]�ΰ�������Ȥʤ�
					System.out.println("DataLoad:InputRandomGain:" + getChildrenValue(ch));
					this.input_random_gain = Double.valueOf(getChildrenValue(ch)).doubleValue();
				}else if (ch.getNodeName().equals("OutputRandomGain")) {
					//�Υ������礭��[-RandomGain, RandomGain]�ΰ�������Ȥʤ�
					System.out.println("DataLoad:OutputRandomGain:" + getChildrenValue(ch));
					this.output_random_gain = Double.valueOf(getChildrenValue(ch)).doubleValue();					
				}else if (ch.getNodeName().equals("Extention")) {
					//��ĥ��
					System.out.println("DataLoad:Extention:"
							+ getChildrenValue(ch));
					this.Extention = getChildrenValue(ch);
				}

			}
		}
	}
				
	String getChildrenValue(Node node) {
		Node snd;
		snd = node.getFirstChild();
		return snd.getNodeValue();
	}

	/**
	 * @return the learningDataFilename
	 */
	public String getLearningDataFilename() {
		return LearningDataFilename;
	}

	/**
	 * @return the testDataFilename
	 */
	public String getTestDataFilename() {
		return TestDataFilename;
	}

	/**
	 * @return the tokenStr
	 */
	public String getTokenStr() {
		return TokenStr;
	}

	/**
	 * @return the datatype
	 */
	public String getDatatype() {
		return datatype;
	}

	/**
	 * @return the selectedInputFeatures
	 */
	public String getSelectedInputFeatures() {
		return SelectedInputFeatures;
	}

	/**
	 * @return the selectedOutputFeatures
	 */
	public String getSelectedOutputFeatures() {
		return SelectedOutputFeatures;
	}

	/**
	 * @return the selectedInputIndex
	 */
	public int[] getSelectedInputIndex() {
		return SelectedInputIndex;
	}

	/**
	 * @return the selectedOutputIndex
	 */
	public int[] getSelectedOutputIndex() {
		return SelectedOutputIndex;
	}

	/**
	 * @return the normalizeValues
	 */
	public double[] getNormalizeValues() {
		return NormalizeValues;
	}

	/**
	 * @return the maxDates
	 */
	public int getMaxDates() {
		return MaxDates;
	}

	/**
	 * @return the numPracticesPerDay
	 */
	public int getNumPracticesPerDay() {
		return NumPracticesPerDay;
	}

	/**
	 * @return the numTestSamples
	 */
	public int getNumTestSamples() {
		return NumTestSamples;
	}

	/**
	 * @return the numLearningSamples
	 */
	public int getNumLearningSamples() {
		return NumLearningSamples;
	}

	/**
	 * @return the inputDim
	 */
	public int getInputDim() {
		return InputDim;
	}

	/**
	 * @return the outputDim
	 */
	public int getOutputDim() {
		return OutputDim;
	}

	/**
	 * @return the step
	 */
	public int getStep() {
		return Step;
	}

	

	/**
	 * @return the isAddOutputRandom_LearningData
	 */
	public boolean isAddOutputRandom_LearningData() {
		return isAddOutputRandom_LearningData;
	}

	/**
	 * @return the isAddInputRandom_LearningData
	 */
	public boolean isAddInputRandom_LearningData() {
		return isAddInputRandom_LearningData;
	}

	/**
	 * @return the isAddOutputRandom_TestData
	 */
	public boolean isAddOutputRandom_TestData() {
		return isAddOutputRandom_TestData;
	}

	/**
	 * @return the isAddInputRandom_TestData
	 */
	public boolean isAddInputRandom_TestData() {
		return isAddInputRandom_TestData;
	}

	/**
	 * @return the random_seed
	 */
	public long getRandom_seed() {
		return random_seed;
	}

	/**
	 * @return the input random_gain
	 */
	public double getInputRandom_gain() {
		return input_random_gain;
	}
	

	/**
	 * @return the output_random_gain
	 */
	public double getOutput_random_gain() {
		return output_random_gain;
	}

	/**
	 * @return the numberOfDataSets
	 */
	public int getNumberOfDataSets() {
		return NumberOfDataSets;
	}

	/**
	 * @return the learningDataFileHeadName
	 */
	public String getLearningDataFileHeadName() {
		return LearningDataFileHeadName;
	}

	/**
	 * @return the testDataFileHeadName
	 */
	public String getTestDataFileHeadName() {
		return TestDataFileHeadName;
	}

	/**
	 * @return the extention
	 */
	public String getExtention() {
		return Extention;
	}
	
	/*����
	 *
	<dataload>
	   <LearningSampleFile>/home/yamauchi/work/datasets/servo/conv0.dat</LearningSampleFile>
	   <TestSampleFile>/home/yamauchi....</TestSampleFile>
	   <MaxDates>10</MaxDates>
	   <Token>,</Token>
	   <datatype>double</datatype>
	   <NumberOfPracticePerDay>10</NumberOfPracticePerDay>
	   <NumberOfTestSamples>10</NumberOfTestSamples>
	   <NumberOfLearningSamples>2000</NumberOfLearningSamples>
	   <StepSize>10</StepSize>
	</dataload>
	
	<multipledataload>
	   <LearningSampleHeadName>/home/yamauchi/work/datasets/servo/conv</LearningSampleHeadName>
	   <TestSampleHeadName>/home/yamauchi/work/datasets/servo/conv</TestSampleHeadName>
	            'NULL'�ǲ���ʤ��إå��Ȥʤ�	   
	   <NumberOfDatasets>50</NumberOfDatasets>
	   <TestSampleFile>/home/yamauchi....</TestSampleFile>
	   <MaxDates>10</MaxDates>
	   <Token>,</Token>
	   <datatype>double</datatype>
	   <NumberOfPracticePerDay>10</NumberOfPracticePerDay>
	   <NumberOfTestSamples>10</NumberOfTestSamples>
	   <NumberOfLearningSamples>2000</NumberOfLearningSamples>
	   <StepSize>10</StepSize>
	   <Extention>.dat</Extention>
	</multipledataload>

	<ext_multipledataload>
		<NumberOfInputDim>1</NumberOfInputDim>
		<NumberOfOutputDim>1</NumberOfOutputDim>
		<SelectedInputFeatures>0</SelectedInputFeatures>		
		<SelectedOutputFeatures>0</SelectedOutputFeatures>
	   <TestSampleHeadName>conv</TestSampleHeadName>	   
	   <NumberOfDatasets>50</NumberOfDatasets>
	   <Token>,</Token>
	   <datatype>double</datatype>
	   <NumberOfTestSamples>1000</NumberOfTestSamples>
	   <StepSize>10</StepSize>
	   <Extention>.dat</Extention>
	</ext_multipledataload>

	 *	 */
	
	
}
