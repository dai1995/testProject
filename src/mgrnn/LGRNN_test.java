/*
 * Created on 2010/07/02
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mgrnn;
//import---------------------------------------------------------------------------
//他のパッケージから読み込めるようにする
import ParameterReader.ParameterReader;
import DataLoad.*;
import datalogger.*;
import VectorFunctions.*;
/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */ 
public class LGRNN_test {
	boolean DEBUG = false;
	FunctionOutput functionout=null;
	multiple_hdim_dataOutput dataout=null;
	int UpperLimitOfKernels;
	
	public LGRNN_test(String parameterfilename, String datasetname) {
		
		double err_sum;
		int NumberOfIterations=0;
		LgrnnLearningStatus result;
		ParameterReader pr = new ParameterReader(parameterfilename);//ParameterReader.javaからfilenameをもらう
		MultipleDataload dl = new MultipleDataload(pr.Reader(datasetname));//MultipleDaraloadへ飛ぶ
		LimitedGRNN grnn = new LimitedGRNN(pr.Reader("LGRNN"));
		grnn.setNumberOfInputs(dl.getNumberOfInputs());
		grnn.setNumberOfOutputs(dl.getNumberOfOutputs());
		multiple_dataOutput numberout = new multiple_dataOutput(pr.Reader("CellNumberOutput"));
		multiple_dataOutput errout = new multiple_dataOutput(pr.Reader("ErrorOutput"));
		multiple_dataOutput sampleout = new multiple_dataOutput(pr.Reader("sampleout"));
		double cumulative_error = 0D;
		
		if (DEBUG) {
			dataout=new multiple_hdim_dataOutput(pr.Reader("testOut"));
			functionout = new FunctionOutput(pr.Reader("FunctionOut"));
		}
		
		for (int ds=0; ds<dl.getNumberOfDataSets(); ds++) {
			dl.read(ds);
			NumberOfIterations=0;
			
			numberout.open(); errout.open();
			for (int day=0; day<dl.getMaxDates(); day++) {
				for (int p=0; p<dl.getNumberOfPracticePerDay() &&
					NumberOfIterations < dl.getActualNumberOfLearningSamples(); p++) {
					//mgrnnと名前がついているが、grnnなので注意!
					
					result = grnn.learning(dl.learning_input_patterns[day][p], //入力
								dl.learning_desired_patterns[day][p]);//出力
					
					cumulative_error += result.residual_error;
					/*sampleout.put(dl.learning_input_patterns[day][p][0], 
						dl.learning_desired_patterns[day][p][0]);*/
					NumberOfIterations++;
				}
			
				//一般化誤差計測
				if (grnn.IsCumlativeErrorEstimation()) {
					errout.put(NumberOfIterations, cumulative_error);
				}else{
					err_sum = this.get_MSE(grnn, dl);
					errout.put(NumberOfIterations, err_sum);
				}
			
				//素子数計測
				numberout.put((double)NumberOfIterations, (double)grnn.getNumberOfHiddenUnits());
			
				if (DEBUG) {
					dataout.open();
					for (int t=0; t<dl.getActualNumberOfTestSamples(); t++) {
						dataout.put(dl.test_input_patterns[t],
							grnn.calculate_outputs(dl.test_input_patterns[t])[0]);
					}
					dataout.close();
					functionout.open();
					grnn.HiddeUnitOutput(functionout);
					functionout.close();
				}
			}// for day
			System.out.println("Number of iterations: " + NumberOfIterations);
			numberout.close();
			errout.close();
		
			//中間細胞出力をgnuplot形式で出力(DEBUGの時のみ)
			if (this.DEBUG) {
				sampleout.open();
				//grnn.HiddeUnitOutput(functionout);
				for (int t=0; t<dl.getActualNumberOfTestSamples(); t++) {
					sampleout.put(dl.test_input_patterns[t][0],dl.test_desired_patterns[t][0]);
				}
				sampleout.close();
			}
			//pr = new ParameterReader(parameterfilename);
			grnn = new LimitedGRNN(pr.Reader("LGRNN"));//grnnをrefresh
			grnn.setNumberOfInputs(dl.getNumberOfInputs());
			grnn.setNumberOfOutputs(dl.getNumberOfOutputs());
			cumulative_error = 0D;
		}//for
	}

