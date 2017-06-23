/*
 * Created on 2005/08/20
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package DataAnalysis;
import ParameterReader.ParameterReader;
import DataLoad.*;
import datalogger.*;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class EachSampleAnalysis {
	public ExtendedMultipleDataload emd[];
	double mean_data[][], var_data[][], upper[][], lower[][], std[][];
	int NumberOfInputs, NumberOfOutputs;
	int NumberOfDataSets, TotalNumberOfSamples;
	boolean IsHeadPathNumbering = true;
	String header;
	
	public EachSampleAnalysis(ParameterReader pr, String DataTag, String HeadPath, int NumberOfDataSets, int NumberOfOutputDim, boolean IsHeadPathNumbering) {
		this.NumberOfInputs = 1;
		this.NumberOfOutputs = NumberOfOutputDim;
		this.NumberOfDataSets = NumberOfDataSets;
		this.TotalNumberOfSamples = 0;
		this.IsHeadPathNumbering = IsHeadPathNumbering;
		if (!this.IsHeadPathNumbering) {
			this.emd = new ExtendedMultipleDataload[1];
			header = HeadPath + "/"; 
			this.emd[0] = new ExtendedMultipleDataload(pr.Reader(DataTag),this.NumberOfInputs,this.NumberOfOutputs, header);
			this.TotalNumberOfSamples += this.emd[0].getActualNumberOfTestSamples();
			//this.NumberOfDataSets = 1;/// added by k.yamauchi 2010-8-6
		}else{
			this.emd = new ExtendedMultipleDataload[NumberOfDataSets];
			for (int data=0; data<this.NumberOfDataSets; data++) {
				System.out.println(">>>>>>Head path is " + HeadPath);
				header = HeadPath + data + "/"; 				
				this.emd[data] = new ExtendedMultipleDataload(pr.Reader(DataTag),this.NumberOfInputs,this.NumberOfOutputs, header);
				this.TotalNumberOfSamples += this.emd[data].getActualNumberOfTestSamples();
			}
		}
		mean_data = new double[this.emd[0].getActualNumberOfTestSamples()][this.NumberOfOutputs];
		var_data = new double[this.emd[0].getActualNumberOfTestSamples()][this.NumberOfOutputs];
		upper = new double[this.emd[0].getActualNumberOfTestSamples()][this.NumberOfOutputs];		
		lower = new double[this.emd[0].getActualNumberOfTestSamples()][this.NumberOfOutputs];				
		std = new double[this.emd[0].getActualNumberOfTestSamples()][this.NumberOfOutputs];						
	}
	public EachSampleAnalysis(ParameterReader pr, String DataTag, String HeadPath, int NumberOfDataSets, int NumberOfOutputDim, boolean IsHeadPathNumbering, String ID_str) {
		this.NumberOfInputs = 1;
		this.NumberOfOutputs = NumberOfOutputDim;
		this.NumberOfDataSets = NumberOfDataSets;
		this.TotalNumberOfSamples = 0;
		this.IsHeadPathNumbering = IsHeadPathNumbering;
		this.Log("EachSampleAnalysis(): constructor ");
		if (!this.IsHeadPathNumbering) {
			this.Log("EachSampleAnalysis() is not HeadPathNumbering");
			this.emd = new ExtendedMultipleDataload[1];
			header = HeadPath + "/"; 
			this.emd[0] = new ExtendedMultipleDataload(pr.Reader(DataTag),this.NumberOfInputs,this.NumberOfOutputs, header, ID_str);
			this.TotalNumberOfSamples += this.emd[0].getActualNumberOfTestSamples();
			//this.NumberOfDataSets = 1;/// added by k.yamauchi 2010-8-6
		}else{
			this.emd = new ExtendedMultipleDataload[NumberOfDataSets];
			for (int data=0; data<this.NumberOfDataSets; data++) {
				System.out.println(">>>>>>Head path is " + HeadPath);
				header = HeadPath + data + "/"; 				
				this.emd[data] = new ExtendedMultipleDataload(pr.Reader(DataTag),this.NumberOfInputs,this.NumberOfOutputs, header, ID_str);
				this.TotalNumberOfSamples += this.emd[data].getActualNumberOfTestSamples();
			}
		}
		mean_data = new double[this.emd[0].getActualNumberOfTestSamples()][this.NumberOfOutputs];
		var_data = new double[this.emd[0].getActualNumberOfTestSamples()][this.NumberOfOutputs];
		upper = new double[this.emd[0].getActualNumberOfTestSamples()][this.NumberOfOutputs];		
		lower = new double[this.emd[0].getActualNumberOfTestSamples()][this.NumberOfOutputs];				
		std = new double[this.emd[0].getActualNumberOfTestSamples()][this.NumberOfOutputs];						
	}	
	
	void calculate_mean_variance(double t_value) {
		double sum;
		double var;
		double standard_var;
		int NumberOfInstances=0;
		this.Log("calculate_mean_variance(" + t_value + ")");
		if (this.NumberOfDataSets > 0) {
			NumberOfInstances = this.NumberOfDataSets * emd[0].getNumberOfDataSets();
		}else{
			NumberOfInstances = emd[0].getNumberOfDataSets();
		}
		for (int t=0; t<emd[0].getActualNumberOfTestSamples(); t++) {
			for (int o=0; o<this.NumberOfOutputs; o++) {
				sum = 0;
				for (int data=0; data<this.NumberOfDataSets; data++) {
					for (int dataset=0; dataset<this.emd[data].getNumberOfDataSets(); dataset++) {
						sum += emd[data].test_desired_patterns[dataset][t][o];
					}
				}
				sum /= (double)NumberOfInstances;//平均
				
				var = 0;
				for (int data=0; data<this.NumberOfDataSets; data++) {
					for (int dataset=0; dataset<this.emd[data].getNumberOfDataSets(); dataset++) {
						var += Math.pow((emd[data].test_desired_patterns[dataset][t][o] - sum),2D);
					}
				}
				var /= (double)(NumberOfInstances-1);//不偏分散
				System.out.println("NumberOfInstances is " + NumberOfInstances);
				//配列に格納
				this.mean_data[t][o] = sum;
				this.var_data[t][o] = var;
				standard_var =Math.sqrt(var / NumberOfInstances);
				this.upper[t][o] = this.mean_data[t][o] + t_value * standard_var;
				this.std[t][o] = standard_var;
				this.lower[t][o] = this.mean_data[t][o] - t_value * standard_var;
			}
		}
	}
	
	void write_results(dataOutput dataout) {
		this.Log("write_results() NumberOfOutputs = " + this.NumberOfOutputs);
		for (int t=0; t<emd[0].getActualNumberOfTestSamples(); t++) {
			for (int o=0; o<this.NumberOfOutputs; o++) {
				//System.out.println("t=" + t + " o=" + o);
				this.Log("write_results()  mean_data[" + t +"][" + o + "]=" + this.mean_data[t][o]);
				dataout.put(this.getInput(t), this.mean_data[t][o], this.upper[t][o], this.lower[t][o]);
			}
		}
	}
	
	void Log(String log) {
		System.out.println("EachSampleAnalysis." + log);
	}
	
	public double getInput(int index) {
		return this.emd[0].test_input_patterns[0][index][0];//どの入力も共通かつ１次元なので[0][index][0]になっている
	}
	
	public double getUpper(int index, int output) {
		return this.upper[index][output];
	}
	
	public double getLower(int index, int output) {
		return this.lower[index][output];
	}
	
	public double getMean(int index, int output) {
		return this.mean_data[index][output];
	}
	
	public int getNumberOfDataSets() {
		return this.emd[0].getNumberOfDataSets() * this.NumberOfDataSets;
	}

}
