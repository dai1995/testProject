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
					//�ؽ�����ץ�����ϼ�����������ץ���ǤϤʤ��Τ����
					System.out.println("MixtureOfDistributions:NumberOfInputs:" + this.getChildrenValue(ch));
					this.NumberOfInputs = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("NumberOfOutputs")) {
					//������������פʥѥ�᡼���ʤ�⡢�ؽ�����ץ���ɤ߹���Ȥ��˻Ȥ���礬����
					System.out.println("MixtureOfDistributions:NumberOfOutputs:" + this.getChildrenValue(ch));
					this.NumberOfOutputs = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("MaxNumberOfIterations")) {
					//EM���르�ꥺ�෫���֤�����κ����͡��ɤ����Ƥ��«���ʤ��Ȥ��Ϥ��ο��ǻߤ��
					System.out.println("MixtureOfDistributions:MaxNumberOfIterations:" + this.getChildrenValue(ch));
					this.MaxNumberOfIterations = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("DefaultNumberOfModels")) {
					//ɸ��Ū�ʥ�ǥ�����ǽ餫���ǥ�ο�����ꤷ�������Τ�����Ѱա����������ΤȤ�����Ѥ���
					System.out.println("MixtureOfDistributions:DefaultNumberOfModels:" + this.getChildrenValue(ch));
					this.DefaultNumberOfModels = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("UpperLimitOfFreedom")) {
					//��ͳ�٤κ������StudentTʬ�ۤ���������Ȥ���ɬ�ס�(�ơ�������ʬ�ۤ��տ路�ƺ�뤳�Ȥˤ��Ƥ���)
					System.out.println("MixtureOfDistributions:UpperLimitOfFreedom:" + this.getChildrenValue(ch));
					this.UpperLimitOfFreedom = Integer.valueOf(this.getChildrenValue(ch)).intValue();				
				}else if (ch.getNodeName().equals("MaxNumberOfModels")) {
					//AIC�ǥ٥��ȥ�ǥ��õ���ݤˤ����Ĥޤǥ�ǥ���������ƻ�Ƥߤ뤫�����κ����ͤ�Ϳ����
					System.out.println("MixtureOfDistributions:MaxNumberOfModels:" + this.getChildrenValue(ch));
					this.MaxNumberOfModels = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("StopThreshold")) {
					//EM���르�ꥺ��ν�λ���ѥ�᡼�����Ѳ��̤����Υ�ब�����Ͱʲ��Ȥʤä���ߤޤ�
					System.out.println("MixtureOfDistributions:StopThreshold:" + this.getChildrenValue(ch));
					this.stop_threshold = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				}else if (ch.getNodeName().equals("GammaThreshold")) {
					//�ɲ�EM���르�ꥺ��Υѥ�᡼������Ȥ����˦�ξ��
					System.out.println("MixtureOfDistributions:GammaThreshold:" + this.getChildrenValue(ch));
					this.GammaThreshold = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				}else if (ch.getNodeName().equals("ActivationThreshold")) {
					//��ǥ��ɲäκݤ�ȯ�Ф��Ƥ����˦�����뤫�ݤ��򸫤�Ȥ��˻Ȥ�
					System.out.println("MixtureOfDistributions:ActivationThreshold:" + this.getChildrenValue(ch));
					this.activation_threshold = Double.valueOf(this.getChildrenValue(ch)).doubleValue();					
				}else if (ch.getNodeName().equals("InitSigma")) {
					//ʬ����ʬ��������г����Ǥν����
					System.out.println("MixtureOfDistributions:InitSigma:" + this.getChildrenValue(ch));
					this.init_sigma = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				}else if (ch.getNodeName().equals("MinSigma")) {
					//���ͱ黻��Overflow����Τ��ɤ������Ƴ����¿�����ξ����ɤ�NaN�ˤʤäƤ��ޤ����Ȥ��ɤ��Ǥ��롣
					System.out.println("MixtureOfDistributions:MinSigma:" + this.getChildrenValue(ch));
					this.min_sigma = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				}else if (ch.getNodeName().equals("UseSMD")) {
					//��ñ�������ޥϥ�ӥΥ���Υ��Ƴ���������true�Ȥ��롣�⼡���ξ��ζ���׻��ѡʸ��ߤνꤦ�ޤ������������ᤷ���ʤ���
					System.out.println("MixtureOfDistributions:UseSMD:" + this.getChildrenValue(ch));
					this.SMD = Boolean.valueOf(this.getChildrenValue(ch)).booleanValue();
				}else if (ch.getNodeName().equals("QuickMode")) {
					//��ñ��������ǥ���������Ѥ��뤫�ݤ�
					System.out.println("MixtureOfDistributions:QuickMode:" + this.getChildrenValue(ch));
					this.QuickMode = Boolean.valueOf(this.getChildrenValue(ch)).booleanValue();
				}else if (ch.getNodeName().equals("RANInitMode")) {
					//������⡼�ɤȤ���RAN��ˡ��Ȥ�����true �����Ǥʤ�����ñ��˥ǡ������åȤκǽ�κ�˦��ʬ���濴���֤Ȥʤ�
					System.out.println("MixtureOfDistributions:RANInitMode:" + this.getChildrenValue(ch));
					this.RANInitMode = Boolean.valueOf(this.getChildrenValue(ch)).booleanValue();					
				}else if (ch.getNodeName().equals("CorrectTDistribution")) {
					//Tʬ�ۤ�Ȥ���硢��������ޥȥ�å�������Ѥ������True, ʬ����ʬ�������Ȥä��㤦�Ф�����False������ְ㤤�餷�����ɤ�.
					System.out.println("MixtureOfDistributions:CorrectTDistribution:" + this.getChildrenValue(ch));
					this.CorrectTDistribution = Boolean.valueOf(this.getChildrenValue(ch)).booleanValue();					
				}else if (ch.getNodeName().equals("MatrixMode")) {
					//ʬ����ʬ��������гѹ���ˤ��Ƥ��ޤ����ݤ����⼡���ξ�硢����Ū�ˤ��гѹ��󤷤�ư���ʤ��Ȼפ��Τ������ɤ������͡�
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
