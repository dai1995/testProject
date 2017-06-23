package ActorCritic_kato;

import java.util.ArrayList;

import DataLoad.MultipleDataload;
import ParameterReader.ParameterReader;
import PuddleWorld.PuddleWorld;
import datalogger.multiple_dataOutput;

public class AC_integration {
	int NumberOfAC = 5;
	ArrayList<ActorCritic2> AClist = new ArrayList<ActorCritic2>();
	ActorCritic2 integrate_AC;
	ParameterReader pr;
	
	public AC_integration(String parameterfilename){
		this.pr  = new ParameterReader(parameterfilename);
		
		//カーネル数は50
		integrate_AC = new ActorCritic2(pr.Reader("ValueFunction_inte"), pr.Reader("Actor_inte"));
		integrate_AC.setInputSize(2, 4);
		
		for(int i = 0; i < NumberOfAC; i++){
			ActorCritic2 new_AC = new ActorCritic2(pr.Reader("ValueFunction"), pr.Reader("Actor"));
			new_AC.setInputSize(2, 4);
			new_AC.actor.setMaxVariance(2D);
			AClist.add(new_AC);
		}
	}
	
	//ActorCritic学習
	public void ReinforcementLearning(ActorCritic2 AC){
		double[] AgentPoint = new double[2];
		int action;
		double[][] init_inputs = new double[441][2];
		double[][] init_outputs = new double[441][4];
		double reward = 0;
		
		MultipleDataload init = new MultipleDataload(pr.Reader("init"));
		
		//Actor 初期状態学習
		init.read(0);
		for(int i = 0; i < 25; i++){
			for(int j = 0; j < 6; j++){
				if(j < 2){
					init_inputs[i][j] = init.learning_input_patterns[i][0][j];
					System.out.print(init_inputs[i][j] + " ");
				}
				else{
					init_outputs[i][j - 2] = init.learning_input_patterns[i][0][j];
					System.out.print(init_outputs[i][j - 2] + " ");
				}		
			}
			System.out.println();
		}
		
		for(int i = 0; i < 25; i++){
			AC.initLearning(init_inputs[i], init_outputs[i]);
		}
		
		//水たまり問題学習
		for(int i = 0; i < 100; i++){
			PuddleWorld pw = new PuddleWorld();
			AgentPoint[0] = pw.getagentPoint().getX();
			AgentPoint[1] = pw.getagentPoint().getY();
			
			for(int j = 0; j < 10000; j++){
				System.out.println("count = " + i + "_" + j);
				action = AC.getAction(AgentPoint, j);//
				pw.update(action);
				AgentPoint[0] = pw.getagentPoint().getX();
				AgentPoint[1] = pw.getagentPoint().getY();
				reward = pw.getRward();
				AC.getValue(AgentPoint);
				AC.ReinforcementLearning(reward, 0.9, 0.1);
				if(reward == 0){
					break;
				}
			}
		}
		
	}
	
	//Actor統合用学習サンプル生成
	private double[][] getActorSample(ActorCritic2 AC){
		//カーネル上限数を変更した場合に第一要素数を修正 現在の設定：上限数10
		double[][] output = new double[10][];
		double[][] T = new double[10][];
		double[][] sample = new double[10][6];
		
		T = AC.actor.getT();
		output = AC.actor.getOutput(T);
		
		for(int i = 0; i < T.length; i++){
			for(int j = 0; j < 6; j++){
				if(j < 2){
					//System.out.println("happy");
					sample[i][j] = T[i][j]; 
				}
				else{
					sample[i][j] = output[i][j - 2];
				}
			}
		}
		
		return sample;
		
	}
	
	//Critic統合用学習サンプル生成
	private double[][] getCriticSample(ActorCritic2 AC){
		//カーネル上限数を変更した場合に第一要素数を修正 現在の設定：上限数10
		double[][] output = new double[10][];
		double[][] T = new double[10][];
		double[][] sample = new double[10][3];
		T = AC.critic.getT();
		output = AC.critic.getOutput(T);
		
		for(int i = 0; i < T.length; i++){
			for(int j = 0; j < 3; j++){
				if(j < 2){
					sample[i][j] = T[i][j]; 
				}
				else{
					sample[i][j] = output[i][j - 2];
				}
			}
		}
		
		return sample;
	}
	