	public LGRNN_test(String parameterfilename, String datasetname, int UpperLimitOfKernels) {
		
		double err_sum;
		int NumberOfIterations=0;
		this.UpperLimitOfKernels = UpperLimitOfKernels;
		double cumlative_error= 0D;
		LgrnnLearningStatus result;
		
		ParameterReader pr = new ParameterReader(parameterfilename);
		MultipleDataload dl = new MultipleDataload(pr.Reader(datasetname));
		LimitedGRNN grnn = new LimitedGRNN(pr.Reader("LGRNN"));
		grnn.setNumberOfInputs(dl.getNumberOfInputs());
		grnn.setNumberOfOutputs(dl.getNumberOfOutputs());
		grnn.setUpperLimitOfHiddenUnits(this.UpperLimitOfKernels);
		multiple_dataOutput numberout = new multiple_dataOutput(pr.Reader("CellNumberOutput"));
		multiple_dataOutput errout = new multiple_dataOutput(pr.Reader("ErrorOutput"));
		multiple_dataOutput sampleout = new multiple_dataOutput(pr.Reader("sampleout"));
		
		if (DEBUG) {
			dataout=new multiple_hdim_dataOutput(pr.Reader("testOut"));
			functionout = new FunctionOutput(pr.Reader("FunctionOut"));
		}
		
		for (int ds=0; ds<dl.getNumberOfDataSets(); ds++) {
			dl.read(ds);
			NumberOfIterations=0;
			
			numberout.open(); errout.open();
			for (int day=0; day<dl.getMaxDates(); day++) {
				for (int p=0; p<dl.getNumberOfPracticePerDay() &&
					NumberOfIterations < dl.getActualNumberOfLearningSamples(); p++) {
					//mgrnnと名前がついているが、grnnなので注意!
					result =  grnn.learning(dl.learning_input_patterns[day][p], 
								dl.learning_desired_patterns[day][p]);
					System.out.println("upper limit of kernels: " + grnn.UpperLimitOfHiddenUnits);
					cumlative_error += result.residual_error;
					/*sampleout.put(dl.learning_input_patterns[day][p][0], 
						dl.learning_desired_patterns[day][p][0]);*/
					NumberOfIterations++;
				}
			
				//一般化誤差計測
				
				if (grnn.IsCumlativeErrorEstimation()) {
					System.out.println("=========================Cumulative Error Estimation==========================");
					errout.put(NumberOfIterations, cumlative_error);
				}else{
					System.out.println("=========================Mean Squared Error Estimation==========================");
					err_sum = this.get_MSE(grnn, dl);
					errout.put(NumberOfIterations, err_sum);
				}
			
				//素子数計測
				numberout.put((double)NumberOfIterations, (double)grnn.getNumberOfHiddenUnits());
			
				if (DEBUG) {
					dataout.open();
					for (int t=0; t<dl.getActualNumberOfTestSamples(); t++) {
						dataout.put(dl.test_input_patterns[t],
							grnn.calculate_outputs(dl.test_input_patterns[t])[0]);
					}
					dataout.close();
					functionout.open();
					grnn.HiddeUnitOutput(functionout);
					functionout.close();
				}
			}// for day
			System.out.println("Number of iterations: " + NumberOfIterations);
			numberout.close();
			errout.close();
		
			//中間細胞出力をgnuplot形式で出力(DEBUGの時のみ)
			if (this.DEBUG) {
				sampleout.open();
				//grnn.HiddeUnitOutput(functionout);
				for (int t=0; t<dl.getActualNumberOfTestSamples(); t++) {
					sampleout.put(dl.test_input_patterns[t][0],dl.test_desired_patterns[t][0]);
				}
				sampleout.close();
			}
			//pr = new ParameterReader(parameterfilename);
			grnn = new LimitedGRNN(pr.Reader("LGRNN"));//grnnをrefresh
			grnn.setNumberOfInputs(dl.getNumberOfInputs());
			grnn.setNumberOfOutputs(dl.getNumberOfOutputs());
			grnn.setUpperLimitOfHiddenUnits(this.UpperLimitOfKernels);
			cumlative_error = 0D;
		}//for
	}
	
