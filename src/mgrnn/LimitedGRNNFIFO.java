package mgrnn;

import java.util.ArrayList;


import VectorFunctions.*;

import org.w3c.dom.Node;
//import java.util.Scanner;



public class LimitedGRNNFIFO  {

	int UpperLimitOfHiddenUnits;

	// ��ա��ʲ���6�Ĥ��ѿ����������ξ��ˤƽ��������ȥ��󥹥��󥹲����������˥ѥ�᡼���ե�����ˤ�������˼��Ԥ��롣
	// ���äƤ����Ǥϰʲ��Τ褦�˽�����򤷤ʤ����ȡ��ʤ�Ǥ����ʤä��㤦�Τ�������
	double ImportanceWeight4NewSample; //����������ץ�ν����٤�ɽ�����̾�ϣ����ɤ���
	
	//�ʲ����Ĥ�boolean�ѿ��ϡ���Ӽ¸��Τ��ᣵ�Ĥγؽ����ץ�����Ȥ����ɤ��������椹�뤿��˻��Ѥ��롣
	//�㤨�и������դ��֤��������ץ�������Ѥ������UseSubstitution=true�Ȥ��Ƥ����ȡ��ºݤ˻Ȥ��뤬��
	//false�˥��åȤ���ȡ����Υ��ץ����ϻȤ��ʤ���
	boolean isVariableSigma;
	boolean UsePseudoInverse;
	boolean isCumulatedErrorEstimation = false;
	private int NumberOfInputs;
	private int NumberOfOutputs;
	
	private double variance;
	ArrayList<Cell> hidden_units = new ArrayList<Cell>();
	
	public LimitedGRNNFIFO(Node nd) {
		
		this.init_parameters(nd);
		// �����Ϥ��Τ褦�˽񤫤ʤ��Ƥ�ƥ��饹����init_parameters()���ƤӽФ����Ϥ�����
		// ���������ιԤ��ʤ���硢�Ի׵Ĥʤ��Ȥˤ��Υ��饹���ѿ�UseModify������������ʤ���init_parameter()�᥽�åɤ���ǤϳΤ��˹������줿���Ȥϳ�ǧ���Ƥ��뤬��
		// �ºݤˤϹ�������ʤ��Τ���
		// ���Υץ����Ǥϼ¼�init_parameters()��2��¹Ԥ���뤬��ͭ����Ư���Τ�2���ܤ˸ƤӽФ��줿���Ȥʤ롣
		// �����Java-vm�ΥХ����⤷��ʤ���
	}

	
	void init_parameters(Node nd) {
		LGRNN_parameters lgp = new LGRNN_parameters();
		lgp.getParameter(nd);
		this.UpperLimitOfHiddenUnits = lgp.getNumberOfUpperLimitOfUnits();
		this.NumberOfInputs = lgp.NumberOfInputs;
		this.NumberOfOutputs = lgp.NumberOfOutputs;
		
		this.variance = lgp.default_variance;
		this.ImportanceWeight4NewSample = lgp.getImportanceWeight4NewSample();
		this.isCumulatedErrorEstimation = lgp.IsEstimatedByCumulativeError();
		
		this.isVariableSigma = lgp.isVariableSigma();
		

		this.clear_check_flag();
	}

	void clear_check_flag() {
		for (Cell cell_obj : this.hidden_units) {
			cell_obj.set_check(false);
		}
	}	
	
