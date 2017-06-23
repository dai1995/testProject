package mgrnn;

import java.util.ArrayList;

import java.util.Iterator;
import java.util.Scanner;



import matrix.MatrixException;
import matrix.MatrixObj;

import org.w3c.dom.Node;

import VectorFunctions.VectorFunctions;

public class LimitedGRNNLC extends LimitedGRNN {
	boolean DEBUG = false; 

	int ID=1;
	public LimitedGRNNLC(Node nd) {
		super(nd);
	}
	
	public LgrnnLearningStatus learning(double inputs[], double outputs[]) {
		try {
			return this.LGRNNLC_learning(inputs, outputs, this.UpperLimitOfHiddenUnits, this.ImportanceWeight4NewSample, this.UsePseudoInverse);
		} catch (MatrixException e) {
			Scanner sc = new Scanner(System.in);
			// TODO Auto-generated catch block
			e.printStackTrace();
			this.K.display("LimitedGRNNLC:learning():k");
			if (this.DEBUG) {
				sc.next();
			}
			return null;
		}
	}
	
	//If you want to use 'UseEditing=true', please note that other four options should be false.
	//Editing option can be used separately. (UseEditing + Ignore is ok!)
	LgrnnLearningStatus LGRNNLC_learning(double inputs[], double outputs[], int UpperLimitOfHiddenUnits, double importance_weight, boolean UsePseudoInverse) throws MatrixException {
		CellLinearDependency new_cell, each_cell;
		double MinInterference = Double.MAX_VALUE;
		int Action=-1;
		LgrnnLearningStatus result = new LgrnnLearningStatus();
		
		//final int SUBSTITUTION = 1, PRUNING = 2, MODIFY = 3, IGNORE = 4;
		Scanner keyboardScanner = new Scanner(System.in);
		double[] error = VectorFunctions.diff(outputs, this.calculate_outputs(inputs));
		result.residual_error = VectorFunctions.getSqureNorm(error);		
		
		if (this.hidden_units.size() < UpperLimitOfHiddenUnits) {// only adding new units
			new_cell = new CellLinearDependency(this.NumberOfInputs, this.NumberOfOutputs);
			new_cell.setStandardDeviation(this.variance);
			new_cell.set_check(false);
			new_cell.learn(inputs, outputs);
			new_cell.setID(this.ID);
			this.ID++;
			this.hidden_units.add(new_cell);
			System.out.println("LGRNNLC:learning():Append: number of hiddenunits: " + hidden_units.size());
			this.ReOrganizeEdgesAppend(new_cell);
			if (this.DEBUG) {
				this.display_connection(this.hidden_units);
				keyboardScanner.next();
			}
		}else{//substitution & replace, only replace(pruning), modification and ignore.
			// find the most useless unit in terms of ALD
			UselessCell uselesscell_LD = this.getMostUselessCellLD(UsePseudoInverse);
			// find the most useless unit in terms of Editing (for comparison)
			UselessCell uselesscell_EDIT=null;
			if (this.UseEditing) {
				uselesscell_EDIT = this.getMostUselessCellEdit();
			}
			// obtain the linear dependency of the new input
			CellLinearDependency nearest_cell = (CellLinearDependency)this.getNearestUnit(inputs);
			dependency dep_new = this.Calculate_LinearDependency(inputs, nearest_cell); //this.alpha is also calculated
			// estimate the expected loss
			ExpectedLoss eloss = this.EstimateExpectedLoss(inputs, outputs, importance_weight, uselesscell_LD, uselesscell_EDIT, dep_new, nearest_cell);

			if (this.UseEditing && eloss.mag_editing < eloss.mag_ignore) {
				//Editing option can be used separately. (only UseEditing + Ignore is ok!)
				uselesscell_EDIT.target_cell.learn(inputs, outputs);
			}else{
				//get Min interference
				if (this.UseSubstitution && (eloss.mag_substitution < eloss.mag_pruning && eloss.mag_substitution < eloss.mag_modification)) {//substitution and replace
					MinInterference = eloss.mag_substitution;
					Action = LgrnnLearningStatus.SUBSTITUTION;
				}else if (this.UsePrune && (eloss.mag_pruning < eloss.mag_modification)) {
					MinInterference = eloss.mag_pruning;
					Action = LgrnnLearningStatus.PRUNING;
				}else if (this.UseModify) {
					MinInterference = eloss.mag_modification;
					Action = LgrnnLearningStatus.MODIFY;
				}
				
				if (this.UseIgnore && eloss.mag_ignore<=MinInterference) {
					Action = LgrnnLearningStatus.IGNORE;
				}
				
				switch(Action) {
				case LgrnnLearningStatus.SUBSTITUTION:
					Iterator<CellLinearDependency> ed = uselesscell_LD.target_cell.Edge.iterator();
					int index=0;
					//double[] A = new double[this.NumberOfOutputs]; 
					System.out.println("========substitution & replace==========");
					while (ed.hasNext()) {
						each_cell = ed.next();
						each_cell.Increment(uselesscell_LD.target_cell.get_normalized_t_alpha(), 
								uselesscell_LD.target_cell.getAlpha()*uselesscell_LD.alpha.getData(index, 0));
						index++;
					}
					//replace cell learns the new sample
					if (this.DEBUG)	this.display_connection(hidden_units);
					this.ReOrganizeEdgesRemove(uselesscell_LD.target_cell);
					if (this.DEBUG) this.display_connection(hidden_units);
					uselesscell_LD.target_cell.learn(inputs, outputs);
					this.ReOrganizeEdgesAppend(uselesscell_LD.target_cell);
					if (this.DEBUG) {
						this.display_connection(hidden_units);
						keyboardScanner.next();
					}
					result.LearningOption = LgrnnLearningStatus.SUBSTITUTION;
					break;
					
				case LgrnnLearningStatus.PRUNING:
					System.out.println("======== replace ==========");
					if (this.DEBUG) this.display_connection(hidden_units);
					this.ReOrganizeEdgesRemove(uselesscell_LD.target_cell);
					if (this.DEBUG) this.display_connection(hidden_units);
					uselesscell_LD.target_cell.learn(inputs, outputs);
					//uselesscell_EDIT.target_cell.learn(inputs, outputs);
					this.ReOrganizeEdgesAppend(uselesscell_LD.target_cell);
					if (this.DEBUG) {
						this.display_connection(hidden_units);
						keyboardScanner.next();						
					}
					result.LearningOption = LgrnnLearningStatus.PRUNING;
					break;
					
				case LgrnnLearningStatus.MODIFY:
					Iterator<CellLinearDependency> ed3 = nearest_cell.Edge.iterator();
					int index2=0;
					//double[] A = new double[this.NumberOfOutputs]; 
					System.out.println("========Modify=========");
					if (this.DEBUG) this.display_connection(hidden_units);
					while (ed3.hasNext()) {
						each_cell = ed3.next();
						each_cell.Increment(outputs, dep_new.alpha.getData(index2, 0));
						index2++;
					}
					// the nearest_cell also should learn.
					nearest_cell.Increment(outputs, dep_new.alpha.getData(index2,0));
					if (this.DEBUG) {
						this.display_connection(hidden_units);
						keyboardScanner.next();
					}
					result.LearningOption = LgrnnLearningStatus.MODIFY;
					break;
					
				case LgrnnLearningStatus.IGNORE:
					// do nothing!
					System.out.println("=====IGNORE=====");
					result.LearningOption = LgrnnLearningStatus.IGNORE;
					break;
				}

			}
		}
		//System.out.println("grnn:learn() number of hidden units:" + this.hidden_units.size());
		return result;
	}	
	
