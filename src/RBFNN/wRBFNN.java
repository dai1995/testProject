package RBFNN;

import java.util.Enumeration;
import java.util.Vector;

import matrix.MatrixException;
import matrix.MatrixObj;

import org.w3c.dom.Node;


import FIFO.MinMoutWeightVariableSizedBuffer;
import MixtureOfDistributions.IncMixtureOfDistributions;
import MixtureOfDistributions.MixtureOfNormalDistributions;


public class wRBFNN extends RBFNN implements RBFNet, Cloneable {
	//String name;
	
	
	boolean DEBUG=false;

	MatrixObj Wold, Wnew;
	MatrixObj Hat;//Hat matrix

	public wRBFNN(Node nd) {
		super(nd);
		// TODO Auto-generated constructor stub
	}
	
	public boolean Learning(int NumberOfLearningSamples, 
			double inputs[][], double target_outputs[][], double weights[], double MinSigma, double MaxSigma, double Overlap, boolean UseWeightedKmeans) throws MatrixException {
		//init hidden centers
		this.init_hidden_centers(this.HiddenUnits, inputs);

		//setup hidden units
		if (this.k_means(this.HiddenUnits,
				this.rbfnn_parameters.getMinDistance(),
				NumberOfLearningSamples, 
				this.rbfnn_parameters.getKMeansChangeThreshold(), inputs,
				MinSigma, MaxSigma, Overlap, this.rbfnn_parameters.getKMeansMaxIteration())) {
			if (this.DEBUG) {
				this.display_hidden_centers(this.HiddenUnits);
			}
		
			//setup output connections
			this.wLeastSquare(this.HiddenUnits, NumberOfLearningSamples, inputs, target_outputs, weights);
			return true;
		}else{
			return false;
		}
	}	
	
	public boolean Learning(MinMoutWeightVariableSizedBuffer buffer, boolean UseWeightedKmeans) throws MatrixException {
		//this.DEBUG = true;
		if (this.DEBUG) {
			System.out.println("wRBFNN:Learning()");
		}
			
		this.init_hidden_centers(this.HiddenUnits, buffer);
		//int AddNumberOfHiddenUnits = this.rbfnn_parameters.getNumberOfHiddenUnits();
		//if (this.HiddenUnits.size()>0) this.HiddenUnits.removeAllElements();
		//this.init_newhidden_centers(this.HiddenUnits, AddNumberOfHiddenUnits, buffer);//�����Ȥ��Ȳ��Τ�NaN��¿ȯ*/
		
		if (k_means(this.HiddenUnits,
				this.rbfnn_parameters.getMinDistance(),
				this.rbfnn_parameters.getKMeansChangeThreshold(), buffer,
				this.rbfnn_parameters.getMinSigma(), this.rbfnn_parameters.getMaxSigma(), 
				this.rbfnn_parameters.getOverlap(), 
				UseWeightedKmeans, this.rbfnn_parameters.getKMeansMaxIteration())) {
			if (this.DEBUG) {
				this.display_hidden_centers(this.HiddenUnits);
			}
			wLeastSquare(this.HiddenUnits, buffer);
			//this.DEBUG = false;
			//�ؽ��ǡ����򰵽���¸
			this.CompressRecordPastData(buffer, null);
			return true;
		}else{
			return false;
		}
	}
	

	/*public void RemoveRelatedHiddenUnits(MinMoutWeightVariableSizedBuffer buffer) {
		HiddenUnit h_cell;
		//�ƹ����оݤȤʤ���֥�˥åȤΥե饰��Ω�Ƥ�
		this.check_isVariable(buffer, this.rbfnn_parameters.getMinActivationThreshold());

		//�ƹ����оݤȤʤ��˦���ö�ä�.

		int cell=0;
		while (cell < this.HiddenUnits.size()) {
			h_cell = (HiddenUnit)this.HiddenUnits.elementAt(cell);   
			if (!h_cell.isFixed()) {
				this.HiddenUnits.removeElementAt(cell);
			}else{
				cell++;
			}
		}
		
	}*/

