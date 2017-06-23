package MountainCar;

public class MountainCar {
	private double position;
	private double velocity;
	
	final public double minPosition = -1.2;
	final public double maxPosition = 0.6;
	final public double minVelocity = -0.07;
	final public double maxVelocity = 0.07;
	final public double goalPosition = 0.5;
	final public double accelerationFactor = 0.001;//加速度
	final public double gravityFactor = -0.0025;//重力
	final public double hillPeakFrequency = 3.0;//sin(3x)の3
	
	final public double defaultInitPosition = -0.5d;
	final public double defaultInitVelocity = 0.0d;
	final public double rewardPerStep = -1.0;
	final public double rewardGoal = 0.0d;
	
	private int lastAction = 0;
	


	
	public MountainCar(){
		this.position = defaultInitPosition;
		this.velocity = defaultInitVelocity;
	}
	
	public double getAgentPosintion(){
		return this.position;
	}
	
	public double getAgentVelocity(){
		return this.velocity;
	}
	
	public void update(int a){
		
		if(a == 0){//前進
			this.lastAction = 1;
		}else if(a == 1){//何もしない
			this.lastAction = 0;
		}else if(a == 2){//後退
			this.lastAction = -1;
		}
		
		//速度の更新
		this.velocity += 0.001 * this.lastAction - 0.0025 * Math.cos(3 * this.position);
		if(this.velocity > this.maxVelocity){
			this.velocity = this.maxVelocity;
		}
		if(this.velocity < this.minVelocity){
			this.velocity = this.minVelocity;
		}
		
		//位置の更新
		this.position += this.velocity;
		if(this.position > this.maxPosition){
			this.position = this.maxPosition;
		}
		if(this.position < this.minPosition){
			this.position = this.minPosition;
		}
		//後ろの壁にぶつかった場合
		if(this.position == this.minPosition && this.velocity < 0){
			velocity = 0;
		}
		
	}
	
	public double getHeight(){
		return Math.sin(3 * this.position);
	}
	
	public boolean checkGoal(){
		if(this.position >= 0.5){
			return true;
		}
		else{
			return false;
		}
	}

	
	public double getReward(){
		if(checkGoal()){
			//return 1;
			return this.rewardGoal;
		}
		else{
			return getHeight();
			//return this.rewardPerStep;
		}
	}
	
}