	//統合
	public void integrate(){
		//LGRNNの数やカーネル上限数を変更した場合に第一要素数を修正　LGRNN数×上限数 現在の設定：LGRNN数5 上限数10
		double[][] ActorInputs = new double[50][2];
		double[][] ActorOutputs = new double[50][4];
		double[][] CriticInputs = new double[50][2];
		double[][] CriticOutputs = new double[50][1];
		int ActorKernelcount = 0;
		int CriticKernelcount = 0;
		double sigma = integrate_AC.actor.getVeriance();
		
		
		//Deruta de = new Deruta(50, 2, 0.02);
		System.out.println("sigma = " + sigma);
		Deruta de = new Deruta(50, 2, sigma);
		
		MultipleDataload ActorSample = new MultipleDataload(pr.Reader("ActorSampleLoad"));
		MultipleDataload CriticSample = new MultipleDataload(pr.Reader("CriticSampleLoad"));
		
		ActorSample.read(0);
		CriticSample.read(0);
		//LGRNNの数やカーネル上限数を変更した場合に第一要素数を修正　LGRNN数×上限数 現在の設定：LGRNN数5 上限数10
		for(int i = 0; i < 50; i++){
			for(int j = 0; j < 6; j++){
				
				if(j < 2){
					ActorInputs[i][j] = ActorSample.learning_input_patterns[i][0][j];
					System.out.print(ActorInputs[i][j] + " ");
				}
				else{
					ActorOutputs[i][j - 2] = ActorSample.learning_input_patterns[i][0][j];
					System.out.print(ActorOutputs[i][j - 2] + " ");
				}		
			}
			System.out.println();
			
			for(int j = 0; j < 3; j++){
				
				if(j < 2){
					CriticInputs[i][j] = CriticSample.learning_input_patterns[i][0][j];
					System.out.print(CriticInputs[i][j] + " ");
				}
				else{
					CriticOutputs[i][j - 2] = CriticSample.learning_input_patterns[i][0][j];
					System.out.print(CriticOutputs[i][j - 2] + " ");
				}		
			}
			System.out.println();
		}
		
		//ただの統合(このfor文を実行する場合下のfor文をコメントアウトしてください)
//		for(int i = 0; i < ActorInputs.length; i++){
//			integrate_AC.actor.inte_learning(ActorInputs[i], ActorOutputs[i]);
//			integrate_AC.critic.inte_learning(CriticInputs[i], CriticOutputs[i]);
//		}

		//カーネルを削減して統合(このfor文を実行する場合上のfor文をコメントアウトしてください)
		for(int i = 0; i < ActorInputs.length; i++){
			if(i < 2){
				integrate_AC.actor.inte_learning(ActorInputs[i], ActorOutputs[i]);
				integrate_AC.critic.inte_learning(CriticInputs[i], CriticOutputs[i]);
				ActorKernelcount++;
				CriticKernelcount++;
			}
			//先にサンプルを学習する必要がある 学習してから判定して削除する
			else{
				integrate_AC.actor.inte_learning(ActorInputs[i], ActorOutputs[i]);
				ActorKernelcount++;
				if(de.deruta(ActorInputs[i], integrate_AC.actor.getT(ActorKernelcount), ActorKernelcount) < 0.99){
					integrate_AC.actor.DeleteuUnit();
					ActorKernelcount--;
				}
				integrate_AC.critic.inte_learning(CriticInputs[i], CriticOutputs[i]);
				CriticKernelcount++;
				if(de.deruta(ActorInputs[i], integrate_AC.critic.getT(CriticKernelcount), CriticKernelcount) < 0.99){
					integrate_AC.critic.DeleteuUnit();;
					CriticKernelcount--;
				}
			}
		}
		System.out.println("ActorKernelcount = " + ActorKernelcount);
		System.out.println("CriticKernelcount = " + CriticKernelcount);
	}
	//評価用水たまり問題
	public double[][] evaluation(ActorCritic2 AC){
		double[] AgentPoint = new double[2];
		int action;
		double reward = 0;
		//カーネル上限数を変更した場合に第一要素数を修正 現在の設定：上限数10
		double[][] rewardsum = new double[10][100];
		double sum;
		
		//multiple_dataOutput md_rewardave_inte = new multiple_dataOutput(pr.Reader("rewardaveoutput_inte"));
		
		for(int n = 0; n < 10; n++){
			for(int i = 0; i < 100; i++){
				PuddleWorld pw = new PuddleWorld();			
				for(int j = 0; j < 10000; j++){
					System.out.println("count = " + n + "_" + i + "_" + j);
					AgentPoint[0] = pw.getagentPoint().getX();
					AgentPoint[1] = pw.getagentPoint().getY();
					action = AC.getAction(AgentPoint, j);
					pw.update(action);
					reward = pw.getRward();
					//System.out.println("rewrad = " + reward);
					rewardsum[n][i] += reward;
					if(reward == 0){
						break;
					}
				}
			}
		}
		
		return rewardsum;
	}
	
	//実行
	public void execute(){
		//カーネル上限数を変更した場合に第一要素数を修正 現在の設定：上限数10
		double[][] ActorSample = new double[10][6];
		double[][] CriticSample = new double[10][3];
		double sum;
		double[][] rewardsum = new double[10][100];
		
		multiple_dataOutput md_ActorSample = new multiple_dataOutput(pr.Reader("ActorSample"));
		multiple_dataOutput md_CriticSample = new multiple_dataOutput(pr.Reader("CriticSample"));
		multiple_dataOutput md_rewardave_inte = new multiple_dataOutput(pr.Reader("rewardaveoutput_inte"));
		
		//複数のActorCriticの学習//学習サンプルの取り出し
		md_ActorSample.open();
		md_CriticSample.open();
		for(int i = 0; i < this.NumberOfAC; i++){
			this.ReinforcementLearning(AClist.get(i));
			ActorSample = getActorSample(AClist.get(i));
			CriticSample = getCriticSample(AClist.get(i));
			md_ActorSample.put(ActorSample);
			md_CriticSample.put(CriticSample);
		}
		md_ActorSample.close();
		md_CriticSample.close();
		
		//AC統合
		integrate();
		
		//評価
		//統合してないやつ
		for(int i = 0; i < this.NumberOfAC; i++){
			rewardsum = evaluation(AClist.get(i));
			md_rewardave_inte.open();
			for(int j = 0; j < 100; j++){
				sum = 0;
				for(int n = 0; n < 10; n++){
					sum += rewardsum[n][j];
				}
				sum = sum / 10;
				md_rewardave_inte.put(j + 1, sum);
			}
			md_rewardave_inte.close();
		}
		//統合したやつ
		rewardsum = evaluation(integrate_AC);
		md_rewardave_inte.open();
		for(int i = 0; i < 100; i++){
			sum = 0;
			for(int n = 0; n < 10; n++){
				sum += rewardsum[n][i];
			}
			sum = sum / 10;
			md_rewardave_inte.put(i + 1, sum);
		}
		md_rewardave_inte.close();
	}
	

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		AC_integration ACinte = new AC_integration(args[0]);
		ACinte.execute();
	}

}
