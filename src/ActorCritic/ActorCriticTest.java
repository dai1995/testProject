package ActorCritic;

/*import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Scanner;
import java.io.*;
*/
import java.util.Scanner;

import ParameterReader.ParameterReader;
import datalogger.multiple_dataOutput;

public class ActorCriticTest {
	final boolean DEBUG = true;
	//ActorCtiticの動作----------------------------------------------------------------------------------------------------
	public ActorCriticTest(String parameterfilename) {
		ActorCritic AC;
		DBcontrol dbctrl;
		input data_in;
		int count = 0, n = 0;
		double reward = 0.0D, profit = 0.0D, currntprofit = 0.0D, sell_power = 0.0D;
		double [] data = new double [8];
		double[] action_output = new double[1];
		ParameterReader pr = new ParameterReader(parameterfilename);
		multiple_dataOutput md = new multiple_dataOutput(pr.Reader("logoutput"));		
		
		for (int iteration=0; iteration<3; iteration++) {//dat用
			AC = new ActorCritic(pr.Reader("ValueFunction"), pr.Reader("Actor"));
			dbctrl = new DBcontrol();
			data_in = new input(pr.Reader("VirtualData"), 8, n);//状態データを取ってくる。8入力,データファイル名は「data+n」 ex.data0
			AC.setInputSize(8,1);//入力,出力の次元数をセット。今回は8入力,1出力
			AC.actor.setMaxVariance(10D);//乱数の振れ幅
//			AC.setMaxVariance(10D);↑にすればいいのでは？
			//connect conne = new connect();
			//Connection con = conne.getCon();
			System.out.println("this.battery" + data_in.getBattery());
			md.open();
			this.Log("ActorCriticTest() pass open");
			count = 0;
			for (int year = 1; year <= 2; year++){//サンプルデータNEDOを繰り返す
//				System.out.println("【" + year + "年目】");
//				for(int ac_day=1;ac_day<=365;ac_day++){//日数(0日〜364日)
				for (int month = 1; month <= 12; month++){
//					System.out.println("【" + month + "月】");
					for (int day = 1; day <= 30; day++){
//						System.out.println("【" + day + "日】");
						int ac_day = (month - 1) * 30 + day;
						for (int hour = 0; hour < 24; hour++) {//時刻(0時〜23時)
							System.out.println("【" + year + "年目" + month + "月" + day + "日" + hour + "時】");
							this.Log("【" +hour + "時】");
							data = data_in.readData();//その日の天気と気温データを読み込む
							data = data_in.getInstance();//readDate以外のデータを入力へ入れる
							action_output = AC.getAction(data,year,month);
							if (action_output[0] >= 0.25) {
								this.Log("=======学習(sell)=======");
								this.Log("action_output[0] = " + action_output[0]);
								sell_power = data_in.PopBattery(action_output[0]);//バッテリー充電量からactin_output%だけ放電させる。制限があるときには改めてaoにその値が入る
								this.Log("OverCharge = " + data_in.getOverCharge());
								reward = dbctrl.auto_sell(sell_power, data_in.getOverCharge(), hour, ac_day, data[2]);
//								System.out.println("reward = " + reward);
								this.Log("reward = " + reward);
								profit = dbctrl.getProfit();//売電量の取得
								currntprofit += profit;
								AC.ReinforcementLearning(reward, 0.3, 0.3);//現時点での報酬、ない場合は0,ディスカウントファクタ,value_functionの学習速度を表す計数
							}else{
								this.Log("-------学習(non - sell)------");
								sell_power = data_in.PopBattery(action_output[0]);
								reward = dbctrl.auto_sell(sell_power, data_in.getOverCharge(), hour, ac_day, data[2]);
								AC.ReinforcementLearning(reward, 0.3, 0.3);//現時点での報酬、ない場合は0,ディスカウントファクタ,value_functionの学習速度を表す計数
							}//end if
//							data_in.setPrevious_time();ここでやらなくてもいいのでは?
						}//end for hour
//						dbctrl.setDay_profit(0D);
					}//end for day
					count++;
					md.put((double)count, currntprofit);
					System.out.println(currntprofit);
					currntprofit = 0D;
				}//end for month
//				}//end for ac_day
			}//end for year
			md.close();
		}//end for iteration
	}
	//log用メソッド--------------------------------------------------------------------------
	void Log(String log) {
		if (this.DEBUG) {
			System.out.println("ActorCriticTest." + log);
		}
	}
	//main関数-----------------------------------------------------------------------------
	public static void main(String[] args) {
		if (args.length<1) {
			System.err.println("Usage java ActorCriticTest [parameter-ActorCritic.xml])");
			System.exit(1);
		}
		new ActorCriticTest(args[0]);
	}
}