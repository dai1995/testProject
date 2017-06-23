package RBFNN;

import java.util.Scanner;

import matrix.MatrixException;
import datalogger.multiple_hdim_dataOutput;
import DataLoad.dataload;
import FIFO.MinMoutWeightVariableSizedBuffer;
import ParameterReader.ParameterReader;

public class testWRBF {
	
	public testWRBF(String ModelName, String parameterfile) {
		ParameterReader pr = new ParameterReader(parameterfile);
		
		multiple_hdim_dataOutput mp_rbfnn_out = new multiple_hdim_dataOutput(pr.Reader("OutputFile"));
		mp_rbfnn_out.open();
		if (ModelName.equals("WRBFNNELM")) {
				wRBFNNELM net = new wRBFNNELM(pr.Reader("WRBFNNELM"));
				dataload dl = new dataload(pr.Reader("dataload"), 
						net.getNumberOfInputs(), 
						net.getNumberOfOutputs());
				testWRBFNNELM(dl,net,mp_rbfnn_out);
	    }
		mp_rbfnn_out.close();
		//else if (ModelName.)
	}
	
	void testWRBFNNELM(dataload dl, wRBFNNELM net, multiple_hdim_dataOutput mout) {
		Scanner KeyboardScanner = new Scanner(System.in);
		//データをバッファに貯める
		MinMoutWeightVariableSizedBuffer buffer = new MinMoutWeightVariableSizedBuffer(net.getNumberOfInputs(), net.getNumberOfOutputs());
		for (int day=0; day<dl.getMaxDates(); day++) {
			for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
				buffer.push_data(dl.learning_desired_patterns[day][p], dl.learning_desired_patterns[day][p], 1, 1);
			}
		}
		//offline学習
		try {
			System.out.println("buffer size is " + buffer.getSize());
			net.Learning(buffer, buffer.getSize());
		}catch(MatrixException ex) {
			ex.printStackTrace();
		}
		KeyboardScanner.next();
		//テスト
		
		double each_error=0, total_error=0;
		for (int n=0; n<dl.getActualNumberOfTestSamples(); n++) {
			mout.put(dl.test_input_patterns[n], 
					net.getOutputs(dl.test_input_patterns[n])[0]);
			each_error = net.get_sqare_error(dl.test_input_patterns[n], dl.test_desired_patterns[n], 1);
			total_error += each_error;
		}
		total_error /= (double)dl.getActualNumberOfTestSamples();
		System.out.println("MSE for test samples = " + total_error);
		mout.close();
	}// testWRBFNNELM()

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length < 2) {
			System.err.println("Usage: java RBFNN.testWRBF 'WRBFNNELM/*' <parameterfile>");
			System.exit(1);
		}
		new testWRBF(args[0],args[1]);
	}

}
