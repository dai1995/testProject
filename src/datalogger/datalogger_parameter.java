/*
 * Created on 2005/08/05
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package datalogger;
import org.w3c.dom.Node;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class datalogger_parameter {
	String filename;
	int NumberOfInputs;
	String first_command;
	public datalogger_parameter() {};
	
	String getChildrenValue(Node node) {
		Node snd;
		snd = node.getFirstChild();
		return snd.getNodeValue();
	}
	
	void getParameter(Node node) {
		for (Node ch = node.getFirstChild(); ch != null; ch = ch
				.getNextSibling()) {
			if (ch.getNodeType() == Node.ELEMENT_NODE
					|| ch.getNodeType() == Node.ENTITY_REFERENCE_NODE) {
				if (ch.getNodeName().equals("filename")) {
					System.out.println("DataLogger:filename:"
							+ getChildrenValue(ch));
					this.filename = this.getChildrenValue(ch);
				}else{
					read_additional_parameter(ch);
				}
			}
		}
	}
	
	void read_additional_parameter(Node ch) {
		if (ch.getNodeName().equals("NumberOfInputs")) {
			System.out.println("DataLogger:NumberOfInputs:"
					+ getChildrenValue(ch));
			this.NumberOfInputs = Integer.valueOf(this.getChildrenValue(ch)).intValue();
		}else if (ch.getNodeName().equals("FirstCommand")) {
			System.out.println("FunctionOutput:FirstCommand" + getChildrenValue(ch));
			this.first_command = getChildrenValue(ch);
		}
	}
	
	/*
	 * ∞ÏŒ„
	 <datalogger>
	 <filename>/home/yamauchi/work/outout.plt</filename>
	 <datalogger>
	 */
	/**
	 * @return Returns the filename.
	 */
	public String getFilename() {
		return filename;
	}
	/**
	 * @return Returns the first_command.
	 */
	public String getFirst_command() {
		return first_command;
	}
	/**
	 * @return Returns the numberOfInputs.
	 */
	public int getNumberOfInputs() {
		return NumberOfInputs;
	}
}
