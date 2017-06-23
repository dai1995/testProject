package RBFNN;

public interface HiddenUnit {
	public double calculate_output(double input[]);
	public double[] getCenter();
	public void setCenter(double u[]);
	public double getVariance();
	public void setVariance(double sigma);
	
	// for learning
	public void init_M_step();
	public void M_step_add(double inputs[], double sumOfHiddenOutputs);
	public double M_step_update_center(); // return the change in the center;
	
	public double get_square_norm(double x[], double y[]);
	public double getDistance(double x[]);
	//public boolean isFixed();
	//public void setFixed(boolean isFixed);
	public boolean WithinRegion(double x[]);
	public String getPlot();
}
