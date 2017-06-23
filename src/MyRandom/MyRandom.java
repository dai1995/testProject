package MyRandom;

public class MyRandom {
	public MyRandom() {
		
	}
	
	//平均0, 分散1の正規分布 N(0,1)
	public double nrnd(java.util.Random R) {
		int sw = 0;
		double r1=0, r2=0, s=0;

		if (sw == 0) {
			sw=1;
			do {
				r1 = 2 * R.nextDouble() - 1D;
				r2 = 2 * R.nextDouble() - 1D;
				s = r1 * r1 + r2 * r2;
			}while(s>1 || s==0);
			s = Math.sqrt(-2*Math.log(s)/s);
			return r1 * s;
		}else{
			sw = 0;
			return r2 * s;
		}
	}
	
}
