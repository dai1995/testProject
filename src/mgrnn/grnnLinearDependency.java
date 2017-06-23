package mgrnn;//mgrnnで使用されているクラスを使う

import org.w3c.dom.Node;

import datalogger.FunctionOutput;

import java.util.*;

import matrix.*;



public class grnnLinearDependency {
	MatrixObj K;
	//MatrixObj alpha;
	
	//使用する変数の宣言
	int NumberOfInputs, NumberOfOutputs;
	double epsilon, variance, ErrThreshold, allocationThreshold;
	double shortestDistanse; // the distance from the closest unit.
	boolean isCumulativeErrorEstimation; // evaluation is done by the cumulative error.
	final boolean DEBUG = false;
	ArrayList<Cell> hidden_units = new ArrayList<Cell>();
	
	//冗長度を表現するクラス
	class dependency {
		double delta;
		MatrixObj alpha;
	}
	

	
	public grnnLinearDependency(Node nd) {//GRNNLD_test(29,87行目へ),LimitedGRNN(56行目へ)
		init_parameters(nd);
	}
	//init_parametersの関数作り（構造体？）
	void init_parameters(Node nd) {
		this.Log_method("init_parameters---------------");
		grnnLD_parameter ldgp = new grnnLD_parameter();
		ldgp.getParameter(nd);
		this.NumberOfInputs = ldgp.NumberOfInputs;
		this.NumberOfOutputs = ldgp.NumberOfOutputs;
		this.epsilon = ldgp.epsilon;
		this.variance = ldgp.default_variance;
		this.allocationThreshold = ldgp.getAllocation_threshold();
		this.isCumulativeErrorEstimation = ldgp.IsEstimatedByCumulativeError();//mgrnnLinearDependency(198行目から)
		this.K = new MatrixObj(0,0);
		//this.alpha = new MatrixObj(0,0);
		this.clear_check_flag();
		// TODO Auto-generated constructor stub		
	}
	
	
	//外からアクセスするためのメソッド
	public LgrnnLearningStatus learning(double inputs[], double outputs[]) {
		//使用するglobal変数が明確に記述されていることに注意(本体のメソッドが使用する変数が一目で分かる）
		this.Log_method("LgrnnLearningStatus learning---------------");
		return this.GRNNLD_learning(inputs, outputs, this.allocationThreshold);
	}
	
