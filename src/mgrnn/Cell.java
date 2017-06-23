/*
 * Created on 2005/07/19
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mgrnn;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface Cell {
	
	public void set_variance(double nearest_point[], double lambda);	
	public void set_default_variance(double variance, double R);
	public void clean_check();
	public void inc_activation_number();
	public int getNumberOfActivations();
	public double getNumberOfLearnedSamples();
	public void set_check(boolean value);
	public boolean is_check();
	public double[] output(double input[]);	
	public double exp_output(double input[]);
	public double getActualDistance();
	public double exp_output();
	public void learn(double input[], double output[]);
	public String OutputHiddenUnitFunction();
	public double responsiblity();
	public void setSumOfOutputs(double sum);
	public double relativeResponsibility();
	public double[] get_normalized_t_alpha();
	public double getAlpha();
	public double[] getT_alpha();
	/**
	 * @return the t
	 */
	public double[] getT();

	public void setT(double new_center[]);

	/**
	 * @return the c
	 */
	public double[] getC();
	/**
	 * @param numberOfActivations the numberOfActivations to set
	 */
	public void setNumberOfActivations(int numberOfActivations);

	/**
	 * @return the r
	 */
	public double getR();

	/**
	 * @param r the r to set
	 */
	public void setR(double r);

	/**
	 * @param c the c to set
	 */
	public void setC(double[] c);


}
