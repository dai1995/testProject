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
		
		//k_s����
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
		
		if (prev_K != null) {//���������դ�����˦��1�ġˤ��ѹ�ʬ�Τߥ��åȤ���
			K = prev_K;
			K.increase_allocate_data(size, size);//������Ϳ����size�������Υ����������礭�����ɲ�Ū�˳���դ���
			for (int i=0; i<size; i++) {
				h_cell_i = hiddenUnits.elementAt(i);
				h_cell_j = hiddenUnits.elementAt(size-1);
				each_output = h_cell_j.calculate_output(h_cell_i.getCenter());
				K.set_data(i, size-1, each_output);
				K.set_data(size-1, i, each_output);				
			}			
		}else{//���Τ򥻥åȤ���
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
	
	
	//�����ǡ����νŤߤ�buffer��������äƤ��롣
	//���Υǡ����νŤߤ�Wold�����source weight�Ȥ����ݻ�����Ƥ��롣���Υǡ����ϸŤ�design matrix����inverse mapping���۵����롣
	//kappa(=��)��ѥ�᡼���Ȥ��Ƽ�����ꡢ���Υǡ����νŤߤ����ƺ�ä�design matrix��ƹ�������

	/*public boolean IncLearning(MinMoutWeightVariableSizedBuffer buffer, 
			double kappa, wRBFNN old_RBFNN, IncMixtureOfDistributions MN, 
			double UpperLimitOfWeight, double LowerLimitOfWeight) throws MatrixException {
		//if (this.DEBUG) {
			if (this.PHI!=null) System.out.println("wKernelMachine:IncLearning()" + " Phi(" + this.PHI.getL() +", " +this.PHI.getM() + ")");
		//}
		if (this.isFirstLearning) {//�ǽ�γؽ��Ͻ����̤�γؽ�ˡ
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

		}else{//�����ܰʹߤϰʲ���¹�
			//if (this.DEBUG) {
				System.out.println("wRBFNN: IncLearning(): Incremental Learning");
			//}
			
			//�ƹ����оݤȤʤ���֥�˥åȤΥե饰��Ω�ơ��ط������˦���ö�ä�(�������ȡ�
			//��˦���ξ��ʤ��ΰ���㤨��1�ġˤǵ����ǡ����˿�������̵���ʤ�ICw������̵
			//���������ʤäƤ����ߤޤ�ʤ��ʤ��礢��
			//this.RemoveRelatedHiddenUnits(buffer);
			
			//�ƹ����оݤȤʤ���֥�˥åȤΥե饰��Ω�Ƥ�
			//this.check_isVariable(buffer, this.rbfnn_parameters.getMinActivationThreshold());			
			
			System.out.println("Hidden unit size =" + this.HiddenUnits.size() );
			System.out.println("Add unit size =" + AddNumberOfHiddenUnits);
			//��֥�˥åȤ򿷤�������դ��ƥѥ�᡼����buffer�Υ���ץ�ǽ��������
			int ActualAddedUnits = this.init_newhidden_centers(this.HiddenUnits, AddNumberOfHiddenUnits, buffer);
			//int ActualAddedUnits = 0;
			
			if (this.DEBUG) {
				System.out.println("wRBFNN:IncLearning()" + " HiddenUnits = " + this.HiddenUnits.size());
			}
			
			//����������դ�����֥�˥åȡ��ƹ��������˥åȤ�kMeansˡ���濴���֤����
			if (
					this.Inc_k_means(this.HiddenUnits,
							this.rbfnn_parameters.getMinDistance(),
							buffer, this.rbfnn_parameters.getKMeansChangeThreshold(), 
							this.rbfnn_parameters.getMinSigma(), 
							this.rbfnn_parameters.getMaxSigma(), 
							this.rbfnn_parameters.getOverlap(),
							UseWeightedKmeans,
							kappa, old_RBFNN,
							this.rbfnn_parameters.getKMeansMaxIteration()) ) {//��֥�˥åȤΥ��åȥ��åפ�����
				
				if (this.DEBUG) {
					this.display_hidden_centers(this.HiddenUnits);
				}
			
				//design matrix�κƹ���(�������˿����ǡ���ʬ�����������ɲä����˥åȿ�ʬ���䤹)
				int old_L=this.PHI.getL();//������̤��뤿��˸�ǻȤ�
				int old_M=this.PHI.getM();//������̤��뤿��˸�ǻȤ�
				if (this.DEBUG) {
					System.out.println("PHI is (" + old_L + ", " + old_M + ")");
					System.out.println("PHIOLD is (" + old_RBFNN.getPHI().getL() + ", " + old_RBFNN.getPHI().getM() + ")");
				}
				this.Wnew = new MatrixObj(old_L+buffer.getSize(), old_L+buffer.getSize());			
				this.PHI.increase_allocate_data(old_L+buffer.getSize(), old_M+ActualAddedUnits);

				//Wold�ο���������ץ���б������ΰ���ɲä���
				this.Wold.increase_allocate_data(old_L + buffer.getSize(), old_L + buffer.getSize());
				//�����ѥ�������Ф���Ťߤ�Wnew�˥��å�
				//�Ĥ��Ǥ�Wold�ˤ�source weight���ɲå��åȤ��Ƥ���
				for (int b=0; b<buffer.getSize(); b++) {
					this.Wnew.set_data(b+old_L, b+old_L, buffer.getActualWeight(b));
					this.Wold.set_data(b+old_L, b+old_L, buffer.getSourceWeight(b));
				}
				//�����ǡ������Ф��붵�տ�����ΰ��Teacher���ɲå��å�
				for (int o=0; o<this.NumberOfOutputs; o++) {
					this.Teacher[o].increase_allocate_data(old_L+buffer.getSize(), 1);
				}


				//�����ǡ�����ʬ��design matrix�򥻥å�
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
					//Teaching signal����򥻥å�
					for (int o=0; o<this.NumberOfOutputs; o++) {			
						this.Teacher[o].set_data(old_L+p, 0, buffer.getOutput(p)[o]);
					}
				}

			
				//���Υǡ�����ʬ�ο���������դ�����֥�˥å���ʬ�򥻥å�
				for (int p=0; p<old_L; p++) {
					double[] old_x = old_RBFNN.generate_pseudo_input2(p, old_RBFNN); //��������ץ���۵�
					//VectorFunctions.VectorFunctions.display_vector("wRBFNN:IncLearning() pseudo sample", old_x);
					//���Υѥ�������Ф���Ťߤ򿷤���kappa(=��)�ǺƷ׻�����Wnew�˥��åȤ���
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
			
				//incWLeastSqureˡ
				this.IncWLeastSquare(this.HiddenUnits, this.PHI, this.Teacher, this.Wnew);
				if (this.PHI!=null) System.out.println("wRBFNN:IncLearning()" + " Phi(" + this.PHI.getL() +", " +this.PHI.getM() + ")");
			
				//�ؽ��ǡ����򰵽���¸
				this.CompressRecordPastData(buffer, old_RBFNN);
				return true;
			}else{
				return false;
			}
		}// 2���ܰʹߤγؽ��ʤ��
	}
	
	
	public boolean Learning(double inputs[], double target_outputs[], double weight, double sigma) throws MatrixException {
		
	}
		//�Ĥ����
		//�� x weight�����ͤ򤳤����顡inputs[]���濴���֤Ȥ���kernel�����դ���
		//��������ץ��۵�
		//WLS�ؽ���Ԥ�
		 * 
		 */
}
