package mgrnn;

import org.w3c.dom.Node;

public class grnnLD_parameter extends grnn_parameter {
	private double allocation_threshold; //for adding new unit (for grnnLinearDependency)
	private boolean AdaptiveImportanceWeight = true;
	private double WeightThreshold = 0D;
	
	public grnnLD_parameter() {
		super();
	}

	public void additional_parameters(Node node) {
		for (Node ch = node.getFirstChild(); ch != null; ch = ch.getNextSibling()) {
			if (ch.getNodeType() == Node.ELEMENT_NODE
				|| ch.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
				if (ch.getNodeName().equals("allocation_threshold")) {
					System.out.println("GRNNLD:allocation_threshold : " + this.getChildrenValue(ch));
					this.allocation_threshold = Double.valueOf(this.getChildrenValue(ch)).doubleValue();			
				}else if (ch.getNodeName().equals("AdaptiveImportanceWeight")) {
					System.out.println("LGRNNAIW: is AdaptiveImportanceWeight = " + this.getChildrenValue(ch));
					this.AdaptiveImportanceWeight = Boolean.valueOf(this.getChildrenValue(ch));
				}else if (ch.getNodeName().equals("WeightThreshold")) {
					System.out.println("LGRNNAIW: WeightThreshold = " +  this.getChildrenValue(ch));
					this.WeightThreshold = Double.valueOf(this.getChildrenValue(ch));
				}
			}
		}		
	}

	/**
	 * @return the adaptiveImportanceWeight
	 */
	public boolean isAdaptiveImportanceWeight() {
		return AdaptiveImportanceWeight;
	}

	/**
	 * @return the allocation_threshold
	 */
	public double getAllocation_threshold() {
		return allocation_threshold;
	}

	/**
	 * @return the weightThreshold
	 */
	public double getWeightThreshold() {
		return WeightThreshold;
	}


}
