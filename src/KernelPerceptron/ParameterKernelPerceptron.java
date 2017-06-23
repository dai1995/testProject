package KernelPerceptron;

import org.w3c.dom.Node;

import ParameterReader.Parameters;



public class ParameterKernelPerceptron implements Parameters {

	private int NumberOfInputs;
	private int NumberOfOutputs;
	private int UpperLimitOfHiddenUnits;
	private double DefaultSigma;
	private double err_threshold;
	private double LDThreshold;
	private boolean UseSoftProjection=true;
	private boolean UsePseudoInverse=true;
	private boolean CumulativeErrorEstimation=false;
	private double ImportanceWeightForNewSample=1D;
	private boolean UseAdaptiveImportanceWeight=false;
	private boolean UseHindgeLossFunction = false; 
	private double lambda = 0.0005;

	//constructor
	public ParameterKernelPerceptron() {};
	
	@Override
	public void getParameter(Node node) {
		// TODO Auto-generated method stub
		for (Node ch = node.getFirstChild(); ch != null; ch = ch
				.getNextSibling()) {
			if (ch.getNodeType() == Node.ELEMENT_NODE
					|| ch.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
				if (ch.getNodeName().equals("NumberOfInputs")) {
					System.out.println("KernelPerceptron:NumberOfInputs:"
							+ getChildrenValue(ch));
					this.NumberOfInputs = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				} else if (ch.getNodeName().equals("NumberOfOutputs")) {
					System.out.println("KernelPerceptron:NumberOfOutputs:"+ getChildrenValue(ch));
					this.NumberOfOutputs = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				} else if (ch.getNodeName().equals("UpperLimitOfHiddenUnits")) {
					System.out.println("KernelPerceptron:UpperLimitOfHiddenUnits:"+ getChildrenValue(ch));
					this.UpperLimitOfHiddenUnits = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				} else if (ch.getNodeName().equals("DefaultVariance")) {
					System.out.println("KernelPerceptron:default variance:" + this.getChildrenValue(ch));
					this.DefaultSigma = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				} else if (ch.getNodeName().equals("ImportanceWeightForNewSample")) {
					System.out.println("KernelPerceptron:default ImportanceWeightForNewSample:" + this.getChildrenValue(ch));
					this.ImportanceWeightForNewSample = Double.valueOf(this.getChildrenValue(ch)).doubleValue();					
				} else if (ch.getNodeName().equals("ErrThreshold")) {
					System.out.println("KernelPerceptron:ErrThreshold:" + this.getChildrenValue(ch));
					this.err_threshold = Double.valueOf(this.getChildrenValue(ch)).doubleValue();				
				} else if (ch.getNodeName().equals("LDThreshold")) {
					System.out.println("KernelPerceptron:LDThreshold:" + this.getChildrenValue(ch));
					this.LDThreshold = Double.valueOf(this.getChildrenValue(ch)).doubleValue();				
				} else if (ch.getNodeName().equals("UseSoftProjection")) {
					System.out.println("KernelPerceptron:UseSoftProjection:" + this.getChildrenValue(ch));
					this.UseSoftProjection = Boolean.valueOf(this.getChildrenValue(ch));
				} else if (ch.getNodeName().equals("CumulativeErrorEstimation")) {
					System.out.println("KernelPerceptron:CumlativeErrorEstimation:" + this.getChildrenValue(ch));
					this.CumulativeErrorEstimation = Boolean.valueOf(this.getChildrenValue(ch));	
				} else if (ch.getNodeName().equals("UseHindgeLossFunction")) {
					System.out.println("KernelPerceptron:UseHindgeLossFunction:" + this.getChildrenValue(ch));
					this.UseHindgeLossFunction = Boolean.valueOf(this.getChildrenValue(ch));					
				} else if (ch.getNodeName().equals("UseAdaptiveImportanceWeight")) {
					System.out.println("KernelPerceptron:UseAdaptiveImportanceWeight:" + this.getChildrenValue(ch));
					this.UseAdaptiveImportanceWeight = Boolean.valueOf(this.getChildrenValue(ch));									
				} else if (ch.getNodeName().equals("UsePseudoInverse")) {
					System.out.println("KernelPerceptron:UsePseudoInverse:" + this.getChildrenValue(ch));
					this.UsePseudoInverse = Boolean.valueOf(this.getChildrenValue(ch));					
				} else if  (ch.getNodeName().equals("lambda")) {
					System.out.println("KernelPerceptron4LRFU:lambda:"
							+ getChildrenValue(ch));
					this.lambda = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				}
			}
		}
		this.additional_parameters(node);
	}

	/**
	 * @return the cumulativeErrorEstimation
	 */
	public boolean isCumulativeErrorEstimation() {
		return CumulativeErrorEstimation;
	}

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
	 * @return the upperLimitOfHiddenUnits
	 */
	public int getUpperLimitOfHiddenUnits() {
		return UpperLimitOfHiddenUnits;
	}

	/**
	 * @return the defaultSigma
	 */
	public double getDefaultSigma() {
		return DefaultSigma;
	}

	/**
	 * @return the err_threshold
	 */
	public double getErr_threshold() {
		return err_threshold;
	}

	/**
	 * @return the lDThreshold
	 */
	public double getLDThreshold() {
		return LDThreshold;
	}

	/**
	 * @param upperLimitOfHiddenUnits the upperLimitOfHiddenUnits to set
	 */
	public void setUpperLimitOfHiddenUnits(int upperLimitOfHiddenUnits) {
		UpperLimitOfHiddenUnits = upperLimitOfHiddenUnits;
	}

	/**
	 * @return the useSoftProjection
	 */
	public boolean isUseSoftProjection() {
		return UseSoftProjection;
	}

	/**
	 * @return the usePseudoInverse
	 */
	public boolean isUsePseudoInverse() {
		return UsePseudoInverse;
	}

	/**
	 * @return the importanceWeightForNewSample
	 */
	public double getImportanceWeightForNewSample() {
		return ImportanceWeightForNewSample;
	}

	/**
	 * @return the useHindgeLossFunction
	 */
	public boolean isUseHindgeLossFunction() {
		return UseHindgeLossFunction;
	}

	/**
	 * @return the useAdaptiveImportanceWeight
	 */
	public boolean isUseAdaptiveImportanceWeight() {
		return UseAdaptiveImportanceWeight;
	}
	
	public double getLambda() {
		return lambda;
	}

	

}