	public LGRNN_test(String parameterfilename, String datasetname, int UpperLimitOfKernels, String ID_str) {
		
		double err_sum;
		int NumberOfIterations=0;
		this.UpperLimitOfKernels = UpperLimitOfKernels;
		double cumlative_error= 0D;
		LgrnnLearningStatus result;
		
		ParameterReader pr = new ParameterReader(parameterfilename);
		MultipleDataload dl = new MultipleDataload(pr.Reader(datasetname));
		LimitedGRNN grnn = new LimitedGRNN(pr.Reader("LGRNN"));
		grnn.setNumberOfInputs(dl.getNumberOfInputs());
		grnn.setNumberOfOutputs(dl.getNumberOfOutputs());
		grnn.setUpperLimitOfHiddenUnits(this.UpperLimitOfKernels);
		multiple_dataOutput numberout = new multiple_dataOutput(pr.Reader("CellNumberOutput"), ID_str);
		multiple_dataOutput errout = new multiple_dataOutput(pr.Reader("ErrorOutput"), ID_str);
		multiple_dataOutput sampleout = new multiple_dataOutput(pr.Reader("sampleout"), ID_str);
		
		if (DEBUG) {
			dataout=new multiple_hdim_dataOutput(pr.Reader("testOut"));
			functionout = new FunctionOutput(pr.Reader("FunctionOut"));
		}
		
		for (int ds=0; ds<dl.getNumberOfDataSets(); ds++) {
			dl.read(ds);
			NumberOfIterations=0;
			
			numberout.open(); errout.open();
			for (int day=0; day<dl.getMaxDates(); day++) {
				for (int p=0; p<dl.getNumberOfPracticePerDay() &&
					NumberOfIterations < dl.getActualNumberOfLearningSamples(); p++) {
					//mgrnnと名前がついているが、grnnなので注意!
					result =  grnn.learning(dl.learning_input_patterns[day][p], 
								dl.learning_desired_patterns[day][p]);
					System.out.println("upper limit of kernels: " + grnn.UpperLimitOfHiddenUnits);
					cumlative_error += result.residual_error;
					/*sampleout.put(dl.learning_input_patterns[day][p][0], 
						dl.learning_desired_patterns[day][p][0]);*/
					NumberOfIterations++;
				}
			
				//一般化誤差計測
				
				if (grnn.IsCumlativeErrorEstimation()) {
					System.out.println("=========================Cumulative Error Estimation==========================");
					errout.put(NumberOfIterations, cumlative_error);
				}else{
					System.out.println("=========================Mean Squared Error Estimation==========================");
					err_sum = this.get_MSE(grnn, dl);
					errout.put(NumberOfIterations, err_sum);
				}
			
				//素子数計測
				numberout.put((double)NumberOfIterations, (double)grnn.getNumberOfHiddenUnits());
			
				if (DEBUG) {
					dataout.open();
					for (int t=0; t<dl.getActualNumberOfTestSamples(); t++) {
						dataout.put(dl.test_input_patterns[t],
							grnn.calculate_outputs(dl.test_input_patterns[t])[0]);
					}
					dataout.close();
					functionout.open();
					grnn.HiddeUnitOutput(functionout);
					functionout.close();
				}
			}// for day
			System.out.println("Number of iterations: " + NumberOfIterations);
			numberout.close();
			errout.close();
		
			//中間細胞出力をgnuplot形式で出力(DEBUGの時のみ)
			if (this.DEBUG) {
				sampleout.open();
				//grnn.HiddeUnitOutput(functionout);
				for (int t=0; t<dl.getActualNumberOfTestSamples(); t++) {
					sampleout.put(dl.test_input_patterns[t][0],dl.test_desired_patterns[t][0]);
				}
				sampleout.close();
			}
			//pr = new ParameterReader(parameterfilename);
			grnn = new LimitedGRNN(pr.Reader("LGRNN"));//grnnをrefresh
			grnn.setNumberOfInputs(dl.getNumberOfInputs());
			grnn.setNumberOfOutputs(dl.getNumberOfOutputs());
			grnn.setUpperLimitOfHiddenUnits(this.UpperLimitOfKernels);
			cumlative_error = 0D;
		}//for
	}	
	//一般化誤差計測
	double get_MSE(grnnLinearDependency grnn, dataload dl) {
		double err_sum=0D;
		double[] diff;
		double[] test_outputs;
		for (int p=0; p<dl.getActualNumberOfTestSamples(); p++) {
			test_outputs = grnn.calculate_outputs(dl.test_input_patterns[p]);
			diff = VectorFunctions.diff(dl.test_desired_patterns[p], test_outputs);
			err_sum += VectorFunctions.getSqureNorm(diff);
		}
		err_sum /= dl.getActualNumberOfTestSamples();
		return err_sum;
	}
	
	
	public static void main(String[] args) {
		if (args.length<2) {
			System.err.println("Usage java mgrnn.LGRNN_test [parameterfile.xml][datasetname]([upper limit of hiddenunits][ID_str])");
			System.exit(1);
		}
		if (args.length == 2) {
			new LGRNN_test(args[0], args[1]);
		}else if (args.length == 3) {
			new LGRNN_test(args[0], args[1], Integer.valueOf(args[2]));
		}else if (args.length == 4) {
			new LGRNN_test(args[0], args[1], Integer.valueOf(args[2]), args[3]);
		}
	}
}
