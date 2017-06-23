package MixtureOfDistributions;

import java.util.Enumeration;
import java.util.Vector;

import matrix.MatrixException;
import matrix.MatrixObj;
import DataLoad.dataload;
import FIFO.MinMoutWeightVariableSizedBuffer;
import RBFNN.RBFNet;

public class IncMixtureOfDistributions extends MixtureOfNormalDistributions implements Cloneable {
	protected boolean DEBUG = false;
	protected int AddNumberOfModels;
	protected int target_date;
	protected double AIC_value=0;
	
	public IncMixtureOfDistributions(dataload dl, int NumberOfModels,
			int NumberOfInputs, double activation_threshold, double init_sigma_value, boolean isDiagonal,
			boolean SMD, boolean RANInitMode) {

		super(dl, NumberOfModels, NumberOfInputs, activation_threshold, init_sigma_value, isDiagonal,
				SMD, RANInitMode);
		if (this.DEBUG) {
			System.out.println("IncMixtureOfDistributions: # of models=" + this.HiddenNodes.size());
		}

		// TODO Auto-generated constructor stub
	}
	
		

	public double EM(dataload dl, int target_date, RBFNet rbfnn, MixtureOfDistributionParameters parameter) throws MatrixException {
		//FIFO.MinNoOutputsVariableSizedBuffer buffer = CorrectDatasets(dl, target_date, rbfnn, parameter.GammaThreshold, 2);//��Ϣ����ǡ����Τߤ��۵����������
		int NumberOfIterations=0;

		reset_variance_covariance_matrix(parameter.getMin_sigma());
		do {
			init_parameters();			
			E_step(dl, target_date, rbfnn);
			NumberOfIterations++;
		}while(M_step(parameter.getMin_sigma())>parameter.getStop_threshold() &&
				NumberOfIterations < parameter.getMaxNumberOfIterations());
		System.out.println("IncMixtureOfIdistribution.EM(): Number of EM iterations:" + NumberOfIterations);
		this.EstimateNumberOfClassMember(dl, target_date, rbfnn);
		this.AIC_value = this.AIC(dl, target_date, this.isDiagonal, rbfnn);
		if (this.DEBUG) {

			System.out.println("IncMixtureOfDistribution.EM() total number of members " + this.getTotalNumberOfMembers());

			Enumeration<Object> e = this.HiddenNodes.elements();
			while (e.hasMoreElements()) {
				Distribution dis = (Distribution)e.nextElement();
				dis.getSigma().display("Sigma");
				dis.GetCenter().display("center");
			}
		}
		return this.AIC_value;
	}

