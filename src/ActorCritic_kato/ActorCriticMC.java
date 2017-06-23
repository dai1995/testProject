package ActorCritic_kato;

import java.awt.geom.Point2D;

import javax.swing.tree.ExpandVetoException;

import org.w3c.dom.Node;

import KernelPerceptron.KernelPerceptronLRFU;
import MyRandom.DimensionalRandom;
import mgrnn.LimitedGRNN;
import mgrnn.LimitedGRNN4Reinforce;
import mgrnn.LimitedGRNNLRFU;
import PuddleWorld.*;

public class ActorCriticMC {
	Status currentStatus;//現在の状態
	Status futureStatus;//一時刻先の状態
	Actor actor;
	Critic critic;
	public ActorCriticMC(Node nd_value_function, Node nd_actor){
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
		//System.out.println(inputs[0]+ " " + inputs[1] + " " + outputs[0] + " " + outputs[1] + " " + outputs[2] + " " + outputs[3]);
		//System.out.println("init = " + outputs[3]);
		//this.actor.LGRNNactor.calculate_outputs(inputs);
		this.actor.LGRNNactor.learning(inputs, outputs, 1);
	}
	public int getAction(double[] inputs, int count){
		this.currentStatus.action = actor.getAction(inputs, this.currentStatus, count);
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
		System.out.println("u = " + UtilityFunction);
		
		if(UtilityFunction < 1){
			UtilityFunction = 1;
		}
		
		System.out.println("u = " + UtilityFunction);
		System.out.println("CriticLearn");
		
		
		
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
		double ValueFunction, inputs[], drnd[];		
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
		private double beta = 0.5;
		public double[] actor_output = new double[4];
		private double max_variance;
		
		//Node:prameterファイルからの読み込みに使う
		public Actor(Node nd){
			this.LGRNNactor = new LimitedGRNNLRFU(nd);	
			
		}
		
		//Actorの学習 Actorの学習には現在の状態とTDエラーを使用する。
		public void learning(Status currentStatus, double td_error, double UtilityFunction){
			//int mean, variance;
			double minvar = 0.5;
			if(currentStatus == null){
				System.err.println("Actor.supervised_learning(): There is no currentStatus!!");
				System.exit(1);
			}
			
			if(td_error > 0){
				//平均と分散を更新
				//actor_output[0]:xの平均値、actor_output[1]:yの平均値、actor_output[2]:xの分散、actor_output[3]:yの分散
				
			
//				this.actor_output[0] = beta * (currentStatus.drnd[0] + this.actor_output[0]);
//				this.actor_output[1] = beta * (currentStatus.drnd[1] + this.actor_output[1]);
//			
//				//x標準偏差の内側 分散を縮める
//				if(Math.abs(this.actor_output[2]) > Math.abs(currentStatus.drnd[0])){
//					this.actor_output[2] -= 0.1; 
//				}
//				else{
//					//x標準偏差の外側 分散を広げる
//					this.actor_output[2] += 0.1;
//				}
//				
//				if(Math.abs(this.actor_output[3]) > Math.abs(currentStatus.drnd[1])){
//					this.actor_output[3] -= 0.1; 
//				}
//				else{
//					//x標準偏差の外側 分散を広げる
//					this.actor_output[3] += 0.1;
//				}
////				
//				
//
				//mountain_car用
				switch (currentStatus.action){
					case 0:
						this.actor_output[0] += beta * (1.3 - this.actor_output[0]);
						this.actor_output[1] += beta * (0.75 - this.actor_output[1]);
						break;
					case 1:
						this.actor_output[0] += beta * (-1.3 - this.actor_output[0]);
						this.actor_output[1] += beta * (0.75 - this.actor_output[1]);
						break;
					case 2:
						this.actor_output[0] += beta * (0 - this.actor_output[0]);
						this.actor_output[1] += beta * (-1.5 - this.actor_output[1]);
						break;
				}
				
				
				
				this.actor_output[2] += this.beta * (minvar - this.actor_output[2]);
				this.actor_output[3] += this.beta * (minvar - this.actor_output[3]);
				if(this.actor_output[2] < minvar){
					this.actor_output[2] = minvar;
				}
				if(this.actor_output[3] < minvar){
					this.actor_output[3] = minvar;
				}
			}
			else{
				this.actor_output[2] += 0.4 * (this.max_variance - this.actor_output[2]);
				this.actor_output[3] += 0.4 * (this.max_variance - this.actor_output[3]);
				if(this.actor_output[2] > 1.9){
					this.actor_output[2] = 1.9;
				}
				if(this.actor_output[3] > 1.9){
					this.actor_output[3] = 1.9;
				}
				
			}
		
//			this.actor_output[2] = 1.0;
//			this.actor_output[3] = 1.0;
			
			//System.out.println("length = " + this.actor_output[6]);
			//this.LGRNNactor.learning(currentStatus.inputs, this.actor_output, false);
			//RiskSensitive用ActotLearning
			this.LGRNNactor.learning(currentStatus.inputs, this.actor_output, UtilityFunction);
			//this.LGRNNactor.learning(currentStatus.inputs, this.actor_output, td_error);
			//this.LGRNNactor.learning(currentStatus.inputs, this.actor_output, 1D);
			
		}
		
		private double nrnd1() {
			double result = 0D;
			for (int i = 0; i < 6; i++) {
				this.Log("nrnd1() rnd=" + Math.random());
				result += Math.random();
			}
			result -= 3.0D;
			return result;
		}		
		
		//現在の状態からどの方向に動くかを返す inputs[]:現在の状態（座標）
		public int getAction(double inputs[], Status currentStatus, int count){
			int result;
			
//			System.out.println("AgentpointX = " + inputs[0]);
//			System.out.println("AgentPointY = " + inputs[1]);
			this.actor_output = this.LGRNNactor.calculate_outputs(inputs);
			
			
			result = ActionGenerator(this.actor_output[0], this.actor_output[1], this.actor_output[2], this.actor_output[3], currentStatus);
			//System.out.println("Action = " + result[0]);
			
			return result;
		}
		public int ActionGenerator(double mux, double muy, double varx, double vary, Status currentStatus){
			double[] rand = new double[2];
			int Action = 0;
			//共分散 扱いは後で考える。
			double covxy = 0;
			System.out.println("mux = " + mux + " muy = " + muy + " varx = " + varx + " vary = " + vary);
		
			DimensionalRandom drnd = new  DimensionalRandom(mux, muy, varx, vary, covxy);
			
			currentStatus.drnd = drnd.Rand();
			//System.out.println("X = " + currentStatus.drnd[0]);
			//System.out.println("Y = " + currentStatus.drnd[1]);
			//距離計算を入れる
			Action = NearestNeighbor(currentStatus.drnd);
			//System.out.println("Action = " + Action);
			return Action;
		}
		
		//mountain_car用
		public int NearestNeighbor(double[] rand){
			int Action = 0;
			double nearest;
			double[] distance = new double[3];
			Point2D forward = new Point2D.Double(1.3, 0.75);
			Point2D stop = new Point2D.Double(-1.3, 0.75);
			Point2D back = new Point2D.Double(0, -1.5);
			
			distance[0] = forward.distanceSq(rand[0], rand[1]);
			distance[1] = stop.distanceSq(rand[0], rand[1]);
			distance[2] = back.distanceSq(rand[0], rand[1]);
			nearest = distance[0];
			for(int i = 1; i < 3; i++){
				if(nearest > distance[i]){
					nearest = distance[i];
					Action = i;
				}
			}
						
			return Action;
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
			System.out.println("TDerro = " + TDerror);
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
			double N;
			//getNearestUnit
			if (this.LGRNNcritic.getHidden_units().size()>0) {
				N = this.LGRNNcritic.getNearestUnit(currentStatus.inputs).getNumberOfLearnedSamples();
			}else{
				N = 1;
			}
			
			System.out.println("N = " + N);
			
			//要検証
			//this.LGRNNcritic.getHidden_units().get(1).getNumberOfLearnedSamples();
			//critic_desired[0] = currentStatus.ValueFunction + (1 / N) * td_error;//learningspeedを1/Nに変更
			//critic_desired[0] = currentStatus.ValueFunction + learning_speed * td_error;
			critic_desired[0] = currentStatus.ValueFunction + learning_speed * getUtility(td_error);
			//this.LGRNNcritic.learning(currentStatus.inputs, critic_desired, UtilityFunction);
			this.LGRNNcritic.learning(currentStatus.inputs, critic_desired, td_error);
			//this.LGRNNcritic.learning(currentStatus.inputs, critic_desired, 1D);
		}
		
		public double getValue(double[] inputs){
			double[] result = this.LGRNNcritic.calculate_outputs(inputs);
			return result[0];
		}
		
		public double getUtility(double TDerror){
			double k1 = 0.5;
			double l1 = 2;
			double k2 = 2;
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
