/*
 * Created on 2005/08/25
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package DataAnalysis;

import ParameterReader.ParameterReader;
import datalogger.*;

/**
 * @author yamauchi
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class DetectDiffDistribution {
	EachSampleAnalysis esa1, esa2;

	int actual_number_of_dataset;

	public DetectDiffDistribution(String parameterfile) {
		ParameterReader pr = new ParameterReader(parameterfile);
		DataAnalysisParameters d_parameter = new DataAnalysisParameters();
		d_parameter.getParameter(pr.Reader("DataAnalysis"));//パラメータの取得
		dataOutput dataout = new dataOutput(pr.Reader("ResultFileName"));
		multiple_dataOutput m_dataout = new multiple_dataOutput(pr.Reader("EachDiffFileName"));		
		this.esa1 = new EachSampleAnalysis(pr, "ext_multipledataload1",
				d_parameter.DirectoryHeadPath1, d_parameter.NumberOfDataSets,
				d_parameter.NumberOfOutputDim, d_parameter.IsHeadPathNumbering);
		this.esa2 = new EachSampleAnalysis(pr, "ext_multipledataload2",
				d_parameter.DirectoryHeadPath2, d_parameter.NumberOfDataSets,
				d_parameter.NumberOfOutputDim, d_parameter.IsHeadPathNumbering);
		//diff(this.esa1, this.esa2, d_parameter.NumberOfOutputDim);
		diff(this.esa1, this.esa2, d_parameter.NumberOfOutputDim, m_dataout);		

		this.actual_number_of_dataset = this.esa1.getNumberOfDataSets();

		this.esa1.calculate_mean_variance(d_parameter
				.get_t_value(this.actual_number_of_dataset));
		this.esa1.write_results(dataout);
		dataout.close();
	}

	void diff(EachSampleAnalysis esa1, EachSampleAnalysis esa2,
	    int NumberOfOutput, multiple_dataOutput md) {
		for (int data = 0; data < esa1.NumberOfDataSets; data++) {
			for (int dataset = 0; dataset < esa1.emd[0].getNumberOfDataSets(); dataset++) {
				md.open();
				for (int p = 0; p < esa1.emd[0].getActualNumberOfTestSamples(); p++) {
					for (int o = 0; o < NumberOfOutput; o++) {
						esa1.emd[data].test_desired_patterns[dataset][p][o] -= esa2.emd[data].test_desired_patterns[dataset][p][o];
						md.put(esa1.getInput(p), esa1.emd[data].test_desired_patterns[dataset][p][o]);
					}
				}
				md.close();
			}
		}
	}

	public static void main(String[] args) {
		if (args.length < 1) {
			System.err.println("Usage " + "DataAnalysis"
					+ "[parameterfile.xml]\n");
			System.exit(1);
		}
		new DetectDiffDistribution(args[0]);
	}
}