	//���Υ���ץ��֥åե�����������Ѥ���Version
	public double EM(dataload dl, int target_date, MinMoutWeightVariableSizedBuffer old_pattern_buffer, MixtureOfDistributionParameters parameter) throws MatrixException {
		//FIFO.MinNoOutputsVariableSizedBuffer buffer = CorrectDatasets(dl, target_date, rbfnn, parameter.GammaThreshold, 2);//��Ϣ����ǡ����Τߤ��۵����������
		int NumberOfIterations=0;

		reset_variance_covariance_matrix(parameter.getMin_sigma());
		do {
			init_parameters();			
			E_step(dl, target_date, old_pattern_buffer);
			NumberOfIterations++;
		}while(M_step(parameter.getMin_sigma())>parameter.getStop_threshold() &&
				NumberOfIterations < parameter.getMaxNumberOfIterations());
		System.out.println("IncMixtureOfIdistribution: EM(): Number of EM iterations:" + NumberOfIterations);
		this.EstimateNumberOfClassMember(dl, target_date, old_pattern_buffer);
		this.AIC_value = this.AIC(dl, target_date, this.isDiagonal, old_pattern_buffer);
		if (this.DEBUG) {

			System.out.println("IncMixtureOfDistribution: EM() total number of members " + this.getTotalNumberOfMembers());

			Enumeration<Object> e = this.HiddenNodes.elements();
			while (e.hasMoreElements()) {
				Distribution dis = (Distribution)e.nextElement();
				dis.getSigma().display("Sigma");
				dis.GetCenter().display("center");
			}
		}
		return this.AIC_value;
	}
	
	
	/*@SuppressWarnings("unchecked")
	private MinNoOutputsVariableSizedBuffer CorrectDatasets(dataload dl,
			int target_date, RBFNet rbfnn, double gammaThreshold, int Repeats) {
		IncDistribution N;
		MinNoOutputsVariableSizedBuffer buffer = new MinNoOutputsVariableSizedBuffer(this.NumberOfInputs);
		if (rbfnn !=null) {
			System.out.println("IncMixtureOfDistribution:CorrectDatasets(): Phi(" + rbfnn.getPhiSizeL() + ", " + rbfnn.getPhiSizeM() + ")");
		}else{
			System.out.println("IncMixtureOfDistribution:CorrectDatasets(): Phi is null ");
		}
		for (int i=0; i<Repeats; i++) {
			buffer.dispose();
			//�����ǡ�����Хåե��������
			for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
				buffer.push_data(dl.learning_input_patterns[target_date][p]);
			}
			if (i==0) {//��������ƥե饰����Ƥʤ���
				this.checkFixedUnits(buffer, target_date, gammaThreshold, true);
			}else{//������������ɲ�Ū�˥ե饰��Ω�Ƥ�
				this.checkFixedUnits(buffer, target_date, gammaThreshold, false);				
			}

			//���Υǡ����򽸤��
			if (rbfnn != null) {
				System.out.println("IncMixtureOfDistribution:CorrectDatasets(): buffer size " + buffer.getSize() + " rbfnn learned samples : " + rbfnn.getNumberOfLearnedSamples());				
				for (int p=0; p<rbfnn.getNumberOfLearnedSamples(); p++) {
					double[] x = rbfnn.generate_pseudo_input(p,rbfnn);//��������ץ�����
					IncDistribution winner = this.getWinnerCell(x);
					double[] gamma = this.calculate_gamma(x);
					Enumeration e = this.HiddenNodes.elements();
					int cell=0;
					while (e.hasMoreElements()) {
						N = (IncDistribution)e.nextElement();
						if (gamma[cell]>0.01 && !N.isFix()) {
							buffer.push_data(x);
						}
					}
				}
				if (i<Repeats-1) {//�Ǹ�Ǥʤ�����ɲ�Ū�˥ե饰��Ω�Ƥ�
					this.checkFixedUnits(buffer, target_date, gammaThreshold, false);
				}
			}
			System.out.println("IncMixtureOfDistribution:CorrectDatasets(): buffer size after recalling " + buffer.getSize());			
		}
		return buffer;
	}*/

	
	//���Υ���ץ��rbfnn�����۵�����version
	void E_step(dataload dl, int target_date, RBFNet old_rbfnn) {
		IncDistribution N;
		double gamma[], pseudo_x[];
		int m;
		for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
			gamma = this.calculate_gamma(dl.learning_input_patterns[target_date][p]);
			m=0;

			Enumeration<Object> e = this.HiddenNodes.elements();
			while (e.hasMoreElements()) {
				N = (IncDistribution)e.nextElement();
				N.push_center(dl.learning_input_patterns[target_date][p], gamma[m]);
					//System.out.println("gamma[" + m + "]=" + gamma[m]);
				m++;
			}
		}
		if (old_rbfnn != null) {
			for (int q=0; q<old_rbfnn.getNumberOfLearnedSamples(); q++) {
				pseudo_x = old_rbfnn.generate_pseudo_input2(q, old_rbfnn);
				gamma = this.calculate_gamma(pseudo_x);
				m=0;

				Enumeration<Object> e = this.HiddenNodes.elements();
				while (e.hasMoreElements()) {
					N = (IncDistribution)e.nextElement();
					N.push_center(pseudo_x, gamma[m]);
					//System.out.println("gamma[" + m + "]=" + gamma[m]);
					m++;
				}			
			}
		}
	}
	
	//buffer����Ѥ����Ρ����ξ��������ʤ�����Υ���ץ������buffer������Ƥ���ɬ�פ����롣
	void E_step(dataload dl, int target_date, MinMoutWeightVariableSizedBuffer old_pattern_buffer) {
		IncDistribution N;
		double gamma[], x[];
		int m;
		for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
			gamma = this.calculate_gamma(dl.learning_input_patterns[target_date][p]);
			m=0;

			Enumeration<Object> e = this.HiddenNodes.elements();
			while (e.hasMoreElements()) {
				N = (IncDistribution)e.nextElement();
				N.push_center(dl.learning_input_patterns[target_date][p], gamma[m]);
					//System.out.println("gamma[" + m + "]=" + gamma[m]);
				m++;
			}
		}
		if (old_pattern_buffer != null) {
			for (int q=0; q<old_pattern_buffer.getSize(); q++) {
				x = old_pattern_buffer.getInput(q);
				gamma = this.calculate_gamma(x);
				m=0;

				Enumeration<Object> e = this.HiddenNodes.elements();
				while (e.hasMoreElements()) {
					N = (IncDistribution)e.nextElement();
					N.push_center(x, gamma[m]);
					//System.out.println("gamma[" + m + "]=" + gamma[m]);
					m++;
				}			
			}
		}
	}
		

	double M_step(double min_sigma) throws MatrixException {
		IncDistribution N;
		double change_in_parameters = 0D;

		Enumeration<Object> e = this.HiddenNodes.elements();
		while (e.hasMoreElements()) {
			N = (IncDistribution)e.nextElement();
			change_in_parameters += N.calculate_center();//������isFix()ǡ���˷���餺�¹ԡ����ʬ���Ϥ��Υ᥽�åɤ�������		
			//if (!N.isFix()) {
				change_in_parameters += N.calculate_variance_covariance_matrix(min_sigma);
			//}
		}
		if (this.DEBUG) {
			System.out.println("IncMixtureOfNormalDistribution:M_step(): change_in_parameters=" + change_in_parameters);
		}
		return change_in_parameters;
	}
	
	//@SuppressWarnings("unchecked")
	/*void checkFixedUnits(dataload dl, int target_date, double gammaThreshold) {
		double gamma[];
		if (this.HiddenNodes==null) return;
		//��ö����˥åȥѥ�᡼������ե饰��Ω�Ƥ�
		Enumeration e = this.HiddenNodes.elements();
		while (e.hasMoreElements()) {
			IncDistribution ND = (IncDistribution)e.nextElement();
			ND.setFix(true);
		}
		
		//��������ץ���Ф���gamma���ͤ����Ͱʾ�Υ�˥åȤϥѥ�᡼�����Ѱ����ˤ���
		//��������ץ�� dl.learning_input_patterns[target_date] �Υ���ץ�Ȥ���
		for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
			gamma = this.calculate_gamma(dl.learning_input_patterns[target_date][p]);
			for (int m=0; m<this.NumberOfModels; m++) {
				if (gamma[m]>gammaThreshold) {
					IncNormalDistributionD ND = (IncNormalDistributionD)this.HiddenNodes.elementAt(m);
					ND.setFix(false);//�ѥ�᡼������Ѱ����ˤ���
				}
			}
		}
	}*/
	
	//@SuppressWarnings("unchecked")
