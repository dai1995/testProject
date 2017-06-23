package RBFNN;

/*
 * weighted kernel machine (in progress)
 * This method is similar to resource allocating network.
 * However, the main difference between the weighted kernel machine and the RAN is that the 
 * variance of each kernel is fixed.
 */
//import java.util.Enumeration;
import java.util.Vector;

import matrix.MatrixException;
import matrix.MatrixObj;

import org.w3c.dom.Node;

//import FIFO.MinMoutWeightVariableSizedBuffer;
//import MixtureOfDistributions.IncMixtureOfDistributions;

class dependency {
	double delta;
	MatrixObj alpha;
}

public class wKernelMachine extends wRBFNN implements Cloneable {
	boolean DEBUG=false;
	MatrixObj K;

	public wKernelMachine(Node nd) {
		super(nd);
		// TODO Auto-generated constructor stub
	}
	
	/*public boolean Learning(MinMoutWeightVariableSizedBuffer buffer, double err_threshold) {
		//Allocate new hidden units
		for (int p=0; p<buffer.getSize(); p++) {
			double x[] = buffer.getInput(p);
			double weight = buffer.getActualWeight(p);
			double outputs[] = this.getOutputs(x);
			try {
				dependency dep = this.Calculate_LinearDependency(x);
				if (dep.delta * weight < err_threshold) {
					RBF new_cell = new RBF(this.NumberOfInputs, this.init_sigma);
					this.C[]
					new_cell.setCenter(x);
					this.HiddenUnits.add(new_cell);
				}
			}catch(MatrixException me) {
				me.printStackTrace();
			}
		}
		
	}*//* Learning() */
	
	dependency Calculate_LinearDependency(double input[]) throws MatrixException {
		dependency dep = new dependency();
		
		if (this.HiddenUnits ==null) {
			dep.delta = Double.MAX_VALUE;
			dep.alpha = null;
			return dep;
		}
		
		this.K = this.matrix_K(this.HiddenUnits, K);
		
		//k_sを作る
		MatrixObj k_s = new MatrixObj(this.HiddenUnits.size(),1);
		for (int i=0; i<this.HiddenUnits.size(); i++) {
			k_s.set_data(i, 0, this.HiddenUnits.elementAt(i).calculate_output(input));
		}
		dep.alpha = this.K.inverse().multiply(k_s);
		//K.display("K");
		//alpha.display("grnnLinearDependency:Calculate_LinearDependency()");
		dep.delta = 1 - k_s.Transport().multiply(dep.alpha).getData(0, 0);
		return dep;
	}	
	
	MatrixObj matrix_K(Vector<HiddenUnit> hiddenUnits, MatrixObj prev_K) {
		int size;
		if (hiddenUnits==null) {
			size = 0;
		}else{
			size = hiddenUnits.size();
		}
		MatrixObj K = null;
		HiddenUnit h_cell_i, h_cell_j;
		double each_output;
		
		if (prev_K != null) {//新しく割付けた細胞（1個）の変更分のみセットする
			K = prev_K;
			K.increase_allocate_data(size, size);//引数で与えるsizeが以前のサイズよりも大きいと追加的に割り付ける
			for (int i=0; i<size; i++) {
				h_cell_i = hiddenUnits.elementAt(i);
				h_cell_j = hiddenUnits.elementAt(size-1);
				each_output = h_cell_j.calculate_output(h_cell_i.getCenter());
				K.set_data(i, size-1, each_output);
				K.set_data(size-1, i, each_output);				
			}			
		}else{//全体をセットする
			K = new MatrixObj(size, size);
			for (int i=0; i<size; i++) {
				h_cell_i = hiddenUnits.elementAt(i);
				for (int j=i; j<size; j++) {
					h_cell_j = hiddenUnits.elementAt(j);
					each_output = h_cell_j.calculate_output(h_cell_j.getCenter());
					K.set_data(i, j, each_output);
					K.set_data(j, i, each_output);				
				}
			}
		}
		return K;
	}
	
	
	//新規データの重みはbufferの中に入ってくる。
	//既知データの重みはWoldの中にsource weightとして保持されている。既知データは古いdesign matrixからinverse mappingで想起する。
	//kappa(=λ)もパラメータとして受け取り、既知データの重みを改めて作ってdesign matrixを再構成する

