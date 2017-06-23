package ActorCritic_kato;

import java.awt.geom.Point2D;
import java.util.Random;

import javax.swing.tree.ExpandVetoException;

import org.w3c.dom.Node;

import KernelPerceptron.KernelPerceptronLRFU;
import MyRandom.DimensionalRandom;
import mgrnn.LimitedGRNN;
import mgrnn.LimitedGRNN4Reinforce;
import mgrnn.LimitedGRNNLRFU;
import PuddleWorld.*;

public class ActorCritic_kai {
	Status currentStatus;//現在の状態
	Status futureStatus;//一時刻先の状態
	Actor actor;
	Critic critic;
	public ActorCritic_kai(Node nd_value_function, Node nd_actor){
		this.currentStatus = null;
		this.futureStatus = null;
		this.actor = new Actor(nd_actor);
		this.critic = new Critic(nd_value_function);
	}
	
	public void setInputSize(int InputSize, int OutputSize){
		this.actor.setInputSize(InputSize);
		this.actor.setOutputSize(OutputSize);
		this.critic.setInputSize(InputSize);
		this.currentStatus = new Status();
		this.futureStatus = new Status();
	}
	//多分最初にこれをしないとダメ
	public void initLearning(double[] inputs, double[] outputs){
		//System.out.println("testtest");
		System.out.println(inputs[0]+ " " + inputs[1] + " " + outputs[0] + " " + outputs[1] + " " + outputs[2] + " " + outputs[3]);
		//System.out.println("init = " + outputs[3]);
		//this.actor.LGRNNactor.calculate_outputs(inputs);
		this.actor.LGRNNactor.learning(inputs, outputs, 1);
	}
	public int getAction(double[] inputs, int count){
		this.currentStatus.action = actor.getAction(inputs);
		this.currentStatus.inputs = inputs;
		//System.out.println(this.futureStatus.ValueFunction);
		//this.futureStatus.ValueFunction = this.critic.getValue(this.currentStatus.inputs);
		
		//System.out.println(this.currentStatus.ValueFunction);
		return this.currentStatus.action;
	}
	public void getValue(double[] input){
		this.futureStatus.inputs = input;
		this.futureStatus.ValueFunction = this.critic.getValue(input);
	}
	public void ReinforcementLearning(double reward, double gamma, double learning_speed){
		
		double td_error = this.critic.TDerror(reward, this.currentStatus, this.futureStatus, gamma);
		double UtilityFunction = Math.abs(this.critic.getUtility(td_error));
		
		if(UtilityFunction < 1){
			UtilityFunction = 1;
		}
		
		System.out.println("u = " + UtilityFunction);
		System.out.println("CriticLearn");
		
		
		System.out.printf("Vt = %f\n", currentStatus.ValueFunction);
		System.out.printf("Vt+1 = %f\n", futureStatus.ValueFunction);
		this.critic.learning(this.currentStatus, td_error, learning_speed, UtilityFunction);
		System.out.println("ActorLearn");
		this.actor.learning(this.currentStatus, td_error, UtilityFunction);
		//this.currentStatus = this.futureStatus;
		this.currentStatus.inputs = this.futureStatus.inputs;
		this.currentStatus.ValueFunction = this.futureStatus.ValueFunction;
		this.currentStatus.action = this.futureStatus.action;
	}
	public class Status{
		final boolean DEBUG = true;
		int action;
		double ValueFunction, inputs[];		
		//Stasusをコンソールに表示---------------------------------------------------------------------------------------
		public void display() {
			this.Log("Status().inputs: ");
			for (int i = 0; i < inputs.length; i++) {
				System.out.printf(" %1.2f ", inputs[i]);
			}
			System.out.println();
			this.Log("Status().action: ");
//			for(int o = 0; o < action.length; o++) {
//				System.out.printf(" %1.2f ", action[o]);
//			}
			System.out.println();
		}
		//log用メソッド-------------------------------------------------------------------------------------------------
		void Log(String str) {
			if (this.DEBUG) {
				System.out.println("ActorCritic." + str);
			}
		}
	}
	
	//要検証
	static class Actor{
		final boolean DEBUG = true;
		private LimitedGRNNLRFU LGRNNactor;//Actorで使用するLGRNN
		private double beta = 0.1;
		public double[] actor_output = new double[4];
		private double max_variance;
		private double c = 0.1;
		private int count = 0;
		
		Random rnd = new Random();
		//Node:prameterファイルからの読み込みに使う
		public Actor(Node nd){
			this.LGRNNactor = new LimitedGRNNLRFU(nd);	
			
		}
		