/*	void checkFixedUnits(FIFO.MinNoOutputsVariableSizedBuffer buffer, int target_date, double gammaThreshold, boolean reset) {
		double gamma[];
		if (this.HiddenNodes==null) return;
		
		if (reset) {//reset�ե饰��true�ʤ��
			//��ö����˥åȥѥ�᡼������ե饰��Ω�Ƥ�
			Enumeration e = this.HiddenNodes.elements();
			while (e.hasMoreElements()) {
				IncDistribution ND = (IncDistribution)e.nextElement();
				//ND.setFix(true);
				ND.setFix(false);//������������
				ND.resetNumberOfMember();
			}
		}
		
		//��������ץ���Ф���gamma���ͤ����Ͱʾ�Υ�˥åȤϥѥ�᡼�����Ѱ����ˤ���
		//��������ץ�� dl.learning_input_patterns[target_date] �Υ���ץ�Ȥ���
		for (int p=0; p<buffer.getSize(); p++) {
			gamma = this.calculate_gamma(buffer.getInput(p));
			for (int m=0; m<this.NumberOfModels; m++) {
				if (gamma[m]>gammaThreshold) {
					IncNormalDistributionD ND = (IncNormalDistributionD)this.HiddenNodes.elementAt(m);
					ND.setFix(false);//�ѥ�᡼������Ѱ����ˤ���
				}
			}
		}
	}*/

	//target_date�Υǡ������å�(�����ǡ���)+old�ǡ������åȤ�AIC��׻����롣
	//old�ǡ������åȤ�RBFNN�����۵�����
	public double AIC(dataload dl, int target_date, boolean isDiagonal, RBFNet old_RBFNN) {
		double likelihood_new_samples = this.LogLikelihood(dl, target_date);
		double likelihood_old_samples = this.LogLikelihood(old_RBFNN);
		System.out.println("IncMixtureOfDistribution: AIC(): Likelihood for new samples = " + likelihood_new_samples);
		System.out.println("IncMixtureOfDistribution: AIC(): Likelihood for old samples = " + likelihood_old_samples);		
		double AIC_value = -2D*(likelihood_new_samples + likelihood_old_samples) + 2D*(double)this.getNumberOfParameters(isDiagonal);
		return AIC_value;
	}	
	
	//target_date�Υǡ������å�(�����ǡ���)+old�ǡ������åȤ�AIC��׻�����
	//old�ǡ������åȤ�buffer�����᤿��Τ���Ѥ���
	public double AIC(dataload dl, int target_date, boolean isDiagonal, MinMoutWeightVariableSizedBuffer old_pattern_buffer) {
		double likelihood_new_samples = this.LogLikelihood(dl, target_date);
		double likelihood_old_samples = this.LogLikelihood(old_pattern_buffer);
		System.out.println("IncMixtureOfDistribution: AIC(): Likelihood for new samples = " + likelihood_new_samples);
		System.out.println("IncMixtureOfDistribution.AIC(): Likelihood for old samples = " + likelihood_old_samples);		
		double AIC_value = -2D*(likelihood_old_samples + likelihood_new_samples) + 2D*(double)this.getNumberOfParameters(isDiagonal);
		return AIC_value;
	}

	//��Ϣ�ǡ������å�(�����ǡ�����ޤ�)�Τߤ�AIC��׻�
	public double AIC(FIFO.MinNoOutputsVariableSizedBuffer buffer, boolean isDiagonal) {
		double AIC_value = -2*this.LogLikelihood(buffer) + 2*this.getNumberOfParameters(isDiagonal);
		return AIC_value;
	}
	
	//target_date�Υǡ������åȤΤ�(�����ǡ����Τ�)���п����٤�פ�
	double LogLikelihood(dataload dl, int target_date) {
		double sum=0D;
		double each_p;
		for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
			each_p = this.P(dl.learning_input_patterns[target_date][p]);
			//System.out.println("IncMixtureOfDistribution: LogLikelihood() each P(x)=" + each_p);
			if (each_p != 0) {//���줬̵���� Infinity�������롪
				sum += Math.log(each_p);
			}
		}
		return sum;
	}

	//�ط�����ǡ����Τߤ��п����٤�פ�
	double LogLikelihood(MinMoutWeightVariableSizedBuffer buffer) {
		double sum=0D;
		double each_p;
		for (int p=0; p<buffer.getSize(); p++) {
			each_p = this.P(buffer.getInput(p));
			if (each_p!=0) {
				sum += Math.log(each_p);
			}
		}
		return sum;
	}	
	
	//�ط�����ǡ����Τߤ��п����٤�פ�
	double LogLikelihood(FIFO.MinNoOutputsVariableSizedBuffer buffer) {
		double sum=0D;
		double each_p;
		for (int p=0; p<buffer.getSize(); p++) {
			each_p = this.P(buffer.getInput(p));
			if (each_p!=0) {
				sum += Math.log(each_p);
			}
		}
		return sum;
	}	
	
	double LogLikelihood(RBFNet old_RBFNN) {
		double sum = 0D;
		double each_p;
		if (old_RBFNN != null) {
			for (int p=0; p<old_RBFNN.getNumberOfLearnedSamples(); p++) {
				each_p = this.P(old_RBFNN.generate_pseudo_input2(p, old_RBFNN));
				if (each_p != 0) {
					sum += Math.log(each_p);
				}
			}
		
			if (Double.isInfinite(sum)) {
				for (int p=0; p<old_RBFNN.getNumberOfLearnedSamples(); p++) {
					double[] x = old_RBFNN.generate_pseudo_input2(p, old_RBFNN);
					for (int i=0; i<this.NumberOfInputs; i++) {
						System.out.printf(" " + x[i] + " ");
					}
					System.out.println(" ");
				}
			}
		}
		//System.out.println("log of old samples: " + sum);
		return sum;
	}
	

	public void EstimateNumberOfClassMember(FIFO.MinNoOutputsVariableSizedBuffer buffer) {
		IncDistribution nd;
		
		//reset NumberOfMember
		Enumeration<Object> e1 = this.HiddenNodes.elements();

		while (e1.hasMoreElements()) {
			nd = (IncNormalDistributionD)e1.nextElement();
			nd.resetNumberOfMember();
		}
		//estimation
		for (int p=0; p<buffer.getSize(); p++) {
			this.gamma = this.calculate_gamma(buffer.getInput(p));
			Enumeration<Object> e2 = this.HiddenNodes.elements();
			int cell=0;
			while (e2.hasMoreElements()) {
				nd = (IncNormalDistributionD)e2.nextElement();
				nd.AddFreedomeness(this.gamma[cell]);
				cell++;
			}

		}
	}


	//�ơ��Υ�ǥ��°���륵��ץ�ο�������ơ������ͳ�٤Ȥ�����Ͽ���롣
	//�����StudentT��׻�����Ȥ��˻Ȥ���
	//���Υ���ץ��old_rbfnn�����۵�����Version
	public void EstimateNumberOfClassMember(dataload dl, int target_date, RBFNet old_rbfnn) {
		IncDistribution nd;
		//IncDistribution winner_model = null;
		int cell;
		
		//reset NumberOfMember

		Enumeration<Object> e1 = this.HiddenNodes.elements();
		while (e1.hasMoreElements()) {
			nd = (IncNormalDistributionD)e1.nextElement();
			nd.resetNumberOfMember();
		}
		
		//estimation for new learning samples
		for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
			//winner_model = this.getWinnerCell(dl.learning_input_patterns[target_date][p]);
			this.gamma = this.calculate_gamma(dl.learning_input_patterns[target_date][p]);
			e1 = this.HiddenNodes.elements();
			cell = 0;
			while (e1.hasMoreElements()) {
				IncDistribution h_cell = (IncDistribution)e1.nextElement();
				h_cell.AddFreedomeness(this.gamma[cell]);
				cell++;
			}
			/*if (winner_model != null) {
				winner_model.incrementClassMember();
				//System.out.print("+");				
			}else{
				System.out.println("IncMixtureOfDistribution: EstimateNumberOfClassMember : null!!!!");
			}*/
			
		}
		//estimation for old learning samples
		if (old_rbfnn != null) {
			for (int q=0; q<old_rbfnn.getNumberOfLearnedSamples(); q++) {
				//winner_model = this.getWinnerCell(old_rbfnn.generate_pseudo_input(q, old_rbfnn));
				this.gamma = this.calculate_gamma(old_rbfnn.generate_pseudo_input2(q, old_rbfnn));
				e1 = this.HiddenNodes.elements();
				cell=0;
				while (e1.hasMoreElements()) {
					IncDistribution h_cell = (IncDistribution)e1.nextElement();
					h_cell.AddFreedomeness(this.gamma[cell]);
					cell++;
				}
				/*if (winner_model != null) {
					winner_model.incrementClassMember();
					//System.out.print("o");									
				}else{
					System.out.println("IncMixtureOfDistribution: EstimateNumberOfClassMember : null!!!!");					
				}*/
			}
		}
		System.out.println("total number of members : " + this.getTotalNumberOfMembers());
	}	

	//�ơ��Υ�ǥ��°���륵��ץ�ο�������ơ������ͳ�٤Ȥ�����Ͽ���롣
	//�����StudentT��׻�����Ȥ��˻Ȥ���
	//���Υ���ץ��buffer�����᤿��Τ�Ȥ�Version
	public void EstimateNumberOfClassMember(dataload dl, int target_date, MinMoutWeightVariableSizedBuffer old_pattern_buffer) {
		IncDistribution nd;
		//IncDistribution winner_model = null;
		int cell;
		
		//reset NumberOfMember

		Enumeration<Object> e1 = this.HiddenNodes.elements();
		while (e1.hasMoreElements()) {
			nd = (IncNormalDistributionD)e1.nextElement();
			nd.resetNumberOfMember();
		}
		
		//estimation for new learning samples
		for (int p=0; p<dl.getNumberOfPracticePerDay(); p++) {
			//winner_model = this.getWinnerCell(dl.learning_input_patterns[target_date][p]);
			this.gamma = this.calculate_gamma(dl.learning_input_patterns[target_date][p]);
			e1 = this.HiddenNodes.elements();
			cell = 0;
			while (e1.hasMoreElements()) {
				IncDistribution h_cell = (IncDistribution)e1.nextElement();
				h_cell.AddFreedomeness(this.gamma[cell]);
				cell++;
			}
		}
		//estimation for old learning samples
		if (old_pattern_buffer != null) {
			for (int q=0; q<old_pattern_buffer.getSize(); q++) {
				//winner_model = this.getWinnerCell(old_rbfnn.generate_pseudo_input(q, old_rbfnn));
				this.gamma = this.calculate_gamma(old_pattern_buffer.getInput(q));
				e1 = this.HiddenNodes.elements();
				cell=0;
				while (e1.hasMoreElements()) {
					IncDistribution h_cell = (IncDistribution)e1.nextElement();
					h_cell.AddFreedomeness(this.gamma[cell]);
					cell++;
				}
			}
		}
		System.out.println("total number of members : " + this.getTotalNumberOfMembers());
	}//EstimateNumberOfClassMember()
	

	public void init_models(dataload dl, int NumberOfModels, double init_sigma_value, boolean isDiagonal, boolean SMD) throws MatrixException {
		this.HiddenNodes = new Vector<Object>();
		for (int m=0; m<NumberOfModels; m++) {
			MatrixObj init_sigma = new MatrixObj(this.NumberOfInputs, this.NumberOfInputs);
			this.init_sigma(init_sigma, init_sigma_value);

			if (isDiagonal) {
				IncNormalDistributionD nd = new IncNormalDistributionD(dl.learning_input_patterns[this.target_date][m], init_sigma);
				this.HiddenNodes.addElement(nd);
			}else{
				IncNormalDistribution  nd = new IncNormalDistribution(dl.learning_input_patterns[this.target_date][m], init_sigma, SMD);
				this.HiddenNodes.addElement(nd);
			}
		}
	}	
	


	public int getTotalNumberOfMembers() {
		int sum = 0;
		IncDistribution h_cell;
		Enumeration<Object> e = this.HiddenNodes.elements();
		
		while (e.hasMoreElements()) {
			h_cell = (IncDistribution)e.nextElement();
			sum += h_cell.getNumberOfMember();
		}
		return sum;
	}
	

	public IncMixtureOfDistributions clone(boolean isDiagonal) {
	    IncMixtureOfDistributions myclone=null;
		try {
			myclone = (IncMixtureOfDistributions)super.clone();
			myclone.HiddenNodes = new Vector<Object>();
			//int i=0;
			Enumeration<Object> e = this.HiddenNodes.elements();
			if (isDiagonal) {
				while (e.hasMoreElements()) {
					IncNormalDistributionD h_cell = (IncNormalDistributionD)e.nextElement();
					myclone.HiddenNodes.addElement(h_cell.clone());
					//i++;
				}
			}else{
				while (e.hasMoreElements()) {
					IncNormalDistribution h_cell = (IncNormalDistribution)e.nextElement();
					myclone.HiddenNodes.addElement(h_cell.clone());
					//i++;
				}
			}
			return myclone;			
		} catch (CloneNotSupportedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return myclone;
	}	
}
