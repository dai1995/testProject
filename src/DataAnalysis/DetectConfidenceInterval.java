/*
 * Created on 2005/08/19
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
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class DetectConfidenceInterval {
	//ExtendedMultipleDataload[] emd;
	
	EachSampleAnalysis esa; 
	int actual_number_of_dataset;
	public DetectConfidenceInterval(String parameterfile) {
		ParameterReader pr = new ParameterReader(parameterfile);
		DataAnalysisParameters d_parameter = new DataAnalysisParameters();
		d_parameter.getParameter(pr.Reader("DataAnalysis"));//パラメータの取得
		dataOutput dataout = new dataOutput(pr.Reader("ResultFileName"));
		//this.read_all_datasets(d_parameter, pr);

		this.esa = new EachSampleAnalysis(pr, "ext_multipledataload", d_parameter.DirectoryHeadPath, d_parameter.NumberOfDataSets, d_parameter.NumberOfOutputDim, d_parameter.IsHeadPathNumbering);
		this.actual_number_of_dataset += this.esa.getNumberOfDataSets();

		System.out.println(">>>> Actual Number of Samples" + this.actual_number_of_dataset);
		this.esa.calculate_mean_variance(d_parameter.get_t_value(this.actual_number_of_dataset));
		this.esa.write_results(dataout);
		dataout.close();
	}
	

	
	public DetectConfidenceInterval(String parameterfile, String datasetname) {
		ParameterReader pr = new ParameterReader(parameterfile);
		DataAnalysisParameters d_parameter = new DataAnalysisParameters();
		d_parameter.getParameter(pr.Reader("DataAnalysis"));//パラメータの取得
		dataOutput dataout = new dataOutput(pr.Reader("ResultFileName"));
		//this.read_all_datasets(d_parameter, pr);

		this.esa = new EachSampleAnalysis(pr, datasetname, d_parameter.DirectoryHeadPath, d_parameter.NumberOfDataSets, d_parameter.NumberOfOutputDim, d_parameter.IsHeadPathNumbering);
		this.actual_number_of_dataset += this.esa.getNumberOfDataSets();

		System.out.println(">>>> Actual Number of Samples" + this.actual_number_of_dataset);
		this.esa.calculate_mean_variance(d_parameter.get_t_value(this.actual_number_of_dataset));
		this.esa.write_results(dataout);
		dataout.close();
	}	
	
	public DetectConfidenceInterval(String parameterfile, String datasetname, String ID_str) {
		
		ParameterReader pr = new ParameterReader(parameterfile);
		DataAnalysisParameters d_parameter = new DataAnalysisParameters();
		d_parameter.getParameter(pr.Reader("DataAnalysis"));//パラメータの取得
		dataOutput dataout = new dataOutput(pr.Reader("ResultFileName"), ID_str);//結果のファイル名を共通にしてしまうとバッティングするため、この部分にもID_strが必要だ。
		//this.read_all_datasets(d_parameter, pr);
		this.esa = new EachSampleAnalysis(pr, datasetname, d_parameter.DirectoryHeadPath, d_parameter.NumberOfDataSets, d_parameter.NumberOfOutputDim, d_parameter.IsHeadPathNumbering, ID_str);
		this.actual_number_of_dataset += this.esa.getNumberOfDataSets();
		this.Log("Read process is finished!");

		System.out.println(">>>> Actual Number of Samples" + this.actual_number_of_dataset);
		
		this.esa.calculate_mean_variance(d_parameter.get_t_value(this.actual_number_of_dataset));
		this.esa.write_results(dataout);
		dataout.close();
	}		
	
	void Log(String log) {
		System.out.println("DetectConficenceInterval." + log);
	}
	/*
	void read_all_datasets(DataAnalysisParameters d_parameter, ParameterReader pr) {
		this.emd = new ExtendedMultipleDataload[d_parameter.NumberOfDataSets];
		for (int d=0; d<d_parameter.NumberOfDataSets; d++) {
			this.emd[d] = new ExtendedMultipleDataload(
						pr.Reader("ext_multipledataload"), 
						d_parameter.NumberOfInputDim, 
						d_parameter.NumberOfOutputDim
						);
			//データセットの中身を読み込む
			for (int i=0; i<emd[d].getNumberOfDataSets(); i++) {
				this.emd[d].read(i);
			}
		}
	}*/
	

}