	//肩代わりさせてpruningする場合(cellには削除するユニットのオブジェクトを指定）
	//新規サンプルをユニットを割つけずに学習(target_cell=nullとする)
	double affectSubstitute(double delta, double target_output[], MatrixObj alpha, CellLinearDependency target_cell) {
		double results[] = new double[this.NumberOfOutputs];
		CellLinearDependency cell;
		int cell_index=0;
		Iterator<CellLinearDependency> ed = target_cell.Edge.iterator();
		while (ed.hasNext()) {
			cell = ed.next();
			if (cell != target_cell) {
				for (int o=0; o<this.NumberOfOutputs; o++) {

					results[o] += cell.getAlpha() * Math.pow((
							alpha.getData(cell_index, 0) * 
							(target_cell.ActualWeight()[o]-cell.ActualWeight()[o])/(cell.getAlpha() + alpha.getData(cell_index, 0))), 2D);

					//results[o] +=cell.getAlpha() * Math.pow(alpha.getData(cell_index, 0)*(target_cell.C[o]-cell.getC()[o]), 2D);
					/*if (target_cell==null) {
						results[o] += Math.pow(target_output[o], 2) * delta;
					}else{
						results[o] += target_cell.getAlpha() * Math.pow(target_cell.C[o], 2) * delta; 
					}*/
				}// for o
				cell_index++;
			}// cell != target_cell
		}//while
		double loss_of_target=0;
		for (int j=0; j<this.NumberOfOutputs; j++) {
			loss_of_target += Math.pow(target_cell.ActualWeight()[j],2D) * delta * target_cell.getAlpha();
		}		
		return VectorFunctions.getSqureNorm(results) + loss_of_target;
		//return 0;
	}// affectSubstitute()

