package RBFNN;

import matrix.MatrixException;
import datalogger.multiple_dataOutput;
import datalogger.multiple_hdim_dataOutput;
import DataLoad.dataload;
import FIFO.MinMoutWeightVariableSizedBuffer;
import ParameterReader.ParameterReader;



public class test {
	double beta[]=new double[1];
	MinMoutWeightVariableSizedBuffer buffer;
	public test(String parameterfile) {
		ParameterReader pr = new ParameterReader(parameterfile);
		RBFNN rbfnn = new RBFNN(pr.Reader("RBFNN"));
		this.beta[0]=100;
		//wRBFNN_with_BiasVarianceOutput wrbfnn_v = new wRBFNN_with_BiasVarianceOutput(pr.Reader("WRBFNN"));		
		wRBFNN_with_VarianceOutput wrbfnn_v = new wRBFNN_with_VarianceOutput(pr.Reader("WRBFNN"));		
		dataload dl = new dataload(pr.Reader("dataload"), rbfnn.NumberOfInputs, rbfnn.NumberOfOutputs);
		multiple_hdim_dataOutput mp_rbfnn_out = new multiple_hdim_dataOutput(pr.Reader("OutputFile"));
		multiple_hdim_dataOutput mp_wrbfnn_out = new multiple_hdim_dataOutput(pr.Reader("WOutputFile"));		
		multiple_dataOutput mp_rbfnn_error = new multiple_dataOutput(pr.Reader("ErrorOutput"));
		multiple_dataOutput mp_wrbfnn_error = new multiple_dataOutput(pr.Reader("WErrorOutput"));	
		buffer = new MinMoutWeightVariableSizedBuffer(1, 1);
		
		System.out.println("Actual number of learning samples is " + dl.getActualNumberOfLearningSamples());
		double weights[] = new double[dl.getActualNumberOfLearningSamples()];
		double w_center[] = new double[1];
		w_center[0] = 1;
		for (int day=0; day<dl.getMaxDates(); day++) {
			//for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
			
			for (int p=0; p<dl.getActualNumberOfLearningSamples(); p++) {
				//weights[p] = Math.exp(-Math.pow(VectorFunctions.getSqureNorm(VectorFunctions.diff(w_center, dl.learning_input_patterns[day][p])),2D)/10);
				weights[p] = 1;
				buffer.push_data(dl.learning_input_patterns[day][p], dl.learning_desired_patterns[day][p], 1, 1);
				System.out.println("weight[" + p + "]=" + weights[p]);
			}
			System.out.println("learning data is " + dl.learning_desired_patterns[day][0][0]);
				//rbfnn.Learning(dl.getActualNumberOfLearningSamples(), dl.learning_input_patterns[day], dl.learning_desired_patterns[day]);
			try {
				rbfnn.ARD(dl.getActualNumberOfLearningSamples(), dl.learning_input_patterns[day], dl.learning_desired_patterns[day], 0.000001d);
			} catch (MatrixException e) {
					// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("RBFNN: ARD() alpha=" + rbfnn.getAlpha());
			System.out.println("RBFNN: ARD() beta=" + rbfnn.getBeta()[0]);

				
				//wrbfnn.Learning(dl.getActualNumberOfLearningSamples(), dl.learning_input_patterns[day], 
						//dl.learning_desired_patterns[day],weights, 0.01, 10.0, 2.5,true);
			//wrbfnn_v.Learning(buffer, true);
			try {
				wrbfnn_v.ARD(buffer, true, 0.01);
			} catch (MatrixException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
				//α<βでなければならないことに注意
			//}
			for (int p=0; p<dl.getActualNumberOfLearningSamples(); p++) {
				//double[] pseudo_inputs = wrbfnn_v.generate_pseudo_input(p, 0.01, 0.01);
				double[] pseudo_inputs = wrbfnn_v.generate_pseudo_input2(p,wrbfnn_v);				
				double[] learning_inputs = buffer.getInput(p);
				for (int i=0; i<rbfnn.getNumberOfInputs(); i++) {
					System.out.println("pseudo_input[" + i + "]=" + pseudo_inputs[i]);
					System.out.println("learning_input[" + i + "]=" + learning_inputs[i]);
				}
			}			
		}

		//record outputs
		for (int day=0; day<dl.getMaxDates(); day++) {
			mp_rbfnn_out.open(); mp_rbfnn_error.open();
			mp_wrbfnn_out.open(); mp_wrbfnn_error.open();
			this.GetOutputs(dl, day, rbfnn, mp_rbfnn_out);
			this.GetOutputs(dl, day, wrbfnn_v, mp_wrbfnn_out);			
			this.EvalutateGeneralizationError(dl, day, rbfnn, mp_rbfnn_error);
			this.EvalutateGeneralizationError(dl, day, wrbfnn_v, mp_wrbfnn_error);			
			mp_rbfnn_out.close(); mp_rbfnn_error.close();
			mp_wrbfnn_out.close(); mp_wrbfnn_error.close();			
		}

	}
	
	void GetOutputs(dataload dl, int day, RBFNet solution,
			multiple_hdim_dataOutput mp_out) {
		double output[];
		for (int p = 0; p < dl.getActualNumberOfTestSamples(); p++) {
			output = solution.getOutputs(dl.test_input_patterns[p]);
			mp_out.put(dl.test_input_patterns[p], output[0]);
		}
	}	
	
	void GetOutputs(dataload dl, int day, wRBFNN_with_BiasVarianceOutput solution,
			multiple_hdim_dataOutput mp_out) {
		double output[];
		double variance[];
		for (int p = 0; p < dl.getActualNumberOfTestSamples(); p++) {
			output = solution.getOutputs(dl.test_input_patterns[p]);
			variance = solution.VarianceOutput(dl.test_input_patterns[p]);

			variance[0] = Math.sqrt(variance[0]);
			mp_out.put(dl.test_input_patterns[p], output[0], output[0]-variance[0], output[0]+variance[0]);
		}
	}			
	void GetOutputs(dataload dl, int day, wRBFNN_with_VarianceOutput solution,
			multiple_hdim_dataOutput mp_out) {
		double output[];
		double variance[];
		for (int p = 0; p < dl.getActualNumberOfTestSamples(); p++) {
			output = solution.getOutputs(dl.test_input_patterns[p]);
			variance = solution.VarianceOutput(dl.test_input_patterns[p]);

			variance[0] = Math.sqrt(variance[0]);
			mp_out.put(dl.test_input_patterns[p], output[0], output[0]-variance[0], output[0]+variance[0]);
		}
	}		
	void EvalutateGeneralizationError(dataload dl, int day, RBFNet solution,
			multiple_dataOutput mp_err) {
		double each_err;
		double err_sum = 0;
		for (int p = 0; p < dl.getActualNumberOfTestSamples(); p++) {
			each_err = solution.get_sqare_error(dl.test_input_patterns[p],
					dl.test_desired_patterns[p]);
			err_sum	 += each_err;
		}
		err_sum /= (double) dl.getActualNumberOfTestSamples();
		mp_err.put((double) day, err_sum);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length < 1) {
			System.err.println("Usage " + "test" + "[parameterfile.xml]\n"); 
			System.exit(1);		
		}
		new test(args[0]);		
	}

}
