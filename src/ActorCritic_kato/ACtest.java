package ActorCritic_kato;

import DataLoad.MultipleDataload;
import ParameterReader.ParameterReader;
import PuddleWorld.PuddleWorld;
import datalogger.multiple_dataOutput;

public class ACtest {
	int SizeOfHiddenUnits;
	public ACtest(String parameterfilename, int SizeOfHiddenUnits){
		
		
		double[] AgentPoint = new double[2];
		int action_output;
		double[][] init_inputs = new double[441][2]; 
		double[][] init_outputs = new double[441][4];
		double reward = 0;
		double[][] rewardsum = new double[10][100];
		double[][] karnel_parameter;
		double[] kp;
		double[] kpinput = new double[2];
		this.SizeOfHiddenUnits = SizeOfHiddenUnits;
		ParameterReader pr = new ParameterReader(parameterfilename);
		MultipleDataload init = new MultipleDataload(pr.Reader("init"));
		multiple_dataOutput md = new multiple_dataOutput(pr.Reader("logoutput"), this.getIDString(this.SizeOfHiddenUnits));
		multiple_dataOutput md_init = new multiple_dataOutput(pr.Reader("initoutput"), this.getIDString(this.SizeOfHiddenUnits));
		multiple_dataOutput md_reward = new multiple_dataOutput(pr.Reader("rewardoutput"), this.getIDString(this.SizeOfHiddenUnits));
		multiple_dataOutput md_rewardave = new multiple_dataOutput(pr.Reader("rewardaveoutput"), this.getIDString(this.SizeOfHiddenUnits));
		multiple_dataOutput md_karnel = new multiple_dataOutput(pr.Reader("karneloutput"), this.getIDString(this.SizeOfHiddenUnits));
		
		
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
		
		for(int n = 0; n < 10; n++){
			ActorCritic2 AC;
			AC = new ActorCritic2(pr.Reader("ValueFunction"), pr.Reader("Actor"), this.SizeOfHiddenUnits);
			AC.setInputSize(2, 4);
			

			AC.actor.setMaxVariance(2D);
			for(int i = 0; i < 25; i++){
				AC.initLearning(init_inputs[i], init_outputs[i]);
			}

			//init.
			//AC.initLearning(init);
			

			md_init.open();
			md_reward.open();
			md_karnel.open();
			for(int i = 0; i < 100; i++){
				PuddleWorld pw = new PuddleWorld();
				AgentPoint[0] = pw.getagentPoint().getX();
				AgentPoint[1] = pw.getagentPoint().getY();
				md_init.put(AgentPoint[0], AgentPoint[1]);
				rewardsum[n][i] = 0;
				md.open();
				md.put(AgentPoint[0], AgentPoint[1]);
				for(int j = 0; j < 10000; j++){				

					action_output = AC.getAction(AgentPoint, j);
					System.out.println("Action = " + action_output);
					pw.update(action_output);
					AgentPoint[0] = pw.getagentPoint().getX();
					AgentPoint[1] = pw.getagentPoint().getY();
					reward = pw.getRward();
					rewardsum[n][i] += reward;
					System.out.println("count = " + n + "_" + i + "_" + j);
					System.out.println("x = " + AgentPoint[0]);
					System.out.println("y = " + AgentPoint[1]);

					System.out.println("reward = " + reward);
					System.out.println("rewardsum = " + rewardsum[n][i]);
					AC.getValue(AgentPoint);
					AC.ReinforcementLearning(reward, 0.9, 0.1);
					md.put(AgentPoint[0], AgentPoint[1]);
					if(reward == 0){
						break;
					}
				}
				
				this.Log("ACtest() out of loop" + rewardsum[n][i]);
				md_reward.put(i + 1, rewardsum[n][i]);
				md.close();
			}
			md_reward.close();
			karnel_parameter = AC.critic.display_learned_Critic();
			
			//criticの学習内容をファイルに書き出し
			for(double i = 0; i <= 1; i += 0.1){
				kpinput[0] = i;
				for(double j = 0; j <= 1; j += 0.1){
					kpinput[1] = j;
					kp = AC.critic.displayCritic(kpinput);
					System.out.println(kpinput[0] + " " + kpinput[1] + " " + kp[0]);
					md_karnel.put(kpinput, kp);
				}
			}
			//md_karnel.put(karnel_parameter);
			md_karnel.close();
			md_init.close();
			
			
			
		}
		
		
		
		
		
		double sum;
		md_rewardave.open();
		for(int i = 0; i < 100; i++){
			sum = 0;
			for(int n = 0; n < 10; n++){
				sum += rewardsum[n][i];
			}
			sum = sum / 10;
			md_rewardave.put(i + 1, sum);
		}
		md_rewardave.close();
		
	}

	String getIDString(int n) {
		String str="_"+n+"_";
		return str;
	}
	
	void Log(String log) {
		System.out.println("ACTest."+log);
	}
	
	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		if (args.length < 2) {
			System.err.println("Usage java -jar KatoActorCritic.jar [parameterfilename][size of kernels]");
			System.exit(1);
		}
		new ACtest(args[0], Integer.parseInt(args[1]));
	}

}