	//新規サンプルをユニットを割つけずに学習
	double affectModify(double delta, double target_output[], MatrixObj alpha, double importance_weight, CellLinearDependency nearest_cell) {
		double results[] = new double[this.NumberOfOutputs];
		CellLinearDependency cell;
		Iterator<CellLinearDependency> ed = nearest_cell.Edge.iterator();
		int cell_index=0;
		while (ed.hasNext()) {
			cell = ed.next();
			for (int o=0; o<this.NumberOfOutputs; o++) {
				results[o] +=cell.getAlpha() * Math.pow(alpha.getData(cell_index, 0)*(target_output[o]-cell.ActualWeight()[o])/(cell.getAlpha() + alpha.getData(cell_index, 0)), 2D); // affect of substitution				
				
			/*	results[o] +=cell.getAlpha() * Math.pow(alpha.getData(cell_index, 0)*(target_output[o]-cell.getC()[o]), 2D)+ // affect of substitution
						importance_weight * delta * Math.pow(target_output[o],2D); // affect of remained error */
			}
			cell_index++;
		}//while
		for (int o=0; o<this.NumberOfOutputs; o++) {						
			/*results[o] +=nearest_cell.getAlpha() * Math.pow(alpha.getData(cell_index, 0)*(target_output[o]-nearest_cell.getC()[o]), 2D)+ // affect of substitution
				importance_weight * delta * Math.pow(target_output[o],2D); // affect of remained error*/
			
			results[o] +=
				importance_weight * delta * Math.pow(target_output[o],2D);			
		}
		return VectorFunctions.getSqureNorm(results);
		//return 0;
	}// affectTakeOver()		
	
	double affectJustPruning(CellLinearDependency target_cell) {
		double[] affect=new double[this.NumberOfOutputs];
		CellLinearDependency ClosestUnit;
		Iterator<CellLinearDependency> ed = target_cell.Edge.iterator();
		while (ed.hasNext()) {
			ClosestUnit = ed.next();

			for (int o=0; o<this.NumberOfOutputs; o++) {
				affect[o] += target_cell.getAlpha() * 
				Math.pow((target_cell.ActualWeight()[o]-ClosestUnit.ActualWeight()[o]), 2);
			}
		}
		//normalize
		for (int o=0; o<this.NumberOfOutputs; o++) {
			affect[o] /= (double)target_cell.Edge.size();
		}
		return VectorFunctions.getSqureNorm(affect);
		//return 0;
	}// affectJustPruning()	
	
	ExpectedLoss EstimateExpectedLoss(double inputs[], double outputs[], double importance_weight, UselessCell uselesscell_LD, UselessCell uselesscell_EDIT, dependency dep_new, CellLinearDependency nearest_cell) throws MatrixException {
		ExpectedLoss e = new ExpectedLoss();
		//double delta_new_sample;
		//initialize data
		e.mag_editing = e.mag_ignore = e.mag_modification = e.mag_pruning = 0;
		
		//estimate expected loss
		System.out.print("LimitedGRNNLC:EstimateExpectedLoss():");
		if (this.UseSubstitution) {
			e.mag_substitution = this.affectSubstitute(uselesscell_LD.minimum_delta, outputs, uselesscell_LD.alpha, uselesscell_LD.target_cell);
			System.out.print(" Affect substition & modify=" + e.mag_substitution);
		}else e.mag_substitution = Double.MAX_VALUE;
		if (this.UsePrune) {
			e.mag_pruning= this.affectJustPruning(uselesscell_LD.target_cell);
			//mag_pruning= this.affectEditing(uselesscell_EDIT.target_cell);
			System.out.print(" prune=" + e.mag_pruning);
		}else e.mag_pruning = Double.MAX_VALUE;
		if (this.UseIgnore) {
			e.mag_ignore = this.affectIgnore(inputs, outputs, importance_weight);
			System.out.print(" Ignore=" + e.mag_ignore);
		}else e.mag_ignore = Double.MAX_VALUE;
		if (this.UseModify) {
			e.mag_modification = this.affectModify(dep_new.delta, outputs, dep_new.alpha, importance_weight, nearest_cell);
			System.out.print(" Modify=" + e.mag_modification);
		}else e.mag_modification = Double.MAX_VALUE;
		if (this.UseEditing) {//Read the explanations above!
			e.mag_editing = this.affectEditing(uselesscell_EDIT.target_cell);
			System.out.print(" Editing=" + e.mag_editing);
		}else{
			e.mag_editing=Double.MAX_VALUE;
		}
		System.out.println(" ");
		return e;
	}// EstimateExpectedLoss();	
	
