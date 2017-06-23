package DataLoad;

import org.w3c.dom.Node;

public class MultipleDataLoad4CrossValidation {
    public final int BOOLEAN=0;
    public final int DOUBLE = 1;
    public final int INT = 2;	
	int NumberOfInputs, NumberOfOutputs;
	int data_type = this.DOUBLE;
	int MaxDates;
	int NumberOfPracticesPerDay;
	MultipleDataload4CrossValidationParameters pr;
	
	public MultipleDataLoad4CrossValidation(Node nd, int NumberOfInputs, int NumberOfOutputs) {
		this.NumberOfInputs = NumberOfInputs;
		this.NumberOfOutputs = NumberOfOutputs;
		this.pr = new MultipleDataload4CrossValidationParameters();
		pr.getParameter(nd);
		this.MaxDates = pr.getMaxDates();
		this.NumberOfPracticesPerDay = pr.getNumberOfPracticePerDay();
		
	     //データタイプを読み込む
		if (pr.getDatatype().equals("double")) {
			this.data_type = this.DOUBLE;
			System.out.println("dataLoad: datatype is double");
		}else{
			if (pr.getDatatype().equals("boolean")) {
				this.data_type = this.BOOLEAN;
				System.out.println("dataLoad: datatype is boolean");
			}else{
				if (pr.getDatatype().equals("integer")) {
					this.data_type = this.INT;
					System.out.println("dataLoad: datatype is integer");
				}else{
					System.err.println("Invarid data type!!: the datatypes are:");
					System.err.println(" integer, double, boolean.");
					System.exit(1);
				}
	        }
		}
	}// constructor
	
}
