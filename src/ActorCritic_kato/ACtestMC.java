package ActorCritic_kato;

import DataLoad.MultipleDataload;
import MountainCar.MountainCar;
import ParameterReader.ParameterReader;
import datalogger.multiple_dataOutput;

public class ACtestMC {

	public ACtestMC(String parameterfilename){
		int initnum = 285;
		double AgentPosintion;
		double AgentVelocity;
		double[] inputs = new double[2];
		int  action_output;
		double[][] init_inputs = new double[initnum][2];
		double[][] init_outputs = new double[initnum][4];
		double reward = 0;
		double[][] rewardsum = new double[10][1000];
		double[][] stepsum = new double[10][1000];
		double[][] karnel_parameter;
		double[] kp;
		double[] kpinput = new double[2];

		ParameterReader pr = new ParameterReader(parameterfilename);
		MultipleDataload init = new MultipleDataload(pr.Reader("initMC"));
		multiple_dataOutput md = new multiple_dataOutput(pr.Reader("logoutput"));
		multiple_dataOutput md_reward = new multiple_dataOutput(pr.Reader("rewardoutput"));
		multiple_dataOutput md_rewardave = new multiple_dataOutput(pr.Reader("rewardaveoutput"));
		multiple_dataOutput md_karnel = new multiple_dataOutput(pr.Reader("karneloutput"));

		init.read(0);

		for(int i = 0; i < initnum; i++){
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


//		init_inputs[0] = 0.5;
//		init_inputs[1] = 0.0;
//
//		init_outputs[0] = 0.33333333333;
//		init_outputs[1] = 0.33333333333;
//		init_outputs[2] = 0.33333333333;
		//init_outputs[3] = 1.0;

		System.out.println("happy");

		for(int n = 0; n < 10; n++){
			ActorCriticMC AC;
			AC = new ActorCriticMC(pr.Reader("ValueFunction"), pr.Reader("Actor"));
			AC.setInputSize(2, 4);

			AC.actor.setMaxVariance(2D);

			//AC.initLearning(init_inputs, init_outputs);


			for(int i = 0; i < initnum; i++){
				AC.initLearning(init_inputs[i], init_outputs[i]);
			}
//
			md_reward.open();
			md_karnel.open();
			for(int i = 0; i < 100; i++){
				MountainCar mc = new MountainCar();
				AgentPosintion = mc.getAgentPosintion();
				AgentVelocity = mc.getAgentVelocity();
				inputs[0] = AgentPosintion;
				inputs[1] = AgentVelocity * 14.3;
				rewardsum[n][i] =0;
				md.open();
				//put改造
				md.put(AgentPosintion);
				for(int j = 0; j < 10000; j++){
					System.out.println("count = " + n + "_" + i + "_" + j);
					action_output = AC.getAction(inputs, j);
					mc.update(action_output);
					AgentPosintion = mc.getAgentPosintion();
					AgentVelocity = mc.getAgentVelocity();
					inputs[0] = AgentPosintion;
					inputs[1] = AgentVelocity * 14.3;
					reward = mc.getReward();
					System.out.println("reword = " + reward);
					//rewardsum[n][i] += reward;
					stepsum[n][i]++;
					System.out.println("x = " + AgentPosintion);
					System.out.println("v = " + AgentVelocity);
					AC.getValue(inputs);
					AC.ReinforcementLearning(reward, 0.9, 0.1);
					//put改造
					md.put(AgentPosintion);
					if(reward  == 0){
						break;
					}
				}
				md.close();
				md_reward.put(i + 1, stepsum[n][i]);
			}
			md_reward.close();


			//criticの学習内容をファイルに書き出し
			for(double i = -1.2; i <= 0.6; i += 0.1){
				kpinput[0] = i;
				for(double j = -0.07; j <= 0.07; j += 0.01){
					kpinput[1] = j;
					kp = AC.critic.displayCritic(kpinput);
					md_karnel.put(kpinput, kp);

				}
			}

			md_karnel.close();

		}

		double sum;
		md_rewardave.open();
		for(int i = 0; i < 100; i++){
			sum = 0;
			for(int n = 0; n < 10; n++){
				sum += stepsum[n][i];
			}
			sum = sum / 10;
			md_rewardave.put(i + 1, sum);
		}
		md_rewardave.close();

	}

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		new ACtestMC(args[0]);
	}

}
