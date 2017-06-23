package MixtureOfDistributions;

import org.w3c.dom.Node;

import ParameterReader.Parameters;

public class MixtureOfDistributionParameters implements Parameters {

	int NumberOfInputs;
	int NumberOfOutputs;
	int DefaultNumberOfModels;
	int MaxNumberOfModels;
	int MaxNumberOfIterations;
	
	int UpperLimitOfFreedom=50;
	double activation_threshold;
	double stop_threshold;
	double GammaThreshold;
	double init_sigma;
	double min_sigma;
	boolean SMD=false;
	boolean DIAGONAL=true;
	boolean QuickMode = true;
	boolean CorrectTDistribution=true;
	boolean RANInitMode = true;

	
	public MixtureOfDistributionParameters() {
		
	}
	
	@Override
	public void additional_parameters(Node node) {
		// TODO Auto-generated method stub

	}


	public String getChildrenValue(Node node) {
		Node snd;
		snd = node.getFirstChild();
		return snd.getNodeValue();
	}
	
	@Override
	public void getParameter(Node node) {
		// TODO Auto-generated method stub
		for (Node ch = node.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
			if (ch.getNodeType() == Node.ELEMENT_NODE
			|| ch.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
				if (ch.getNodeName().equals("NumberOfInputs")) {
					//学習サンプルの入力次元数。サンプル数ではないので注意
					System.out.println("MixtureOfDistributions:NumberOfInputs:" + this.getChildrenValue(ch));
					this.NumberOfInputs = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("NumberOfOutputs")) {
					//これは本来不要なパラメータなるも、学習サンプルを読み込むときに使う場合がある
					System.out.println("MixtureOfDistributions:NumberOfOutputs:" + this.getChildrenValue(ch));
					this.NumberOfOutputs = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("MaxNumberOfIterations")) {
					//EMアルゴリズム繰り返し回数の最大値。どうしても収束しないときはこの数で止める
					System.out.println("MixtureOfDistributions:MaxNumberOfIterations:" + this.getChildrenValue(ch));
					this.MaxNumberOfIterations = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("DefaultNumberOfModels")) {
					//標準的なモデル数。最初からモデルの数を固定したい場合のために用意。しかし今のところ使用せず
					System.out.println("MixtureOfDistributions:DefaultNumberOfModels:" + this.getChildrenValue(ch));
					this.DefaultNumberOfModels = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("UpperLimitOfFreedom")) {
					//自由度の最大数。StudentT分布を生成するときに必要。(各々の正規分布に付随して作ることにしてある)
					System.out.println("MixtureOfDistributions:UpperLimitOfFreedom:" + this.getChildrenValue(ch));
					this.UpperLimitOfFreedom = Integer.valueOf(this.getChildrenValue(ch)).intValue();				
				}else if (ch.getNodeName().equals("MaxNumberOfModels")) {
					//AICでベストモデルを探す際にいくつまでモデルを生成して試してみるか、その最大値を与える
					System.out.println("MixtureOfDistributions:MaxNumberOfModels:" + this.getChildrenValue(ch));
					this.MaxNumberOfModels = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("StopThreshold")) {
					//EMアルゴリズムの終了条件。パラメータの変化量の二乗ノルムがこの値以下となったら止まる
					System.out.println("MixtureOfDistributions:StopThreshold:" + this.getChildrenValue(ch));
					this.stop_threshold = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				}else if (ch.getNodeName().equals("GammaThreshold")) {
					//追加EMアルゴリズムのパラメータ固定とする細胞の条件
					System.out.println("MixtureOfDistributions:GammaThreshold:" + this.getChildrenValue(ch));
					this.GammaThreshold = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				}else if (ch.getNodeName().equals("ActivationThreshold")) {
					//モデル追加の際に発火している細胞があるか否かを見るときに使う
					System.out.println("MixtureOfDistributions:ActivationThreshold:" + this.getChildrenValue(ch));
					this.activation_threshold = Double.valueOf(this.getChildrenValue(ch)).doubleValue();					
				}else if (ch.getNodeName().equals("InitSigma")) {
					//分散共分散行列の対角要素の初期値
					System.out.println("MixtureOfDistributions:InitSigma:" + this.getChildrenValue(ch));
					this.init_sigma = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				}else if (ch.getNodeName().equals("MinSigma")) {
					//数値演算がOverflowするのを防ぐために導入。多次元の場合に良くNaNになってしまうことを防いでいる。
					System.out.println("MixtureOfDistributions:MinSigma:" + this.getChildrenValue(ch));
					this.min_sigma = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				}else if (ch.getNodeName().equals("UseSMD")) {
					//簡単化したマハラビノス距離を導入する場合にtrueとする。高次元の場合の近似計算用（現在の所うまくうごいたためしがない）
					System.out.println("MixtureOfDistributions:UseSMD:" + this.getChildrenValue(ch));
					this.SMD = Boolean.valueOf(this.getChildrenValue(ch)).booleanValue();
				}else if (ch.getNodeName().equals("QuickMode")) {
					//簡単化したモデル選択を利用するか否か
					System.out.println("MixtureOfDistributions:QuickMode:" + this.getChildrenValue(ch));
					this.QuickMode = Boolean.valueOf(this.getChildrenValue(ch)).booleanValue();
				}else if (ch.getNodeName().equals("RANInitMode")) {
					//初期化モードとしてRAN手法を使う場合はtrue そうでない場合は単純にデータセットの最初の細胞数分が中心位置となる
					System.out.println("MixtureOfDistributions:RANInitMode:" + this.getChildrenValue(ch));
					this.RANInitMode = Boolean.valueOf(this.getChildrenValue(ch)).booleanValue();					
				}else if (ch.getNodeName().equals("CorrectTDistribution")) {
					//T分布を使う場合、スケールマトリックスを使用する場合はTrue, 分散共分散行列を使っちゃうばあいはFalse←これ間違いらしいけどね.
					System.out.println("MixtureOfDistributions:CorrectTDistribution:" + this.getChildrenValue(ch));
					this.CorrectTDistribution = Boolean.valueOf(this.getChildrenValue(ch)).booleanValue();					
				}else if (ch.getNodeName().equals("MatrixMode")) {
					//分散共分散行列を対角行列にしてしまうか否か。高次元の場合、現実的には対角行列しか動かないと思うのだが、どうだろうね。
					System.out.println("MixtureOfDistributions:MatrixMode:" + this.getChildrenValue(ch));
					if (this.getChildrenValue(ch).equals("DIAGONAL")) {
						this.DIAGONAL =true;
					}else{
						this.DIAGONAL = false;
					}
				}
			}
			this.additional_parameters(node);
		}
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
	 * @return the defaultNumberOfModels
	 */
	public int getDefaultNumberOfModels() {
		return DefaultNumberOfModels;
	}

	/**
	 * @return the stop_threshold
	 */
	public double getStop_threshold() {
		return stop_threshold;
	}

	/**
	 * @return the maxNumberOfModels
	 */
	public int getMaxNumberOfModels() {
		return MaxNumberOfModels;
	}

	/**
	 * @return the maxNumberOfIterations
	 */
	public int getMaxNumberOfIterations() {
		return MaxNumberOfIterations;
	}

	/**
	 * @return the init_sigma
	 */
	public double getInit_sigma() {
		return init_sigma;
	}

	/**
	 * @return the sMD
	 */
	public boolean isSMD() {
		return SMD;
	}

	/**
	 * @return the min_sigma
	 */
	public double getMin_sigma() {
		return min_sigma;
	}

	/**
	 * @return the dIAGONAL
	 */
	public boolean isDIAGONAL() {
		return DIAGONAL;
	}

	/**
	 * @return the upperLimitOfFreedom
	 */
	public int getUpperLimitOfFreedom() {
		return UpperLimitOfFreedom;
	}

	/**
	 * @return the quickMode
	 */
	public boolean isQuickMode() {
		return QuickMode;
	}

	/**
	 * @return the gammaThreshold
	 */
	public double getGammaThreshold() {
		return GammaThreshold;
	}

	/**
	 * @return the activation_threshold
	 */
	public double getActivation_threshold() {
		return activation_threshold;
	}

	/**
	 * @return the correctTDistribution
	 */
	public boolean isCorrectTDistribution() {
		return CorrectTDistribution;
	}

	/**
	 * @return the rANInitMode
	 */
	public boolean isRANInitMode() {
		return RANInitMode;
	}

	
/*
 * 
 

 
 * 
 */


}
