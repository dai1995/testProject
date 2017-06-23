package MixtureOfDistributions;

import java.util.Enumeration;
import java.util.Vector;

import DataLoad.dataload;
import FIFO.MinMoutWeightVariableSizedBuffer;
import matrix.MatrixException;
import matrix.MatrixObj;

public class wMixtureOfNormalDistributions implements Cloneable {
	//MixtureOfDistributions.MixtureOfDistributionParameters parameter;
	
	protected int NumberOfInputs, NumberOfOutputs;
	protected int NumberOfModels;
	protected double init_sigma_value;

	protected Vector<Object> HiddenNodes=null;
	protected boolean isDiagonal=false;
	protected boolean isSMD = false;
	protected double MinimumDistances[];//入力ベクトルとセンターとの距離を各次元ごとに計る
	protected double gamma[];
	
	boolean DEBUG = false;
	
	public wMixtureOfNormalDistributions(MinMoutWeightVariableSizedBuffer buffer, 
			int NumberOfModels, 
			int NumberOfInputs, 
			double activation_threshold, 
			double init_sigma_value, 
			boolean isDiagonal, 
			boolean SMD, 
			boolean RANInitMode) {
		this.NumberOfInputs = NumberOfInputs;
		this.NumberOfModels = NumberOfModels;
		this.isDiagonal = isDiagonal;
		this.init_sigma_value = init_sigma_value;
		this.isSMD = SMD;
		this.gamma = new double[this.NumberOfModels];

		try {
			if (RANInitMode) {
				this.Append_init_models(this.NumberOfModels, activation_threshold, buffer, init_sigma_value, isDiagonal, SMD);
			}else{
				this.init_models(buffer, this.NumberOfModels, init_sigma_value, isDiagonal, SMD);
			}
		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//this.dl = new MultipleDataload(pr.Reader("multipledataload"), this.NumberOfInputs, this.NumberOfOutputs);		
	}
	
	
	//重みつきEM
	public void EM(MinMoutWeightVariableSizedBuffer buffer, double stop_threshold, int MaxNumberOfIterations, double min_sigma) throws MatrixException {
		int iterations = 0;
		/*try {
			this.init_models(dl, this.init_sigma_value, this.isDiagonal, this.isSMD);
		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/		
		do {
			init_parameters();
			E_step(buffer);
			iterations ++;
		}while(M_step(min_sigma)>stop_threshold && iterations < MaxNumberOfIterations);
	}	
	

	void init_parameters() {
		//NormalDistributionD N;
		Distribution N;
		if (this.HiddenNodes == null) return;
		Enumeration<Object> e = this.HiddenNodes.elements();
		while (e.hasMoreElements()) {
			N = (Distribution)e.nextElement();
			N.reset_parameters();
		}
	}
	

	void reset_variance_covariance_matrix(double min_sigma) throws MatrixException {
		//NormalDistributionD N;
		Distribution N;
		if (this.HiddenNodes == null) return;
		Enumeration<Object> e = this.HiddenNodes.elements();
		while (e.hasMoreElements()) {
			N = (Distribution)e.nextElement();
			MatrixObj cov = new MatrixObj(this.NumberOfInputs, this.NumberOfInputs);
			for (int i=0; i<this.NumberOfInputs; i++) {
				cov.set_data(i, i, min_sigma);
			}
			N.SetSigma(cov);
		}		
	}
	
	//重みつきEM
	void E_step(MinMoutWeightVariableSizedBuffer buffer) {
		Distribution N;
		double gamma[];
		int m;

			for (int p=0; p<buffer.getSize(); p++) {
				gamma = this.calculate_gamma(buffer.getInput(p));
				m=0;
				Enumeration<Object> e = this.HiddenNodes.elements();
				while (e.hasMoreElements()) {
					N = (Distribution)e.nextElement();
					N.push_center(buffer.getInput(p), buffer.getActualWeight(p) * gamma[m]);//重みを掛けていることに注意
					//System.out.println("gamma[" + m + "]=" + gamma[m]);
					m++;
				}
			}
	}
	
	double[] calculate_gamma(double input[]) {
		double total_output=Double.MIN_NORMAL;//NaNを防ぐため
		double each_out[] = new double[this.NumberOfModels];
		double gamma[] = new double[this.NumberOfModels];
		int m;
		Distribution N;
		m=0;
		Enumeration<Object> e = this.HiddenNodes.elements();
		while (e.hasMoreElements()) {
			N = (Distribution)e.nextElement();
			each_out[m] = N.P(input);
			total_output += each_out[m];
			/*if (each_out[m] > 1000 || Double.isNaN(each_out[m])) {
				total_output += 1000;
				each_out[m] = 1000;
			}else{
				total_output += each_out[m];
			}*/

			//System.out.println("MixtureOfNormalDistribution:calculate_gamma(): each_out[" + m + "]=" + each_out[m]);			
			m++;
		}
		//System.out.println("MixtureOfNormalDistribution:calculate_gamma(): total_output=" + total_output);
		for (m=0; m<this.NumberOfModels; m++) {
			gamma[m] = each_out[m] / total_output;
			//if (gamma[m]>0.5) {
				//	System.out.println("MixtureOfNormalDistribution:calculate_gamma(): gamma[" + m + "]=" + gamma[m]);
			//}
		}
		return gamma;
	}
	

