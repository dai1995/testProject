package DataLoad;

import org.w3c.dom.Node;

import ParameterReader.Parameters;

public class MultipleDataload4CrossValidationParameters implements Parameters {
	private String LearningDataFileHeadName=null;
	private int NumberOfSamples=0;
	private int MaxDates=0;
	private int NumberOfPracticePerDay=0;
	private int NumberOfFolds=2;
	private String TokenStr = " ";
	private String datatype = "int";
	private int InputDim, OutputDim;
	private int NumberOfDataSets;
	private String Extention = ".dat";
	
	@Override
	public void getParameter(Node node) {
		// TODO Auto-generated method stub
		for (Node ch = node.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
			if (ch.getNodeType() == Node.ELEMENT_NODE
			|| ch.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
				if (ch.getNodeName().equals("MaxDates")) {
					//最大何日分あるのか
					System.out.println("DataLoad:MaxDate:"
							+ getChildrenValue(ch));
					this.MaxDates = Integer.valueOf(getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("NumberOfFolds")) {
					//１日あたりの学習サンプル数
					System.out.println("DataLoad:NumberOfFolds:"
							+ getChildrenValue(ch));
					this.NumberOfFolds = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("NumberOfSamples")) {
					//テスト用サンプルの数
					System.out.println("DataLoad:NumberOfSamples:"
							+ getChildrenValue(ch));
					this.NumberOfSamples = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("Token")) {
					//トークン:例えば空白スペース
					System.out.println("DataLoad:TokenStr:"
							+ getChildrenValue(ch));
					this.TokenStr = this.getChildrenValue(ch);
				}else if (ch.getNodeName().equals("datatype")) {
					//データの型
					System.out.println("DataLoad:datatype:"
							+ getChildrenValue(ch));
					this.datatype = this.getChildrenValue(ch);
					//for multiple dataloader					
				}else if (ch.getNodeName().equals("NumberOfInputDim")) {
					//入力次元数
					System.out.println("DataLoad:NumberOfInputDim:"
							+ getChildrenValue(ch));
					this.InputDim = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("NumberOfOutputDim")) {
					//出力次元数
					System.out.println("DataLoad:NumberOfOutputDim:"
							+ getChildrenValue(ch));
					this.OutputDim = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("NumberOfDatasets")) {
					//データセットの数
					System.out.println("DataLoad:NumberOfDatasets:"
							+ getChildrenValue(ch));
					this.NumberOfDataSets = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("LearningSampleHeadName")) {
					//学習サンプルデータファイルのヘッド名
					System.out.println("DataLoad:LearningSampleHeadName:"
							+ getChildrenValue(ch));
					if (getChildrenValue(ch).equals("NULL")) this.LearningDataFileHeadName = "";
					else this.LearningDataFileHeadName = getChildrenValue(ch);
				}else if (ch.getNodeName().equals("Extention")) {
					System.out.println("DataLoad:Extention:"
						+ getChildrenValue(ch));
					this.Extention = getChildrenValue(ch);
				}
			}/* if */
		}/* for */
		this.additional_parameters(node);
	}//getParameter();

	@Override
	public void additional_parameters(Node node) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getChildrenValue(Node node) {
		// TODO Auto-generated method stub
		Node snd;
		snd = node.getFirstChild();
		return snd.getNodeValue();		
	}

	/**
	 * @return the learningDataFileHeadName
	 */
	public String getLearningDataFileHeadName() {
		return LearningDataFileHeadName;
	}

	/**
	 * @return the numberOfSamples
	 */
	public int getNumberOfSamples() {
		return NumberOfSamples;
	}

	/**
	 * @return the maxDates
	 */
	public int getMaxDates() {
		return MaxDates;
	}

	/**
	 * @return the numberOfPracticePerDay
	 */
	public int getNumberOfPracticePerDay() {
		return NumberOfPracticePerDay;
	}

	/**
	 * @return the numberOfFolds
	 */
	public int getNumberOfFolds() {
		return NumberOfFolds;
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
	 * @return the numberOfDataSets
	 */
	public int getNumberOfDataSets() {
		return NumberOfDataSets;
	}

	/**
	 * @return the extention
	 */
	public String getExtention() {
		return Extention;
	}

}
