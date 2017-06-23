package mgrnn;

public class CellFIFO extends GRNNCell  {
	private int age;
	public CellFIFO(int NumberOfInput, int NumberOfOutput) {
		super(NumberOfInput, NumberOfOutput);
		// TODO Auto-generated constructor stub
		this.age = 0;
	}
	/**
	 * @return the age
	 */
	public int getAge() {
		return age;
	}
	public void incrementalAge() {
		this.age ++;
	}
	public void resetAge() {
		this.age = 0;
	}

}
