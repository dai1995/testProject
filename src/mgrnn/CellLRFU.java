package mgrnn;

import matrix.MatrixObj;

public class CellLRFU extends CellLinearDependency implements Cell {
	private final boolean DEBUG = false;
	private double gain, Cb=0D;
	private double delta;
	private MatrixObj MatrixAlpha;
	
	public CellLRFU(int NumberOfInput, int NumberOfOutput, double lambda) {
		super(NumberOfInput, NumberOfOutput);
		// TODO Auto-generated constructor stub
		System.out.println("lambda = " + lambda);
		this.gain = Math.pow(0.5, lambda);
		this.SetActivate(1.0D);
	}
	
	public void SetActivate(double ImportanceWeight) {
		this.Cb = ImportanceWeight + this.gain * this.Cb;
		//this.Cb = 1;
		this.Log("SetActivate() Cb=" + this.Cb);
	}
	
	public void Initialization(double initvalue) {
		this.Cb = initvalue;
		
		this.Log("SetActivate() Cb=" + this.Cb);
	}
	
	public void InActive() {
		this.Cb *= this.gain;
		this.Log("InActive() gain=" + this.gain + " Cb =" + this.Cb);
	}

	public double getCb() {
		return Cb;
	}

	/**
	 * @return the delta
	 */
	public double getDelta() {
		return delta;
	}

	/**
	 * @param delta the delta to set
	 */
	public void setDelta(double delta) {
		if (delta >= 0) {
			this.delta = delta;
		}else{
			System.err.println("CellLRFU.setDelta() delta value should be positive value. delta = " + delta);
			System.exit(1);
		}
	}

	/**
	 * @return the matrixAlpha
	 */
	public MatrixObj getMatrixAlpha() {
		return MatrixAlpha;
	}

	/**
	 * @param matrixAlpha the matrixAlpha to set
	 */
	public void setMatrixAlpha(MatrixObj matrixAlpha) {
		MatrixAlpha = matrixAlpha;
	}

	void Log(String log) {
		if (this.DEBUG) {
			System.out.println("CellLRFU." + log);
		}
	}

}
