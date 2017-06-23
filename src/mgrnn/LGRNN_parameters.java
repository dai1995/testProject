package mgrnn;

import org.w3c.dom.Node;

public class LGRNN_parameters extends grnn_parameter {
	private int NumberOfUpperLimitOfUnits;
	private double ImportanceWeight4NewSample=1;
	//choose the four options
	private boolean UseSubstitution = true;
	private boolean UseModify = true;
	private boolean UseIgnore = true;
	private boolean UsePrune = true;
	private boolean UseEditing = false;
	private boolean UsePseudoInverse = false;
	private boolean UseSoftProjection = true;
	private boolean UseAdaptiveImportanceWeight = true;
	private boolean UseDistributionOfGivenSamples = true;
	private double flattening=0.5;
	private int UpperLimitOfNearestNeighbors=100; // used for LGRNNKNN
	private double WeightThreshold = 0D; // used for LGRNNAIW
	
	private double lambda = 0.0005; // used for LGRNNLRFU
	private double delta_threshold = 0D; // used for LGRNNLRFU
	
	public LGRNN_parameters() {
		super();
	}
	
	public void additional_parameters(Node node) {
		for (Node ch = node.getFirstChild(); ch != null; ch = ch
			.getNextSibling()) {
			if (ch.getNodeType() == Node.ELEMENT_NODE
			|| ch.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
				if (ch.getNodeName().equals("NumberOfUpperLimitOfUnits")) {
					System.out.println("LGRNN_parameters:NumberOfUpperLimitOfUnits:"
					+ getChildrenValue(ch));
					this.NumberOfUpperLimitOfUnits = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("ImportanceWeight4NewSample")) {
					System.out.println("LGRNN_parameters:ImportanceWeight4NewSample:" + getChildrenValue(ch));
					this.ImportanceWeight4NewSample = Double.valueOf(getChildrenValue(ch)).doubleValue();
				}else if (ch.getNodeName().equals("UseSubstitution")) {
					System.out.println("LGRNN_parameters:UseSubstitution:" +getChildrenValue(ch));
					this.UseSubstitution = Boolean.valueOf(getChildrenValue(ch)).booleanValue();
				}else if (ch.getNodeName().equals("UseModify")) {
					System.out.println("LGRNN_parameters:UseModify:" +getChildrenValue(ch));
					this.UseModify = Boolean.valueOf(getChildrenValue(ch)).booleanValue();
				}else if (ch.getNodeName().equals("UsePrune")) {
					System.out.println("LGRNN_parameters:UsePrune:" +getChildrenValue(ch));
					this.UsePrune = Boolean.valueOf(getChildrenValue(ch)).booleanValue();
				}else if (ch.getNodeName().equals("UseIgnore")) {
					System.out.println("LGRNN_parameters:UseIgnore:" +getChildrenValue(ch));
					this.UseIgnore = Boolean.valueOf(getChildrenValue(ch)).booleanValue();
				}else if (ch.getNodeName().equals("UpperLimitOfKNN")) {
					System.out.println("LGRNN_parameters:UpperLimitOfKNN:" +getChildrenValue(ch));
					this.UpperLimitOfNearestNeighbors = Integer.valueOf(getChildrenValue(ch)).intValue();
				}else if (ch.getNodeName().equals("UseEditing")) {
					System.out.println("LGRNN_parameters:UseEditing:" +getChildrenValue(ch));
					this.UseEditing = Boolean.valueOf(getChildrenValue(ch)).booleanValue();		
				}else if (ch.getNodeName().equals("UsePseudoInverse")) {
					System.out.println("LGRNN_parameters:UsePseudoInverse:" +getChildrenValue(ch));
					this.UsePseudoInverse = Boolean.valueOf(getChildrenValue(ch)).booleanValue();					
				}else if (ch.getNodeName().equals("UseSoftProjection")) {
					System.out.println("LGRNN_parameters:UseSoftProjection:" +getChildrenValue(ch));
					this.UseSoftProjection = Boolean.valueOf(getChildrenValue(ch)).booleanValue();					
				}else if (ch.getNodeName().equals("flattening_parameter")) {
					System.out.println("LGRNNAIW: flattening_parameter = " + this.getChildrenValue(ch));
					this.flattening = Double.valueOf(this.getChildrenValue(ch));
				}else if (ch.getNodeName().equals("UseAdaptiveImportanceWeight")) {
					System.out.println("LGRNN_parameters:UseAdaptiveImportanceWeight:" +getChildrenValue(ch));
					this.UseAdaptiveImportanceWeight = Boolean.valueOf(getChildrenValue(ch)).booleanValue();
				}else if (ch.getNodeName().equals("UseDistributionOfGivenSamples")) {
					System.out.println("LGRNN_parameters:UseDistributionOfGivenSamples:" +getChildrenValue(ch));
					this.UseDistributionOfGivenSamples = Boolean.valueOf(getChildrenValue(ch)).booleanValue();	
				}else if (ch.getNodeName().equals("WeightThreshold")) {
					System.out.println("LGRNNAIW: WeightThreshold = " +  this.getChildrenValue(ch));
					if (this.getChildrenValue(ch).equals("MAX")) {
						this.WeightThreshold = Double.MAX_VALUE;//�Ҥκ�Ŭ����Ԥ���ImportanceWegith������ʾ�ʤ�Ԥ������¾���ˤϤҤä�����ʤ��ʤ�Ϥ�
					}else{
						this.WeightThreshold = Double.valueOf(this.getChildrenValue(ch));
					}
				}				
			}
		}
	}