	double M_step(double min_sigma) throws MatrixException {
		Distribution N;
		double change_in_parameters = 0D;
		Enumeration<Object> e = this.HiddenNodes.elements();
		while (e.hasMoreElements()) {
			N = (Distribution)e.nextElement();
			change_in_parameters += N.calculate_center();
			change_in_parameters += N.calculate_variance_covariance_matrix(min_sigma);
		}
		//System.out.println("MixtureOfNormalDistribution:M_step(): change_in_parameters=" + change_in_parameters);
		return change_in_parameters;
	}
	

	public void init_models(MinMoutWeightVariableSizedBuffer buffer, int NumberOfModels, double init_sigma_value, boolean isDiagonal, boolean SMD) throws MatrixException {
		this.HiddenNodes = new Vector<Object>();
		for (int m=0; m<NumberOfModels; m++) {
			MatrixObj init_sigma = new MatrixObj(this.NumberOfInputs, this.NumberOfInputs);
			this.init_sigma(init_sigma, init_sigma_value);

			if (isDiagonal) {
				NormalDistributionD nd = new NormalDistributionD(buffer.getInput(m), init_sigma);
				this.HiddenNodes.addElement(nd);
			}else{
				NormalDistribution  nd = new NormalDistribution(buffer.getInput(m), init_sigma, SMD);
				this.HiddenNodes.addElement(nd);
			}
		}
	}
	

	public int Append_init_models(int AddNumberOfHiddenUnits, double activation_threshold, MinMoutWeightVariableSizedBuffer buffer, double init_sigma_value, boolean isDiagonal, boolean SMD) throws MatrixException {
		boolean ExistFiredCell = false;
		int cell=0;
		if (this.HiddenNodes==null) this.HiddenNodes = new Vector<Object>();
		for (int p=0; p<buffer.getSize() && cell<AddNumberOfHiddenUnits; p++) {
			Distribution winner = this.getWinnerCell(buffer.getInput(p));
			ExistFiredCell = false;
			if (winner != null) {
				if (winner.getExp(buffer.getInput(p))>activation_threshold) {
					ExistFiredCell = true;
				}
			}
			if (!ExistFiredCell) {
				MatrixObj init_sigma = new MatrixObj(this.NumberOfInputs, this.NumberOfInputs);
				init_sigma(init_sigma, init_sigma_value);
				if (isDiagonal) {
					IncNormalDistributionD nd = new IncNormalDistributionD(
							buffer.getInput(p), init_sigma);
					this.HiddenNodes.addElement(nd);
				}else{
					IncNormalDistribution nd = new IncNormalDistribution(
							buffer.getInput(p), init_sigma, false);
					this.HiddenNodes.addElement(nd);
				}
				//nd.setPi(0);
				cell++;
			}
		}

		this.NumberOfModels += cell;
		if (this.DEBUG) {
			System.out.println("IncMixtureOfDistribution: Append_init_models(): # of hidden units: " + cell);
		}
		return cell;
	}//Append_init_models()			


