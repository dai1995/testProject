package MountainCar;

import java.util.Random;

public class MountainCarTest {
	
	MountainCar mc = new MountainCar();
	
	double position;
	double velocity;
	
	public MountainCarTest() {
		Random r = new Random();
		int Action;

		this.position = mc.getAgentPosintion();
		System.out.println("x = " + this.position);
		System.out.println("v = " + this.mc.getAgentVelocity());
		for(int i = 0; i < 2000; i++){
			//Action = r.nextInt(3);
			if(i < 30){
				Action = 0;
			}else if(i >= 30 && i < 70){
				Action = 2;
			}else if(i >= 70 && i < 100){
				Action = 0;
			}else{
				Action = 2;
			}
			System.out.println("Action = " + Action);
			this.mc.update(Action);
			this.position = mc.getAgentPosintion();
			System.out.println("x = " + this.position);
			System.out.println("v = " + this.mc.getAgentVelocity());
			System.out.println("reward = " + this.mc.getReward());
			
		}
	}
	
	

	public static void main(String[] args) {
		// TODO 自動生成されたメソッド・スタブ
		new MountainCarTest();
	}

}
