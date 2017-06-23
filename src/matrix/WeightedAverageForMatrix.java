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
 */
public class WeightedAverageForMatrix {
	protected MatrixObj X;
	protected double S = 1D;
	MatrixCalc mc;
	/**
	 * 
	 */
	public WeightedAverageForMatrix(MatrixObj init_x) {
		this.X = init_x;
		this.mc = new MatrixCalc();
	}
	
	public MatrixObj getWeightedAverageMatrix(MatrixObj x, double eta, double weight) {
		MatrixObj add_x;
		add_x = mc.DiffMatrix_M(
				mc.MultiplyMatrix_M(x,weight), this.X);
		add_x = mc.MultiplyMatrix_M(add_x, eta);
		this.X = mc.AddMatrix_M(this.X, add_x);

		return this.X;
	}

	public MatrixObj getWeightedAverageMatrix_hayami(MatrixObj x, double eta, double weight) {
		MatrixObj add_x;
		add_x = mc.MultiplyMatrix_M(this.X, eta);
		add_x = mc.AddMatrix_M(
				mc.MultiplyMatrix_M(x,weight), add_x);
		this.X = add_x;

		return this.X;
	}
	
	
	public MatrixObj getWeightedAverageMatrix() {
		return this.X;
	}

}
