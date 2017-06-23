package MyRandom;

public class testGenerator {
	int OrderOfExcersize[];
	int NumberOfExcersize;
	
	public testGenerator(int NumberOfExcersize) {
		this.NumberOfExcersize = NumberOfExcersize;
		this.OrderOfExcersize = new int[this.NumberOfExcersize];
		for (int i=0; i<this.NumberOfExcersize; i++) {
			this.OrderOfExcersize[i] = i+1;
		}
		shuffle(this.NumberOfExcersize);
		display_result(this.NumberOfExcersize);
	}
	
	void display_result(int size) {
		for (int j=0; j<size; j++) {
			System.out.println((j+1)+": " + this.OrderOfExcersize[j]);
		}
	}
	
	void shuffle(int size) {
		int target;
		for (int i=0; i<size; i++) {
			target = (int)((double)size * Math.random());
			swap(i,target);
		}
	}
	
	void swap(int i, int j) {
		int data = this.OrderOfExcersize[i];
		this.OrderOfExcersize[i] = this.OrderOfExcersize[j];
		this.OrderOfExcersize[j] = data;
	}
	
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage: MyRandom.testGenerator [# of Excersizes]");
			System.exit(1);
		}
		new testGenerator(Integer.valueOf(args[0]).intValue());
	}
}
