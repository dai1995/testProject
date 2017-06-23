package ActorCritic;

import org.w3c.dom.Node;

import mgrnn.LimitedGRNN;

//ActorCriticクラス----------------------------------------------------------------------------------------
public class ActorCritic {
	final boolean DEBUG = true;
	//初期化------------------------------------------------------------------------------------------------
	Status currentStatus;//現在の状態
	Status futureStatus;//一時刻先の状態
	Actor actor;
	Critic critic;
	double gain=0.5;//教師あり学習の学習速度
	double k;//k=1のときにはActorが出力し、k=0のときには人間が出力する
	double[] supervised_action = new double [2];
	//インスタンス化---------------------------------------------------------------------------------		
	public ActorCritic(Node nd_value_function, Node nd_actor) {	
//		this.prevStatus=null;
		this.currentStatus = null;
		this.futureStatus = null;
		this.actor = new Actor(nd_actor);
		this.critic = new Critic(nd_value_function);
	}	
	//強化学習エンジンから現在の制御信号を出させるメソッド------------------------------------------------
	// double inputs[] 入力ベクトル, int month;　時間情報（human actionを取り入れる条件設定に使用)
	public double[] getAction(double inputs[], int year, int month) {//入力は多次元の入力値

		this.currentStatus.action = actor.getAction (inputs); //Actorから制御信号を出力させる(現在のアクション)
		this.currentStatus.inputs = inputs;//現在の状態を書き込む
		for (int i =0; i < currentStatus.action.length; i++) {
			this.Log("getAction() currentStatus.action[" + i + "] = " + this.currentStatus.action[i]);
		}

		if (year < 1) {//人間のactionを取り入れる
			this.Log("getAction() Manual Mode!-------------------------------------------------------------------");
			getSupervised_action(inputs, year, month);//人間のアクションを擬似的に取り出す
			//以下条件式を修正
			if (Math.abs(this.currentStatus.action[0] - this.supervised_action[0]) > 0.01){//inputよりAcotrが計算したactionと人間の考えるactionの比較し、人間のアクションとずれているときに学習する
				this.Log("supervised_action[0] = " + this.supervised_action[0]);
				setSupervised_Signal(this.currentStatus.inputs, this.supervised_action, this.gain);
			}
		}else{
			this.Log("getAction() Automatic Mode!-------------------------------------------------------------------");
		}

		this.futureStatus.ValueFunction = this.critic.getValue (this.currentStatus.inputs);	
		this.Log("getAction() this.currentStatus.action=[0]" + this.currentStatus.action[0] + " critic.getValue()=" + this.futureStatus.ValueFunction);
		return this.currentStatus.action;
	}	
	//Human actionを擬似的に作り出すメソッド
	public void getSupervised_action(double inputs[], int year, int month) {
		this.Log("Supervised_action() inputs[0] = " + inputs[0]);
		this.Log("Supervised_action() inputs[5] = " + inputs[5]);
		if (inputs[0] == 0.375 || inputs[0] == 0.75) {//ピークの時のみ
			if (inputs[2] > inputs[3]) {
				this.supervised_action[0] = 0.0;
				if (month >=7 && month <=9) this.supervised_action[0] = 0.5;//夏用
			} else {
				this.supervised_action[0] = 0.5;
			}
		}
/*		if (year == 0) {
			human_action[0] -= 0.1;
		} else {
			if (human_action[0] <= 0.0)
				human_action[0] = 0.0;
		}
*/	
		if (inputs[5] >= 0.8) this.supervised_action[0] = 0.3;
//			this.supervised_action[0] = this.currentStatus.action[0];//なんでcurrentのactionを代入するの？	
		if (inputs[5] <= 0.5) this.supervised_action[0] = 0.0;
		for (int i = 0; i < this.supervised_action.length; i++) {
			this.Log("Supervised_action() this.supervised_action[" + i + "] = " + this.supervised_action[i]);
		}
	}
	//人間のアクションを supervised_action[]として受け取り、currentStatus.action[]にそれをセットする
	//このとき係数kを０にセットする。
	//そしてactorを学習させる
	public void setSupervised_Signal(double inputs[], double supervised_action[], double gain) {
		this.k=0;
		this.Log("setSupervised_Signal()");
/*		for (int i = 0; i < this.actor.GetInputSize(); i++) {//必要ないのでは?
			this.currentStatus.inputs[i] = inputs[i];
		}*/
		for (int j = 0; j < this.actor.LGRNN4actor.getOutputSize()/2; j++) {
			this.currentStatus.action[j] = supervised_action[j];
		}
		this.actor.supervised_learning(this.currentStatus, gain);//学習する
	}	
	//人間が休む場合（自動実行モードにする場合に呼び出す）---------------------------------------------------------------------
	public void UnsetSupervisedSignal() {
		this.k=1;
	}	
	//criticとActorの学習-----------------------------------------------------------------------------------------------
	//(reward=現時点での報酬、ない場合は0, gamma=ディスカウントファクタ, learning_speed=value functionの学習速度を表す計数)
	public void ReinforcementLearning(double reward, double gamma, double learning_speed) {//入力は多次元の入力値
		double td_error = this.critic.TDerror(reward, this.futureStatus, this.currentStatus, gamma);//td errorを計算
		this.critic.learning(this.currentStatus, this.futureStatus, td_error, learning_speed);//criticの学習
		this.actor.learning(this.currentStatus, td_error);//actorの学習
//		this.actor.supervised_learning(this.currentStatus, gain);
//		System.out.println("this.currentStatus.ValueFunction = " + this.currentStatus.ValueFunction);
//		System.out.println("this.futureStatus.ValueFunction = " + this.futureStatus.ValueFunction);
		this.currentStatus = this.futureStatus;
		this.Log("ReinforcementLearning()");
//		this.prevStatus.display();
	}	
	//LGRNNの入出力次元数のセット(学習操作前に使用すること!!)--------------------------------------------------------------------------
	public void setInputSize(int InputSize, int OutputSize) {
		this.actor.setInputSize(InputSize);
		this.actor.setOutputSize(OutputSize);
		this.Log("Actor input size is " + this.actor.LGRNN4actor.getInputSize());
		this.Log("Actor output size is " + this.actor.LGRNN4actor.getOutputSize());
		this.critic.setInputSize(InputSize);
		this.currentStatus = new Status();
		this.futureStatus = new Status();
	}
	/*
	//乱数の振れ幅の最大値の設定-----------------------------------------------------------------------------------------------------------
	public void setMaxVariance(double max_variance) { //max_varianceはactorでしか使わないのでactorのsetMaxVarianceで直接セットすればいいのでは？
		this.actor.setMax_variance(max_variance);
	}*/
	//logの出力---------------------------------------------------------------------------------------------------------------
	void Log(String str) {
		if (this.DEBUG) {
			System.out.println("ActorCritic." + str);
		}
	}	
	//Statusクラス(状態保持)-------------------------------------------------------------------------------------------------------------------------
	private class Status {
		//初期化------------------------------------------------------------------------------------------------------------
		final boolean DEBUG = true;
		double ValueFunction, action[], inputs[];		
		//Stasusをコンソールに表示---------------------------------------------------------------------------------------
		public void display() {
			this.Log("Status().inputs: ");
			for (int i = 0; i < inputs.length; i++) {
				System.out.printf(" %1.2f ", inputs[i]);
			}
			System.out.println();
			this.Log("Status().action: ");
			for(int o = 0; o < action.length; o++) {
				System.out.printf(" %1.2f ", action[o]);
			}
			System.out.println();
		}
		//log用メソッド-------------------------------------------------------------------------------------------------
		void Log(String str) {
			if (this.DEBUG) {
				System.out.println("ActorCritic." + str);
			}
		}
	}
	
	
	//Actorクラス---------------------------------------------------------------------------------------------------------------
	static class Actor {
		final boolean DEBUG = true;
		private static final double gain = 0.0;//gainはtd_erroeに比例させた方がいいかも？
		//インスタンス化---------------------------------------------------------------------------------------------------------
		private LimitedGRNN LGRNN4actor;//Actor内で参照
		private double beta = 0.8; //分散を表す次元の学習に使われる係数 learning()を見よ
		public double[] actor_output,supervised_actor_output; //actorの出力値を保持しておく配列。getAction()で値が代入され、learning()で活用される