	//�����ǡ����νŤߤ�buffer��������äƤ��롣
	//���Υǡ����νŤߤ�Wold�����source weight�Ȥ����ݻ�����Ƥ��롣���Υǡ����ϸŤ�design matrix����inverse mapping���۵����롣
	//kappa(=��)��ѥ�᡼���Ȥ��Ƽ�����ꡢ���Υǡ����νŤߤ����ƺ�ä�design matrix��ƹ�������

	public boolean IncLearning(MinMoutWeightVariableSizedBuffer buffer, 
			int AddNumberOfHiddenUnits, 
			boolean UseWeightedKmeans, 
			double kappa, wRBFNN old_RBFNN, IncMixtureOfDistributions MN, 
			double UpperLimitOfWeight, double LowerLimitOfWeight) throws MatrixException {
		//if (this.DEBUG) {
			if (this.PHI!=null) System.out.println("wRBFNN:IncLearning()" + " Phi(" + this.PHI.getL() +", " +this.PHI.getM() + ")");
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

	//MixtureOfGaussian����HiddenUnitCenter�򥤥�ݡ��Ȥ�����(kmeans���ȸ�Ω��������������������԰���ˤʤ�Τ�EM�ˤ�����
	public void Learning(MinMoutWeightVariableSizedBuffer buffer, MixtureOfNormalDistributions mn) throws MatrixException {
		//this.DEBUG = true;
		if (this.DEBUG) {
			System.out.println("wRBFNN:Learning()");
		}
		this.init_hidden_centers(this.HiddenUnits, buffer);
		this.ImportHiddenCenters(this.HiddenUnits,
				this.rbfnn_parameters.getMinDistance(),
				mn, this.rbfnn_parameters.getMinSigma(),
				this.rbfnn_parameters.getMaxSigma(), 
				this.rbfnn_parameters.getOverlap());
		if (this.DEBUG) {
			this.display_hidden_centers(this.HiddenUnits);
		}
		wLeastSquare(this.HiddenUnits, buffer);
		//this.DEBUG = false;
		this.CompressRecordPastData(buffer, null);
	}	
	
	
	//�������wLeastSquare��IncrementalLearning���ͤˤϤʤäƤ��ʤ��Τ���ա�

	void wLeastSquare(Vector<HiddenUnit> hidden_unit, int NumberOfLearningSamples, double inputs[][], double target[][], double weights[]) throws MatrixException {
		HiddenUnit h_cell;
		MatrixObj phi = new MatrixObj(NumberOfLearningSamples, hidden_unit.size());
		MatrixObj T[] = new MatrixObj[this.NumberOfOutputs]; 
		this.Wnew = new MatrixObj(NumberOfLearningSamples, NumberOfLearningSamples);
		MatrixObj c;
		MatrixObj c_result[] = new MatrixObj[this.NumberOfOutputs];
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			T[o] = new MatrixObj(NumberOfLearningSamples, 1);
		}

		for (int p=0; p<NumberOfLearningSamples; p++) {
			this.Wnew.set_data(p, p, weights[p]);
			Enumeration<HiddenUnit> e = hidden_unit.elements();
			int m=0;
			while (e.hasMoreElements()) {
				h_cell = (HiddenUnit)e.nextElement();
				phi.set_data(p, m, h_cell.calculate_output(inputs[p]));
				m++;
			}
//			System.out.println("p=" + p);
			//System.out.println("target[0][" + p + "]=" + target[p][0]);
			for (int o=0; o<this.NumberOfOutputs; o++) {
				T[o].set_data(p, 0, target[p][o]);				
			}
		}
		this.PHI = phi;//pseudo input�����˻Ȥ�
		this.Teacher = T;
		
		if (this.DEBUG) {
			//T.display("wRBFNN:wLeastSquare():T");
			phi.display("wRBFNN:wLeastSquare():phi");
		}
		c = phi.Transport().multiply(this.Wnew.Transport()); //phi^{T} W^{T}
		c = c.multiply(phi); //phi^{T} W^{T} phi
		c = c.inverse(); // (phi^{T} W^{T} phi)^{-1}
		c = c.multiply(phi.Transport()); // (phi^{T} W^{T} phi)^{-1} phi^{T}
		c = c.multiply(this.Wnew); //(phi^{T} W^{T} phi)^{-1} phi^{T} W
		this.Hat = new MatrixObj(c.getL(), c.getM(), c.getMatrix()); // hat matrix;
		this.Hat = phi.multiply(this.Hat);
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			c_result[o] = c.multiply(T[o]);// (phi^{T} W^{T} phi)^{-1} phi^{T} T
		}
		this.C_result = c_result;//�쥳���ɤ����Ѥ����		
		if (this.DEBUG) {
			c.display("wRBFNN:wLeastSquare():Wmap");
		}