	// calculate the differences between the height of the cell and those of its neighbors.
	double DiffFromNeighbors(CellLinearDependency target_cell) {
		CellLinearDependency cell_obj;
		double sum_diff = 0D;
		Iterator<CellLinearDependency> ed = target_cell.Edge.iterator();
		while (ed.hasNext()) {
			cell_obj = ed.next();
			sum_diff += VectorFunctions.getSqureNorm(
					VectorFunctions.diff(target_cell.getC(), cell_obj.getC()));
		}//while
		return sum_diff;
	}

	void ReOrganizeEdgesRemove(CellLinearDependency replace_cell) {
		CellLinearDependency Neighbor1=null, Neighbor2=null;
		CellLinearDependency NearestUnit;
		double[] middle_point;
		int NumberOfEdges = replace_cell.Edge.size();
		for (int i=0; i<NumberOfEdges-1; i++) {
			Neighbor1 = replace_cell.Edge.get(i);
			for (int j=i+1; j<NumberOfEdges; j++) {
				if (this.DEBUG) System.out.println("LimitedGRNNLC:ReOrganizeEdgesRemove(): Edge size=" + replace_cell.Edge.size() + " j="+j);
				Neighbor2 = replace_cell.Edge.get(j);
				if (!Neighbor1.isConnected(Neighbor2)) {
					middle_point = this.getMiddlePoint(Neighbor1, Neighbor2);
					NearestUnit = this.getNearestUnit(middle_point, Neighbor2, replace_cell);
					if (NearestUnit.equals(Neighbor1)) {
						if (!Neighbor1.isConnected(Neighbor2)) Neighbor1.Edge.add(Neighbor2);
						if (!Neighbor2.isConnected(Neighbor1)) Neighbor2.Edge.add(Neighbor1);
					}
				}
				//if (Neighbor1.equals(replace_cell)) System.out.println("Equall!!");
				Neighbor2.removeEdge(replace_cell);
			}
			Neighbor1.removeEdge(replace_cell);
		}
		replace_cell.removeAllEdges();
		//System.out.println("LimitedGRNNLC:ReOrganizeEdgesRemove(): replace cell Edge size" + replace_cell.Edge.size());
	}
	