	LgrnnLearningStatus GRNNLD_learning(double inputs[], double outputs[], double allocation_threshold) {
		this.Log_method("LgrnnLearningStatus GRNNLD_learning---------------");
		CellLinearDependency new_cell, each_cell;
		//System.out.println("grnnLinearDependency:learning():number of hiddenunits: " + hidden_units.size());
		dependency dep=null;
		LgrnnLearningStatus result = new LgrnnLearningStatus();
		double[] error = VectorFunctions.VectorFunctions.diff(outputs, this.calculate_outputs(inputs));
		result.residual_error = VectorFunctions.VectorFunctions.getSqureNorm(error);
		try {
			dep = this.Calculate_LinearDependency(inputs);
		} catch (MatrixException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (dep.delta > allocation_threshold) {
			new_cell = new CellLinearDependency(this.NumberOfInputs, this.NumberOfOutputs);
			//new_cell.set_default_variance(this.default_variance);
			new_cell.set_check(false);
			new_cell.learn(inputs, outputs);
			this.hidden_units.add(new_cell);
		}else{
			//Enumeration<CellLinearDependency> e = this.hidden_units.elements();
			int index=0;
			for (Cell cell: this.hidden_units) {
				each_cell = (CellLinearDependency)cell;
				//this.alpha.display("grnnLinearDependency:learning(): alpha");
				each_cell.Increment(outputs, dep.alpha.getData(index, 0));
				index ++;
			}
		}
		return result;
		//System.out.println("grnn:learn() number of hidden units:" + this.hidden_units.size());
	}
	
	
	dependency Calculate_LinearDependency(double input[]) throws MatrixException {
		this.Log_method("dependency Calculate_LinearDependency---------------");
		dependency dep = new dependency();
		if (this.hidden_units ==null) {
			dep.delta = Double.MAX_VALUE;
			dep.alpha = null;
			return dep;
		}
		
		this.K = this.matrix_K(hidden_units, K);
		
		//k_sを作る
		MatrixObj k_s = new MatrixObj(hidden_units.size(),1);
		for (int i=0; i<hidden_units.size(); i++) {
			k_s.set_data(i, 0, hidden_units.get(i).exp_output(input));
		}
		//this.K.inverse().display(name)
		dep.alpha = this.K.inverse().multiply(k_s);
		//K.display("K");
		//alpha.display("grnnLinearDependency:Calculate_LinearDependency()");
		dep.delta = 1 - k_s.Transport().multiply(dep.alpha).getData(0, 0);
		return dep;
	}
	
	//----------------------------------------------------------------------------
	public double[] calculate_outputs(double input[]) {
		this.Log_method("calculate_outputs---------------");
		int count = 1;
		double sum_output=0D;
		double[] each_output, sum_outputs = new double[this.NumberOfOutputs];
		this.Log("this.hidden_units = " + Integer.toString(this.hidden_units.size()));//194が追加
		if (this.hidden_units.size() > 0) {
			for (Cell cell_obj: this.hidden_units) {
				if (!cell_obj.is_check()) {//checkが付いていないならば 
					
					each_output = cell_obj.output(input);
					//System.out.println("each output value is " + cell_obj.exp_output());
					sum_output += cell_obj.responsiblity();//この部分は他のクラスとは違うので注意
					for (int i=0; i<this.NumberOfOutputs; i++) {
						//System.out.println("cell_obj.output(input)[" + i + "] = " + cell_obj.output(input)[i] + ", count = " + count);
						sum_outputs[i] += each_output[i]; 
					}
				}
				count++;
			}
			//this.Log("test1---------------");
			
			// Following 5 lines are wasteful! You should remove these lines if it is possible. 
			for (Cell cell_obj: this.hidden_units) {
				if (!cell_obj.is_check()) {//checkが付いていないならば
					cell_obj.setSumOfOutputs(sum_output);
				}
			}
			this.Log("test2---------------");
			if (sum_output > Double.MIN_VALUE) { //NANを防ぐ
				for (int i=0; i<this.NumberOfOutputs; i++) {
					sum_outputs[i] /= sum_output;//LGRNNのy
		
				}
			}else{
				//System.out.println("grnnLinearDependency.calculate_outputs(): pass!");
				for (int i=0; i<this.NumberOfOutputs; i++) {
					System.out.println("(・´з`・)");//194
					sum_outputs[i] = 0;
				}
			}
			this.Log("test3---------------");
		}
		return sum_outputs;
	}
	
	public MatrixObj matrix_K(ArrayList<Cell> hidden_units, MatrixObj prev_K) {
		this.Log_method("MatrixObj matrix_K---------------");
		int size;
		if (hidden_units==null) {
			size = 0;
		}else{
			size = hidden_units.size();
		}
		MatrixObj K = null;
		Cell h_cell_i, h_cell_j;
		double each_output;
		
		if (prev_K != null) {//新しく割付けた細胞（1個）の変更分のみセットする
			K = prev_K;
			K.increase_allocate_data(size, size);//引数で与えるsizeが以前のサイズよりも大きいと追加的に割り付ける
			for (int i=0; i<size; i++) {
				h_cell_i = hidden_units.get(i);
				h_cell_j = hidden_units.get(size-1);
				each_output = h_cell_j.exp_output(h_cell_i.getT());
				K.set_data(i, size-1, each_output);
				K.set_data(size-1, i, each_output);				
			}			
		}else{//全体をセットする
			K = new MatrixObj(size, size);
			for (int i=0; i<size; i++) {
				h_cell_i = hidden_units.get(i);
				for (int j=i; j<size; j++) {
					h_cell_j = hidden_units.get(j);
					each_output = h_cell_j.exp_output(h_cell_i.getT());
					K.set_data(i, j, each_output);
					K.set_data(j, i, each_output);				
				}
			}
		}
		return K;
	}
	
	public int getNumberOfHiddenUnits() {
		this.Log_method("getNumberOgHiddenUnits---------------");
		int number=0;
		for (Cell cell_obj : this.hidden_units) {
			if (!cell_obj.is_check()) {
				number++;
			}
		}
		return number;
	}
	
	//gnuplot形式で、隠れユニットの出力を全部出力
	public void HiddeUnitOutput(FunctionOutput fo) {
		this.Log_method("HiddenUnitOutput---------------");
		for (Cell h_cell : this.hidden_units) {
			fo.put(h_cell.OutputHiddenUnitFunction());
		}
		fo.close();
	}	
	
	void clear_check_flag() {
		this.Log_method("clear_check_flag---------------");
		for (Cell cell_obj : this.hidden_units) {
			cell_obj.set_check(false);
		}
	}	
	
	public Cell getNearestUnit(double inputs[]) {
		this.Log_method("Cell getNearestUnit(引数double型)---------------");
		Cell nearest_cellobj=null;
		double min_distance = Double.MAX_VALUE;
		this.calculate_outputs(inputs);
		for (Cell cellobj : this.hidden_units) {
			if (cellobj.getActualDistance() < min_distance ) {
				nearest_cellobj = cellobj;
				min_distance = cellobj.getActualDistance();
			}
		}
		return nearest_cellobj;
	}
	
	Cell getNearestUnit(Cell target_cell) {
		this.Log_method("Cell getNearestUnit(引数Cell型)---------------");
		Cell nearest_cellobj=null;
		double min_distance = Double.MAX_VALUE;
		this.calculate_outputs(target_cell.getT());
		for (Cell cellobj : this.hidden_units) {
			if (!cellobj.equals(target_cell)) {
				if (cellobj.getActualDistance() < min_distance ) {
					nearest_cellobj = cellobj;
					min_distance = cellobj.getActualDistance();
				}
			}
		}
		return nearest_cellobj;
	}

	/**
	 * @param numberOfInputs the numberOfInputs to set
	 */
	public void setNumberOfInputs(int numberOfInputs) {
		this.Log_method("setNumberOfInputs---------------");
		NumberOfInputs = numberOfInputs;
	}

	/**
	 * @param numberOfOutputs the numberOfOutputs to set
	 */
	public void setNumberOfOutputs(int numberOfOutputs) {
		this.Log_method("setNumberOfOutputs---------------");
		NumberOfOutputs = numberOfOutputs;
	}

	/**
	 * @return the shortestDistanse
	 */
	public double getShortestDistanse() {
		this.Log_method("getShortestDistanse---------------");
		return shortestDistanse;
	}

	/**
	 * @param shortestDistanse the shortestDistanse to set
	 */
	public void setShortestDistanse(double shortestDistanse) {
		this.Log_method("setShortestDistanse---------------");
		this.shortestDistanse = shortestDistanse;
	}
	
	/**
	 * @return the hidden_units
	 */
	public ArrayList<Cell> getHidden_units() {
		this.Log_method("getHidden_units---------------");
		return hidden_units;
	}	
	//log用メソッド--------------------------------------------------------------------------
	void Log_method(String str) {
		if (this.DEBUG) {
			System.out.println("grnnLinearDependency." + str);
		}
	}		
	void Log(String str) {
		if (this.DEBUG) {
			System.out.println(str);
		}
	}	
}
