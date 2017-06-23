/*
 * Created on 2005/08/19
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package DataAnalysis;
import org.w3c.dom.Node;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DataAnalysisParameters {
	int NumberOfDataSets=1;
	String[] FileNames;
	String DirectoryHeadPath;
	String DirectoryHeadPath1;//一つめ 複数読み込む場合
	String DirectoryHeadPath2;//二つめ 複数読み込む場合
	int NumberOfInputDim=1, NumberOfOutputDim=1;
	boolean IsHeadPathNumbering = true;
	//String ResultFilename;
	int ConficenceThreshold;
	
	//df(横軸)  	0.1  	0.05  	0.025  	0.01  	0.005  例えば信頼度95%の場合両側合わせて0.05なので片側0.025を選択すること。
	double t_distribution[][] = 
		    {{3.0777, 6.3138, 12.7062,31.8205,63.6567}, //1
			{1.8856, 2.9200, 4.3027, 6.9646, 9.9248}, //2
			{1.6377, 2.3534, 3.1824, 4.5407, 5.8409}, //3
			{1.5332, 2.1318, 2.7764, 3.7470, 4.6041}, //4
			{1.4759, 2.0150, 2.5706, 3.3649, 4.0322}, //5
			{1.4398, 1.9432, 2.4469, 3.1427, 3.7074}, //6
			{1.4149, 1.8946, 2.3646, 2.9980, 3.4995}, //7
			{1.3968, 1.8595, 2.3060, 2.8965, 3.3554}, //8
			{1.3830, 1.8331, 2.2622, 2.8214, 3.2498}, //9
			{1.3722, 1.8125, 2.2281, 2.7638, 3.1693}, //10
			{1.3634, 1.7959, 2.2010, 2.7181, 3.1058}, //11
			{1.3562, 1.7823, 2.1788, 2.6810, 3.0545}, //12
			{1.3502, 1.7709, 2.1604, 2.6503, 3.0123}, //13
			{1.3450, 1.7613, 2.1448, 2.6245, 2.9768}, //14
			{1.3406, 1.7531, 2.1314, 2.6025, 2.9467}, //15
			{1.3368, 1.7459, 2.1199, 2.5835, 2.9208}, //16
			{1.3334, 1.7396, 2.1098, 2.5669, 2.8982}, //17
			{1.3304, 1.7341, 2.1009, 2.5524, 2.8784}, //18
			{1.3277, 1.7291, 2.0930, 2.5395, 2.8609}, //19
			{1.3253, 1.7247, 2.0860, 2.5280, 2.8453}, //20  ここから二つ飛びに注意
			{1.3212, 1.7171, 2.0739, 2.5083, 2.8188}, //22
			{1.3178, 1.7109, 2.0639, 2.4922, 2.7969}, //24
			{1.3150, 1.7056, 2.0555, 2.4786, 2.7787}, //26
			{1.3125, 1.7011, 2.0484, 2.4671, 2.7633}, //28
			{1.3104, 1.6973, 2.0423, 2.4573, 2.7500}, //30 ここから10飛びに注意
			{1.3031, 1.6839, 2.0211, 2.4233, 2.7045}, //40
			{1.2987, 1.6759, 2.0086, 2.4033, 2.6778}, //50
			{1.2958, 1.6706, 2.0003, 2.3901, 2.6603}, //60
			{1.2938, 1.6669, 1.9944, 2.3808, 2.6479}, //70
			{1.2922, 1.6641, 1.9901, 2.3739, 2.6387}, //80
			{1.2910, 1.6620, 1.9867, 2.3685, 2.6316}, //90
			{1.2901, 1.6602, 1.9840, 2.3642, 2.6259}, //100
			{1.2893, 1.6588, 1.9818, 2.3607, 2.6213}, //110
			{1.2886, 1.6577, 1.9799, 2.3578, 2.6174}, //120
			{1.2816, 1.6449, 1.9600, 2.3263, 2.5758}}; //infty ここだけ無限大
		
	public DataAnalysisParameters() {
		
	}
	
	String getChildrenValue(Node node) {
		Node snd;
		snd = node.getFirstChild();
		return snd.getNodeValue();
	}
	
	public double get_t_value(int NumberOfSamples) {
		return this.t_distribution[this.get_respected_number_index_of_datasets(NumberOfSamples)]
								  [this.get_respected_confidence_index(this.ConficenceThreshold)];
		//if (!(data==80 || data==90 || data==95 || data==98 || data==99)) {
	}
	
	void getParameter(Node node) {
		for (Node ch = node.getFirstChild(); ch != null; ch = ch
				.getNextSibling()) {
			if (ch.getNodeType() == Node.ELEMENT_NODE
					|| ch.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
				if (ch.getNodeName().equals("NumberOfDataSets")) {
					System.out.println("DataAnalysis:NumberOfDataSets:"
							+ getChildrenValue(ch));
					this.NumberOfDataSets = Integer.valueOf(getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("IsHeadPathNumbering")) {
					System.out.println("DataAnalysis:IsHeadPathNumbering"
							+ getChildrenValue(ch));
					if (getChildrenValue(ch).toString()=="true") {
						this.IsHeadPathNumbering = true;
					}else{
						this.IsHeadPathNumbering = false;
					}
				}else if (ch.getNodeName().equals("DirectoryHeadPath")) {
					System.out.println("DataAnalysis:DirectoryHeadPath:"
							+ getChildrenValue(ch));
					this.DirectoryHeadPath = getChildrenValue(ch);
				}else if (ch.getNodeName().equals("DirectoryHeadPath1")) {
					System.out.println("DataAnalysis:DirectoryHeadPath1:"
							+ getChildrenValue(ch));
					this.DirectoryHeadPath1 = getChildrenValue(ch);
				}else if (ch.getNodeName().equals("DirectoryHeadPath2")) {
					System.out.println("DataAnalysis:DirectoryHeadPath2:"
							+ getChildrenValue(ch));
					this.DirectoryHeadPath2 = getChildrenValue(ch);
				}else if (ch.getNodeName().equals("NumberOfInputs")) {
					System.out.println("DataAnalysis:NumberOfInputs:"
							+ getChildrenValue(ch));
					this.NumberOfInputDim = Integer.valueOf(getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("ConfidenceThreshold")) {
					System.out.println("DataAnalysis:ConfidenceThreshold:"
							+ getChildrenValue(ch));
					this.ConficenceThreshold = Integer.valueOf(getChildrenValue(ch)).intValue();
					this.check_threshold(this.ConficenceThreshold);
				}else if (ch.getNodeName().equals("NumberOfOutputs")) {
					System.out.println("DataAnalysis:NumberOfOutputs:"
							+ getChildrenValue(ch));
					this.NumberOfOutputDim = Integer.valueOf(getChildrenValue(ch)).intValue();
				}
			}
		}
	}
	
	void check_threshold(int data) {
		if (!(data==80 || data==90 || data==95 || data==98 || data==99)) {
			System.err.println("ERROR:  Confidence threshold should be 80 or 90 or 95 or 98 or 99!(percent)");
			System.exit(1);
		}
	}
	
	int get_respected_confidence_index(int data) {
		int index;
		if (data==80) {
			index =0;
		}else if (data==90) {
			index = 1;
		}else if (data==95) {
			index = 2;
		}else if (data==98) {
			index = 3;
		}else if (data==99) {
			index = 4;
		}else{
			index = 4;
		}
		System.out.println("Respected number of confidence index is : " + index);
		return index;
	}
	
	int get_respected_number_index_of_datasets(int data) {
		int index;
		data --; //自由度は1を引いた値となる!
		if (data <= 20) {
			index = data-1;
		}else if (data > 20 && data <=30) {
			index = 20 + (int)((data-20)/2)-1;
		}else if (data > 30 && data <= 120) {
			index = 25 + (int)((data-30)/10) - 1;
		}else index = 34;
		System.out.println("Respected number of freedom index: " + index);
		return index;
	}
	
	/*
	 *
	 *
<?xml version="1.0"?>
<parameter>
	<DataAnalysis>
	  <NumberOfDataSets>10</NumberOfDataSets> //10試行行った場合。ここを-1とするとHeadPathに数字をつけずに読み込む。ただしこの場合はデータセットの数は１
	  <NumberOfInputs>1</NumberOfInputs>
	  <NumberOfOutputs>1</NumberOfOutputs>
	  <ConfidenceThreshold>95</ConfidenceThreshold>
	  <DirectoryHeadPath>/home/yamauchi/work/results/metall-mackey</DirectoryHeadPath>
	</DataAnalysis>
	<ResultFileName>
		<filename>/home/yamauchi/work/test.dat</filename> //結果のファイル名
	</ResultFileName>
	<ext_multipledataload>
	   <TestSampleHeadName>non-meta-number</TestSampleHeadName>	   
	   <NumberOfDatasets>10</NumberOfDatasets> //各々の試行毎に何トライアルしたか
	   <Token> </Token> //区切り文字
	   <datatype>double</datatype>
	   <NumberOfTestSamples>51</NumberOfTestSamples> //各々の試行毎の各トライアルのデータ長
	   <StepSize>1</StepSize> //何ステップ毎にデータを読み込むか
	   <Extention>.dat</Extention> //各々のデータファイルの拡張子
	</ext_multipledataload>
</parameter>

//DetectDiffDistribution
 * 
<?xml version="1.0"?>
<parameter>
	<DataAnalysis>
	  <NumberOfDataSets>10</NumberOfDataSets>
	  <NumberOfInputs>1</NumberOfInputs>
	  <NumberOfOutputs>1</NumberOfOutputs>
	  <ConfidenceThreshold>95</ConfidenceThreshold>
	  <DirectoryHeadPath1>/home/yamauchi/work/results-non-meta3/metall-mackey</DirectoryHeadPath1>
	  <DirectoryHeadPath2>/home/yamauchi/work/results-minus5/metall-mackey</DirectoryHeadPath2>	  
	</DataAnalysis>
	<ResultFileName>
		<filename>/home/yamauchi/work/diff95.dat</filename>
	</ResultFileName>
	<EachDiffFileName>
		<filename>/home/yamauchi/work/diff/diff</filename>
	</EachDiffFileName>
	<ext_multipledataload1>
	   <TestSampleHeadName>non-meta-number</TestSampleHeadName>	   
	   <NumberOfDatasets>10</NumberOfDatasets>
	   <Token> </Token>
	   <datatype>double</datatype>
	   <NumberOfTestSamples>51</NumberOfTestSamples>
	   <StepSize>1</StepSize>
	   <Extention>.dat</Extention>
	</ext_multipledataload1>
	<ext_multipledataload2>
	   <TestSampleHeadName>number-minus</TestSampleHeadName>	   
	   <NumberOfDatasets>10</NumberOfDatasets>
	   <Token> </Token>
	   <datatype>double</datatype>
	   <NumberOfTestSamples>51</NumberOfTestSamples>
	   <StepSize>1</StepSize>
	   <Extention>.dat</Extention>
	</ext_multipledataload2>
</parameter>

	 * 
	 *  
	 */
	
}
