/*
 * Created on 2008/08/23
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package MixtureOfDistributions;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MyMath {
	static final double LOG_2PI = 1.83787706640934548;
	static final int N=8;
	static final double B0=1.0;
	static final double B1=-0.5;
	static final double B2=1.0/6;
	static final double B4=-1.0/30.0;
	static final double B6=1.0/42.0;
	static final double B8=-1.0/30.0;
	static final double B10=5.0/66.0;
	static final double B12=-691.0/2730.0;
	static final double B14=7.0/6.0;
	static final double B16=-3617.0/510.0;

	static double loggamma(double x) {
		double v, w;
		double X;
		v=1;
		X = x;
		while (X < (double)N) {
			v *= X;
			X+=1D;
		}
		w = 1/(X*X);
		return ((((((((B16/(double)(16*15))  * w + (B14 / (double)(14*13))) * w
				    + (B12/(double)(12*11))) * w + (B10 / (double)(10*9)))  * w
					+ (B8 /(double)( 8*7 ))) * w + (B6  / (double)( 6*5)))  * w
					+ (B4 /(double)( 4*3 ))) * w + (B2  / (double)(2 *1))) / X
					+ 0.5 * LOG_2PI - Math.log(v) - X + (X-0.5) * Math.log(X);
	}
	
	public static double gamma(double x) {
		if (x < 0) {
			return Math.PI / (Math.sin(Math.PI * x) * Math.exp(loggamma(1-x)));
		}else{
			return Math.exp(loggamma(x));
		}
	}

}