		private double max_variance;
		//LGRNNのNodeをActor用に変換する----------------------------------------------------------------------------------------
		public Actor(Node nd) {
			this.LGRNN4actor = new LimitedGRNN(nd);//入力次元数は任意、出力次元数は２であることを想定する
		}
		//actorの学習------------------------------------------------------------------------------------------------------------
		public void learning(Status currentStatus, double td_error) {
			if (currentStatus == null) {
				System.err.println("Actor.supervised_learning(): There is no currentStatus!!");
				System.exit(1);
			}
			//actor_outputを更新
			for (int i = 0; i < this.LGRNN4actor.getOutputSize()/2; i++) {
				//actor_output[i]が制御信号の平均値, actor_output[i+1]が分散とする(i=0,2,4,6...)ことに注意
				int mean = 2*i;//制御信号成分の平均値の配列番号(0,2,4,...)
				int variance = 2*i+1;//制御信号成分の分散値の配列番号(1,3,5,...)
				this.Log("ActorCritic.learning() mean = " + mean);
				this.Log("ActorCritic.learning() variance_index = " + variance);
				//以下逆の操作を行なっていたことが判明
				//if (Math.abs(current_status.action[action_index] - this.actor_output[action_index]) > Math.sqrt(this.max_variance - actor_output[variance_index])) {
				if (td_error > 0) {
					this.actor_output[mean] += td_error * (currentStatus.action[i] - this.actor_output[mean]); //actionに相当するouput[0]を現在の状態へ近づける
					this.actor_output[variance] += this.beta * (this.max_variance - this.actor_output[variance]);//自信を高める
					if (this.actor_output[variance] > this.max_variance) this.actor_output[variance] = this.max_variance;
				} else {
					this.actor_output[variance] -= this.beta * (this.max_variance - this.actor_output[variance]);//自信をなくす
					if (this.actor_output[variance] < 0) this.actor_output[variance] = 0; 
				}
//				this.actor_output[mean] += td_error * (currentStatus.action[i] - this.actor_output[mean]); //actionに相当するouput[0]を現在の状態へ近づけるor遠ざける
				if (this.actor_output[mean] < 0) this.actor_output[mean] = 0;
				this.Log("learning() desired_output[" + mean + "] = " + this.actor_output[0]);
				this.Log("learning() before learning current_status.action[" + mean + "]=" + currentStatus.action[mean]);
				this.Log("learning() before learning actor_output[" + mean + "] = " + this.actor_output[mean]);
				this.Log("learning() td_error = " + td_error);
				if (td_error > 1) {
					System.err.println("Actor.learning() td_errpr > 1!!");
					System.exit(1);
				}
				/*
				if (td_error > 0) {
					this.actor_output[mean] += td_error * (current_status.action[i] - this.actor_output[mean]); //actionに相当するouput[0]の更新
					if (this.actor_output[mean] < 0) this.actor_output[mean] = 0;
				}*/
			}
//			current_status.display();
			this.LGRNN4actor.learning(currentStatus.inputs, this.actor_output);
		}	
		// userStatusにユーザーのアクションを入れる。gainには学習速度を入れる-------------------------------------------------------------------
		public void supervised_learning(Status userStatus, double gain) {
			if (userStatus == null) 	{
				System.err.println("Actor.supervised_learning(): There is no userStatus!!");
				System.exit(1);
			}
			this.Log("supervised_learning() userStatus.Action[0]="+userStatus.action[0]);
			for (int i = 0; i < this.LGRNN4actor.getOutputSize()/2; i++) {//actor_outputを更新
				//actor_output[i]が制御信号の平均値, currnet_output[i+1]が分散とする(i=0,2,4,6...)ことに注意
				int mean = 2*i;//制御信号成分の配列番号
				int variance = 2*i+1;//分散成分の配列番号
				this.Log("ActorCritic.supervised_learning() action_index = " + mean + " variance_index = " + variance);
				this.actor_output[mean] = userStatus.action[i];//actionに相当するouput[0]の更新
				//ユーザーアクションを学習した場合、それが正しいと仮定すると分散は小さくする必要がある。このため、actor_output[variance_ndex]はmax_varianceに近づける
				this.actor_output[variance] += this.beta * (this.max_variance - actor_output[variance]);//自信の度合いを高める。
//				this.actor_output[mean] += userStatus.action[i]; 
			}
			this.Log("supervised_learning() actor_output=" + this.actor_output[0] + ", " + this.actor_output[1]);
			//user_status.display();
			this.LGRNN4actor.learning(userStatus.inputs, this.actor_output); 
		}				
		//平均ゼロ分散１の正規分布による乱数発生器(簡易版)-------------------------------------------------------------------------------
		private double nrnd1() {
			double result = 0D;
			for (int i = 0; i < 6; i++) {
				this.Log("nrnd1() rnd=" + Math.random());
				result += Math.random();
			}
			result -= 3.0D;
			return result;
		}		
		//getメソッド-----------------------------------------------------------------------------------------------------------------
		public double[] getAction(double inputs[]) {//今回は出力制御信号は8次元
			this.Log("getAction() inputsize = " + inputs.length);//inputsの長さ,次元数
			double[] result = new double[this.LGRNN4actor.getOutputSize()/2];//result[1](出力数2)
			this.actor_output = this.LGRNN4actor.calculate_outputs(inputs); //actorを構成するLGRNNの出力値を計算
			for (int o = 0; o < this.LGRNN4actor.getOutputSize(); o++) {
				if (this.actor_output[o] < 0) this.actor_output[o] = 0;
			}
			//actor_output[i]が制御信号の平均値, actor_output[i+1]が分散とする。(i=0,2,4,6...)
			for (int i = 0; i < this.LGRNN4actor.getOutputSize()/2; i++) {
				int mean = i*2;
				int variance = i*2+1;
				this.Log("getAction() actor_output[mean] = " + actor_output[mean]);//偶数番目が		
				this.Log("getAction() actor_output[variance] = " + actor_output[variance] + " nrnd1() = " + this.nrnd1());//nrnd1は131行目の乱数発生器
				//注：LGRNNが出力する自信の度合いが大きいほど乱数の分散が小さくなる設定
				
				result[i] = this.nrnd1() * (this.max_variance - actor_output[variance]) + actor_output[mean]; //平均=output[0],分散=output[1],nrnd1=正規乱数発生器
				//System.out.println("max_variance =" + this.max_variance + " actor_output[0]=" + actor_output[0] + " actor_output[1]=" + actor_output[1] + " result[0]=" + result[0]);
				if (result[i] < 0) result[i] = 0; 
				if (result[i] > 1) result[i] = 1;
			}
			return result;
		}
/*	//今は使用してない
		int GetInputSize() {//入力次元数
			return this.LGRNN4actor.getInputSize();
		}
		int GetOutputSize() {//出力次元数
			return this.LGRNN4actor.getOutputSize();
		}
*/		
		//setメソッド----------------------------------------------------------------------------------------------------------------
		void setOutputSize(int OutputSize) {//出力次元数を外からセット
			if (OutputSize < 0) {
				System.err.printf("ActorCritic.Actor.SetOutputSize(): OutputSize( = %d) must be positive value!\n", OutputSize);
				System.exit(1);
			}			
			this.LGRNN4actor.setNumberOfOutputs(2*OutputSize);//各々の出力の分散値を出力させる必要から、実際の出力サイズの2倍必要
		}
		
