package ActorCritic;


/*import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
*/import java.util.Calendar;

public class DBcontrol {
	final boolean DEBUG = true;
	final double peak_gain = 1.0D;
	public double profit = 0.0D, day_profit = 0.0D;
	public double ave_profit = 0.0D, yest_ac_profit = 0.0D, today_ac_profit = 0.0D, gain = 0.015D;
	public int yest_count = 0, today_count = 0;
//	Connection con;
	
//	public DBcontrol(Connection con) {
//		this.con = con;
//	}
	
	public DBcontrol() {
		//インスタンス化---------------------------------------------------------------------------------------------------------------
	}
	
	//------------ sell the power automatically and return the reward ----------
	// x: day -> rename to 'day' by k.yamauchi
	// day_time: 24hour // i is removed by k.yamauchi because i is duplicated with day_time.
	// ao : ratio of battery for sell
	public double auto_sell(double sell_power, double overCharge, double hour, double day, double weather){// Controlled by Actor Critic
		double reward;
		profit = sell_power * peak(hour) * (2 - weather); // The profit depends not only on the time but also the weather.
/*
		this.Log("auto_sell() " + day + "日目");
		this.Log("auto_sell() profit = " + profit);
		if (day_time == 0){
			yest_ac_profit = today_ac_profit;
			yest_count = today_count;
			today_ac_profit = 0.0D;
			today_count = 0;
		}
		if (day == 1 || yest_count == 0){//0日目or前日1度も売電しなかった場合
			ave_profit = 0.0D;
		}else{
			ave_profit = yest_ac_profit / (double)yest_count;
		}
		
		day_profit += profit * gain;
		today_ac_profit += profit;
		today_count++;
*/
		//reardの計算--------------------------------------------------------------------------------------------------------
//		if (day == 1){
//			reward = 0.0;
//		}else{
			if (overCharge <= 0){
//				reward = (profit - ave_profit) * gain;
				reward = profit * gain;
			}else{
//				reward = ((profit - ave_profit)* gain - 0.1 * (overCharge * gain));
				reward = (profit * gain - 0.1 * (overCharge));
			}
//		}
		//rewardに対するerror処理--------------------------------------------------------------------------------------------
		if (reward < 0) reward = 0.0;//rewardが0以下(マイナス)の場合
		if (reward > 1) {//rewardが1以上の場合
			this.Log("auto_sell() reward > 1 !!: reward = " + reward);
			this.Log("auto_sell() profit=" + profit + " ave_profit=" + ave_profit);
			reward = 1.0;
		}
		return reward;
	}
	
	
	//売電カーブ上での現在時刻の変数----------------------------------------------------------------------------------------------------------------------
	public double peak(double hour) {
		//double electric_power_selling_corve = this.PeakGain * (0.027*(Math.exp(-0.032*(now_time-8.5)*(now_time-8.5)) + Math.exp(-0.037*(now_time-18.5)*(now_time-18.5)))+0.011);
		double electric_power_selling_corve = this.peak_gain * 0.27 * (Math.exp(-30*(hour-0.35)*(hour-0.35)) + Math.exp(-30*(hour-0.77)*(hour-0.77))) + 0.11;
		this.Log("peak() now_time =" + hour + " electric_power_selling_corve = " + electric_power_selling_corve);
		return electric_power_selling_corve;
	}
	
	
	//getメソッド---------------------------------------------------------------------------------------------------
	public double getProfit(){
		return this.profit;
	}
	public double getAve_profit(){
		return this.ave_profit;
	}
	public double getDay_profit(){
		return this.day_profit;
	}
	
	
	//setメソッド---------------------------------------------------------------------------------------------------------
	public void setProfit(double profit){
		this.profit = profit;
	}
	
	public void setDay_profit(double day_profit) {
		this.day_profit = day_profit;
	}

	
	//Logメソッド---------------------------------------------------------------------------------------------------------
	void Log(String log) {
		if(this.DEBUG){
			System.out.println("DBcontrol." + log);
		}
	}

}