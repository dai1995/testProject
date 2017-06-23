package DataAnalysis;

public class test_DetectConficenceInterval {
	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage " + "DataAnalysis" + "[parameterfile.xml]\n"); 
			System.exit(1);		
		}
		if (args.length == 1) {
			new DetectConfidenceInterval(args[0]);
		}
	}
}
