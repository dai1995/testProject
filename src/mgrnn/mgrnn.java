/*
 * Created on 2005/07/20
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

package mgrnn;

import java.util.*;

import org.w3c.dom.Node;

import VectorFunctions.VectorFunctions;
import datalogger.*;

/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class mgrnn {
	int NumberOfInputs, NumberOfOutputs;
	double epsilon, lambda;
	double default_variance, max_variance;
	double activation_threshold;
	double ErrThreshold;
	double confidence;
	Vector<Cell> hidden_units = new Vector<Cell>();
	
	public mgrnn(int NumberOfInputs, int NumberOfOutputs, double default_variance, double epsilon, double lambda, double activation_threshold) {
		this.NumberOfInputs = NumberOfInputs;
		this.NumberOfOutputs = NumberOfOutputs;
		this.default_variance = default_variance;
		this.epsilon = epsilon;
		this.lambda = lambda;
		this.activation_threshold = activation_threshold;
		this.clear_check_flag();
	}
	
	public mgrnn(Node nd) {
		this.init_parameters(nd);
	}
	
	void init_parameters(Node nd) {
		grnn_parameter gp = new grnn_parameter();
		gp.getParameter(nd);
		this.NumberOfInputs = gp.NumberOfInputs;
		this.NumberOfOutputs = gp.NumberOfOutputs;
		this.epsilon = gp.epsilon;
		this.lambda = gp.lambda;
		this.default_variance = gp.default_variance;
		this.activation_threshold = gp.activation_threshold;
		this.ErrThreshold = gp.ErrThreshold; //for EFuNN 別途ちゃんと作りなおすのが面倒くさいのでここに置くことにした
		this.max_variance = gp.max_variance; //for EFuNN 別途ちゃんと作りなおすのが面倒くさいのでここに置くことにした
		
		this.clear_check_flag();		
	}
	public void mgrnn_learn(double inputs[], double outputs[]) {
		GRNNCell nearest_cell;
		GRNNCell new_cell;
		
		if ((nearest_cell = this.getNearetUnit(inputs))!=null) {
			nearest_cell.set_variance(inputs, this.lambda);
		}
		new_cell = new GRNNCell(this.NumberOfInputs, this.NumberOfOutputs);
		//new_cell.set_default_variance(this.default_variance);
		new_cell.learn(inputs, outputs);
		this.hidden_units.addElement(new_cell);
		//System.out.println("grnn:learn() number of hidden units:" + this.hidden_units.size());
	}
	
	public void grnn_learn(double inputs[], double outputs[]) {
		GRNNCell new_cell;

		new_cell = new GRNNCell(this.NumberOfInputs, this.NumberOfOutputs);
		//new_cell.set_default_variance(this.default_variance);
		new_cell.learn(inputs, outputs);
		this.hidden_units.addElement(new_cell);
		//System.out.println("grnn:learn() number of hidden units:" + this.hidden_units.size());
	}
		
	public double[] calculate_outputs(double input[]) {
		double sum_output=0D;
		double[] each_output;
		double[] sum_outputs = new double[this.NumberOfOutputs];
		Cell cell_obj;
		this.confidence = 0D;
		if (this.hidden_units.size()>0) {
			Enumeration<Cell> e = this.hidden_units.elements();
			while (e.hasMoreElements()) {
				cell_obj = e.nextElement();
				if (!cell_obj.is_check()) {//checkが付いていないならば 
					each_output = cell_obj.output(input);
					//System.out.println("each output value is " + cell_obj.exp_output());
					sum_output += cell_obj.exp_output();
					for (int i=0; i<this.NumberOfOutputs; i++) {
						sum_outputs[i] += each_output[i]; 
					}
				}
			}
			if (sum_output > 0.00000001) { //NANを防ぐ
				for (int i=0; i<this.NumberOfOutputs; i++) {
					sum_outputs[i] /= sum_output;
				}
			}else{
				for (int i=0; i<this.NumberOfOutputs; i++) {
					sum_outputs[i] = 0;
				}
			}

			confidence = sum_output;
		}
		return sum_outputs;
	}
	
	//ネットワークの出力と信頼度との両方の計算を行う関数 ILSで使用する
	public double[] calculate_outputs_with_confidence(double input[], double confidence_threshold) {
		double sum_output=0D;
		double[] each_output;
		double[] sum_outputs = new double[this.NumberOfOutputs];
		Cell cell_obj;
		this.confidence = 0D;
		if (this.hidden_units.size()>0) {
			Enumeration<Cell> e = this.hidden_units.elements();
			while (e.hasMoreElements()) {
				cell_obj = e.nextElement();
				if (!cell_obj.is_check()) {//checkが付いていないならば 
					each_output = cell_obj.output(input);
					//System.out.println("each output value is " + cell_obj.exp_output());
					sum_output += cell_obj.exp_output();
					for (int i=0; i<this.NumberOfOutputs; i++) {
						sum_outputs[i] += each_output[i]; 
					}
				}
			}
			for (int i=0; i<this.NumberOfOutputs; i++) {
				sum_outputs[i] /= sum_output;
			}
			//信頼度は this.confidenceに書き出される。
			this.confidence = this.confidence_value(sum_output, confidence_threshold);
		}
		return sum_outputs;
	}
	
	//出力に対する信頼性を計算する関数。ILSで使用する
	double confidence_value(double x, double threshold) {
		double result;
		result = (double)1 / (1 + Math.exp(-(x-threshold)));
		return result;
	}
	
	GRNNCell getNearetUnit(double inputs[]) {
		GRNNCell cellobj, nearest_cellobj=null;
		double min_distance = Double.MAX_VALUE;
		this.calculate_outputs(inputs);
		for (Cell cell: this.hidden_units) {
			cellobj = (GRNNCell)cell;
			//System.out.println("mgrnn:getNearestUnit(): actualDistance: " + cellobj.getActualDistance());
			if (cellobj.getActualDistance() < min_distance ) {
				nearest_cellobj = cellobj;
				min_distance = cellobj.getActualDistance();
			}
		}
		return nearest_cellobj;
	}
	
	void editing() {
		GRNNCell cell_obj, s_cell_obj;
		double diff;
		boolean is_same=true;
		for (Cell celli : this.hidden_units) {
			cell_obj = (GRNNCell)celli;
			if (!cell_obj.is_check()) {
				this.calculate_outputs(cell_obj.T);//その中心位置で出力を計算する。
				
				is_same = false;
				for (Cell cellj : this.hidden_units) {
					s_cell_obj = (GRNNCell)cellj;
					if (s_cell_obj != cell_obj && !s_cell_obj.is_check()) {
						if (s_cell_obj.exp_output()>this.activation_threshold) {
							diff =VectorFunctions.getNorm(VectorFunctions.diff(cell_obj.C, s_cell_obj.C)); 
							if (diff <	this.epsilon) {
								is_same = true; 
							}
							//System.out.println("diff is " + diff);
						}
					}
				}
				if (is_same) cell_obj.set_check(true);
			}
		}//while
	}
	
	void clear_check_flag() {
		Cell cell_obj;
		Enumeration<Cell> e = this.hidden_units.elements();
		while (e.hasMoreElements()) {
			cell_obj = (Cell)e.nextElement();
			cell_obj.set_check(false);
		}
	}
	
	public int getNumberOfHiddenUnits() {
		int number=0;
		Cell cell_obj;
		Enumeration<Cell> e = this.hidden_units.elements();
		while (e.hasMoreElements()) {
			cell_obj = (Cell)e.nextElement();
			if (!cell_obj.is_check()) {
				number++;
			}
		}
		return number;
	}
	
	
	//gnuplot形式で、隠れユニットの出力を全部出力
	public void HiddeUnitOutput(FunctionOutput fo) {
		Cell h_cell;
		Enumeration<Cell> e = this.hidden_units.elements();
		while (e.hasMoreElements()) {
			h_cell = (Cell)e.nextElement();
			fo.put(h_cell.OutputHiddenUnitFunction());
		}
		fo.close();
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
	
}
