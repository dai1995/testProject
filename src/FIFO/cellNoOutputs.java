package FIFO;

public class cellNoOutputs implements cell {
	protected double inputs[];
	protected int input;
	protected double error;
	
	public cellNoOutputs(double inputs[]) {
		this.inputs = inputs;
	}
	
	public cellNoOutputs(int data) {
		this.input = data;
	}
	
	@Override
	public double getError() {
		// TODO Auto-generated method stub
		return this.error;
	}

	@Override
	public double[] getInputs() {
		// TODO Auto-generated method stub
		return this.inputs;
	}

	/**
	 * @return the input
	 */
	public int getInput() {
		return input;
	}

	@Override
	public double[] getOutputs() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setError(double error) {
		// TODO Auto-generated method stub
		this.error = error;
	}

	@Override
	public int getOutputClass() {
		// TODO Auto-generated method stub
		System.err.println("Sorry! cellNoOutputs.getOutputClass() is not implemented!");
		System.exit(1);
		return 0;
	}

}
