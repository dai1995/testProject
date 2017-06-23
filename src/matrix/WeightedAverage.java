/*
 * Created on 2007/02/07
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package matrix;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * << x >>を求めるクラス
 */
public class WeightedAverage {
	double x=0D;

	/**
	 *  
	 */
	public WeightedAverage(double init_x) {
		this.x = init_x;
	}

	public double getWeightedAverage(double d, double eta, double weight) {
		this.x += eta * ( weight * d - this.x);

		return this.x;
	}

	public double getWeightedAverage_hayami(double d, double eta, double weight) {
		this.x = eta * this.x + weight * d;
		return this.x;
	}
	
	public double getWeightedAverage() {
		return this.x;
	}
}