	public double[] calculate_outputs(double input[]) {
		double sum_output=0D;
		double[] each_output;
		double[] sum_outputs = new double[this.NumberOfOutputs];
		
		if (this.hidden_units.size()>0) {
			for (Cell cell_obj: this.hidden_units) {
				if (!cell_obj.is_check()) {//check���դ��Ƥ��ʤ��ʤ�� 
					each_output = cell_obj.output(input);
					//System.out.println("each output value is " + cell_obj.exp_output());
					sum_output += cell_obj.responsiblity();//������ʬ��¾�Υ��饹�Ȥϰ㤦�Τ����
					for (int i=0; i<this.NumberOfOutputs; i++) {
						sum_outputs[i] += each_output[i]; 
					}
				}
			}
			if (sum_output > Double.MIN_VALUE) { //NAN���ɤ�
				for (int i=0; i<this.NumberOfOutputs; i++) {
					sum_outputs[i] /= sum_output;
				}
			}else{
				//System.out.println("grnnLinearDependency.calculate_outputs(): pass!");
				for (int i=0; i<this.NumberOfOutputs; i++) {
					
					sum_outputs[i] = 0;
				}
			}
		}
		return sum_outputs;
	}

	
	public LgrnnLearningStatus learning(double inputs[], double outputs[]) {
		return this.FIFO_learning(inputs, outputs, this.UpperLimitOfHiddenUnits);
	}

	// If you want to use 'UseEditing=true', please note that other four options
	// should be false.
	// Editing option can be used separately. (UseEditing + Ignore is ok!)
	LgrnnLearningStatus FIFO_learning(double inputs[], double outputs[], int UpperLimitOfHiddenUnits) {
		CellFIFO new_cell, each_cell;
		LgrnnLearningStatus result = new LgrnnLearningStatus();
		double[] error = VectorFunctions.diff(outputs, this.calculate_outputs(inputs));
		result.residual_error = VectorFunctions.getSqureNorm(error);
		
		if (this.hidden_units.size() < UpperLimitOfHiddenUnits) {// only adding
																	// new units
			new_cell = new CellFIFO(this.NumberOfInputs,
					this.NumberOfOutputs);
			new_cell.set_default_variance(this.variance, 1);
			new_cell.learn(inputs, outputs);
			new_cell.set_check(false);// DO NOT FORGET!
			this.hidden_units.add(new_cell);
			System.out
					.println("LGRNN:learning():Append: number of hiddenunits: "
							+ hidden_units.size());
			if (this.isVariableSigma && this.hidden_units.size() == UpperLimitOfHiddenUnits) {
				OptimizeSigma();
			}
		} else {// substitution & replace, only replace(pruning), modification

				System.out.println("======== replace ==========");
				CellFIFO oldest_kernel = this.getOldestKernel();
				
				oldest_kernel.learn(inputs, outputs);
				oldest_kernel.resetAge();
					// uselesscell_EDIT.target_cell.learn(inputs, outputs);
				if (this.isVariableSigma) this.OptimizeSigma();
			
		}
		for (Cell cell : this.hidden_units) {
			each_cell = (CellFIFO)cell;
			each_cell.incrementalAge();
		}
		return result;
		// System.out.println("grnn:learn() number of hidden units:" +
		// this.hidden_units.size());
	}

	CellFIFO getOldestKernel() {
		int oldest_age = -1;
		CellFIFO each_cell;
		CellFIFO oldest_kernel=null;
		for (Cell cell : this.hidden_units) {
			each_cell = (CellFIFO)cell;
			if (oldest_age < each_cell.getAge()) {
				oldest_age = each_cell.getAge();
				oldest_kernel = each_cell;
			}
		}
		return oldest_kernel;
	}
	
	
	void OptimizeSigma() {
		double mean_distance = this.MeanMinimumDistance();
		double sigma_opt = this.appropreate_sigma(0.1*mean_distance, 10*mean_distance, 0.1);
		System.out.println("LimitedGRNN:OptimizeSigma(): simga_opt=" + sigma_opt + " mean_distance is " + mean_distance);
		//keyboard_scanner.next();
		for (Cell cell: this.hidden_units) {
			CellFIFO each_cell = (CellFIFO)cell;
			each_cell.setR(sigma_opt);
		}		
	}
	
	
	double appropreate_sigma(double min_sigma, double max_sigma) {
		double upper_error, lower_error;
		double middle_sigma = (min_sigma+max_sigma)/2D;
		
		if (Math.abs(max_sigma - min_sigma) < 0.001) {
			return middle_sigma;
		}
		upper_error = this.EstimateSigma(max_sigma);

		lower_error = this.EstimateSigma(min_sigma);
		//System.out.println("LimitedGRNN:appropreate_sigma(): upper_error=" + upper_error + ", lower_error=" + lower_error);
		if (lower_error <= upper_error) {
			return appropreate_sigma(min_sigma, middle_sigma);
		} else {
			return appropreate_sigma(middle_sigma, max_sigma);
		}
	}

