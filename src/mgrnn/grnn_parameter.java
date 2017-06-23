/*
 * Created on 2005/09/05
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mgrnn;
import ParameterReader.*;
import org.w3c.dom.Node;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class grnn_parameter implements Parameters {

	double default_variance;
	double max_variance;
	boolean VariableSigma=true;
	double epsilon;
	int NumberOfInputs;
	int NumberOfOutputs;
	double lambda; //coefficent for the variance
	double gamma; //forgetting factor (for RFWR)
	double activation_threshold;
	double Distance_threshold;
	double LinearDependency_Threshold;
	double rm_threshold; //pruning threshold (for RFWR)
	int FIFOLENGTH; //for calclating mean/std (for RFWR)
	private boolean IsEstimatedByCumulativeError = false; //for general online learning method
	double ErrThreshold; 
	
	public grnn_parameter() {};//constructor
	
	public String getChildrenValue(Node node) {
		Node snd;
		snd = node.getFirstChild();
		return snd.getNodeValue();
	}
	
	public void additional_parameters(Node node) {
		
	}
	
	public void getParameter(Node node) {
		for (Node ch = node.getFirstChild(); ch != null; ch = ch
				.getNextSibling()) {
			if (ch.getNodeType() == Node.ELEMENT_NODE
					|| ch.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
				if (ch.getNodeName().equals("NumberOfInputs")) {
					System.out.println("GRNN:NumberOfInputs:"
							+ getChildrenValue(ch));
					this.NumberOfInputs = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				} else if (ch.getNodeName().equals("NumberOfOutputs")) {
					System.out.println("GRNN:NumberOfOutputs:"+ getChildrenValue(ch));
					this.NumberOfOutputs = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				} else if (ch.getNodeName().equals("DefaultVariance")) {
					System.out.println("GRNN:default variance:" + this.getChildrenValue(ch));
					this.default_variance = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				} else if (ch.getNodeName().equals("epsilon")) {
					System.out.println("GRNN:epsilon : " + this.getChildrenValue(ch));
					this.epsilon = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				} else if (ch.getNodeName().equals("lambda")) {
					System.out.println("GRNN:lambda : " + this.getChildrenValue(ch));
					this.lambda = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				} else if (ch.getNodeName().equals("Gamma")) {
					System.out.println("RFWR:Gamma : " + this.getChildrenValue(ch));
					this.gamma = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				} else if (ch.getNodeName().equals("FIFOLENGTH")) {
					System.out.println("RFWR:FIFOLENGTH : " + this.getChildrenValue(ch));
					this.FIFOLENGTH = Integer.valueOf(this.getChildrenValue(ch)).intValue();
				} else if (ch.getNodeName().equals("rm_threshold")) {
					System.out.println("RFWR:RmThreshold : " + this.getChildrenValue(ch));
					this.rm_threshold = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				} else if (ch.getNodeName().equals("activation_threshold")) {
					System.out.println("GRNN:activation_threshold : " + this.getChildrenValue(ch));
					this.activation_threshold = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				} else if (ch.getNodeName().equals("ErrThreshold")) {
					System.out.println("EFuNN:error_threshold : " + this.getChildrenValue(ch));
					this.ErrThreshold = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				} else if (ch.getNodeName().equals("MaxVariance")) {
					System.out.println("EFuNN:max variance : " + this.getChildrenValue(ch));
					this.max_variance = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				} else if (ch.getNodeName().equals("VariableSigma")) {
					System.out.println("LGRNN:VariableSigma : " + this.getChildrenValue(ch));
					this.VariableSigma = Boolean.valueOf(this.getChildrenValue(ch)).booleanValue();
				} else if (ch.getNodeName().equals("IsEstimatedByCumulativeError")) {
					System.out.println("LGRNN:IsEstimatedByCumulativeError : " + this.getChildrenValue(ch));
					this.IsEstimatedByCumulativeError = Boolean.valueOf(this.getChildrenValue(ch)).booleanValue();
				} else if (ch.getNodeName().equals("DistanceThreshold")) {
					System.out.println("RFWR:DistanceThreshold : " + this.getChildrenValue(ch));
					this.Distance_threshold = Double.valueOf(this.getChildrenValue(ch)).doubleValue();
				} else if (ch.getNodeName().equals("LinearDependencyThreshold")) {
					System.out.println("RFWR:LinearDependency_threshold : " + this.getChildrenValue(ch));
					this.LinearDependency_Threshold = Double.valueOf(this.getChildrenValue(ch)).doubleValue();					
				}
			}
		}
		this.additional_parameters(node);
	}

	/**
	 * @return the default_variance
	 */
	public double getDefault_variance() {
		return default_variance;
	}

	/**
	 * @return the max_variance
	 */
	public double getMax_variance() {
		return max_variance;
	}

	/**
	 * @return the epsilon
	 */
	public double getEpsilon() {
		return epsilon;
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
	 * @return the lambda
	 */
	public double getLambda() {
		return lambda;
	}

	/**
	 * @return the gamma
	 */
	public double getGamma() {
		return gamma;
	}

	/**
	 * @return the activation_threshold
	 */
	public double getActivation_threshold() {
		return activation_threshold;
	}

	/**
	 * @return the rm_threshold
	 */
	public double getRm_threshold() {
		return rm_threshold;
	}

	/**
	 * @return the fIFOLENGTH
	 */
	public int getFIFOLENGTH() {
		return FIFOLENGTH;
	}

	/**
	 * @return the errThreshold
	 */
	public double getErrThreshold() {
		return ErrThreshold;
	}

	/**
	 * @return the variableSigma
	 */
	public boolean isVariableSigma() {
		return VariableSigma;
	}

	/**
	 * @return the distance_threshold
	 */
	public double getDistance_threshold() {
		return Distance_threshold;
	}

	/**
	 * @return the linearDependency_Threshold
	 */
	public double getLinearDependency_Threshold() {
		return LinearDependency_Threshold;
	}

	/**
	 * @return the isEstimatedByCumulativeError
	 */
	public boolean IsEstimatedByCumulativeError() {
		return IsEstimatedByCumulativeError;
	}
					
					
	/*∞ÏŒ„
	 *
	 <grnn>
	 	<NumberOfInputs>12</NumberOfInputs>
	 	<NumberOfOutputs>1</NumberOfOutputs>
	 	<DefaultVariance>0.5</DefaultVariance>
	 	<epsilon>0.01</epsilon>
	 	<lambda>0.5</lambda>
	 	<activation_threshold>0.001</activation_threshold>
	 </grnn>
	 
	 <EFuNN>
	 	<NumberOfInputs>12</NumberOfInputs>
	 	<NumberOfOutputs>1</NumberOfOutputs>
	 	<DefaultVariance>0.5</DefaultVariance>
	 	<epsilon>0.01</epsilon>
	 	<lambda>0.5</lambda>
	 	<activation_threshold>0.001</activation_threshold>
	 	<ErrThreshold>0.05</ErrThreshold>
	 	<MaxVariance>10</MaxVariance>
	 </EFuNN>

	 *	 */
}
