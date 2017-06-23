package mgrnn;

import org.w3c.dom.Node;

public interface grnn {
	void init_parameters(Node nd);
	public int learning(double inputs[], double outputs[]);
	public double[] calculate_outputs(double input[]);
}
