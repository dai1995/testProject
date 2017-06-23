package PuddleWorld;
import MyRandom.MyRandom;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Random;

//reward未実装
public class PuddleWorld {
	
	Point2D agentPoint;//現在のエージェントの場所
	
	double GoalReward = 0.0;
	double StepReward = -1.0;
	
	int Action = 0;
	
	double agentSpeed = 0.05;
	//水たまりの生成
	Puddle P1 = new Puddle(0.1, 0.75, 0.45, 0.75, 0.1);
	Puddle P2 = new Puddle(0.45, 0.4, 0.45, 0.8, 0.1);
	
	//ワールドの生成
	Rectangle2D worldRect = new Rectangle2D.Double(0, 0, 1, 1);
	//ゴールの位置
	Rectangle2D goalRect = new Rectangle2D.Double(0.9, 0.9, 1, 1);
	double reward = 0.0;
	
	long seed = 4;
	
	MyRandom mrand = new MyRandom();
	Random rnd = new Random();
	
	public PuddleWorld(){//Actionがどんな感じで出てくるのか不明
		//this.Action = Action;
		setInitAgentPoint(seed);//Agentの初期位置をセット
	}
	
	private void setInitAgentPoint(long seed){
		Random r = new Random();
		
		
		double x = r.nextDouble();
		double y = r.nextDouble();
		
		
		while(true){
			if(x >= 0.9 && y >= 0.9){
				x = r.nextDouble();
				y = r.nextDouble();
			}
			else{
				break;
			}
		}
		
//		
//		double x = 0.05;
//		double y = 0.05;
		
		//System.out.println("init_AgentPointX = " + x);
		//System.out.println("init_AgentpointY = " + y);
		agentPoint = new Point2D.Double(x, y); 
	}
	
	public Point2D getagentPoint(){
		return this.agentPoint;
	}
	
	public void update(int a){
		Action = a;
		
		double reward;
		
		//はみ出し判定 行動前のの位置を保持
		double tmpX = agentPoint.getX();
		double tmpY = agentPoint.getY();
		
		double nextX = agentPoint.getX();
		double nextY = agentPoint.getY();
		
		if(a == 0){//右方向への移動
			nextX += agentSpeed;
		}
		if(a == 1){//左方向
			nextX -= agentSpeed;
		}
		if(a == 2){//上方向
			nextY += agentSpeed;
		}
		if(a == 3){//下方向
			nextY -= agentSpeed;
		}
		
		double XNoise = this.mrand.nrnd(rnd) * 0.01;
		double YNoise = this.mrand.nrnd(rnd) * 0.01;
		
		nextX += XNoise;
		nextY += YNoise;
		
		//はみ出し判定処理 
		/*
		nextX = Math.min(nextX, this.worldRect.getMaxX());//右にはみ出したら位置を枠線上に戻す
		nextX = Math.max(nextX, this.worldRect.getMinX());//左にはみ出したら
		nextY = Math.min(nextY, this.worldRect.getMaxY());//上にはみ出したら
		nextY = Math.max(nextY, this.worldRect.getMinY());//下にはみ出したら
		*/
		
		
		this.agentPoint.setLocation(nextX, nextY);
		
		//はみ出し判定改良 行動前に戻る
		if(agentPoint.getX() > 1.0 || agentPoint.getX() < 0.0 || agentPoint.getY() > 1.0 || agentPoint.getY() < 0.0){
			this.agentPoint.setLocation(tmpX, tmpY);
			//System.out.println("happy");
		}
		
		reward = getRward();
		//System.out.println("x = " + this.agentPoint.getX() + " y = " + this.agentPoint.getY());
		
		//System.out.println("reward = " + reward);
	}
	
	//ゴール判定
	public boolean check_Goal(){
		return goalRect.contains(agentPoint);
	}
	
	public double getRward(){
		double Puddlereward = getPuddleReward();
		if(check_Goal()){
			return Puddlereward + this.GoalReward;
		}
		else {
			return Puddlereward + this.StepReward;
		}
	}
	
	public double getPuddleReward(){
		double totalePuddleReward = 0;
		
		totalePuddleReward = this.P1.getReward(this.agentPoint) + this.P2.getReward(this.agentPoint);
		
		return totalePuddleReward;
	}
	/*
	double getReward(){
		double reward = 1;
		//領域内ならエージェントを移動して報酬を取得（領域から出たら元の位置に戻す）
		if(this.AgentX + this.Action[0] >= 0.0 && this.AgentX + this.Action[0] <= 1.0 
				&& this.AgentY + this.Action[1] >= 0.0 && this.AgentY + this.Action[1] <= 1.0){
			
			this.AgentX += this.Action[0];//左右の移動
			this.AgentY += this.Action[1];//上下の移動
			if(check_Puddle()){
				reward -= 400; 
			}
			
			reward--;//1ステップで-1の報酬
			
			if(check_Goal()){
				reward = 0;
			}
		}
		//rewardが1なら領域からはみ出た
		//Actor-Criticのほうで何らかの対処をする。
		return reward;
	}
	*/
}