		public void setMaxVariance(double max_variance) {//乱数の振れ幅の最大値
			this.max_variance = max_variance;
		}
		
		void setInputSize(int InputSize) {
			if (InputSize < 0) {
				System.err.printf("ActorCritic.Actor.SetInputSize(): InputSize( = %d) must be positive value!\n", InputSize);
				System.exit(1);
			}
			this.LGRNN4actor.setNumberOfInputs(InputSize);
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
			this.LGRNN4actor.display_all_kernels();
		}
		
	}
	
	
	//Criticクラス-------------------------------------------------------------------------------------------------------------
	static class Critic {
		final boolean DEBUG = true;
		//インスタンス化---------------------------------------------------------------------------------------------------------
		private LimitedGRNN LGRNN4valueFunction;	
		
		//LGRNNのNodeをCritic用に変換し、--------------------------------------------------------------------------------------
		public Critic(Node nd) {
			this.LGRNN4valueFunction = new LimitedGRNN(nd);
			this.LGRNN4valueFunction.setNumberOfOutputs(1);
		}		
		//TDエラーを出力する----------------------------------------------------------------------------------------
		public double TDerror(double reward, Status futureStatus, Status currentStatus, double gamma) {
			if (currentStatus == null) return 0;
//			this.Log("current_status.ValueFunction = " + current_status.ValueFunction);
//			this.Log("future_status.ValueFunction = " + future_status.ValueFunction);
			double result = reward + gamma * futureStatus.ValueFunction - currentStatus.ValueFunction;//r_t+1+γV(st+1)-V(st)
/*		if (result < 0) {
				result = 0;
//				this.Log("TDerror() reward = " + reward  + " gamma =" + gamma + " current_status.value=" + current_status.ValueFunction + " previous_status.value = " + previous_status.ValueFunction);
//				this.Log("TDerror() other = " + (gamma * previous_status.ValueFunction - current_status.ValueFunction));
//				System.exit(1);
			}*/
			return result; 
		}		
		//Critic(ValueFunction)の学習-----------------------------------------------------------------------------
		public void learning(Status currentStatus, Status futureStatus, double td_error, double learning_speed) {
			if (currentStatus == null) {
				System.err.println("Actor.supervised_learning(): There is no currentStatus!!");
				System.exit(1);
			}
			double critic_desired = currentStatus.ValueFunction + learning_speed * td_error;//p(s_t,a_t)+βδ_t???
//			System.out.println("desired_output:" + desired_output + " current_status.ValueFunction:" + current_status.ValueFunction + " learning_speed:" + learning_speed + " td_error:" + td_error);
//			System.out.println("←");
			this.Log("learning() : desired = " + critic_desired);
			double t[] = new double[1];
			t[0] = critic_desired;
			//previous_status.display();
			this.LGRNN4valueFunction.learning(currentStatus.inputs, t);//学習。現時点での状態価値ではなく、その１つ前の状態価値が学習されることに注意
		}
		//getメソッド(現在の状態の予測価値を書き込む)-----------------------------------------------------------------------------
		public double getValue(double inputs[]) {//inputで表される状態の価値を返す
			//とても小さな値を出す？
			double[] result = this.LGRNN4valueFunction.calculate_outputs(inputs);
//			System.out.println("getValue = " + result[0]);
			return result[0];
		}		
		//setメソッド-----------------------------------------------------------------------------------------------------------------------
		void setInputSize(int InputSize) {
			this.LGRNN4valueFunction.setNumberOfInputs(InputSize);
		}		
		//logメソッド--------------------------------------------------------------------------------------------------------------
		void Log(String str) {
			if (this.DEBUG) {
				System.out.println("ActorCritic.Critic." + str);
			}
		}
		//学習結果の表示-------------------------------------------------------------------------------------------------------------
		public void display_learned_valueFunction() {
			this.LGRNN4valueFunction.display_all_kernels();
		}
	}
}