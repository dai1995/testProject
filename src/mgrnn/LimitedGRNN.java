package mgrnn;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

import matrix.MatrixException;
import matrix.MatrixObj;
import VectorFunctions.*;

import org.w3c.dom.Node;

import datalogger.dataOutput;
//import java.util.Scanner;

class ExpectedLoss {
	double mag_substitution, mag_pruning, mag_ignore, mag_modification, mag_editing;
}

class UselessCell {
	final boolean DEBUG = false;
	CellLinearDependency target_cell;
	MatrixObj alpha;
	double minimum_delta;
	
	public double relative_number(int index) {
		this.Log_method("relatibe_number---------------");
		double total_a=0;
		for (int i=0; i<alpha.getL(); i++) {
			//total_a += alpha.getData(i, 0);
			total_a += Math.abs(alpha.getData(i, 0));			
		}
		//return target_cell.getNumberOfLearnedSamples() * alpha.getData(index, 0)/total_a; 
		return target_cell.getNumberOfLearnedSamples() * Math.abs(alpha.getData(index, 0))/total_a; 		
	}
	//log用メソッド--------------------------------------------------------------------------
	void Log_method(String str) {
		if (this.DEBUG) {
			System.out.println("UselessCell." + str);
		}
	}		
	void Log(String str) {
		if (this.DEBUG) {
			System.out.println(str);
		}
	}
}

public class LimitedGRNN extends grnnLinearDependency {
	final boolean DEBUG = false;
	int UpperLimitOfHiddenUnits;

	// 注意：以下の6つの変数だが、この場所にて初期化するとインスタンス化したさいにパラメータファイルによる初期化に失敗する。
	// 従ってここでは以下のように初期かをしないこと。なんでこうなっちゃうのか不明！
	double ImportanceWeight4NewSample; //新しいサンプルの重要度を表す。通常は１で良い。
	
	//以下５つのboolean変数は、比較実験のため５つの学習オプションを使うかどうかを制御するために使用する。
	//例えば肩代わり付き置き換えオプションを使用する場合はUseSubstitution=trueとしておくと、実際に使われるが、
	//falseにセットすると、そのオプションは使われない。
	boolean UseSubstitution;//肩代わり付き置き換えオプションを使用する場合にはtrue
	boolean UseModify;//
	boolean UseIgnore;
	boolean UsePrune;
	boolean UseEditing;//NN-editingを使用する場合にはtrue
	
	boolean isVariableSigma;
	boolean UsePseudoInverse;
	//boolean IsCumlativeErrorEstimation = false;
	Scanner keyScanner;
	public LimitedGRNN(Node nd) {
		super(nd);
		// this.init_parameters(nd);
		// 本当はこのように書かなくても親クラスからinit_parameters()が呼び出されるはずだ。
		// しかしこの行がない場合、不思議なことにこのクラスの変数UseModify等が更新されない。init_parameter()メソッドの中では確かに更新されたことは確認しているが、
		// 実際には更新されないのだ。
		// このプログラムでは実質init_parameters()が2回実行されるが、有効に働くのは2回目に呼び出された時となる。
		// これはJava-vmのバグかもしれない。
		this.keyScanner = new Scanner(System.in);
	}

	void init_parameters(Node nd) {
		this.Log_method("init_parameters---------------");
		LGRNN_parameters lgp = new LGRNN_parameters();
		lgp.getParameter(nd);
		this.UpperLimitOfHiddenUnits = lgp.getNumberOfUpperLimitOfUnits();
		this.NumberOfInputs = lgp.NumberOfInputs;
		this.NumberOfOutputs = lgp.NumberOfOutputs;
		this.epsilon = lgp.epsilon;
		this.variance = lgp.default_variance;
		this.ImportanceWeight4NewSample = lgp.getImportanceWeight4NewSample();
		this.UseIgnore = lgp.isUseIgnore();
		this.UseModify = lgp.isUseModify();
		this.UsePrune = lgp.isUsePrune();
		this.UseSubstitution = lgp.isUseSubstitution();
		this.UseEditing = lgp.isUseEditing();
		this.UsePseudoInverse = lgp.isUsePseudoInverse();
		this.isVariableSigma = lgp.isVariableSigma();
		this.isCumulativeErrorEstimation = lgp.IsEstimatedByCumulativeError();
		this.Log("LimitedGRNN.init_parameters(): IsCumulatieveErrorEstimation = " + this.isCumulativeErrorEstimation);

		this.Log(UseIgnore + " " + UseModify + " " + UsePrune + " "
				+ UseSubstitution + " variance=" + this.variance);

		this.K = new MatrixObj(0, 0);
		// this.alpha = new MatrixObj(0,0);
		this.clear_check_flag();
		//this.keyScanner.next();
	}