	/*public boolean IncLearning(MinMoutWeightVariableSizedBuffer buffer, 
			double kappa, wRBFNN old_RBFNN, IncMixtureOfDistributions MN, 
			double UpperLimitOfWeight, double LowerLimitOfWeight) throws MatrixException {
		//if (this.DEBUG) {
			if (this.PHI!=null) System.out.println("wKernelMachine:IncLearning()" + " Phi(" + this.PHI.getL() +", " +this.PHI.getM() + ")");
		//}
		if (this.isFirstLearning) {//最初の学習は従来通りの学習法
			//if (this.DEBUG) {
				System.out.println("wRBFNN: IncLearning(): 1st Learning");
				System.out.println("wRBFNN:IncLearning()" + " HiddenUnits = " + this.HiddenUnits.size());
			//}
			if (this.Learning(buffer, UseWeightedKmeans)) {
				this.isFirstLearning = false;
				return true;
			}else{
				return false;
			}
			//this.Wold.display("first Wold");

		}else{//２回目以降は以下を実行
			//if (this.DEBUG) {
				System.out.println("wRBFNN: IncLearning(): Incremental Learning");
			//}
			
			//再構築対象となる中間ユニットのフラグを立て、関係する細胞を一旦消す(これをやると、
			//細胞数の少ない領域（例えば1個）で疑似データに信頼性が無くなりICwが限度無
			//く小さくなっていき止まらなくなる場合あり
			//this.RemoveRelatedHiddenUnits(buffer);
			
			//再構築対象となる中間ユニットのフラグを立てる
			//this.check_isVariable(buffer, this.rbfnn_parameters.getMinActivationThreshold());			
			
			System.out.println("Hidden unit size =" + this.HiddenUnits.size() );
			System.out.println("Add unit size =" + AddNumberOfHiddenUnits);
			//中間ユニットを新しく割り付けてパラメータをbufferのサンプルで初期化する
			int ActualAddedUnits = this.init_newhidden_centers(this.HiddenUnits, AddNumberOfHiddenUnits, buffer);
			//int ActualAddedUnits = 0;
			
			if (this.DEBUG) {
				System.out.println("wRBFNN:IncLearning()" + " HiddenUnits = " + this.HiddenUnits.size());
			}
			
			//新しく割り付けた中間ユニット、再構成するユニットをkMeans法で中心位置を決める
			if (
					this.Inc_k_means(this.HiddenUnits,
							this.rbfnn_parameters.getMinDistance(),
							buffer, this.rbfnn_parameters.getKMeansChangeThreshold(), 
							this.rbfnn_parameters.getMinSigma(), 
							this.rbfnn_parameters.getMaxSigma(), 
							this.rbfnn_parameters.getOverlap(),
							UseWeightedKmeans,
							kappa, old_RBFNN,
							this.rbfnn_parameters.getKMeansMaxIteration()) ) {//中間ユニットのセットアップが成功
				
				if (this.DEBUG) {
					this.display_hidden_centers(this.HiddenUnits);
				}
			
				//design matrixの再構築(縦方向に新規データ分、横方向に追加するユニット数分増やす)
				int old_L=this.PHI.getL();//新旧区別するために後で使う
				int old_M=this.PHI.getM();//新旧区別するために後で使う
				if (this.DEBUG) {
					System.out.println("PHI is (" + old_L + ", " + old_M + ")");
					System.out.println("PHIOLD is (" + old_RBFNN.getPHI().getL() + ", " + old_RBFNN.getPHI().getM() + ")");
				}
				this.Wnew = new MatrixObj(old_L+buffer.getSize(), old_L+buffer.getSize());			
				this.PHI.increase_allocate_data(old_L+buffer.getSize(), old_M+ActualAddedUnits);

				//Woldの新しいサンプルに対応する領域を追加する
				this.Wold.increase_allocate_data(old_L + buffer.getSize(), old_L + buffer.getSize());
				//新規パターンに対する重みをWnewにセット
				//ついでにWoldにもsource weightを追加セットしておく
				for (int b=0; b<buffer.getSize(); b++) {
					this.Wnew.set_data(b+old_L, b+old_L, buffer.getActualWeight(b));
					this.Wold.set_data(b+old_L, b+old_L, buffer.getSourceWeight(b));
				}
				//新規データに対する教師信号の領域をTeacherに追加セット
				for (int o=0; o<this.NumberOfOutputs; o++) {
					this.Teacher[o].increase_allocate_data(old_L+buffer.getSize(), 1);
				}


				//新規データ部分のdesign matrixをセット
				HiddenUnit h_cell;
				for (int p=0; p<buffer.getSize(); p++) {
					Enumeration<HiddenUnit> e = this.HiddenUnits.elements();
					int cell=0;
					while (e.hasMoreElements()) {
						h_cell = (HiddenUnit)e.nextElement();
						//System.out.println("PHI set (" + (old_L + p) + ", " + cell);
						this.PHI.set_data(old_L+p, cell, 
								h_cell.calculate_output(buffer.getInput(p)));
						cell++;
					}
					//Teaching signal行列をセット
					for (int o=0; o<this.NumberOfOutputs; o++) {			
						this.Teacher[o].set_data(old_L+p, 0, buffer.getOutput(p)[o]);
					}
				}

			
				//既知データ部分の新しく割り付けた中間ユニット部分をセット
				for (int p=0; p<old_L; p++) {
					double[] old_x = old_RBFNN.generate_pseudo_input2(p, old_RBFNN); //疑似サンプルの想起
					//VectorFunctions.VectorFunctions.display_vector("wRBFNN:IncLearning() pseudo sample", old_x);
					//既知パターンに対する重みを新しいkappa(=λ)で再計算してWnewにセットする
					double weight = Math.pow(MN.q_div_p(old_x), kappa);
					if (weight < LowerLimitOfWeight) weight = LowerLimitOfWeight;
					if (weight > UpperLimitOfWeight) weight = UpperLimitOfWeight;
					this.Wnew.set_data(p, p, weight); 

					for (int m=0; m<this.PHI.getM(); m++) {
						//System.out.println("PHI.m="+this.PHI.getM()+"#of hidden units=" +this.HiddenUnits.size());
						h_cell = (HiddenUnit)this.HiddenUnits.elementAt(m);
						//if (!h_cell.isFixed()) {
						this.PHI.set_data(p, m, h_cell.calculate_output(old_x));
						//}
					}
				
					double[] outputs = old_RBFNN.getOutputs(old_x);
					for (int o=0; o<this.NumberOfOutputs; o++) {
						this.Teacher[o].set_data(p, 0, outputs[o]); 
					}
				}
			
				//incWLeastSqure法
				this.IncWLeastSquare(this.HiddenUnits, this.PHI, this.Teacher, this.Wnew);
				if (this.PHI!=null) System.out.println("wRBFNN:IncLearning()" + " Phi(" + this.PHI.getL() +", " +this.PHI.getM() + ")");
			
				//学習データを圧縮保存
				this.CompressRecordPastData(buffer, old_RBFNN);
				return true;
			}else{
				return false;
			}
		}// 2回目以降の学習ならば
	}
	
	
	public boolean Learning(double inputs[], double target_outputs[], double weight, double sigma) throws MatrixException {
		
	}
		//δを求める
		//δ x weightが閾値をこえたら　inputs[]を中心位置とするkernelを割り付ける
		//疑似サンプル想起
		//WLS学習を行う
		 * 
		 */
}
