/*
 * Created on 2005/08/10
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package DataLoad;


import org.w3c.dom.Node;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MultipleDataload extends dataload {

	String LearningHeadFileName;
	String extention;
	int NumberOfDataSets;
	/**
	 * 
	 */
	public MultipleDataload(Node node, int NumberOfInputs, int NumberOfOutputs) {
		super(node, NumberOfInputs, NumberOfOutputs);
	}
	
	public MultipleDataload(Node node, int NumberOfInputs, int NumberOfOutputs, String header_path) {
		super(node, NumberOfInputs, NumberOfOutputs, header_path);
	}
	
	public MultipleDataload(Node node, int NumberOfInputs, int NumberOfOutputs, String header_path, String ID_str) {
		super(node, NumberOfInputs, NumberOfOutputs, header_path, ID_str);
	}	

	public MultipleDataload(Node node) {
		super(node);
	}	
	
    void init_function(dataloadParameter dp, String header_path) {
    	if (header_path!=null) {
    		this.filename = header_path + dp.TestDataFilename;
    	}else{
    		this.filename = dp.TestDataFilename;    		
    	}
        this.read_test_data();
        this.LearningHeadFileName = dp.LearningDataFileHeadName;
        this.extention = dp.Extention;
        this.NumberOfDataSets = dp.NumberOfDataSets;
        this.AddInputRandom_LearningData = dp.isAddInputRandom_LearningData();
        this.AddOutputRandom_LearningData = dp.isAddOutputRandom_LearningData();
        this.AddInputRandom_TestData = dp.isAddInputRandom_TestData();
        this.AddOutputRandom_TestData = dp.isAddOutputRandom_TestData();
    }
    
    void init_function(dataloadParameter dp, String header_path, String ID_str) {
    	if (header_path!=null) {
    		this.filename = header_path + dp.TestDataFilename;
    	}else{
    		this.filename = dp.TestDataFilename;    		
    	}
        this.read_test_data();
        if (ID_str != null) {
        	this.LearningHeadFileName = dp.LearningDataFileHeadName + ID_str;
        }
        this.extention = dp.Extention;
        this.NumberOfDataSets = dp.NumberOfDataSets;
        this.AddInputRandom_LearningData = dp.isAddInputRandom_LearningData();
        this.AddOutputRandom_LearningData = dp.isAddOutputRandom_LearningData();
        this.AddInputRandom_TestData = dp.isAddInputRandom_TestData();
        this.AddOutputRandom_TestData = dp.isAddOutputRandom_TestData();
    }    
    
    public void read(int index) {
    	if (index > this.NumberOfDataSets) {
    		System.err.println("MultipleDataload: index must be less than " + this.NumberOfDataSets);
    		System.exit(1);
    	}
        this.filename = this.LearningHeadFileName + index + this.extention;
        this.Log("read(" + index + ") filename=" + this.filename);
        this.read_learning_data();
    }
    
    public int getNumberOfDataSets() {
    	return this.NumberOfDataSets;
    }
    
    void Log(String log) {
    	System.out.println("MultipleDataload." + log);
    }
}
