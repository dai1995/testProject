package MixtureOfDistributions;

import org.w3c.dom.Node;

import datalogger.dataOutput;
import datalogger.multiple_dataOutput;
import DataLoad.MultipleDataload;
import ParameterReader.ParameterReader;

public class test {
	MultipleDataload dl;
	MixtureOfNormalDistributions MN;
	
	public test(String parameterfile) {

		ParameterReader pr = new ParameterReader(parameterfile);
		GetStatisticalModel st = new GetStatisticalModel(pr);
		this.dl = new MultipleDataload(pr
				.Reader("multipledataload"), st.NumberOfInputs,
				st.NumberOfOutputs);
		this.dl.read(0);
		this.MN = st.constructBestModel(dl);
		
		//出力してみる
		//int target_dimension = 0;
		//this.put_distribution(pr.Reader("OutputFile"), this.dl, target_dimension, this.MN); //特定の次元対P(x)
		this.put_multidim_distribution(pr.Reader("OutputFile"), this.dl, this.MN);//全次元対P(x)
		
	}
	
	void put_distribution(Node nd, MultipleDataload dl, int target_dimension, MixtureOfNormalDistributions MN) {
		dataOutput data_out = new dataOutput(nd);
		double out;

		for (int p=0; p<dl.getActualNumberOfTestSamples(); p++) {

			out = MN.P(dl.test_input_patterns[p]);
			data_out.put(dl.test_input_patterns[p][target_dimension], out);
		}
		data_out.close();
	}
	
	void put_multidim_distribution(Node nd, MultipleDataload dl, MixtureOfNormalDistributions MN) {
		multiple_dataOutput data_out = new multiple_dataOutput(nd);
		double out;

		data_out.open();
		for (int p=0; p<dl.getActualNumberOfTestSamples(); p++) {

			out = MN.P(dl.test_input_patterns[p]);
			data_out.put(dl.test_input_patterns[p], out);
		}
		data_out.close();
		data_out.open();
		for (int p=0; p<dl.getActualNumberOfTestSamples(); p++) {
			out = MN.q_div_p(dl.test_input_patterns[p]);
			//out = MN.studentT(dl.test_input_patterns[p]);
			//System.out.println("out is " + out);
			data_out.put(dl.test_input_patterns[p], out);
		}
		data_out.close();
		
	}	
	
	public static void main(String[] args) {
		if (args.length<1) {
			System.err.println("Usage: MxtureOfDistribution.test <parameterfilename>");
			System.exit(1);
		}
		
		new test(args[0]);
	}
}