		//Actorの学習 Actorの学習には現在の状態とTDエラーを使用する。
		public void learning(Status currentStatus, double td_error, double UtilityFunction){
			//int mean, variance;
			
			if(currentStatus == null){
				System.err.println("Actor.supervised_learning(): There is no currentStatus!!");
				System.exit(1);
			}
			
			
			//this.actor_output = update_output(this.actor_output, currentStatus.action, td_error);
			update_output(this.actor_output, currentStatus.action, td_error);
			
			//System.out.println("length = " + this.actor_output[6]);
			//this.LGRNNactor.learning(currentStatus.inputs, this.actor_output, false);
			//RiskSensitive用ActotLearning
			//if(td_error > 0)
			//this.LGRNNactor.learning(currentStatus.inputs, this.actor_output, UtilityFunction);
			this.LGRNNactor.learning(currentStatus.inputs, this.actor_output, 1D);
			
		}
		
		public void update_output(double output[], int action, double TDerror){
			double[] new_output = new double[4];
			double sum = 0;
//			for(int j = 0; j < output.length; j++){
//				sum += output[j];
//			}
			this.actor_output[action] += beta * TDerror;


				
			if(this.actor_output[action] < 30){
				this.actor_output[action] = 30;
			}	
			

			//return actor_output;
		}
		
		//現在の状態からどの方向に動くかを返す inputs[]:現在の状態（座標）
		public int getAction(double inputs[]){
			int action;
			double[] softp = new double[4];
			double[] p = new double[4];
			int[] a = new int[4];
			
			this.actor_output = this.LGRNNactor.calculate_outputs(inputs);
			
			double drand = this.rnd.nextDouble(); 
			
			for(int i = 0; i < this.actor_output.length; i++){
				System.out.printf("output[%d] = %f\n" , i, this.actor_output[i]);
			}
//			System.out.printf("action0:0 <= drand <= %f\n", this.actor_output[0]);
//			System.out.printf("action1:%f < drand <= %f\n", this.actor_output[0], this.actor_output[0] + this.actor_output[1]);
//			System.out.printf("action2:%f < drand <= %f\n", this.actor_output[0] + this.actor_output[1], this.actor_output[0] + this.actor_output[1] + this.actor_output[2]);
//			System.out.printf("action3:%f < drand <= %f\n", 
//								this.actor_output[0] + this.actor_output[1] + this.actor_output[2], this.actor_output[0] + this.actor_output[1] + this.actor_output[2] + this.actor_output[3]);
//			System.out.println("drand = " + drand);
			
			//p = softmax();
			p = getP();
			
//			if(drand < this.actor_output[0]){
//				action = 0;
//			}
//			else if(drand < this.actor_output[0] + this.actor_output[1]){
//				action = 1;
//			}
//			else if(drand < this.actor_output[0] + this.actor_output[1] + this.actor_output[2]){
//				action = 2;
//			}
//			else{
//				action = 3;
//			}
			
			if(drand < p[0]){
				action = 0;
			}
			else if(drand < p[0] + p[1]){
				action = 1;
			}
			else if(drand < p[0] + p[1] + p[2]){
				action = 2;
			}
			else{
				action = 3;
			}
			
//			for(int i = 0; i < 4; i++){
//				a[i] = (int)Math.round(this.actor_output[i]);
//			}
//				
//			action = rouletteChoice(a);
			
			return action;
		}
		
		public int rouletteChoice(int[] rate){
			int max = 0;
			
			for(int i = 0;i < rate.length ;i++ )
			max += rate[i];
			int temp = rnd.nextInt(max);
			for(int i = 0;i < rate.length ;i++ ){
				temp -= rate[i];
				if(temp < 0){
					return i;
				}
			}
			
			System.out.println("予期しないエラー＠ルーレット選択");
			return -1;
		}
		
		public double[] getP(){
			double[] P = new double[4];
			double sum = 0;
			
			for(int i = 0; i < 4; i++){
				sum += this.actor_output[i];
			}
			
			for(int i = 0; i < 4; i++){
				P[i] = actor_output[i] / sum;
			}
			
			for(int i = 0; i < 4; i++){
				System.out.printf("p[%d] = %f\n" , i, P[i]);
				
				//System.out.println("sum[" + i + "] = " + sum[i] + " p[" + i + "] = " + (double)sum[i] / n);
			}
			
			return P;
		}
		
		public double[] softmax(){
			double[] y = new double[4];
			for(int i = 0; i < 4; i++){
				y[i] = Math.exp(this.actor_output[i]) / (Math.exp(this.actor_output[0]) + Math.exp(this.actor_output[1]) + Math.exp(this.actor_output[2])+ Math.exp(this.actor_output[3]));
				System.out.println("y[" + i + "]= " + y[i]);
			}
			return y;
		}
		