		for (int o=0; o<this.NumberOfOutputs; o++) {
			for (int cell=0; cell<hidden_unit.size(); cell++) {
				this.C[cell][o] = c_result[o].getData(cell, 0);
				System.out.println("C[cell=" + cell + "][" + o + "]=" + this.C[cell][o]);
			}
		}

	}

	
	//Incremental Learning���͡��Ȥ��äƤ�ǽ�Σ����ܤγؽ��ˤΤ߻Ȥ��롣

	void wLeastSquare(Vector<HiddenUnit> hidden_unit, MinMoutWeightVariableSizedBuffer buffer) throws MatrixException {
		HiddenUnit h_cell;
		MatrixObj phi = new MatrixObj(buffer.getSize(), hidden_unit.size());
		MatrixObj T[] = new MatrixObj[this.NumberOfOutputs];
		this.Wnew = new MatrixObj(buffer.getSize(), buffer.getSize());
		MatrixObj c;
		MatrixObj c_result[] = new MatrixObj[this.NumberOfOutputs];
		
		//Wold��Incremental learning�λ��˻Ȥ��롣���Υ᥽�åɤǤ�ɬ�ܤΤ�ΤǤϤʤ�		
		this.Wold = new MatrixObj(buffer.getSize(), buffer.getSize());
		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			T[o] = new MatrixObj(buffer.getSize(), 1);
		}
		
		for (int p=0; p<buffer.getSize(); p++) {
			this.Wnew.set_data(p, p, buffer.getActualWeight(p));
			this.Wold.set_data(p, p, buffer.getSourceWeight(p));//sourceWeight�������ݻ����롣��Ǧˤ��Ѥ�뤫�⤷��ʤ��Τ�
			Enumeration<HiddenUnit> e = hidden_unit.elements();
			int m=0;
			while (e.hasMoreElements()) {
				h_cell = (HiddenUnit)e.nextElement();
				phi.set_data(p, m, h_cell.calculate_output(buffer.getInput(p)));
				m++;
			}
//			System.out.println("p=" + p);
			//System.out.println("target[0][" + p + "]=" + target[p][0]);
			for (int o=0; o<this.NumberOfOutputs; o++) {
				T[o].set_data(p, 0, buffer.getOutput(p)[o]);
			}
		}
		this.PHI = phi;//pseudo input�����˻Ȥ�	
		this.Teacher = T;
		if (this.DEBUG) {
			//T.display("wRBFNN:wLeastSquare():T");
			phi.display("wRBFNN:wLeastSquare():phi");
		}
		c = phi.Transport().multiply(this.Wnew.Transport()); //phi^{T} W^{T}
		c = c.multiply(phi); //phi^{T} W^{T} phi
		c = c.inverse(); // (phi^{T} W^{T} phi)^{-1}
		c = c.multiply(phi.Transport()); // (phi^{T} W^{T} phi)^{-1} phi^{T}
		c = c.multiply(this.Wnew); //(phi^{T} W^{T} phi)^{-1} phi^{T} W
		this.Hat = new MatrixObj(c.getL(), c.getM(), c.getMatrix()); // hat matrix;
		this.Hat = phi.multiply(this.Hat);

		
		for (int o=0; o<this.NumberOfOutputs; o++) {
			c_result[o] = c.multiply(T[o]); // (phi^{T} W^{T} phi)^{-1} phi^{T} T
		}
		this.C_result = c_result;//�쥳���ɤ����Ѥ����		
		if (this.DEBUG) {
			c.display("wRBFNN:wLeastSquare():Wmap");
		}

		for (int o=0; o<this.NumberOfOutputs; o++) {		
			for (int cell=0; cell<hidden_unit.size(); cell++) {
				this.C[cell][o] = c_result[o].getData(cell, 0);
				System.out.println("C[cell=" + cell + "][" + o + "]=" + this.C[cell][o]);				
			}
		}
		//Incremental learning�ǻ��Ѥ����ѿ��򥻥åȤ��Ƥ���

		//this.TargetOutputs.display("targetOutputs");
	}
	
	
	//Incremental wLeastSqure
	//�ǽ�ΰ���ܤ�wLeastSqure��Ȥ�������ܰʹߤϤ������Ȥ���

	void IncWLeastSquare(Vector<HiddenUnit> hidden_unit, MatrixObj phi, MatrixObj T[], MatrixObj W) throws MatrixException {
		MatrixObj c;
		MatrixObj c_result[] = new MatrixObj[this.NumberOfOutputs];
		
		if (this.DEBUG) {
			//T.display("RBFNN:LeastSquare():T");
			//phi.display("wRBFNN:IncLeastSquare():phi");
			W.display("wRBFNN: IncWLeastSquare(): W");
		}
		
		c = phi.Transport().multiply(W.Transport()); //phi^{T} W^{T}
		c = c.multiply(phi); //phi^{T} W^{T} phi
		//c.display("wRBFNN: IncWLeastSquare(): c");
		c = c.inverse(); // (phi^{T} W^{T} phi)^{-1}
		c = c.multiply(phi.Transport()); // (phi^{T} W^{T} phi)^{-1} phi^{T}
		c = c.multiply(W); //(phi^{T} W^{T} phi)^{-1} phi^{T} W
		this.Hat = new MatrixObj(c.getL(), c.getM(), c.getMatrix()); // hat matrix;
		this.Hat = phi.multiply(this.Hat);		
		

		for (int o=0; o<this.NumberOfOutputs; o++) {
			c_result[o] = c.multiply(T[o]); // (phi^{T} W^{T} phi)^{-1} phi^{T} T			
		}
		this.C_result = c_result;//�쥳���ɤ����Ѥ����		
		if (this.DEBUG) {
			c.display("wRBFNN:IncWLeastSquare():Wmap");
		}
		
		//����줿W_{MP}������ѿ��˥��ԡ�����
		this.C = new double[hidden_unit.size()][this.NumberOfOutputs];
		for (int cell=0; cell<hidden_unit.size(); cell++) {
			for (int o=0; o<this.NumberOfOutputs; o++) {
				this.C[cell][o] = c_result[o].getData(cell, 0);
			}
		}
		System.out.println("WRBFNN:IncWLeastSquare");
		//this.PHIOld = (MatrixObj)phi.clone();
	}	
	

	boolean k_means(Vector<HiddenUnit> hidden_units, double MinDistance, double change_threshold, MinMoutWeightVariableSizedBuffer buffer, double MinSigma, double MaxSigma, double Overlap, boolean UseWeightedSamples, int KMeansMaxIteration) {
		RBF h_cell=null;
		double changes = Double.MAX_VALUE;
		double sumOfHiddenOutputs;
		int NumOfIterations = 0;

		//�ޤ����Ƥ���֥�˥åȤ�ʬ����MinSigma�˽��������
		this.reset_variance(hidden_units, MinSigma);
		//k-meansˡ��¹�
		while (changes > change_threshold && NumOfIterations < KMeansMaxIteration) {
			changes = 0D;
			//E-step�ѥѥ�᡼���ν����
			this.ResetMstepParameters(hidden_units);
			
			//��������ץ뤫��
			for (int p=0; p<buffer.getSize(); p++) {
				sumOfHiddenOutputs = this.getSumOfHiddenOutputs(hidden_units, buffer.getInput(p));
				Enumeration<HiddenUnit> e = hidden_units.elements();
				while (e.hasMoreElements()) {
					h_cell = (RBF)e.nextElement();
					if (this.DEBUG) {
						System.out.println("wRBFNN:k_means(): sumOutput=" + sumOfHiddenOutputs);
					}
					/*if (sumOfHiddenOutputs < 1.0E-50) {
						sumOfHiddenOutputs = 0.0000001;
					}*/
					if (UseWeightedSamples) { 
						h_cell.w_M_step_add(buffer.getInput(p), sumOfHiddenOutputs, buffer.getActualWeight(p));
					}else{
						h_cell.M_step_add(buffer.getInput(p), sumOfHiddenOutputs);
					}
				}
			}
			//update center
			changes = this.UpdateCenter(hidden_units);
			NumOfIterations ++;
		}
		return this.set_variance(hidden_units, MinDistance, MinSigma, MaxSigma, Overlap);		
	}	
	

	public boolean Inc_k_means(Vector<HiddenUnit> hidden_units, //��֥�˥åȤΥꥹ�� 
			double MinDistance,//��֥�˥åȤκǾ���
			FIFO.MinMoutWeightVariableSizedBuffer buffer, //�����ѥ�����Хåե� 
			double change_threshold, //�ѥ�᡼�����Ѳ������͡ʽ�λ����
			double MinSigma, //HiddenUnit��ʬ���Ǿ���
			double MaxSigma, //ʬ��������
			double Overlap, //�����С���åץե�������
			boolean UseWeightedSamples, //Weighted k-meansˡ��Ĥ������ɤ���
			double lambda,//�ˤ���
			RBFNet old_RBFNN, //pseudo sample�۵��Τ��������RBFNN
			int KMeansMaxIteration) {//k-meansˡ�η����֤����
		RBF h_cell=null;
		double changes = Double.MAX_VALUE;//�ѥ�᡼���Ѳ���
		double sumOfHiddenOutputs;
		int NumOfIterations = 0;
		System.out.println("wRBFNN:Inc_k_means(): start");
		
		//���Ƥ���֥�˥åȤ�ʬ����MinSigma�˽��������
		this.reset_variance(hidden_units, MinSigma);//�����˺���ʡ�
		while (changes > change_threshold && NumOfIterations < KMeansMaxIteration) {
			changes = 0D;
			this.ResetMstepParameters(hidden_units);
			
			for (int p=0; p<buffer.getSize(); p++) {
				//����˥åȤν��Ϥ����¤�����isFixed() ǡ���˷���餺����)
				sumOfHiddenOutputs = this.getSumOfHiddenOutputs(hidden_units, buffer.getInput(p));
				//System.out.println("wRBFNN:Inc_k_means():sumOfHiddenOutputs=" + sumOfHiddenOutputs);
				Enumeration<HiddenUnit> e2 = hidden_units.elements();		
				while (e2.hasMoreElements()) {
					h_cell = (RBF)e2.nextElement();
					//if (!h_cell.isFixed()) {

					//��������ץ�γؽ�
					if (UseWeightedSamples) { 
						h_cell.w_M_step_add(buffer.getInput(p), sumOfHiddenOutputs, buffer.getActualWeight(p));
					}else{
						h_cell.M_step_add(buffer.getInput(p), sumOfHiddenOutputs);
					}				
				}// for cell
			}// for p
			//���Υ���ץ�ʵ�������ץ��
			if (old_RBFNN != null) {
				for (int q=0; q<old_RBFNN.getNumberOfLearnedSamples(); q++) {
					//��������ץ�����
					double[] old_x = old_RBFNN.generate_pseudo_input2(q, old_RBFNN);
					//����˥åȤν��Ϥ����¤����
					sumOfHiddenOutputs = this.getSumOfHiddenOutputs(hidden_units, old_x);
					//System.out.println("wRBFNN:Inc_k_means():sumOfHiddenOutputs=" + sumOfHiddenOutputs);
					
					Enumeration<HiddenUnit> e3 = hidden_units.elements();
					while (e3.hasMoreElements()) {
						h_cell = (RBF)e3.nextElement();
						//System.out.println("Ink_k_means(): Generate Pseudo Samples");
						//System.out.println("wRBFNN: Ink_k_means(): finish Generate Pseudo Samples");
						
						if (this.DEBUG) {
							System.out.print(	"wRBFNN:Inc_k_means(): " + q + " pseudo sample: " + NumOfIterations);							
							
							for (int i=0; i<this.NumberOfInputs; i++) {
								System.out.print(" " + old_x[i]);
							}
							System.out.println(" ");
						}


						//M_STEP��¹�
						if (UseWeightedSamples) { 
							h_cell.w_M_step_add(old_x, sumOfHiddenOutputs, Math.pow(this.Wold.getData(q,q), lambda));
						}else{
							h_cell.M_step_add(old_x, sumOfHiddenOutputs);
						}

					}// for cell
				}// for q (old samples)

			}// if old_RBFNN != null
			
			//��u��ȿ�Ǥ�����
			changes = this.UpdateCenter(hidden_units);
			NumOfIterations++;
		}
		System.out.println("wRBFNN:Inc_k_means(): finished");
		return this.Inc_set_variance(hidden_units, MinDistance, MinSigma, MaxSigma, Overlap);
	}	
	

	
	boolean ImportHiddenCenters(Vector<HiddenUnit> hidden_units, double MinDistance, MixtureOfNormalDistributions mn, double MinSigma, double MaxSigma, double Overlap) {
		RBF h_cell=null;
		int index=0;
		Enumeration<HiddenUnit> e = hidden_units.elements();
		while (e.hasMoreElements()) {
			h_cell = (RBF)e.nextElement();
			h_cell.setCenter(mn.getDistributionCenter(index));
			index++;
		}
		return this.set_variance(hidden_units, MinDistance, MinSigma, MaxSigma, Overlap);
	}
	


	public double get_sqare_error(double[] inputs, double[] target_output, double weight) {
		// TODO Auto-generated method stub
		double output[] = this.getOutputs(inputs);
		double error=0D;
		for (int o=0; o<this.NumberOfOutputs; o++) {
			error += weight * Math.pow(target_output[o]-output[o], 2D);
		}
		return error;
	}	
	
	public double get_hidden_output(double[] inputs, int cell) {
		HiddenUnit h_cell;
		h_cell = (HiddenUnit)this.HiddenUnits.get(cell);
		return h_cell.calculate_output(inputs);
	}

	/**
	 * @return the hat
	 */
	public MatrixObj getHat() {

		return Hat;
	}

	public MatrixObj getWnew() {
		return this.Wnew;
	}
	
	
	/**
	 * @return the wold
	 */
	public MatrixObj getWold() {
		return Wold;
	}


	public Object clone() {  
		wRBFNN myclone = (wRBFNN) super.clone();
	    myclone.C = this.C.clone();//output connection strength
		myclone.beta = this.beta.clone();// for ARD ����γ�ĥ�Τ�����ϻ�����ʬ�Ѱ�
		myclone.PHI=this.PHI.clone();
		myclone.Wnew = this.Wnew.clone();
		myclone.Wold = this.Wold.clone();
		myclone.Hat = this.Hat.clone();
		myclone.name = this.name;
    	Enumeration<HiddenUnit> e = this.HiddenUnits.elements();
    	int i=0;
    	while (e.hasMoreElements()) {
    		RBF h_cell = (RBF)e.nextElement();
    		myclone.HiddenUnits.setElementAt(h_cell.clone(), i);
    		i++;
    	}		
		myclone.Teacher = this.Teacher.clone();
		myclone.WinnerCell = this.WinnerCell;
		myclone.TargetOutputs = this.TargetOutputs.clone();
		return myclone;
	} 	
	
	/*public void setName(String str) {
		this.name = str;
	}
	public String getName() {
		return this.name;
	}*/

	

		
}