	void ReOrganizeEdgesAppend(CellLinearDependency new_cell) {

		CellLinearDependency nearest_cell = this.getNearestUnit(new_cell.getT(), new_cell);
		if (nearest_cell==null) return;
		if (nearest_cell.Edge.size()==0) {// the first time to make the edge.
			if (this.DEBUG) {
				System.out.println("LimitedGRNNLC:ReOrganizeEdgesAppend():new edge is added!");
			}
			if (!nearest_cell.isConnected(new_cell)) nearest_cell.Edge.add(new_cell);
			if (!new_cell.isConnected(nearest_cell)) new_cell.Edge.add(nearest_cell);
		}else{
			// The edge of the nearest neighbor.
			int NumberOfNeighbors = nearest_cell.Edge.size(); 
			for (int i=0; i<NumberOfNeighbors; i++) {
				CellLinearDependency target_nearest_cell = nearest_cell.Edge.get(i);
				//the middle point between the nearest neighbor and its neighbor.
				double MiddlePoint[] = this.getMiddlePoint(nearest_cell, target_nearest_cell); 
				//s_nearest_cell is the nearest neighbor of the middle point
				CellLinearDependency s_nearest_cell = this.getNearestUnit(MiddlePoint, target_nearest_cell);
				//evacuate the ed.Neighbor to neighbor_cell
				// for all neighbor cell of the nearest neighbor of the new_cell.
				if (s_nearest_cell.equals(new_cell)) {//re-organize the existing connections
					if (this.DEBUG) {
						System.out.println("LimitedGRNNLC:ReOrganizeEdgesAppend(): nearest_cell Exsiting edge is revised!");
					}
					if (!nearest_cell.isConnected(new_cell)) {
						nearest_cell.Edge.set(i, new_cell); // re-connect the edge between nearest_cell and new_cell.
					}else{
						nearest_cell.Edge.remove(i);
						NumberOfNeighbors --;
					}
					if (!new_cell.isConnected(nearest_cell)) new_cell.Edge.add(nearest_cell);
					//re-organize the neighbor_cell's connections.
					for (int j=0; j<target_nearest_cell.Edge.size(); j++) {
						//find the edge between the neighbor_cell and the nearest_negihbor.
						if (target_nearest_cell.Edge.get(j).equals(nearest_cell)) {
							// re-connect the edge to new_cell.
							if (this.DEBUG) {
								System.out.println("LimitedGRNNLC:ReOrganizeEdgesAppend(): target_nearest_cell Exsiting edge is revised!");
							}
							if (!target_nearest_cell.isConnected(new_cell)) {
								target_nearest_cell.Edge.set(j, new_cell);
							}
							if (!new_cell.isConnected(target_nearest_cell)) {
								new_cell.Edge.add(target_nearest_cell);
							}
							break;
						}
					}
				}else{
					if (this.DEBUG) {
						System.out.println("LimitedGRNNLC:ReOrganizeEdgesAppend(): New connections to the new !");
					}
					//create a new connections between new_cell and nearest_cell.
					if (!nearest_cell.isConnected(new_cell)) nearest_cell.Edge.add(new_cell);
					//create a new connections between new_cell and neighbor_cell.
					if (!target_nearest_cell.isConnected(new_cell)) target_nearest_cell.Edge.add(new_cell);
					if (!new_cell.isConnected(nearest_cell)) new_cell.Edge.add(nearest_cell);
					if (!new_cell.isConnected(target_nearest_cell)) new_cell.Edge.add(target_nearest_cell);
				}
			}
		}
	}
	
	double[] getMiddlePoint(CellLinearDependency cell1, CellLinearDependency cell2) {
		return VectorFunctions.multiply(0.5, VectorFunctions.add(cell1.getT(), cell2.getT()));
	}
	//Get the nearest cell to the input x[] from the other cells except for 'ExceptCell1,2'
	CellLinearDependency getNearestUnit(double x[], CellLinearDependency ExceptCell1, CellLinearDependency ExceptCell2) {
		CellLinearDependency cellobj, nearest_cellobj=null;
		double min_distance = Double.MAX_VALUE;
		this.calculate_outputs(x);
		
		for (Cell cell : this.hidden_units) {
			cellobj = (CellLinearDependency)cell;
			if (!cellobj.equals(ExceptCell1) && !cellobj.equals(ExceptCell2)) {
				if (cellobj.getActualDistance() < min_distance ) {
					nearest_cellobj = cellobj;
					min_distance = cellobj.getActualDistance();
				}
			}
		}
		return nearest_cellobj;
	}		
	
	dependency Calculate_LinearDependency(CellLinearDependency target_cell) throws MatrixException {
		CellLinearDependency cell;
		//double OutputWeightNorm = VectorFunctions.getSqureNorm(target_cell.get_normalized_t_alpha());
		dependency dep = new dependency();
		if (this.hidden_units ==null) {
			dep.delta = Double.MAX_VALUE;
			dep.alpha = null;
			return dep;
		}

		this.K = this.matrix_K(target_cell, false);
		
		//k_sを作る
		MatrixObj k_s = new MatrixObj(target_cell.Edge.size(),1);
		//System.out.println("target_cell size is " + target_cell.Edge.size());
		//k_s.display("k_s");
		for (int i=0; i<target_cell.Edge.size(); i++) {
			cell = target_cell.Edge.get(i);
			k_s.set_data(i, 0, cell.exp_output(target_cell.getT()));
		}
		
		dep.alpha = K.inverse().multiply(k_s);
		//K.display("LimitedGRNNLC:Calculate_LinearDependency(): K");
		//alpha.display("grnnLinearDependency:Calculate_LinearDependency()");
		//double delta = OutputWeightNorm * (1 - k_s.Transport().multiply(alpha).getData(0, 0));
		dep.delta = (1 - k_s.Transport().multiply(dep.alpha).getData(0, 0));		
		return dep;
	}
	
