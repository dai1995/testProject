/*
 * Created on 2007/02/02
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package ParameterReader;
import org.w3c.dom.Node;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface Parameters {
	public void getParameter(Node node);
	void additional_parameters(Node node);
	String getChildrenValue(Node node);	
}