		void setOutputSize(int OutputSize){
			if (OutputSize < 0) {
				System.err.printf("ActorCritic.Actor.SetOutputSize(): OutputSize( = %d) must be positive value!\n", OutputSize);
				System.exit(1);
			}			
			this.LGRNNactor.setNumberOfOutputs(OutputSize);//各々の出力の分散値を出力させる必要から、実際の出力サイズの2倍必要
		}
		
		public void setMaxVariance(double max_variance) {//乱数の振れ幅の最大値
			this.max_variance = max_variance;
		}
		
		void setInputSize(int InputSize) {
			if (InputSize < 0) {
				System.err.printf("ActorCritic.Actor.SetInputSize(): InputSize( = %d) must be positive value!\n", InputSize);
				System.exit(1);
			}
			this.LGRNNactor.setNumberOfInputs(InputSize);
		}
		
		public void resetBeta(double data) {//beta(分散を変動させるための変数)のリセット
			if (data > 1 || data < 0) {//許容範囲超のβをセットしようとした時には強制終了
				System.err.println("ActorCritic.Actor.resetBeta(): Invalid beta value!! The beta should be 0 < beta < 1.\n");
				System.exit(1);
			}
			this.beta = data;
		}		
		//logメソッド--------------------------------------------------------------------------------------------------------------
		void Log(String str) {
			if (this.DEBUG) {
				System.out.println("ActorCritic.Actor." + str);
			}
		}
		//学習した知識をテキスト表示する。------------------------------------------------------------------------------------------
		public void displayLearnedKnowledge() {
			this.LGRNNactor.display_all_kernels();
		}
	}
	
	static class Critic{
		final boolean DEBUG = true;
		private LimitedGRNNLRFU LGRNNcritic;//Critc用のLGNN
		
		public Critic(Node nd){
			this.LGRNNcritic = new LimitedGRNNLRFU(nd);
			this.LGRNNcritic.setNumberOfOutputs(1);
		}
		
		//TDエラーを出力
		public double TDerror(double reward, Status currentStatus, Status futureStatus, double gamma){
			if(currentStatus == null) return 0;
			
			//TDエラーの計算 reward + γ * V(St + 1) - V(St)
			double TDerror = reward + gamma * futureStatus.ValueFunction - currentStatus.ValueFunction;
			//double TDerror = reward + currentStatus.ValueFunction - gamma * futureStatus.ValueFunction;
//			System.out.println("test = " + (gamma * futureStatus.ValueFunction - currentStatus.ValueFunction));
//			System.out.println("V(st+1) = " + futureStatus.ValueFunction);
//			System.out.println("V(st) = " + currentStatus.ValueFunction);
			System.out.println("TDerror = " + TDerror);
			return TDerror;
		}
		
		//Criticの学習 現在の状態と更新後のV(St)を使用
		public void learning(Status currentStatus, double td_error, double learning_speed, double UtilityFunction){
			if(currentStatus == null){
				System.err.println("Actor.supervised_learning(): There is no currentStatus!!");
				System.exit(1);
			}
			//V(St)の更新
			double[] critic_desired = new double[1];
			//critic_desired[0] = currentStatus.ValueFunction + learning_speed * td_error;//learningspeedを1/Nに変更
			//getNearestUnit
			
			//要検証
			//this.LGRNNcritic.getHidden_units().get(1).getNumberOfLearnedSamples();
			//critic_desired[0] = currentStatus.ValueFunction + (1 / N) * td_error;//learningspeedを1/Nに変更
			critic_desired[0] = currentStatus.ValueFunction + learning_speed * td_error;
			//this.LGRNNcritic.learning(currentStatus.inputs, critic_desired, UtilityFunction);
			//this.LGRNNcritic.learning(currentStatus.inputs, critic_desired, UtilityFunction);
			this.LGRNNcritic.learning(currentStatus.inputs, critic_desired, 1D);
		}
		
		public double getValue(double[] inputs){
			double[] result = this.LGRNNcritic.calculate_outputs(inputs);
			return result[0];
		}
		
		public double getUtility(double TDerror){
			double k1 = 0.5;
			double l1 = 2;
			double k2 = 1;
			double l2 = 2; 
			
			if(TDerror >= 0){
				return k1 * Math.pow(TDerror, l1); 
			}
			else{
				return -k2 * Math.pow(-TDerror, l2);
			}

			
		}
		
		void setInputSize(int InputSize){
			this.LGRNNcritic.setNumberOfInputs(InputSize);
		}
		
		void Log(String str){
			if(this.DEBUG){
				System.out.println("ActorCritic.Critic." + str);
			}
		}
		
		
		public double[] displayCritic(double[] input){
			double[] karnel_parameter = LGRNNcritic.calculate_outputs(input);
					
			return karnel_parameter;
		}
		
		public double[][] display_learned_Critic(){
			double[][] karnel_parameter = this.LGRNNcritic.display_all_kernels();
			
			return karnel_parameter;
		}
	}
}