	MatrixObj matrix_K(CellLinearDependency target_cell, boolean ContainTargetCell) {
		int size;
		MatrixObj K = null;
		CellLinearDependency h_cell_i, h_cell_j;
		double each_output;
		if (this.DEBUG) {
			System.out.println("LimitedGRNNLC:matrix_K(): ContainTargetCell=" + ContainTargetCell);
		}
		if (ContainTargetCell) {
			size  = target_cell.Edge.size()+1;
		}else{
			size  = target_cell.Edge.size();
		}
		K = new MatrixObj(size, size);
		int index_i = 0, index_j=0;
		for (index_i=0; index_i<target_cell.Edge.size(); index_i++) {
			h_cell_i = target_cell.Edge.get(index_i);
			for (index_j=0; index_j < target_cell.Edge.size(); index_j++) {
				h_cell_j = target_cell.Edge.get(index_j);
				if (this.DEBUG) {
					System.out.println("LimitedGRNNLC:matrix_K(target_cell,"+ContainTargetCell+"):Create (" + h_cell_i.getID() + ", " + h_cell_j.getID() + ") index (" + index_i +", " + index_j+")");
				}
				each_output = h_cell_j.exp_output(h_cell_i.getT());
				K.set_data(index_i, index_j, each_output);
				K.set_data(index_j, index_i, each_output);						
			}
		}
		if (ContainTargetCell) {
			ArrayList<CellLinearDependency> neighbors = target_cell.Edge;
			for (int i=0; i<neighbors.size(); i++) {
				h_cell_i = neighbors.get(i);
				if (this.DEBUG) {
					System.out.println("LimitedGRNNLC:matrix_K(target_cell,"+ContainTargetCell+"):Create (" + h_cell_i.getID() + ", " + target_cell.getID() + ") index (" + i +", " + (size-1) +")");
				}
				each_output = h_cell_i.exp_output(target_cell.getT());
				K.set_data(size-1, i, each_output);
				K.set_data(i, size-1, each_output);				
			}
			K.set_data(size-1, size-1, 1D);
		}
		return K;
	}
	
	dependency Calculate_LinearDependency(double input[], CellLinearDependency nearest_cell) throws MatrixException {
		dependency dep = new dependency();
		if (this.hidden_units ==null) {
			dep.delta = Double.MAX_VALUE;
			dep.alpha = null;
			return dep;
		}
		
		// create design matrix which includes the elements corresponding to the target unit.
		this.K = this.matrix_K(nearest_cell, true);
		
		//k_sを作る
		MatrixObj k_s = new MatrixObj(nearest_cell.Edge.size()+1,1);
		for (int i=0; i<nearest_cell.Edge.size(); i++) {
			k_s.set_data(i, 0, nearest_cell.Edge.get(i).exp_output(input));
		}
		k_s.set_data(nearest_cell.Edge.size(), 0, nearest_cell.exp_output(input));
		dep.alpha = this.K.inverse().multiply(k_s);
		//System.out.println("LimitedGRNNLC:Calculate_LinearDependency(): NumberOfHiddenUnits=" + this.hidden_units.size());
		//K.display("LimitedGRNNLC:Calculate_LinearDependency():K");
		//k_s.display("LimitedGRNNLC:Calculate_LinearDependency():k_s");

		//dep.alpha.display("LimitedGRNNLC:Calculate_LinearDependency()");
		dep.delta = 1 - k_s.Transport().multiply(dep.alpha).getData(0, 0);
		return dep;
	}	

	void display_connection(ArrayList<Cell> hidden_units) {
		CellLinearDependency h_cell;
		
		h_cell = null;
		for (Cell cell : hidden_units) {
			h_cell = (CellLinearDependency)cell;
			System.out.print("<" + h_cell.getID() + ">:");
			this.display_neighbors(h_cell.Edge.iterator());
			System.out.println(" ");
		}
	}
	
	void display_neighbors(Iterator<CellLinearDependency> h_cell) {
		if (!h_cell.hasNext()) {
			return;
		}else{
			System.out.print(" " + h_cell.next().getID());
			display_neighbors(h_cell);
		}
	}
	
	
}
