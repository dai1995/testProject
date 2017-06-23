package ActorCritic;

import java.util.Calendar;

import org.w3c.dom.Node;

import DataLoad.MultipleDataload;

public class input {
	final boolean DEBUG = true;
	//以下状態データ配列の次元のデータを指定する定数getInstance()で使われる
	int virtualDataIndex, InputSize; 
	double battery, overCharge, previous_time, max_battery;
	double[] data;
	MultipleDataload dl;//バーチャルデータをロードするクラス
	DBcontrol dbctrl;
	Calendar previousDate = null; //getInstance()を最後に呼び出したタイムスタンプ
	//初期化--------------------------------------------------------------------------------------------------------
	public input(Node VirtualData, int input_size, int dataIndex) {//コンストラクタ
		this.virtualDataIndex = 0;//バーチャルデータのインデックス
		this.InputSize = input_size;//大域変数に次元数をセットする
		this.previous_time = 0D;
		this.overCharge = 0D;
		this.battery = 0D;
		this.max_battery = 1100D;
		this.dl = new MultipleDataload(VirtualData);//バーチャルデータのファイルを読み込む準備
		this.dl.read(dataIndex);//「data+dataIndex」というデータファイルを読み込む ex.「data0」
		this.dbctrl = new DBcontrol();
		data = new double[this.InputSize];
	}	
	//データファイルより値を入力--------------------------------------------------------------------------------------
	double[] readData() {
		for (int i = 0; i < dl.getNumberOfInputs(); i++) {//入力数分繰り返す
			data[i] = dl.learning_input_patterns[this.virtualDataIndex][0][i];
			this.Log("readData() data[" + i + "] = " + data[i]);
			if (i == 4) {
				this.overCharge = this.PushBattery(data[i]);//充電&overCharge計算
				this.Log("readData() battery = " + this.battery); 
				this.Log("readData() overCharge = " + this.overCharge);
			}
		}//end for i
		this.virtualDataIndex++;		
		if (this.virtualDataIndex >= dl.getActualNumberOfLearningSamples()) this.virtualDataIndex = 0;
		return data;
	}
	//データファイル以外の入力の作成------------------------------------------------------------------------------------------
	double[] getInstance() {
		for (int j=dl.getNumberOfInputs(); j<this.InputSize; j++) {//ループ変数jのFrom値に注意
			switch (j) {
				case 5:
					data[j] = this.battery / this.max_battery;//蓄電池の充電率
				case 6:
					data[j] = this.dbctrl.peak(data[0]);//売電率
				case 7:
					data[j] = this.dbctrl.peak(data[0]) - this.dbctrl.peak(previous_time);//売電率の上昇率
			}
			if (data[j] < 0) data[j] = 0D;
			this.Log("getInstance() data[" + j + "]" + data[j]);
		}
		previous_time = data[0];
		return data;
	}
	//売電用メソッド--------------------------------------------------------------------------------------------------------------
	public double PopBattery(double getPowerRatio) {
		//getPowerRatio = 2 * (getPowerRatio - 0.05);//しきい値を引いて２倍しないと正確なアクションが求められない。
		double sell_power = getPowerRatio * this.battery;//売電量 = action_output * バッテリー容量
		String situation = null;
		this.Log("getPowerRatio" + getPowerRatio);
		if ((this.battery / this.max_battery) < 0.5){//充電率が50%以下の場合
			situation = "under 50%";
			sell_power = 0;
		} else if (((this.battery-sell_power) / this.max_battery) < 0.5) {//売電した結果50%以下になってしまう場合
				situation = "keep 50%";
				sell_power = this.battery - (Math.abs(this.battery-sell_power) * 1100);
		} else {//問題なく売れる場合
				situation = "sell batery!!";
		}
		this.Log("PopBattery() battery = " + this.battery);
		this.Log("【" + situation + "】 selled_power = " + sell_power);
		this.battery -= sell_power;//売電分をバッテリー容量から差し引く
		data[5] = this.battery/this.max_battery;//data[5]の更新
		return sell_power;
	}
	//充電用メソッド--------------------------------------------------------------------------------------------------------------
	public double PushBattery(double in_power_ratio) {//in_power_ratio = data[4](日射強度 = 発電量)
		double over=0.0D;
		this.battery += in_power_ratio * this.max_battery;
		if (this.battery > this.max_battery) {
			over = this.battery - this.max_battery;
			this.battery = this.max_battery;
		}
		return over;
	}
	//----------------------------------------------------
	// 発電量を得るメソッド
	// date, weatherが引数
	// 現在の時刻はdateから得るようになっており、ここから[0,1]の値に変換した時刻を得る。
	// この時刻情報から日射強度をモデル化した式を使って発電量を得る
	//----------------------------------------------------
/*	double GeneratedPower(double time, double weather) {//今は使ってない
		double x = (double)date.get(Calendar.HOUR)/(double)24;
		if (date.get(Calendar.AM_PM)==1) {
			x += 0.5;
		}
		//System.out.println("GeneratedPower() x= " + x + " hour = " + date.get(Calendar.) + " date=" + date.get(Calendar.DATE));
		double gain = weather * 10.0;
		double result = gain * (Math.sin(Math.PI*(2*x-0.5)) + 0.5*(Math.random()-0.5));
		System.out.println("GeneratedPower() result = " + result);
		if (result < 0) result = 0;
		return result;
		//return 0;
	}
*/	
	
	//--------------------------------------------------------------
	// boolean isDifferentDate(today, prev_day)
	// 今日の日付(today)が、最後にアクセスした日付(prev_day)と違うのかどうかを判定。違うならtrueを返す
	//--------------------------------------------------------------
/*	boolean isDifferentDate(Calendar today, Calendar prev_day) {
		if (prev_day==null) return true;
		int year = today.get(Calendar.YEAR);
		int month = today.get(Calendar.MONTH);
		int day = today.get(Calendar.DATE);
		int prev_year = prev_day.get(Calendar.YEAR);
		int prev_month = prev_day.get(Calendar.MONTH);
		int prev_date = prev_day.get(Calendar.DATE);
		if (year == prev_year &&
			month == prev_month &&
			day == prev_date ) return false;
		else return true;
	}	
*/		
	//getメソッド------------------------------------------------------------------------------------------------------
	public double getOverCharge() {
		return this.overCharge;
	}
	public double getBattery() {
		return this.battery;
	}	
	
/*	public void setPrevious_time(){//ここでやらなくてもいいのでは?
		this.previous_time = this.data[0];
	}
*/
	//Logメソッド--------------------------------------------------------------------------------------------------------
	void Log(String log) {
		if (this.DEBUG) {
			System.out.println("input." + log);
		}
	}
	
}