	double appropreate_sigma(double min_sigma, double max_sigma, double step_size) {
		double each_error;
		double min_error=Double.MAX_VALUE;
		double optimal_sigma=-1;
		
		for (double s=min_sigma; s<=max_sigma; s+=step_size) {
			each_error = this.EstimateSigma(s);
			//System.out.println("LimitedGRNN: appropreate_sigma(): s=" +s+ " error=" + each_error);
			if (each_error < min_error) {
				optimal_sigma = s;
				min_error = each_error;
			}
		}
		return optimal_sigma;
	}	
	/*
	 * �ơ���sigma���ͤ��Ф����Ʋ����ζ����
	 */
	double EstimateSigma(double sigma) {
		double each_error = 0;
		double[] output;
		int MaxHiddenUnits = 0;
		for (Cell cell: this.hidden_units) {
			CellFIFO each_cell =(CellFIFO)cell;
 			each_cell.setR(sigma);
		}

		for (Cell cell: this.hidden_units) {
			CellFIFO except_cell = (CellFIFO)cell;
			except_cell.set_check(true);// ���Υ�˥åȤ����׻����ʤ�
			output = this.calculate_outputs(except_cell.getT());
			each_error += VectorFunctions.getSqureNorm(VectorFunctions.diff(
					except_cell.getC(), output));
			MaxHiddenUnits++;
			except_cell.set_check(false);// �����᤹
		}
		return each_error / (double) MaxHiddenUnits;
	}// EstimateSigma();

	//kernel�֤��濴���֤ε�Υ��ʿ���ͤ����
	double MeanMinimumDistance() {
		CellFIFO target_unit = null, nearest_unit;
		double each_distance, average_distance;
		
		average_distance = 0;
		for (Cell cell : this.hidden_units) {
			target_unit = (CellFIFO)cell;
			nearest_unit = this.getNearestUnit(target_unit);
			each_distance = VectorFunctions.getNorm(
						VectorFunctions.diff(target_unit.getT(), nearest_unit.getT()));
			average_distance += each_distance;
		}
		average_distance /= (double)this.hidden_units.size();
		return average_distance;
	}// MeanDistance();

	CellFIFO getNearestUnit(CellFIFO target_unit) {
		double min_distance = Double.MAX_VALUE;
		CellFIFO nearest_unit = null;
		for (Cell cell : this.hidden_units) {
			CellFIFO each_cell = (CellFIFO)cell;
			if (!each_cell.equals(target_unit)) {
				double each_distance = VectorFunctions.getSqureNorm(
						VectorFunctions.diff(target_unit.getT(), each_cell.getT())
						);
				if (each_distance < min_distance) {
					nearest_unit = each_cell;
					min_distance = each_distance;
				}
			}
		}
		return nearest_unit;
	}


	/**
	 * @return the upperLimitOfHiddenUnits
	 */
	public int getUpperLimitOfHiddenUnits() {
		return UpperLimitOfHiddenUnits;
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

	public int getNumberOfHiddenUnits() {
		return this.hidden_units.size();
	}

	public double getVariance() {
		return this.variance;
	}


	/**
	 * @param numberOfInputs the numberOfInputs to set
	 */
	public void setNumberOfInputs(int numberOfInputs) {
		NumberOfInputs = numberOfInputs;
	}


	/**
	 * @param numberOfOutputs the numberOfOutputs to set
	 */
	public void setNumberOfOutputs(int numberOfOutputs) {
		NumberOfOutputs = numberOfOutputs;
	}


	/**
	 * @param upperLimitOfHiddenUnits the upperLimitOfHiddenUnits to set
	 */
	public void setUpperLimitOfHiddenUnits(int upperLimitOfHiddenUnits) {
		UpperLimitOfHiddenUnits = upperLimitOfHiddenUnits;
	}
	
	
}