	public LgrnnLearningStatus learning(double inputs[], double outputs[], double importance_weight) {
		this.Log_method("LgrnnLearningStatus learning---------------");
		try {
			
			
			return this.LGRNN_learning(inputs, outputs, this.UpperLimitOfHiddenUnits,
					importance_weight, this.UsePseudoInverse);
		} catch (MatrixException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	// If there is a kernel, whose centroid is the same as center[], this method returns true;
	Cell IsExistTheSameKernel(ArrayList<Cell> hidden_units, double center[]) {
		this.Log_method("Cell IsExistTheSameKernel---------------");
		double diff;
		for (Cell cell: hidden_units) {
			diff = VectorFunctions.getSqureNorm(VectorFunctions.diff(center, cell.getT()));
			//this.Log("IsExistTheSameKernel(): diff = " + diff);
			if (diff==0) {
				return cell;
			}
		}
		return null;
	}
	
	// If you want to use 'UseEditing=true', please note that other four options
	// should be false.
	// Editing option can be used separately. (UseEditing + Ignore is ok!)
	LgrnnLearningStatus LGRNN_learning(double inputs[], double outputs[],
			int UpperLimitOfHiddenUnits, double importance_weight, boolean UsePseudoInverse) throws MatrixException {
		this.Log_method("LgrnnLearningStatus LGRNN_learning---------------");
		//System.out.println("importance_weight = " + importance_weight);
		CellLinearDependency each_cell, new_cell;
		double MinInterference = Double.MAX_VALUE;
		LgrnnLearningStatus result = new LgrnnLearningStatus();
		int Action = -1;
		//final int SUBSTITUTION = 1, PRUNING = 2, MODIFY = 3, IGNORE = 4, AGGREGATE = 5;
		double[] error;
		error = VectorFunctions.diff(outputs, this.calculate_outputs(inputs));
		result.residual_error = VectorFunctions.getSqureNorm(error);
		
		//If there are the same center, the learning is canceled!
		if (this.IsExistTheSameKernel(this.hidden_units, inputs) != null) {
			result.LearningOption = LgrnnLearningStatus.IGNORE;
			return result; 
		}
		
		if (this.hidden_units.size() < UpperLimitOfHiddenUnits) {// only adding
																	// new units
			new_cell = new CellLinearDependency(this.NumberOfInputs,
					this.NumberOfOutputs);
			new_cell.setStandardDeviation(this.variance);
			new_cell.learn(inputs, outputs, 1D);
			new_cell.set_check(false);// DO NOT FORGET!
			this.hidden_units.add(new_cell);
			this.Log("LGRNN_learning() Append: number of hiddenunits: "
							+ hidden_units.size());
			if (this.isVariableSigma && this.hidden_units.size() == UpperLimitOfHiddenUnits) {
				OptimizeSigma();
			}
		} else {// substitution & replace, only replace(pruning), modification
				// and ignore.
				// find the most useless unit in terms of ALD
			UselessCell uselesscell_LD = this.getMostUselessCellLD(UsePseudoInverse);
			// find the most useless unit in terms of Editing (for comparison)
			UselessCell uselesscell_EDIT = null;
			if (this.UseEditing) {
				uselesscell_EDIT = this.getMostUselessCellEdit();
			}
			// obtain the linear dependency of the new input
			dependency dep_new = this.Calculate_LinearDependency(inputs); // this.alpha
																			// is
																			// also
																			// calculated
			// estimate the expected loss
			ExpectedLoss eloss = this.EstimateExpectedLoss(inputs, outputs,
					importance_weight, uselesscell_LD, uselesscell_EDIT,
					dep_new);
//			System.out.println("suberror" + eloss.mag_substitution);
//			System.out.println("moderror" + eloss.mag_modification);
//			System.out.println("moderror" + eloss.mag_pruning);
			if (this.UseEditing && eloss.mag_editing < eloss.mag_ignore) {
				// Editing option can be used separately. (only UseEditing +
				// Ignore is ok!)
				uselesscell_EDIT.target_cell.learn(inputs, outputs, 1D);
			} else {
				// get Min interference
				if (this.UseSubstitution
						&& (eloss.mag_substitution < eloss.mag_pruning) 
						&& (eloss.mag_substitution < eloss.mag_modification)) {// substitution
																																// replace
					MinInterference = eloss.mag_substitution;
					Action = LgrnnLearningStatus.SUBSTITUTION;
				} else if (this.UsePrune
						&& (eloss.mag_pruning < eloss.mag_modification)
						&& (eloss.mag_pruning < eloss.mag_substitution)) {
					
					MinInterference = eloss.mag_pruning;
					Action = LgrnnLearningStatus.PRUNING;
				} else if (this.UseModify
						&& (eloss.mag_modification < eloss.mag_pruning)
						&& (eloss.mag_modification < eloss.mag_substitution)) {
					MinInterference = eloss.mag_modification;
					Action = LgrnnLearningStatus.MODIFY;
				}
				if (this.UseIgnore && eloss.mag_ignore <= MinInterference) {
					Action = LgrnnLearningStatus.IGNORE;
				}

				switch (Action) {
				case LgrnnLearningStatus.SUBSTITUTION:
					int index = 0;
					// double[] A = new double[this.NumberOfOutputs];
					System.out.println("LGRNN_learning() ========substitution & replace==========");
					for (Cell cell : this.hidden_units) {
						each_cell = (CellLinearDependency)cell;
						if (!each_cell.equals(uselesscell_LD.target_cell)) {// each_cellがreplace_cellではない場合

							each_cell.Increment(
									uselesscell_LD.target_cell.get_normalized_t_alpha(),
									uselesscell_LD.target_cell.getAlpha() *	uselesscell_LD.alpha.getData(index, 0),
									uselesscell_LD.relative_number(index));
							index++;
						}
					}
					// replace cell learns the new sample
					uselesscell_LD.target_cell.learn(inputs, outputs, 1D);
					if (this.isVariableSigma) this.OptimizeSigma();
					break;

				case LgrnnLearningStatus.PRUNING:
					System.out.println("LGRNN_learning() ======== replace ==========");
					CellLinearDependency closest_unit = (CellLinearDependency) this.getNearestUnit(uselesscell_LD.target_cell);
					closest_unit.IncrementNumberOfLearnedSamples(uselesscell_LD.target_cell.getNumberOfLearnedSamples());
					uselesscell_LD.target_cell.learn(inputs, outputs, 1D);
					// uselesscell_EDIT.target_cell.learn(inputs, outputs);
					if (this.isVariableSigma) this.OptimizeSigma();
					break;

				case LgrnnLearningStatus.MODIFY:
					System.out.println("LGRNN_learning() ========Modify=========");
					int index2 = 0;
					// double[] A = new double[this.NumberOfOutputs];
					double total_a =0; 
					for (int i=0; i<dep_new.alpha.getL(); i++) {
						//total_a += Math.abs(dep_new.alpha.getData(i, 0));
						total_a += Math.abs(dep_new.alpha.getData(i, 0));
					}					

					for (Cell cell : this.hidden_units) {
						each_cell = (CellLinearDependency)cell;
						each_cell.Increment(outputs,
								dep_new.alpha.getData(index2, 0),
								//dep_new.alpha.getData(index2, 0)/total_a);
								Math.abs(dep_new.alpha.getData(index2, 0))/total_a);
						index2++;
					}
					break;

				case LgrnnLearningStatus.IGNORE:
					// do nothing!
					System.out.println("LGRNN_learning() =====IGNORE=====");
					break;
				}

			}
		}
		return result;
		// this.Log("grnn:learn() number of hidden units:" +
		// this.hidden_units.size());
	}
	


	void OptimizeSigma() {
		this.Log_method("OptimizeSigma---------------");
		double mean_distance = this.MeanMinimumDistance();
		double sigma_opt = this.appropreate_sigma(0.1*mean_distance, 10*mean_distance, 0.1);
		this.Log("LimitedGRNN:OptimizeSigma(): simga_opt=" + sigma_opt + " mean_distance is " + mean_distance);
		//keyboard_scanner.next();
		for (Cell cell : this.hidden_units) {
			cell.setR(sigma_opt);
		}		
	}
	
	
	double appropreate_sigma(double min_sigma, double max_sigma) {
		this.Log_method("appropreate_sigma(引数2ｺ)---------------");
		double upper_error, lower_error;
		double middle_sigma = (min_sigma+max_sigma)/2D;
		if (Math.abs(max_sigma - min_sigma) < 0.001) {
			return middle_sigma;
		}
		upper_error = this.EstimateSigma(max_sigma);

		lower_error = this.EstimateSigma(min_sigma);
		//this.Log("LimitedGRNN:appropreate_sigma(): upper_error=" + upper_error + ", lower_error=" + lower_error);
		if (lower_error <= upper_error) {
			return appropreate_sigma(min_sigma, middle_sigma);
		} else {
			return appropreate_sigma(middle_sigma, max_sigma);
		}
	}

	double appropreate_sigma(double min_sigma, double max_sigma, double step_size) {
		this.Log_method("appropreate_sigma(引数3ｺ)---------------");
		double each_error;
		double min_error=Double.MAX_VALUE;
		double optimal_sigma=-1;
		for (double s=min_sigma; s<=max_sigma; s+=step_size) {
			each_error = this.EstimateSigma(s);
			//this.Log("LimitedGRNN: appropreate_sigma(): s=" +s+ " error=" + each_error);
			if (each_error < min_error) {
				optimal_sigma = s;
				min_error = each_error;
			}
		}
		return optimal_sigma;
	}	
	/*
	 * 各々のsigmaの値に対する汎化誤差の近似値
	 */
	double EstimateSigma(double sigma) {
		this.Log_method("EstimateSigma---------------");
		double each_error = 0;
		double[] output;
		int MaxHiddenUnits = 0;
		for (Cell cell: this.hidden_units) {
			cell.setR(sigma);
		}

		for (Cell cell : this.hidden_units) {
			CellLinearDependency target_unit = (CellLinearDependency)cell;
			target_unit.set_check(true);// このユニットだけ計算しない
			output = this.calculate_outputs(target_unit.getT());
			each_error += VectorFunctions.getSqureNorm(VectorFunctions.diff(
					target_unit.get_normalized_t_alpha(), output));
			MaxHiddenUnits++;
			target_unit.set_check(false);// 元に戻す
		}
		return each_error / (double) MaxHiddenUnits;
	}// EstimateSigma();

	//kernel間の中心位置の距離の平均値を求める
	double MeanMinimumDistance() {
		this.Log_method("MeanMinimumDistance---------------");
		CellLinearDependency target_unit = null, nearest_unit;
		double each_distance, average_distance;
		average_distance = 0;
		for (Cell cell : this.hidden_units) {
			target_unit = (CellLinearDependency)cell;
			nearest_unit = (CellLinearDependency) this.getNearestUnit(target_unit);
			each_distance = VectorFunctions.getNorm(
						VectorFunctions.diff(target_unit.getT(), nearest_unit.getT()));
			average_distance += each_distance;
		}
		average_distance /= (double)this.hidden_units.size();
		return average_distance;
	}// MeanDistance();

	//各々の学習オプションを選んだ時の予測誤差を求める。
	ExpectedLoss EstimateExpectedLoss(double inputs[], double outputs[], double importance_weight, 
			UselessCell uselesscell_LD, UselessCell uselesscell_EDIT, dependency dep_new) throws MatrixException {
		this.Log_method("ExpectedLoss EstimateExpectedLoss---------------");
		ExpectedLoss e = new ExpectedLoss();
		// double delta_new_sample;
		// initialize data
		e.mag_editing = e.mag_ignore = e.mag_modification = e.mag_pruning = 0;

		// estimate expected loss
		if (this.UseSubstitution) {
			e.mag_substitution = this.affectSubstitute(
					uselesscell_LD.minimum_delta, outputs,
					uselesscell_LD.alpha, uselesscell_LD.target_cell);
			this.Log("EstimateExpectedLoss() Affect substition & modify="
					+ e.mag_substitution);
			if (Double.isNaN(e.mag_substitution)) System.exit(1);
		} else
			e.mag_substitution = Double.MAX_VALUE;
		if (this.UsePrune) {
			e.mag_pruning = this.affectJustPruning(uselesscell_LD.target_cell);
			// mag_pruning= this.affectEditing(uselesscell_EDIT.target_cell);
			this.Log("EstimateExpectedLoss() prune=" + e.mag_pruning);
			if (Double.isNaN(e.mag_pruning)) System.exit(1);
		} else
			e.mag_pruning = Double.MAX_VALUE;
		if (this.UseIgnore) {
			e.mag_ignore = this
					.affectIgnore(inputs, outputs, importance_weight);
			this.Log("EstimateExpectedLoss() Ignore=" + e.mag_ignore);
			if (Double.isNaN(e.mag_ignore)) System.exit(1);
		} else
			e.mag_ignore = Double.MAX_VALUE;
		if (this.UseModify) {
			e.mag_modification = this.affectModify(dep_new.delta, inputs, outputs,
					dep_new.alpha, importance_weight);
			this.Log("EstimateExpectedLoss() Modify=" + e.mag_modification);
			if (Double.isNaN(e.mag_modification)) System.exit(1);
		} else
			e.mag_modification = Double.MAX_VALUE;
		if (this.UseEditing) {// Read the explanations above!
			e.mag_editing = this.affectEditing(uselesscell_EDIT.target_cell);
			this.Log("EstimateExpectedLoss() Editing=" + e.mag_editing);
		} else {
			e.mag_editing = Double.MAX_VALUE;
		}
		this.Log(" ");
		return e;
	}// EstimateExpectedLoss();

	// 最も冗長なユニットを求める。seek the most useless cell in terms of linear dependency
	UselessCell getMostUselessCellLD(boolean UsePseudoInverse) {
		this.Log_method("UselessCell getMostUselessCellLD---------------");
		CellLinearDependency each_cell;
		dependency dep;
		double delta;
		UselessCell uselesscell = new UselessCell();
		uselesscell.minimum_delta = Double.MAX_VALUE;
		uselesscell.target_cell = null;
		for (Cell cell : this.hidden_units) {
			each_cell = (CellLinearDependency)cell;
			try {
				dep = this.Calculate_LinearDependency(each_cell, UsePseudoInverse);
			} catch (MatrixException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
				dep = new dependency();
				dep.delta = Double.MAX_VALUE;
				dep.alpha = null;
			}
			// in terms of linear dependency
			// this.Log("LimitedGRNN:getMostUselessCellLD():delta=" +
			// delta);
			// delta = dep.delta *
			// VectorFunctions.getSqureNorm(each_cell.ActualWeight());
			delta = dep.delta; // この方が性能が良いことを確かめた2010.10.11
			if (delta < uselesscell.minimum_delta) {
				uselesscell.minimum_delta = delta;
				uselesscell.alpha = dep.alpha.clone();// coefficient for each
														// unit
				uselesscell.target_cell = each_cell;
			}
		}
		return uselesscell;
	}

	// 比較実験用：NN-editing手法を用いて冗長ユニットを探す（商売敵）
	//seek the most useless cell in terms of editing strategy
	UselessCell getMostUselessCellEdit() throws MatrixException {
		this.Log_method("UselessCell getMostUselessCellEdit---------------");
		CellLinearDependency each_cell;
		double delta;
		UselessCell uselesscell = new UselessCell();
		uselesscell.minimum_delta = Double.MAX_VALUE;
		for (Cell cell : this.hidden_units) {
			each_cell = (CellLinearDependency)cell;
			delta = this.DiffFromNeighbors(each_cell);
			if (delta < uselesscell.minimum_delta) {
				uselesscell.minimum_delta = delta;
				uselesscell.alpha = null;
				uselesscell.target_cell = each_cell;
			}
		}
		return uselesscell;
	}

	//target_cellの冗長度δを算出する。行列式の逆行列を算出する時に一般逆行列を使用する時はUsePseduoInverse=trueとする。
	//普段は使用しなくて良い。(faluseにする)
	dependency Calculate_LinearDependency(CellLinearDependency target_cell, boolean UsePseudoInverse) throws MatrixException {
		//CellLinearDependency cell;
		// double OutputWeightNorm =
		// VectorFunctions.getSqureNorm(target_cell.get_normalized_t_alpha());
		this.Log_method("dependency Calculate_LinearDependency---------------");
		//System.out.println("01");
		dependency dep = new dependency();
		if (this.hidden_units == null) {
			dep.delta = Double.MAX_VALUE;
			dep.alpha = null;
			return dep;
		}
		
		this.K = this.matrix_K(hidden_units, target_cell);


		// k_sを作る
		MatrixObj k_s = new MatrixObj(hidden_units.size() - 1, 1);
		
		int i = 0;
		for (Cell cell : this.hidden_units) {
			if (!cell.equals(target_cell)) {
				k_s.set_data(i, 0, cell.exp_output(target_cell.getT()));
				i++;
			}
		}
		
		if (UsePseudoInverse) {
			dep.alpha = K.PseudoInverse().multiply(k_s);
		}else{
			dep.alpha = K.inverse().multiply(k_s);
		}
		// dep.alpha.display("LimitedGRNN:Calculate_LinearDependency(): alpha");
		// K.display("K");
		// alpha.display("grnnLinearDependency:Calculate_LinearDependency()");
		// double delta = OutputWeightNorm * (1 -
		// k_s.Transport().multiply(alpha).getData(0, 0));
		dep.delta = (1 - k_s.Transport().multiply(dep.alpha).getData(0, 0));
		//this.Log("LimitedGRNN: Calculate_LinearDependency():delta is " + dep.delta);
		return dep;
	}

	//冗長度を求める時に使用する行列Kを準備するメソッド
	MatrixObj matrix_K(ArrayList<Cell> hidden_units, Cell target_cell) {
		this.Log_method("MatrixObj matrix_K---------------");
		int size;
		if (hidden_units == null) {
			size = 0;
		} else {
			size = hidden_units.size();
		}
		MatrixObj K = null;
		double each_output;

		K = new MatrixObj(size - 1, size - 1);
		
		int index_i = 0, index_j = 0;
		for (Cell cell_i : this.hidden_units) {
			if (!cell_i.equals(target_cell)) {
				index_j = 0;
				for (Cell cell_j : this.hidden_units) {
					if (!cell_j.equals(target_cell)) {
						each_output = cell_j.exp_output(cell_i.getT());
						K.set_data(index_i, index_j, each_output);
						K.set_data(index_j, index_i, each_output);
						index_j++;
					}
				}
				index_i++;
			}
		}
		return K;
	}

	/*
	 * //肩代わりさせてpruningする場合(cellには削除するユニットのオブジェクトを指定）
	 * //新規サンプルをユニットを割つけずに学習(target_cell=nullとする) double affectSubstitute(double
	 * delta, double target_output[], MatrixObj alpha, CellLinearDependency
	 * target_cell) { double results[] = new double[this.NumberOfOutputs];
	 * CellLinearDependency cell; Enumeration<CellLinearDependency> e =
	 * this.hidden_units.elements(); int cell_index=0; while
	 * (e.hasMoreElements()) { cell = e.nextElement(); if (cell != target_cell)
	 * { for (int o=0; o<this.NumberOfOutputs; o++) { results[o]
	 * +=cell.getAlpha() * Math.pow(alpha.getData(cell_index,
	 * 0)*(target_cell.C[o]-cell.getC()[o]), 2D); if (target_cell==null) {
	 * results[o] += Math.pow(target_output[o], 2) * delta; }else{ results[o] +=
	 * target_cell.getAlpha() * Math.pow(target_cell.C[o], 2) * delta; } }// for
	 * o cell_index++; }// cell != target_cell }//while return
	 * VectorFunctions.getSqureNorm(results); //return 0; }// affectTakeOver()
	 */

	//EstimateExpectedLossに雇われるメソッド
	// 肩代わりさせてpruningする場合(cellには削除するユニットのオブジェクトを指定）の影響を計算する。
	double affectSubstitute(double delta, double target_output[], MatrixObj alpha, CellLinearDependency target_cell) {
		this.Log_method("affectSubstitute---------------");
		CellLinearDependency ClosestUnit = this.getNearestUnit(target_cell.getT(), target_cell);
		int index_of_closest_unit = -1;
		double results[] = new double[this.NumberOfOutputs];
		CellLinearDependency each_cell;
		//double[] actual_weight = target_cell.ActualWeight();

		int cell_index = 0;
		boolean isNegative = false;
		for (Cell cell: this.hidden_units) {
			if (cell != target_cell) {
				each_cell = (CellLinearDependency)cell;
				double[] each_actual_weight = each_cell.ActualWeight();
				for (int o = 0; o < this.NumberOfOutputs; o++) {
					double sum_alpha = each_cell.getAlpha() + target_cell.getAlpha()*alpha.getData(cell_index, 0);
					results[o] += each_cell.getNumberOfLearnedSamples() *
							 Math.pow(
									(alpha.getData(cell_index, 0)
											* (target_cell.ActualWeight()[o] - each_actual_weight[o]) / (each_cell
											.getAlpha() + alpha.getData(
											cell_index, 0))), 2D);
					//results[o] += cell.getNumberOfLearnedSamples()*((cell.getT_alpha()[o] + target_cell.getT_alpha()[0] * alpha.getData(cell_index, 0))
					// /
					//(cell.getAlpha() + target_cell.getAlpha()*alpha.getData(cell_index, 0))
					//- each_actual_weight[0]);					
					if (sum_alpha < 0) isNegative = true;
				}// for o
				if (cell.equals(ClosestUnit)) index_of_closest_unit = cell_index; //最近傍ユニットのindexを得る。この値はtarget_cellの番号を抜いたindexなので、注意				
				cell_index++;
			}// cell != target_cell
		}// while
		double loss_of_target = 0;
		for (int j = 0; j < this.NumberOfOutputs; j++) {
			loss_of_target += target_cell.getNumberOfLearnedSamples() * 
			Math.pow((target_cell.ActualWeight()[j] * ClosestUnit.alpha - 
					ClosestUnit.getT_alpha()[j])/(ClosestUnit.alpha + 
							alpha.getData(index_of_closest_unit, 0)),2D);			
			/*loss_of_target += Math.pow(target_cell.ActualWeight()[j], 2D)
					* delta * target_cell.getAlpha();*/
		}
		if (isNegative) {
//			System.err.println("LimitedGRNN.affectSubstitute(): isNegative!");
			return Double.MAX_VALUE;
		}
		else return VectorFunctions.getSqureNorm(results) + loss_of_target;
		// return 0;
	}// affectSubstitute()
		// 新規サンプルをユニットを割つけずに学習

	//EstimateExpectedLossに雇われるメソッド
	// パラメータを変えるだけのオプションmodifyの影響を計算する。	
	double affectModify(double delta, double target_input[], double target_output[], MatrixObj alpha, double importance_weight) {
		this.Log_method("affectModify---------------");
		double results[] = new double[this.NumberOfOutputs];
		CellLinearDependency each_cell;
		CellLinearDependency ClosestUnit = this.getNearestUnit(target_input, null);
		int index_of_closest_unit = this.hidden_units.indexOf(ClosestUnit);
		
		int cell_index = 0;
		boolean isNegative = false;
		for (Cell cell : this.hidden_units) {
			each_cell = (CellLinearDependency)cell;
			for (int o = 0; o < this.NumberOfOutputs; o++) {
				double sum_alpha = each_cell.getAlpha() + alpha.getData(cell_index, 0);
				results[o] += each_cell.getNumberOfLearnedSamples() * Math.pow(alpha.getData(cell_index, 0) *
						(target_output[o] - each_cell.ActualWeight()[o])	/ (each_cell.getAlpha() + alpha.getData(cell_index, 0)), 2D);
				// affect of substitution
				if (sum_alpha < 0) isNegative = true;
			}
			cell_index++;
		}// while

		for (int j = 0; j < this.NumberOfOutputs; j++) {
			results[j] += importance_weight * Math.pow(
					(target_output[j] * ClosestUnit.alpha-ClosestUnit.getT_alpha()[j])/
					(ClosestUnit.alpha+alpha.getData(index_of_closest_unit, 0)), 2D);			
			/*results[j] += importance_weight * delta
					* Math.pow(target_output[j], 2D); // affect of remained
														// error*/
		}
		if (isNegative) {
//			System.err.println("LimitedGRNN.affectModify(): isNegative");
			return Double.MAX_VALUE;
		}
		else return VectorFunctions.getSqureNorm(results);
		// return 0;
	}// affectTakeOver()
	//EstimateExpectedLossに雇われるメソッド
	//editingの影響：（商売敵）
	double affectEditing(CellLinearDependency target_cell) {
		this.Log_method("affectEditing---------------");
		double[] affect = new double[this.NumberOfOutputs];
		// CellLinearDependency MostClosedUnit =
		// this.getNearestUnit(target_cell);
		double diff = this.DiffFromNeighbors(target_cell);
		for (int o = 0; o < this.NumberOfOutputs; o++) {
			affect[o] += target_cell.getNumberOfLearnedSamples() * diff;
			// Math.pow((target_cell.getC()[o]-MostClosedUnit.getC()[o]), 2);
		}
		return VectorFunctions.getSqureNorm(affect);
		// return 0;
	}// affectEditing()
	//EstimateExpectedLossに雇われるメソッド
	//置き換えのみの影響
	double affectJustPruning(CellLinearDependency target_cell) {
		this.Log_method("affectJustPruning---------------");
		double[] affect = new double[this.NumberOfOutputs];
		CellLinearDependency ClosestUnit;
		ClosestUnit = (CellLinearDependency)this.getNearestUnit(target_cell);
		for (int o = 0; o < this.NumberOfOutputs; o++) {
			affect[o] += target_cell.getNumberOfLearnedSamples() * Math.pow((target_cell.ActualWeight()[o] - ClosestUnit.ActualWeight()[o]), 2);
		}
		return VectorFunctions.getSqureNorm(affect);
		// return 0;
	}// affectJustPruning()
	//EstimateExpectedLossに雇われるメソッド
	//学習しない場合の影響
	double affectIgnore(double inputs[], double outputs[], double importance_weight) {
		this.Log_method("affectIgnore---------------");
		double activity[] = this.calculate_outputs(inputs);
		return importance_weight * VectorFunctions.getSqureNorm(VectorFunctions.diff(outputs, activity));

	}// affectIngore()

	// 最も近いユニットの出力で占領されてしまうリスクを考慮すべき。
	// このときこの最も近いユニットがこれから割り付ける新ユニットだとすると
	// getNearestUnitを書き換える必要が出てくる。なぜならオリジナル版ではすでに
	// 割つけている細胞の中でしか評価しないから
	/*
	 * CellLinearDependency getNearetUnit(double inputs[], double
	 * NewLearningInputs[]) { CellLinearDependency cellobj,
	 * nearest_cellobj=null; double min_distance = Double.MAX_VALUE;
	 * this.calculate_outputs(inputs); Enumeration<CellLinearDependency> e =
	 * this.hidden_units.elements(); while (e.hasMoreElements()) { cellobj =
	 * e.nextElement(); if (cellobj.getActualDistance() < min_distance ) {
	 * nearest_cellobj = cellobj; min_distance = cellobj.getActualDistance(); }
	 * }
	 * 
	 * return nearest_cellobj; }
	 */

	// for editing. editingを使う時の下請けメソッド（商売敵）
	// calculate the differences between the height of the cell and those of its
	// neighbors.
	double DiffFromNeighbors(CellLinearDependency target_cell) {
		this.Log_method("DiffFromNeighbors---------------");
		CellLinearDependency cell_obj, nearest_cell;
		double sum_diff = 0D;
		double middle_point[];
		for (Cell cell : this.hidden_units) {
			cell_obj = (CellLinearDependency)cell;
			if (cell_obj != target_cell) {
				// get middle point between the two centers.
				// Check whether or not the neighbor of the middle point is the
				// cell_obj.
				// if the neighbor is the cell_obj, the cell_obj is the neighbor
				// of the target_cell.
				middle_point = VectorFunctions.multiply(0.5, VectorFunctions
						.add(target_cell.getT(), cell_obj.getT()));
				nearest_cell = this.getNearestUnit(middle_point, target_cell);
				if (nearest_cell == cell_obj) {
					nearest_cell = this.getNearestUnit(middle_point, cell_obj);
					if (nearest_cell == target_cell) {
						// the closest points to the middle point is the
						// target_cell and cell_obj.
						// calculate the difference between the two target
						// values.
						sum_diff += VectorFunctions
								.getSqureNorm(VectorFunctions.diff(
										target_cell.ActualWeight(),
										cell_obj.ActualWeight()));
					}
				}
			}
		}// while
		return sum_diff;
	}

	// Get the nearest cell to the input x[] from the other cells except for
	// 'ExceptCell' x[]に最も近い中心位置を持つkernelを返すメソッド
	CellLinearDependency getNearestUnit(double x[], CellLinearDependency ExceptCell) {
		this.Log_method("CellLinearDependency getNearestUnit---------------");
		CellLinearDependency cellobj, nearest_cellobj = null;
		double min_distance = Double.MAX_VALUE;
		this.calculate_outputs(x);
		for (Cell cell : this.hidden_units) {
			cellobj = (CellLinearDependency)cell;
			if (!cellobj.equals(ExceptCell)) {
				if (cellobj.getActualDistance() < min_distance) {
					nearest_cellobj = cellobj;
					min_distance = cellobj.getActualDistance();
				}
			}
		}
		return nearest_cellobj;
	}

	//似た2つのkernelを１つのkernelに統合するオプション。(商売敵)-------------------------------
	void Aggregation(CellLinearDependency target_cell, CellLinearDependency nearest_unit) {
		this.Log_method("Aggregation---------------");
		double[] new_center;
		double new_sigma;
		double[] new_output;
		double new_alpha, new_t_alpha[];
		new_center = VectorFunctions.add(target_cell.getT(), nearest_unit.getT());
		new_center = VectorFunctions.multiply(0.5, new_center);
		nearest_unit.setT(new_center);
		new_sigma = 0.5 * (target_cell.getR() + nearest_unit.getR());
		nearest_unit.setR(new_sigma);
		new_alpha = target_cell.getAlpha() + nearest_unit.getAlpha();
		new_t_alpha = VectorFunctions.add(target_cell.getT_alpha(), nearest_unit.getT_alpha());
		new_output = VectorFunctions.multiply(1 / new_alpha, new_t_alpha);
		nearest_unit.setC(new_output);
	}

	/**
	 * @param upperLimitOfHiddenUnits the upperLimitOfHiddenUnits to set
	 */
	//-------------------------------------
	public void setUpperLimitOfHiddenUnits(int upperLimitOfHiddenUnits) {
		this.Log_method("setUpperLimitOfHiddenUnits---------------");
		UpperLimitOfHiddenUnits = upperLimitOfHiddenUnits;
	}
	//log用メソッド--------------------------------------------------------------------------
	void Log_method(String str) {
		if (this.DEBUG) {
			System.out.println("LimitedGRNN." + str);
		}
	}		
	void Log(String str) {
		if (this.DEBUG) {
			System.out.println(str);
		}
	}
	/**
	 * @return the isCumlativeErrorEstimation
	 */
	//---------------------------
	public boolean IsCumlativeErrorEstimation() {
		this.Log_method("IsCumlativeErrorEstimation---------------");
		return this.isCumulativeErrorEstimation;
	}
	//外部からLimitedGRNNの入力数を参照するメソッド--------------------
	public int getInputSize() {
		this.Log_method("getInputSize---------------");
		return this.NumberOfInputs;
	}
	//外部クラスからLimitedGRNNクラスでセットされている出力次元数を参照するときに使うメソッド-------------------------------------------
	public int getOutputSize() {
		this.Log_method("getOUtputSize---------------");
		return this.NumberOfOutputs;
	}
	
	//全カーネルのパラメータを表示する。
	public double[][] display_all_kernels() {
		this.Log_method("display_all_kernels---------------");
		CellLinearDependency each_cell;
		double[][] hidden_unit_parameters = new double[this.hidden_units.size()][]; 
		int num = 0;
		for (Cell cell_i : this.hidden_units) {
			each_cell = (CellLinearDependency)cell_i;
			num ++;
			hidden_unit_parameters[num - 1] = each_cell.display_hidden_unit_parameters(num);
		}
		return hidden_unit_parameters;
	}
	
	//全カーネルのパラメータを表示する。
	public void display_all_kernels(dataOutput dout) {
		this.Log_method("display_all_kernels---------------");
		CellLinearDependency each_cell;
		int num = 0;
		for (Cell cell_i : this.hidden_units) {
			each_cell = (CellLinearDependency)cell_i;
			num ++;
			each_cell.display_hidden_unit_parameters(dout, num);
		}
	}	
	//LGRNNのパラメータをテーブルkernelにセーブする
    public void saveLGRNNParameters(Connection con, int LGRNNid) throws SQLException {
    	this.Log_method("saveLGRNNParameters---------------");
        for (Cell each_cell : this.hidden_units) {
            CellLinearDependency each_kernel = (CellLinearDependency)each_cell;
            each_kernel.save_parameter(LGRNNid, con);
        }
    }
}