	/**
	 * @return the numberOfUpperLimitOfUnits
	 */
	public int getNumberOfUpperLimitOfUnits() {
		return NumberOfUpperLimitOfUnits;
	}

	/**
	 * @return the importanceWeight4NewSample
	 */
	public double getImportanceWeight4NewSample() {
		return ImportanceWeight4NewSample;
	}

	/**
	 * @return the useSubstitution
	 */
	public boolean isUseSubstitution() {
		return UseSubstitution;
	}

	/**
	 * @return the useSoftProjection
	 */
	public boolean isUseSoftProjection() {
		return UseSoftProjection;
	}

	/**
	 * @return the useModify
	 */
	public boolean isUseModify() {
		return UseModify;
	}

	/**
	 * @return the useIgnore
	 */
	public boolean isUseIgnore() {
		return UseIgnore;
	}

	/**
	 * @return the usePrune
	 */
	public boolean isUsePrune() {
		return UsePrune;
	}

	/**
	 * @return the useEditing
	 */
	public boolean isUseEditing() {
		return UseEditing;
	}

	/**
	 * @return the upperLimitOfNearestNeighbors
	 */
	public int getUpperLimitOfNearestNeighbors() {
		return UpperLimitOfNearestNeighbors;
	}
	
	/**
	 * @return the flattening
	 */
	public double getFlattening() {
		return flattening;
	}

	/**
	 * @return the usePseudoInverse
	 */
	public boolean isUsePseudoInverse() {
		return UsePseudoInverse;
	}
	
	/**
	 * @return the weightThreshold
	 */
	public double getWeightThreshold() {
		return WeightThreshold;
	}

	/**
	 * @return the useAdaptiveImportanceWeight
	 */
	public boolean isUseAdaptiveImportanceWeight() {
		return UseAdaptiveImportanceWeight;
	}

	/**
	 * @return the useDistributionOfGivenSamples
	 */
	public boolean isUseDistributionOfGivenSamples() {
		return UseDistributionOfGivenSamples;
	}
	
	public double getLambda() {
		return lambda;
	}

	/**
	 * @return the delta_threshold
	 */
	public double getDelta_threshold() {
		return delta_threshold;
	}

	
}
