package KernelPerceptron;

import org.w3c.dom.Node;

import datalogger.multiple_dataOutput;

import DataLoad.MultipleDataload;
import DataLoad.dataload;
import ParameterReader.ParameterReader;
import VectorFunctions.VectorFunctions;

public class test_KernelPerceptronLRFU4Regression {
	KernelPerceptronLRFU fp4r;
	public test_KernelPerceptronLRFU4Regression(String parameter_filename, String datasetname) {
		ParameterReader pr = new ParameterReader(parameter_filename);
		MultipleDataload dl = new MultipleDataload(pr.Reader(datasetname));
		Node node = pr.Reader("KernelPerceptronLRFU4Regression");
		this.fp4r = new KernelPerceptronLRFU(node);
		this.fp4r.setNumberOfInputs(dl.getNumberOfInputs());
		this.fp4r.setNumberOfOutputs(dl.getNumberOfOutputs());
		
		multiple_dataOutput errout = new multiple_dataOutput(pr.Reader("ErrorOutput"));
		
		learn_and_test(node, dl, errout);
		
	}
	
	public test_KernelPerceptronLRFU4Regression(String parameter_filename, String datasetname, int UpperLimitOfKernels) {
		ParameterReader pr = new ParameterReader(parameter_filename);
		MultipleDataload dl = new MultipleDataload(pr.Reader(datasetname));
		Node node = pr.Reader("KernelPerceptronLRFU4Regression");
		this.fp4r = new KernelPerceptronLRFU(node);
		this.fp4r.setNumberOfInputs(dl.getNumberOfInputs());
		this.fp4r.setNumberOfOutputs(dl.getNumberOfOutputs());
		this.fp4r.setUpperLimitOfKernels(UpperLimitOfKernels);
		
		multiple_dataOutput errout = new multiple_dataOutput(pr.Reader("ErrorOutput"));
		
		learn_and_test(node, dl, errout, UpperLimitOfKernels);
		
	}
	
	void learn_and_test(Node node, MultipleDataload dl, multiple_dataOutput errout) {
		int NumberOfIterations=0;
		double cumulative_error = 0D;
		double err_sum;
		
		for (int ds=0; ds<dl.getNumberOfDataSets(); ds++) {
			cumulative_error = 0D;
			errout.open();
			dl.read(ds);
			NumberOfIterations = 0;
			for (int day=0; day<dl.getMaxDates(); day++) {
				for (int p=0; p<dl.getNumberOfPracticePerDay() &&
					NumberOfIterations < dl.getActualNumberOfLearningSamples(); p++) {
					
					fp4r.learn(dl.learning_input_patterns[day][p], dl.learning_desired_patterns[day][p], 1D, fp4r.CBThreshold);
					if (fp4r.IsCumulativeErrorEstimation) {
						cumulative_error += fp4r.residual_error(dl.learning_input_patterns[day][p], dl.learning_desired_patterns[day][p]);
					}
					
					NumberOfIterations ++;
				}
				
				if (fp4r.IsCumulativeErrorEstimation) {
					errout.put(NumberOfIterations, cumulative_error);
				}else{
					err_sum = this.get_MSE(fp4r, dl);
					errout.put(NumberOfIterations, err_sum);
				}
			}
			errout.close();
			fp4r = new KernelPerceptronLRFU(node);
			fp4r.setNumberOfInputs(dl.getNumberOfInputs());
			fp4r.setNumberOfOutputs(dl.getNumberOfOutputs());
		}
	}
		
	void learn_and_test(Node node, MultipleDataload dl, multiple_dataOutput errout, int UpperLimitOfKernels) {
		int NumberOfIterations=0;
		double cumulative_error = 0D;
		double err_sum; 
		for (int ds=0; ds<dl.getNumberOfDataSets(); ds++) {
			errout.open();
			dl.read(ds);
			NumberOfIterations = 0;
			for (int day=0; day<dl.getMaxDates(); day++) {
				for (int p=0; p<dl.getNumberOfPracticePerDay() &&
					NumberOfIterations < dl.getActualNumberOfLearningSamples(); p++) {
					
					fp4r.learn(dl.learning_input_patterns[day][p], dl.learning_desired_patterns[day][p], 1D, fp4r.CBThreshold);
					if (fp4r.IsCumulativeErrorEstimation) {
						cumulative_error += fp4r.residual_error(dl.learning_input_patterns[day][p], dl.learning_desired_patterns[day][p]);
					}		
					//fp4r.learn_with_shrink(dl.learning_input_patterns[day][p], dl.learning_desired_patterns[day][p]);
					NumberOfIterations ++;
				}
				if (fp4r.IsCumulativeErrorEstimation) {
					errout.put(NumberOfIterations, cumulative_error);
				}else{
					err_sum = this.get_MSE(fp4r, dl);
					errout.put(NumberOfIterations, err_sum);
				}				
			}
			errout.close();
			fp4r = new KernelPerceptronLRFU(node);
			fp4r.setNumberOfInputs(dl.getNumberOfInputs());
			fp4r.setNumberOfOutputs(dl.getNumberOfOutputs());
			fp4r.setUpperLimitOfKernels(UpperLimitOfKernels);
		}		
	}
	
	//一般化誤差計測
	double get_MSE(Forgetron4Regression fp4r, dataload dl) {
		double err_sum=0D;
		double[] diff;
		double[] test_outputs;
		for (int p=0; p<dl.getActualNumberOfTestSamples(); p++) {
			test_outputs = fp4r.output(dl.test_input_patterns[p]);
			diff = VectorFunctions.diff(dl.test_desired_patterns[p], 
					test_outputs);
			err_sum += VectorFunctions.getSqureNorm(diff);
		}
		err_sum /= dl.getActualNumberOfTestSamples();
		return err_sum;
	}	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		if (args.length<2) {
			System.err.println("Usage: java test_kernelPerceptron [parameter filename][datasetname]([UpperLimitOfKernels])");
			System.exit(1);
		}
		if (args.length == 2) {
			new test_KernelPerceptronLRFU4Regression(args[0], args[1]);
		}else if (args.length == 3) {
			new test_KernelPerceptronLRFU4Regression(args[0], args[1], Integer.valueOf(args[2]));
		}
	}

}