	Distribution getWinnerCell(double x[]) {
		double max_output = -1;
		Distribution winner_model = null, nd;
		double each_output;
		if (this.HiddenNodes.size()==0) return null;
		Enumeration<Object> e = this.HiddenNodes.elements();	
		while (e.hasMoreElements()) {
			nd = (Distribution)e.nextElement();
			each_output = nd.P(x);
			if (max_output < each_output) {
				max_output = each_output;
				winner_model = nd;
			}
		}
		return winner_model;
	}

	
	
	void init_sigma(MatrixObj sigma, double init_value) {
		for (int i=0; i<sigma.getL(); i++) {
			sigma.set_data(i, i, init_value);
		}
	}
	
	double LogLikelihood(dataload dl) {
		double sum=0D;
		double each_p=0;
		
		for (int day=0; day<dl.getMaxDates(); day++) {
			for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
				each_p = this.P(dl.learning_input_patterns[day][p]);
				/*if (Double.isNaN(each_p)) {
					sum -= 100;
				}else{
					sum += Math.log(each_p);
				}*/
				sum += Math.log(each_p);
			}
		}
		if (sum > 0) {
			System.out.println("each_p = " + each_p);
		}
		return sum;
	}
	


	public double P(double input[]) {
		Distribution nd;
		//int model=0;
		double result=0, each_output;
		Enumeration<Object> e = this.HiddenNodes.elements();

		while (e.hasMoreElements()) {
			nd = (Distribution)e.nextElement();
			each_output = nd.P(input);
			result += each_output;//nd.Pは π_{i}×p(x|S_{i})

			//System.out.println("MixtureOfNormalDistribution: P(x): each_output[" + model +"]=" + each_output);
			//model ++;
		}
		return result;
	}
	

	public double studentT(double input[]) {
		Distribution nd;
		//int model=0;
		double result=0, each_output;
		Enumeration<Object> e = this.HiddenNodes.elements();

		while (e.hasMoreElements()) {
			nd = (Distribution)e.nextElement();
			each_output = nd.getTvalue(input);
			result += each_output;//π_{i}×studentT(x|S_{i})
			//System.out.println("MixtureOfNormalDistribution: P(x): each_output[" + model +"]=" + each_output);
			//model ++;
		}
		return result;
	}
	

	public double q_div_p(double input[]) {
		Distribution nd;
		double result=0, each_output;
		double max_output = -1000;
		Distribution Winner=null;
		Enumeration<Object> e = this.HiddenNodes.elements();

		while (e.hasMoreElements()) {
			nd = (Distribution)e.nextElement();
			each_output = nd.P(input);
			if (max_output < each_output) {
				max_output = each_output;
				Winner = nd;
			}
		}
		if (Winner != null) {
			result = Winner.q_div_p(input);
			return result;
		}else{
			return -1;
		}

	}	
		

	

	public void CreateStudentT(int UpperLimitOfFreedom, boolean SMD, boolean CorrectTDistribution) throws MatrixException {
		System.out.println("MixtureOfNormalDistribution:CreateStudentT():HiddenUnits=" + this.HiddenNodes.size());
		Enumeration<Object> e = this.HiddenNodes.elements();
		while (e.hasMoreElements()) {
			if (this.isDiagonal) {
				NormalDistributionD ndd = (NormalDistributionD)e.nextElement();
				ndd.CreateStudenTdistribution(UpperLimitOfFreedom, CorrectTDistribution);
				//System.out.println("Freedom " + ndd.ST.N);
			}else{
				NormalDistribution nd = (NormalDistribution)e.nextElement();
				nd.CreateStudenTdistribution(UpperLimitOfFreedom, SMD, CorrectTDistribution);
			}
		}
	}
		
	
	int getNumberOfParameters(boolean isDiagonal) {
		int total_number;
		if (isDiagonal) {
			total_number = this.NumberOfModels * (
				1 + // W[]
				this.NumberOfInputs + // U[]
				this.NumberOfInputs 
				//(int)Math.pow((double)this.NumberOfInputs, 2D) //sigma
				);
		}else{
			total_number = this.NumberOfModels * (
					1 + // W[]
					this.NumberOfInputs + // U[]
					(int)Math.pow((double)this.NumberOfInputs, 2D) //sigma
					);			
		}
		return total_number;
	}

	public int getNumberOfHiddenUnits() {
		return this.HiddenNodes.size();
	}
	
	public MatrixObj getDistributionCenter(int cell) {
		Distribution h_cell = (Distribution)this.HiddenNodes.elementAt(cell);
		return h_cell.GetCenter();
	}

	/**
	 * @return the numberOfInputs
	 */
	public int getNumberOfInputs() {
		return NumberOfInputs;
	}

	/**
	 * @return the numberOfOutputs
	 */
	public int getNumberOfOutputs() {
		return NumberOfOutputs;
	}

	/**
	 * @return the numberOfModels
	 */
	public int getNumberOfModels() {
		return NumberOfModels;
	}

	/**
	 * @return the init_sigma_value
	 */
	public double getInit_sigma_value() {
		return init_sigma_value;
	}

	/**
	 * @return the isDiagonal
	 */
	public boolean isDiagonal() {
		return isDiagonal;
	}

	/**
	 * @return the isSMD
	 */
	public boolean isSMD() {
		return isSMD;
	}

	/**
	 * @return the gamma
	 */
	public double[] getGamma() {
		return gamma;
	}

	/**
	 * @return the hiddenNodes
	 */
	public Vector<Object> getHiddenNodes() {
		return HiddenNodes;
	}

	/**
	 * @param hiddenNodes the hiddenNodes to set
	 */
	@SuppressWarnings("unchecked")
	public void setHiddenNodes(Vector<Object> hiddenNodes) {
		this.HiddenNodes = (Vector<Object>)hiddenNodes.clone();
	}

	@SuppressWarnings("unchecked")
	public wMixtureOfNormalDistributions clone(boolean isDiagonal) {
		int i;
	    try {
	    	wMixtureOfNormalDistributions myclone = (wMixtureOfNormalDistributions)super.clone();
	    	myclone.HiddenNodes = (Vector<Object>)this.HiddenNodes.clone();
	    	if (isDiagonal) {
	    		Enumeration<Object> e = this.HiddenNodes.elements();
	    		i=0;
	    		while (e.hasMoreElements()) {
	    			NormalDistributionD h_cell = (NormalDistributionD)e.nextElement();
	    			myclone.HiddenNodes.setElementAt(h_cell.clone(), i);
	    			i++;
	    		}
	    	}else{
	    		Enumeration<Object> e = this.HiddenNodes.elements();
	    		i=0;
	    		while (e.hasMoreElements()) {
	    			NormalDistribution h_cell = (NormalDistribution)e.nextElement();
	    			myclone.HiddenNodes.setElementAt(h_cell.clone(), i);
	    			i++;
	    		}
	    	}
	    	return myclone;
	    } catch (CloneNotSupportedException e) {  
	    	return null;  
	    }
	}	
	

	public void display_unit_centers(boolean isDiagonal) {
		int cell=0;
		Enumeration<Object> e = this.HiddenNodes.elements();

		while (e.hasMoreElements()) {
			if (isDiagonal) {
				NormalDistributionD h_cell = (NormalDistributionD)e.nextElement();
				h_cell.GetCenter().display("The " + cell + "th HiddenUnit center");
			}else{
				NormalDistribution h_cell2 = (NormalDistribution)e.nextElement();
				h_cell2.GetCenter().display("The " + cell + "th HiddenUnit center");
			}
			cell++;
		}

	}
	
	public MatrixObj getUnitCenter(int cell) {
		Distribution h_cell = (Distribution)this.HiddenNodes.elementAt(cell);
		//h_cell.GetCenter().display("wMixtureOfNormalDistributions: getUnitCenter(): HiddenUnitCenter");
		 return h_cell.GetCenter();
	}
	

	public String getGnuPlot() {
		Distribution h_cell;
		int cell_index = 0;
		String plot_str = "plot ";
		Enumeration<Object> e = this.HiddenNodes.elements();
		while (e.hasMoreElements()) {
			h_cell = (Distribution)e.nextElement();
			plot_str +="(" + h_cell.getPAI() + ") * (" + h_cell.getPlot() + ")";
			cell_index ++;
			if (cell_index < this.HiddenNodes.size()) {
				plot_str += ", ";
			}
		}
		return plot_str;
	}	
}
