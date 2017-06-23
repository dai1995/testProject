package DataAnalysis;

public class testAutoDetectConfidenceInterval {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		if (args.length < 2) {
			System.err.println("Usage " + "testDataAnalysis" + "[parameterfile.xml] [datasetname] [(ID_str)]\n"); 
			System.exit(1);		
		}
		if (args.length ==2) {
			new DetectConfidenceInterval(args[0], args[1]);
		}else if (args.length == 3){
			new DetectConfidenceInterval(args[0], args[1], args[2]);
		}

	}

}
