package RBFNN;

import org.w3c.dom.Node;

import ParameterReader.Parameters;

public class RBFNN_parameters implements Parameters {

	int NumberOfInputs, NumberOfOutputs, NumberOfHiddenUnits;
	int KMeansMaxIteration=10;
	double KMeansChangeThreshold, MinSigma=0.1, MaxSigma=10;
	double Overlap=2.5;
	double init_sigma=0.1;
	double inverse_map_eta=0.01, inverse_map_stopCondition=0.01, MaxNumberOfInverseIterations=10000;
	double MinActivationThreshold = 0.001;
	double MinDistance = 0.0000001D; //��֥�˥åȤ��濴���֤ζ��ܤκǾ���
	//boolean WithBiasTerm = false;
	
	public RBFNN_parameters() {}
	
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

	@Override
	public void getParameter(Node node) {
		// TODO Auto-generated method stub
		for (Node ch = node.getFirstChild(); ch != null; ch = ch
			.getNextSibling()) {
			if (ch.getNodeType() == Node.ELEMENT_NODE
					|| ch.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
				if (ch.getNodeName().equals("NumberOfInputs")) {
					System.out.println("RBFNN:NumberOfInputs:"	+ getChildrenValue(ch));
					this.NumberOfInputs = Integer.valueOf(
							this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("NumberOfOutputs")) {
					System.out.println("RBFNN:NumberOfOutputs: " + getChildrenValue(ch));
					this.NumberOfOutputs = Integer.valueOf(this.getChildrenValue(ch)).intValue();					
				}else if (ch.getNodeName().equals("NumberOfHiddenUnits")) {
					System.out.println("RBFNN:NumberOfHiddenUnits: " + getChildrenValue(ch));
					this.NumberOfHiddenUnits = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("KMeansChangeThreshold")) {
					System.out.println("RBFNN:KMeansChangeThreshold:"	+ getChildrenValue(ch));
					//�ѥ�᡼�����Ѳ���(�濴���֤���ư�ˤ��������Ͱʲ��ˤʤ�ޤ�k-meansˡ��¹Ԥ���
					this.KMeansChangeThreshold = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				}else if (ch.getNodeName().equals("KMeansMaxIteration")) {
					//��ξ����������ʤ���硢�����֤����������ʾ�ˤʤä���ߤ��
					System.out.println("RBFNN:KMeansMaxIteration:"	+ getChildrenValue(ch));
					this.KMeansMaxIteration = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("MaxSigma")) {
					//sigma�ξ��
					System.out.println("RBFNN:MaxSigma:"	+ getChildrenValue(ch));
					this.MaxSigma = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				}else if (ch.getNodeName().equals("MinSigma")) {
					//sigma�β���
					System.out.println("RBFNN:MinSigma:"	+ getChildrenValue(ch));
					this.MinSigma = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				}else if (ch.getNodeName().equals("MinDitance")) {
					//��֥�˥å�Ʊ�ΤκǶ�˵��Υ�κǾ��͡������ͤ򲼲��Ƚ��Ϥ�ȯ������Τǡ����Τ褦�ʥ�����������˽������뤿�������
					System.out.println("RBFNN:MinDistance:"	+ getChildrenValue(ch));
					this.MinDistance = Double.valueOf(this.getChildrenValue(ch)).doubleValue();					
				}else if (ch.getNodeName().equals("OverLap")) {
					//RAN�ǻȤ��Ƥ���Τ�Ʊ�������С���åץե�������
					System.out.println("RBFNN:OverLap:"	+ getChildrenValue(ch));
					this.Overlap = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				/*} else if (ch.getNodeName().equals("WithBias")) {
					System.out.println("RBFNN:WighBias:" + getChildrenValue(ch));
					this.WithBiasTerm = Boolean.valueOf(this.getChildrenValue(ch)).booleanValue();*/					
				}else if (ch.getNodeName().equals("InitSigma")) {
					//k-meansˡ��¹Ԥ������˽��������sigma����
					System.out.println("RBFNN:InitSigma:"	+ getChildrenValue(ch));
					this.init_sigma = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				}else if (ch.getNodeName().equals("MinActivationThreshold")) {
					//�ɵ��ؽ��⡼�ɤǻ��Ѥ��롣�����Ͱʲ�����ֺ�˦�Υѥ�᡼���ϸ��ꤹ��
					System.out.println("RBFNN:MinActivationThreshold:"	+ getChildrenValue(ch));
					this.MinActivationThreshold = Double.valueOf(this.getChildrenValue(ch)).doubleValue();					
				}else if (ch.getNodeName().equals("inverse_map_eta")) {
					//inverse mapping���Ѳ�®��
					System.out.println("RBFNN:inverse_map_eta:"	+ getChildrenValue(ch));
					this.inverse_map_eta = Double.valueOf(this.getChildrenValue(ch)).doubleValue();				
				}else if (ch.getNodeName().equals("inverse_map_stopCondition")) {
					//inverse mapping�ν�λ���
					System.out.println("RBFNN:inverse_map_stopCondition:"	+ getChildrenValue(ch));
					this.inverse_map_stopCondition = Double.valueOf(this.getChildrenValue(ch)).doubleValue();									
				}else if (ch.getNodeName().equals("MaxNumberOfInverseIterations")) {
					//inverse mapping�η����֤�����κ����͡���«���ʤ��Ȥ��Ϥ��β���ǻߤ��
					System.out.println("RBFNN:NumberOfInverseIterations: " + getChildrenValue(ch));
					this.MaxNumberOfInverseIterations = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}
			}else{
				additional_parameters(node);
			}
		}
	}

	/**
	 * @return the numberOfHiddenUnits
	 */
	public int getNumberOfHiddenUnits() {
		return NumberOfHiddenUnits;
	}
	public void AddNumberOfHiddenUnits(int number) {
		this.NumberOfHiddenUnits += number;
	}
	/**
	 * @return the numberOfInputs
	 */
	public int getNumberOfInputs() {
		return NumberOfInputs;
	}

	/**
	 * @return the kMeansChangeThreshold
	 */
	public double getKMeansChangeThreshold() {
		return KMeansChangeThreshold;
	}

	/**
	 * @return the kMeansMaxIteration
	 */
	public int getKMeansMaxIteration() {
		return KMeansMaxIteration;
	}

	/**
	 * @return the numberOfOutputs
	 */
	public int getNumberOfOutputs() {
		return NumberOfOutputs;
	}

	/**
	 * @return the minSigma
	 */
	public double getMinSigma() {
		return MinSigma;
	}

	/**
	 * @return the maxSigma
	 */
	public double getMaxSigma() {
		return MaxSigma;
	}

	/**
	 * @return the init_sigma
	 */
	public double getInit_sigma() {
		return init_sigma;
	}

	/**
	 * @return the overlap
	 */
	public double getOverlap() {
		return Overlap;
	}

	/**
	 * @return the pseudo_inverse_eta
	 */
	public double get_inverse_map_eta() {
		return this.inverse_map_eta;
	}

	/**
	 * @return the pseudo_inverse_stopCondition
	 */
	public double get_inverse_map_stopCondition() {
		return this.inverse_map_stopCondition;
	}

	/**
	 * @return the maxNumberOfInverseIterations
	 */
	public double getMaxNumberOfInverseIterations() {
		return MaxNumberOfInverseIterations;
	}

	/**
	 * @return the minActivationThreshold
	 */
	public double getMinActivationThreshold() {
		return MinActivationThreshold;
	}

	/**
	 * @return the minDistance
	 */
	public double getMinDistance() {
		return MinDistance;
	}

	/**
	 * @return the withBiasTerm
	 */
	/*public boolean isWithBiasTerm() {
		return WithBiasTerm;
	}*/

}
