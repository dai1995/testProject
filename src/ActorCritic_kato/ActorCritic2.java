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

public class ActorCritic2 {
	Status currentStatus;//現在の状態
	Status futureStatus;//一時刻先の状態
	Actor actor;
	Critic critic;
	public ActorCritic2(Node nd_value_function, Node nd_actor){
		this.currentStatus = null;
		this.futureStatus = null;
		this.actor = new Actor(nd_actor);
		this.critic = new Critic(nd_value_function);
	}
	
	public ActorCritic2(Node nd_value_function, Node nd_actor, int UpperLimitOfHiddenUnits){
		this.currentStatus = null;
		this.futureStatus = null;
		this.actor = new Actor(nd_actor, UpperLimitOfHiddenUnits);
		this.critic = new Critic(nd_value_function, UpperLimitOfHiddenUnits);
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
		this.currentStatus.action = actor.getAction(inputs, this.currentStatus, count);
		this.currentStatus.inputs = inputs;
		//System.out.println(this.futureStatus.ValueFunction);
		this.futureStatus.ValueFunction = this.critic.getValue(this.currentStatus.inputs);
		
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
		//System.out.println("u = " + UtilityFunction);
		
		if(UtilityFunction < 1){
			UtilityFunction = 1;
		}
		
		//System.out.println("u = " + UtilityFunction);
		//System.out.println("CriticLearn");
		
		
		//System.out.printf("Vt = %f\n", currentStatus.ValueFunction * 10000000);
		//System.out.printf("Vt+1 = %f\n", futureStatus.ValueFunction * 10000000);
		this.critic.learning(this.currentStatus, td_error, learning_speed, UtilityFunction);
		//System.out.println("ActorLearn");
		this.actor.learning(this.currentStatus, td_error, UtilityFunction);
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
		final boolean DEBUG = false;
		private LimitedGRNNLRFU LGRNNactor;//Actorで使用するLGRNN
		private double beta = 0.5;
		public double[] actor_output = new double[4];
		private double max_variance;
		
		//Node:prameterファイルからの読み込みに使う
		public Actor(Node nd){
			this.LGRNNactor = new LimitedGRNNLRFU(nd);	
		}
		
		public Actor(Node nd, int upperLimitOfHiddenUnits){
			this.LGRNNactor = new LimitedGRNNLRFU(nd);	
			this.LGRNNactor.setUpperLimitOfHiddenUnits(upperLimitOfHiddenUnits);
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
				
				/*
				this.actor_output[0] += beta * (currentStatus.drnd[0] - this.actor_output[0]);
				this.actor_output[1] += beta * (currentStatus.drnd[1] - this.actor_output[1]);
				*/
				
				/*
				Point2D up = new Point2D.Double(1.5, 1.5);
			Point2D down = new Point2D.Double(-1.5, -1.5);
			Point2D right = new Point2D.Double(1.5, -1.5);
			Point2D left = new Point2D.Double(-1.5, 1.5);
				*/
				
				switch ((int)currentStatus.action){
					case 0:
						this.actor_output[0] += beta * (1.5 - this.actor_output[0]);
						this.actor_output[1] += beta * (-1.5 - this.actor_output[1]);
						break;
					case 1:
						this.actor_output[0] += beta * (-1.5 - this.actor_output[0]);
						this.actor_output[1] += beta * (1.5 - this.actor_output[1]);
						break;
					case 2:
						this.actor_output[0] += beta * (1.5 - this.actor_output[0]);
						this.actor_output[1] += beta * (1.5 - this.actor_output[1]);
						break;
					case 3:
						this.actor_output[0] += beta * (-1.5 - this.actor_output[0]);
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
		
			
			//System.out.println("length = " + this.actor_output[6]);
			//this.LGRNNactor.learning(currentStatus.inputs, this.actor_output, false);
			//RiskSensitive用ActotLearning
			//this.LGRNNactor.learning(currentStatus.inputs, this.actor_output, UtilityFunction);
			this.LGRNNactor.learning(currentStatus.inputs, this.actor_output, Math.abs(td_error));
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
			int mean, variance;
//			System.out.println("AgentpointX = " + inputs[0]);
//			System.out.println("AgentPointY = " + inputs[1]);
			this.actor_output = this.LGRNNactor.calculate_outputs(inputs);
			/*if(count == 0){
				this.actor_output[0] = 0;
				this.actor_output[1] = 0;
				this.actor_output[2] = 1;
				this.actor_output[3] = 1;
			}*/
			
			result = ActionGenerator(this.actor_output[0], this.actor_output[1], this.actor_output[2], this.actor_output[3], currentStatus);
			//System.out.println("Action = " + result[0]);
			/*
			for(int i = 0; i < this.LGRNNactor.getOutputSize(); i++){
				if(this.actor_output[i] < 0)
					this.actor_output[i] = 0;
			}
			
			for(int i = 0; i < this.LGRNNactor.getOutputSize() / 2; i++){
				mean = i * 2;
				variance = i * 2 + 1;
				
				//actionを出力
				result[i] = this.nrnd1() * (this.max_variance - this.actor_output[variance]) + actor_output[mean];
				
				//resultに応じてActionを選択 Actionの個数だけ必要？ 上の式の計算結果を要確認
				if(result[i] < 0) 
					result[i] = 0;
				if(result[i] > 1) 
					result[i] = 1;
			}
			*/
			return result;
		}
		
		public int getAction_eva(double inputs[]){
			int result;
			int mean, variance;
//			System.out.println("AgentpointX = " + inputs[0]);
//			System.out.println("AgentPointY = " + inputs[1]);
			this.actor_output = this.LGRNNactor.calculate_outputs(inputs);
			
			result = ActionGenerator_eva(this.actor_output[0], this.actor_output[1], this.actor_output[2], this.actor_output[3]);

			return result;
		}		
		
		public int ActionGenerator(double mux, double muy, double varx, double vary, Status currentStatus){
			double[] rand = new double[2];
			int Action = 0;
			//共分散 扱いは後で考える。
			double covxy = 0;
			//System.out.println("mux = " + mux + " muy = " + muy + " varx = " + varx + " vary = " + vary);
			/*System.out.println("muy = " + muy);
			System.out.println("varx = " + varx);
			System.out.println("vary = " + vary);
			*/
			DimensionalRandom drnd = new  DimensionalRandom(mux, muy, varx, vary, covxy);
			
			currentStatus.drnd = drnd.Rand();
			//System.out.println("X = " + currentStatus.drnd[0]);
			//System.out.println("Y = " + currentStatus.drnd[1]);
			//距離計算を入れる
			Action = NearestNeighbor(currentStatus.drnd);
			//System.out.println("Action = " + Action);
			return Action;
		}
		
		public int ActionGenerator_eva(double mux, double muy, double varx, double vary){
			double[] mu = new double[2];
			int Action = 0;
			//共分散 扱いは後で考える。
			double covxy = 0;
			
			mu[0] = mux;
			mu[1] = muy;
			
			//距離計算を入れる
			Action = NearestNeighbor(mu);
			//System.out.println("Action = " + Action);
			return Action;
		}
		
		public int NearestNeighbor(double[] rand){
			int Action = 0;
			double nearest;
			double[] distance = new double[4];
			Point2D up = new Point2D.Double(1.5, 1.5);
			Point2D down = new Point2D.Double(-1.5, -1.5);
			Point2D right = new Point2D.Double(1.5, -1.5);
			Point2D left = new Point2D.Double(-1.5, 1.5);
			distance[0] = right.distanceSq(rand[0], rand[1]);
			distance[1] = left.distanceSq(rand[0], rand[1]);
			distance[2] = up.distanceSq(rand[0], rand[1]);
			distance[3] = down.distanceSq(rand[0], rand[1]);
			nearest = distance[0];
			for(int i = 1; i < 4; i++){
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
		
		
		//サンプル生成用
		public double[][] getT(){
			double[][] T = new double[10][];
			for(int i = 0; i < 10; i++){
				T[i] = LGRNNactor.getHidden_units().get(i).getT();
			}
			
			return T;	
		}
		
		//統合用
		public double[][] getT(int kernelcount){
			double[][] T = new double[kernelcount][];
			for(int i = 0; i < kernelcount; i++){
				T[i] = LGRNNactor.getHidden_units().get(i).getT();
			}
			
			return T;
		}
		
		//統合用
		public double[][] getOutput(double[][] input){
			double[][] output = new double[10][];
			for(int i = 0; i < 10; i++){
				output[i] = LGRNNactor.calculate_outputs(input[i]);
			}
			
			return output;
		}
		//統合用
		public void DeleteuUnit(){
			LGRNNactor.DeleteUnit();
		}
		//統合用
		public void inte_learning(double[] inputs, double[] outputs){
			LGRNNactor.learning(inputs, outputs);
		}
		
		//統合用
		public double getVeriance(){
			return LGRNNactor.getVariance();
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
		final boolean DEBUG = false;
		private LimitedGRNNLRFU LGRNNcritic;//Critc用のLGNN
		
		public Critic(Node nd){
			this.LGRNNcritic = new LimitedGRNNLRFU(nd);
			this.LGRNNcritic.setNumberOfOutputs(1);
		}
		
		public Critic(Node nd, int upperLimitOfHiddenUnits){
			this.LGRNNcritic = new LimitedGRNNLRFU(nd);
			this.LGRNNcritic.setNumberOfOutputs(1);
			this.LGRNNcritic.setUpperLimitOfHiddenUnits(upperLimitOfHiddenUnits);
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
			//System.out.println("TDerro = " + TDerror);
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
			
			//critic_desired[0] = currentStatus.ValueFunction + learning_speed * getUtility(td_error);
			critic_desired[0] = currentStatus.ValueFunction + learning_speed * td_error;
			//this.LGRNNcritic.learning(currentStatus.inputs, critic_desired, UtilityFunction);
			this.LGRNNcritic.learning(currentStatus.inputs, critic_desired, Math.abs(td_error));
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
		
		//サンプル生成用
		public double[][] getT(){
			double[][] T = new double[10][];
			for(int i = 0; i < 10; i++){
				T[i] = LGRNNcritic.getHidden_units().get(i).getT();
			}
			
			return T;
		}
		
		//統合用
		public double[][] getT(int kernelcount){
			double[][] T = new double[kernelcount][];
			for(int i = 0; i < kernelcount; i++){
				T[i] = LGRNNcritic.getHidden_units().get(i).getT();
			}
			
			return T;
		}		
		
		//統合用
		public double[][] getOutput(double[][] input){
			double[][] output = new double[10][];
			for(int i = 0; i < 10; i++){
				output[i] = LGRNNcritic.calculate_outputs(input[i]);
			}
			
			return output;
		}
		//統合用
		public void inte_learning(double[] inputs, double[] outputs){
			LGRNNcritic.learning(inputs, outputs);
		}
		
		public void DeleteuUnit(){
			LGRNNcritic.DeleteUnit();
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
