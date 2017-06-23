package RBFNN;

import org.w3c.dom.Node;

import ParameterReader.Parameters;

public class WRBFNNELM_parameters extends RBFNN_parameters implements Parameters {

	double MinCenterValue;
	double MaxCenterValue;
	
	public WRBFNNELM_parameters() {
		super();
	}
	
	@Override
	public void additional_parameters(Node node) {
		// TODO Auto-generated method stub
		for (Node ch = node.getFirstChild(); ch != null; ch = ch
		.getNextSibling()) {
			if (ch.getNodeType() == Node.ELEMENT_NODE
				|| ch.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
				if (ch.getNodeName().equals("MaxCenterValue")) {
					System.out.println("wRBFNNELF:MaxCenterValue:"	+ getChildrenValue(ch));
					this.MaxCenterValue = Double.valueOf(
							this.getChildrenValue(ch)).doubleValue();
				}else if (ch.getNodeName().equals("MinCenterValue")) {
					System.out.println("wRBFNNELF:MinCenterValue: " + getChildrenValue(ch));
					this.MinCenterValue = Double.valueOf(this.getChildrenValue(ch)).doubleValue();					
				}
			}
		}

	}

	/**
	 * @return the minCenterValue
	 */
	public double getMinCenterValue() {
		return MinCenterValue;
	}

	/**
	 * @return the maxCenterValue
	 */
	public double getMaxCenterValue() {
		return MaxCenterValue;
	}


}
