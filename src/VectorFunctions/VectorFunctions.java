/*
 * Created on 2005/06/28
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package VectorFunctions;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class VectorFunctions {
	
	public static double DotProduct(double x[], double y[]) {
		double sum=0;
		if (x.length != y.length) {
			System.err.println("VectorFunctions.DotProduct: The sizes of the two vectors are not the same!");
			System.exit(1);
		}
		for (int i=0; i<x.length; i++) {
			sum += x[i] * y[i];
		}
		return sum;
	}
	
	public static double[] Normalize(double x[]) {
		double sum=0;
		double[] y = new double[x.length];
		sum = getNorm(x);
		if (sum > 0) {
			for (int j=0; j<x.length; j++) {
				y[j] = x[j] / sum;
			}
		}
		return y;
	}
	
	public static double getNorm(double x[]) {
		double sum=0D;
		for (int i=0; i<x.length; i++) {
			sum += Math.pow(x[i],2D);
		}
		sum = Math.sqrt(sum);
		return sum;
	}
	

	public static double getSqureNorm(double x[]) {
		double sum=0D;
		for (int i=0; i<x.length; i++) {
			sum += Math.pow(x[i],2D);
		}
		return sum;
	}
	
	public static void display_vector(String VectorName, double x[]) {
		System.out.print("display_vector \"" + VectorName + "\":");
		for (int i=0; i<x.length; i++) {
			System.out.print(" " + x[i]);
			if (i<x.length-1) System.out.print(",");
			else System.out.println(" ");
		}
	}
	
	public static double[] diff(double x[], double y[]) {
		double[] diff = new double[x.length];
		for (int i=0; i<x.length; i++) {
			diff[i] = x[i] - y[i];
		}
		return diff;
	}
	public static double[] add(double x[], double y[]) {
		double[] add = new double[x.length];
		for (int i=0; i<x.length; i++) {
			add[i] = x[i] + y[i];
		}
		return add;
	}
	
	public static double[] multiply(double g, double x[]) {
		double[] result = new double[x.length];
		for (int i=0; i<x.length; i++) {
			result[i] = x[i]*g;
		}
		return result;
	}
	
			
}
