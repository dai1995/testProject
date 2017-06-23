/*
 * Created on 2010/07/02
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mgrnn;
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
public class GRNNLD_test {
	boolean DEBUG = true;
	FunctionOutput functionout=null;
	multiple_hdim_dataOutput dataout=null;
	
	public GRNNLD_test(String parameterfilename) {
		
		double err_sum;
		int NumberOfIterations=0;
		
		ParameterReader pr = new ParameterReader(parameterfilename);
		grnnLinearDependency grnn = new grnnLinearDependency(pr.Reader("GRNNLD"));
		MultipleDataload dl = new MultipleDataload(pr.Reader("multipledataload"), 
								grnn.NumberOfInputs, grnn.NumberOfOutputs);
		multiple_dataOutput numberout = new multiple_dataOutput(pr.Reader("CellNumberOutput"));
		multiple_dataOutput errout = new multiple_dataOutput(pr.Reader("ErrorOutput"));
		//multiple_dataOutput sampleout = new multiple_dataOutput(pr.Reader("sampleout"));
		
		if (this.DEBUG) {
			functionout = new FunctionOutput(pr.Reader("FunctionOut"));
			dataout = new multiple_hdim_dataOutput(pr.Reader("testOut"));			
		}
		
		for (int ds=0; ds<dl.getNumberOfDataSets(); ds++) {
			dl.read(ds);
			NumberOfIterations=0;
			
			numberout.open(); errout.open();
			for (int day=0; day<dl.getMaxDates(); day++) {
				for (int p=0; p<dl.getNumberOfPracticePerDay() &&
					NumberOfIterations < dl.getActualNumberOfLearningSamples(); p++) {
					//mgrnnと名前がついているが、grnnなので注意!
					grnn.learning(dl.learning_input_patterns[day][p], 
							dl.learning_desired_patterns[day][p]);
					/*sampleout.put(dl.learning_input_patterns[day][p][0], 
						dl.learning_desired_patterns[day][p][0]);*/
					NumberOfIterations++;
				}
			
				//一般化誤差計測
				err_sum = this.get_MSE(grnn, dl);
				errout.put(NumberOfIterations, err_sum);
			
				//素子数計測
				numberout.put((double)NumberOfIterations, (double)grnn.getNumberOfHiddenUnits());
				System.out.println("Number of hiddenunits : " + grnn.getNumberOfHiddenUnits());
			
				//一般化誤差計測
				err_sum = this.get_MSE(grnn, dl);
				errout.put(NumberOfIterations+1, err_sum);
				
				if (DEBUG) {
					dataout.open();
					for (int t=0; t<dl.getActualNumberOfTestSamples(); t++) {
						dataout.put(dl.test_input_patterns[t],
								grnn.calculate_outputs(dl.test_input_patterns[t])[0]);
					}
					dataout.close();					
				}
			}
			System.out.println("Number of iterations: " + NumberOfIterations);
			numberout.close();
			errout.close();
		
			//中間細胞出力をgnuplot形式で出力(DEBUGの時のみ)
			if (this.DEBUG) {
				//grnn.HiddeUnitOutput(functionout);
			}
			
			grnn = new grnnLinearDependency(pr.Reader("GRNNLD"));//grnnをrefresh			
		}
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
		if (args.length<1) {
			System.err.println("Usage grnn [parameterfile.xml]");
			System.exit(1);
		}
		new GRNNLD_test(args[0]);
	}
}
