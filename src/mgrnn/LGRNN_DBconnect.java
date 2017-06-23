/*
 * Created on 2010/07/02
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package mgrnn;
//import---------------------------------------------------------------------------
//他のパッケージから読み込めるようにする
import java.sql.Statement;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;


import matrix.MatrixException;
import ParameterReader.ParameterReader;
//import SOM.connect_database;
import DataLoad.*;
import datalogger.*;
import TransrateTimeStamp.TimeData;
import TransrateTimeStamp.TransrateTimeStamp;
import VectorFunctions.*;
/**
 * @author yamauchi
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */ 
public class LGRNN_DBconnect {
	boolean DEBUG = true;
	FunctionOutput functionout=null;
	multiple_hdim_dataOutput dataout=null;
	int UpperLimitOfKernels;
	
	String DBname="virtualfarm";
	String DBusername = "postgres";
	//String URL = "jdbc:postgresql://localhost";
	String URL = "192.168.254.20:5432";
	//String URL = "localhost:10004";
	Connection con;

	ArrayList<Integer> owners;
	public LGRNN_DBconnect(String parameterfilename, int UpperLimitOfKernels){
		
		//double err_sum;
		//int NumberOfIterations=0;
		this.UpperLimitOfKernels = UpperLimitOfKernels;
		//double cumlative_error= 0D;
		//LgrnnLearningStatus result;
		int grnn_id; 
		
		//Open databas
		String detailed_url = URL + DBname;
	
		//LGRNNのパラメータファイルを読み込む
		ParameterReader pr = new ParameterReader(parameterfilename);
		//LGRNNをインスタンス化する。
		
		//ログファイルの設定
		multiple_dataOutput numberout = new multiple_dataOutput(pr.Reader("CellNumberOutput"));
		multiple_dataOutput errout = new multiple_dataOutput(pr.Reader("ErrorOutput"));
		multiple_dataOutput sampleout = new multiple_dataOutput(pr.Reader("sampleout"));

		connect_database connect_db = null;
			try {
				//ownerのリストを得る。
	
				try {
					connect_db = new connect_database(URL, "virtualfarm", DBusername, "*");
				} catch (Exception e) {
					// TODO 自動生成された catch ブロック
					e.printStackTrace();
				}
				this.con = connect_db.getConnection();
			
				//これでエラーが取れない場合には知らせて下さい。
				this.owners = this.getOwnerList(con);
				for (Integer each_owner: this.owners) {//各オーナーについて
					
					//grnnをインスタンス化（オーナ毎にインスタンス化が必要）
					LimitedGRNN grnn = new LimitedGRNN(pr.Reader("LGRNN"));
					grnn.setNumberOfInputs(5);//入力5次元
					grnn.setNumberOfOutputs(1);//売電量に相当
					grnn.setUpperLimitOfHiddenUnits(this.UpperLimitOfKernels);//カーネルの上限数設定
									
					//データベース(テーブルlgrnnとkernel)からカーネル情報を読み込む（あれば）
					//さらにgrnn_idも読み込む（書き込みの時に使う）
					System.out.println(" NumberOfOutputs = " + grnn.NumberOfOutputs);
					grnn_id = this.ReadLgrnn(grnn, con, each_owner.intValue());//ここでLGRNN構築
					if (grnn_id <=0) {//もし何もデータがないなら、
						//テーブルを追加する
						this.WriteLGRNNToDB(null, con, each_owner.intValue(), 0);
						//もう一度grnn_idを読みに行く（grnn_idを得るため）
						grnn_id = this.ReadLgrnn(grnn, con, each_owner.intValue());
					}
					
					//デ	ータベース(テーブルlgrnn)から最後に学習した時間を読み込む（あれば）
					java.sql.Timestamp last_time = null;
					String last_time_query = "select date from lgrnn where owner_id =" + each_owner.intValue();
					java.sql.Statement stmt = con.createStatement();
					ResultSet last_time_rs =stmt.executeQuery(last_time_query);
					this.Log("constructor()" + last_time_query);
					boolean is_exist_record = false;
					while(last_time_rs.next()){
						last_time = last_time_rs.getTimestamp("date");
						//last_time = java.sql.Timestamp.valueOf("2000-01-01 00:00:00");//テスト用						
						this.Log("constructor() last_time = " + last_time.toString());
						is_exist_record = true;
					}
					if (!is_exist_record) {//もしレコードがないなら大昔の時刻にする
						last_time = java.sql.Timestamp.valueOf("2000-01-01 00:00:00");
					}
				
					//テーブルhistoryから最後に学習した日時から現在までのデータを読み込み学習する。
					double[] input = new double[5];//入力用配列
					double[] output= new double[1];//出力用配列追加
					String query = "select date,event,status, magpower, warehouse_idno, weather_idno, price_idno, power_idno from history where date>='" + last_time + "' and owner_id = ?";
					PreparedStatement pstmt = con.prepareStatement(query);
					pstmt.setInt(1, each_owner.intValue());//owner_idをセット
					//this.Log("date test " + TransrateTimeStamp.GetHour(last_time));
					this.Log("constructor() query = " + query);
					ResultSet rs = pstmt.executeQuery();//クエリーを実行
				    double x=0;
				    double x_result=0;
					while (rs.next() ) {//データベース(table history)からのレスポンスを１行ずつ取り出す。
					  this.Log("constructor ----> each row of history");
					  
					  //historyから時刻情報を得る  input[0]の設定
					  java.sql.Timestamp date = rs.getTimestamp("date");
					  TimeData target_time = TransrateTimeStamp.GetHour(date);//日付、時間、分、秒に分解
					  int hour_data = target_time.hour;
					  int minute_data = target_time.minute;
					  input[0] = (double)(hour_data*60+minute_data) / (double)(60*24);//時刻情報を[0,1]で得て最初の要素へ代入
					  this.Log("constructor() date_str = " + date.toString());
					  this.Log("constuctor() the last timestamp of history: Hour=" + target_time.hour + " minuet = " + target_time.minute + " result = " + input[0]);
					  
					  //event情報を得る output[0]の設定
				      int event = rs.getInt("event");
				      //magpower(目標電圧）を得る。
				      float magpower = rs.getFloat("magpower");
					  //目標電圧からバッテリーの容量割合を算出（これが出力となる）
					  x=(magpower-10.5)/(14.4-10.5);
					  x_result=1-x;
					  if(x_result>1.0){
						  x_result=1.0;
					  }
					  else if(x_result<0.0){
						  x_result=0.0;
					  }
					  System.out.println("*************************教師信号result*******************"+x_result);
					  if (event == 1) {//売電リクエストもしくは売電中
						  output[0]=x_result;
					  }else{
						  output[0]= 0F;
					  }

				
					  //天気情報 input[1], input[2]の設定
					  int weather_idno = rs.getInt("weather_idno");//historyに関連付けられたweather_idnoを得る
					  String query2 = "select * from weather where idno = ?";//テーブルweatherから天気情報を得るクエリー
					  PreparedStatement pstmt2 = con.prepareStatement(query2);
					  pstmt2.setInt(1, weather_idno);//?に値を入れる
					  ResultSet rs2 = pstmt2.executeQuery();
					  float today_weather, tomorrow_weather;
					  while (rs2.next()) {
						  today_weather = rs2.getFloat("today");
						  tomorrow_weather = rs2.getFloat("tomorrow");
						  this.Log("constructor() today_weather=" + today_weather);
						  input[1] = today_weather;//山内追加
						  input[2] = tomorrow_weather;//山内
					  }
					  rs2.close(); pstmt2.close();//closeする
					
					  //price input[3]の設定
					  int price_idno = rs.getInt("price_idno");//historyに関連付けられたprice_idnoを得る
					  String query3 = "select price from sellprice where idno = ?";
					  PreparedStatement pstmt3 = con.prepareStatement(query3);
					  pstmt3.setInt(1, price_idno);
					  ResultSet rs3 = pstmt3.executeQuery();
					  while (rs3.next()){
						  double price = rs3.getDouble("price");
						  input[3] = price;//山内
					  }
					  rs3.close(); pstmt3.close();
					
					  //battery v input[4]の設定
					  int power_idno = rs.getInt("power_idno");//hisotryに関連付けられたpower_idnoを得る
					  String query4 = "select current_battery_voltage from power where idno = ?";
					  PreparedStatement pstmt4 = con.prepareStatement(query4);
					  pstmt4.setInt(1, power_idno);
					  ResultSet rs4 = pstmt4.executeQuery();
					  while (rs4.next()){
						  double current_voltage = rs4.getDouble("current_battery_voltage");
						  input[4] = current_voltage;//山内
					  }

					  //学習サンプルの表示
					  Log("input=");
					  for (int i=0; i<input.length; i++) {
						  System.out.print(input[i] + ", ");
					  }
					  System.out.println("");
					  Log("output=");
					  for (int o=0; o<output.length; o++) {
						  System.out.print(output[o] + ", ");
					  }
					  System.out.println("");
					  
					  	double[] out =grnn.calculate_outputs(input);
			    		double sum = 0.0;
			    		for(int i=0;i<out.length;i++){
			    			sum += Math.pow(out[i]-output[i], 2D);
			//    			System.out.println("inputは"+input[i]);
		//	    			System.out.println("outは"+out[i]);
	             // 		System.out.println("outputは"+output[i]);
			    		}
			    		
			    		System.out.println("評価値は...........="+sum);
					  
					  //ここまでで得られたinput[]と、次で得られるoutput[]を使ってLGRNNに学習させる。
					  try{
						  this.Log("Constructor() LGRNN_learning()");
						  grnn.LGRNN_learning(input, output, 10, 1.0, false);
						  /*inputはinout[0~5],outputはoutput[0],10はupperlimithiddenunits(学習ユニットをいくつに設定するか)
						   * 1.0はimportance_weight(どれくらいの強さで学習するか),falseはUsePseudoInverseとりあえずオフで*/
					  }catch(MatrixException mex){
						  mex.printStackTrace();
					  }//例外処理
					}// for each rs.next() of history
					//System.exit(1);
				
					 //学習結果をデータベース(テーブルkernel)に書き込む
					this.Log("Constructor() write learning result into table kernel");
					String query_4_kernel_write;
					PreparedStatement pstmt_4_kernel_write;
					
					//まず該当するカーネルのテーブルを消してしまう。（updateする方法もあるが、このために必要なkernelのidnoを覚えておくための手続きが別途必要になり複雑になるのでこの方法を取る）。
					query_4_kernel_write = "delete from kernel where lgrnn_id = ?";
					pstmt_4_kernel_write = this.con.prepareStatement(query_4_kernel_write);
					pstmt_4_kernel_write.setInt(1, grnn_id);
					pstmt_4_kernel_write.executeUpdate();
					pstmt_4_kernel_write.close();
					
					//追加する形でカーネルのデータを書き込む
					query_4_kernel_write = "insert into kernel (w1, w2, r, sigma, u1, u2, u3, u4, u5,lgrnn_id) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
					pstmt_4_kernel_write = this.con.prepareStatement(query_4_kernel_write);
					ArrayList<Cell> hidden_units = grnn.getHidden_units();
					
					int offset;
					//offset = 1;
					int kernel = 0;
					for (Cell each_cell: hidden_units) {
						this.Log("Writing " + kernel + "-th kernel data.");
						offset = 1;
						// パラメータ W[i]の書き込み
						double[] w = each_cell.getT_alpha();
						//System.out.println("***w.length***="+w.length);
						for (int o=0; o<w.length; o++) {
							System.out.println("write into database : w[" + o + "]=" + w[o] + " offset=" + (offset+o));
							pstmt_4_kernel_write.setDouble(offset+o, w[o]);
						}
						if (w.length < 2) {//出力次元数は２まで対応できるも、１の場合には数が足りなくなるので、その場合には強制的に０を書き込んでおく
							pstmt_4_kernel_write.setDouble(2, 0.0);
						}
						offset = 3;
						//パラメータR（肩代わりした回数)の書き込み
						double R = each_cell.getR();
						pstmt_4_kernel_write.setDouble(offset, R);
						this.Log("write into database : R = " + R + " offset = " + offset);
						offset ++;
						//sigmaの書き込み
						double sigma = grnn.variance;
						pstmt_4_kernel_write.setDouble(offset, sigma);
						this.Log("write into database : sigma = " + sigma + " offset = " + offset);
						offset = 5;
						//カーネルの中心位置の書き込み
						double[] u = each_cell.getT();//カーネルの中心位置が得られる。
						for (int i=0; i<u.length; i++) {
							pstmt_4_kernel_write.setDouble(offset+i, u[i]);
							this.Log("write into database : u" + (i+1) + " = " + u[i] + " offset = " + (offset+i));
						}
						offset += u.length;
						//grnn_idのセット
						System.out.println(offset);
						pstmt_4_kernel_write.setInt(offset, grnn_id);
						
						//書き込み
						pstmt_4_kernel_write.executeUpdate();
						kernel ++;
					}// for cell
					
					//テーブルlgrnnを書き込む
					this.WriteLGRNNToDB(grnn, con, each_owner.intValue(), hidden_units.size());					
				}// owner
			}catch(SQLException sqlex) {
				sqlex.printStackTrace();
				this.Log("construtor()");
			}
			//終了
	}

	ArrayList<Integer> getOwnerList(Connection con) throws SQLException {
		ArrayList<Integer> list = new ArrayList<Integer>();
		String query = "select * from owners order by idno";
		java.sql.Statement stmt = con.createStatement();
		ResultSet rs = stmt.executeQuery(query);
		while (rs.next()) {
			list.add(new Integer(rs.getInt("idno")));
		}
		return list;
	}
	
	
	void WriteLGRNNToDB(LimitedGRNN lgrnn, Connection con, int owner_id, int kernel_size) throws SQLException {
		String query;
		PreparedStatement pstmt;
		if (lgrnn == null) {
			//まだ何もない状態なので空の箱を追加する
			query = "insert into lgrnn (owner_id, kernel_size) values (?, ?)";
			this.Log("WriteLGRNNToDB() query=" + query);
			pstmt = con.prepareStatement(query);
			this.Log("WriteLGRNNToDB() owner_id=" + owner_id);
			pstmt.setInt(1, owner_id);
			this.Log("WriteLGRNNToDB() kernel_size=" + 0);
			pstmt.setInt(2, 0);
			pstmt.executeUpdate();
		}else{
			//lgrnnのデータを書き込む
			query = "update lgrnn set kernel_size = ? where owner_id = ?";
			this.Log("WriteLGRNNToDB() query=" + query);
			pstmt = con.prepareStatement(query);
			this.Log("WriteLGRNNToDB() kernel_size=" + kernel_size);
			pstmt.setInt(1, kernel_size);
			this.Log("WriteLGRNNToDB() owner_id=" + owner_id);
			pstmt.setInt(2, owner_id);
			pstmt.executeUpdate();
		}
	}
	
    int ReadLgrnn(LimitedGRNN lgrnn, Connection con, int owner_id) {
    	ArrayList<Integer> kernel_id = null;
    	//kernelの数（現時点での数）を読み込む
    	this.Log("ReadLgrnn()");
    	String query = "select * from lgrnn where owner_id=?";
    	PreparedStatement pstmt;
    	int UpperLimitOfKernels=0;
    	int lgrnn_id=-1;
    	try {
    		pstmt = con.prepareStatement(query);
			pstmt.setInt(1, owner_id);
		
			ResultSet rs = pstmt.executeQuery();
			while (rs.next()) {
				//UpperLimitOfKernels = rs.getInt("Kernel_size");
				lgrnn_id = rs.getInt("lgrnn_id");
				this.Log("ReadLgrnn() lgrnn_id = " + lgrnn_id);
				//lgrnn.setUpperLimitOfHiddenUnits(UpperLimitOfKernels);//カーネルの上限数を読み込みセット<-既にセット済みなのにセットするのは変
			}
			pstmt.close();
			
			if (lgrnn_id >= 0) {
				kernel_id = this.ReadKernels(lgrnn, con, lgrnn_id);
			}
			return lgrnn_id;
			
    	} catch (SQLException e) {
			// TODO 自動生成された catch ブロック
			e.printStackTrace();
			this.Log("ReadKernels()");
		}
    	return lgrnn_id;
    }
    
    ArrayList<Integer> ReadKernels(LimitedGRNN lgrnn, Connection con, int lgrnn_id) throws SQLException {
    	ArrayList<Integer> kernel_id = new ArrayList<Integer>();//カーネルidを保持するArraylist
    	String query = "select * from kernel where lgrnn_id = ? order by kernel_id";
    	PreparedStatement pstmt = con.prepareStatement(query);
    	pstmt.setInt(1, lgrnn_id);
    	ResultSet rs = pstmt.executeQuery();
    	String column_name;
    	double[] input = new double[lgrnn.NumberOfInputs];
    	double[] output = new double[lgrnn.NumberOfOutputs];
    	while (rs.next()) {
    		kernel_id.add(new Integer(rs.getInt("kernel_id")));
    		column_name = "u";
    		for (int i=0; i<lgrnn.NumberOfInputs; i++) {
    			String each_column_name = column_name + (i+1);
    			System.out.println("ReadKernels() column_name = " + each_column_name);
    			input[i] = rs.getDouble(each_column_name);
    			System.out.print(input[i] + ", ");
    		}
    		System.out.println("");
    		column_name = "w";
    		System.out.println("ReadKenels() NumberOfOutputs=" + lgrnn.NumberOfOutputs);
    		for (int o=0; o<lgrnn.NumberOfOutputs; o++) {
    			String each_column_name = column_name + (o+1);
    			System.out.println("ReadKernels() each_column_name = " + each_column_name);
    			output[o]= rs.getDouble(each_column_name);
    			System.out.print(output[o] + ", ");
    		}
    		System.out.println("");
    		
    		lgrnn.learning(input, output);//学習させることでカーネルを追加する。
    		/*double[] out =lgrnn.calculate_outputs(input);
    		double sum = 0.0;
    		for(int i=0;i<out.length;i++){
    			sum += Math.pow(out[i]-output[i], 2D);
    			System.out.println("inputは"+input[i]);
    			System.out.println("outは"+out[i]);
        		System.out.println("outputは"+output[i]);
    		}
    		
    		System.out.println("評価値は...........="+sum);*/
    		lgrnn.variance = rs.getDouble("r");
    	}
    	return kernel_id;
    }
    
    void Log(String log) {
    	if (this.DEBUG) {
    		System.out.println("LGRNN_DBconnect." + log);
    	}
    }
	public static void main(String[] args) {
		if (args.length<2) {
			System.err.println("Usage java mgrnn.LGRNN_DBconnect [parameterfile.xml][upper limit of hiddenunits]");
			System.exit(1);
		}
		if (args.length == 2) {
			new LGRNN_DBconnect(args[0], Integer.valueOf(args[1]).intValue());//args[1]はString型なのに対して、このコンストラクタの引数はint型
			//従ってargs[1]をint型に変換する必要がある。これには上記のようにIntege.valueOf(String).intValue()とする。
			//これは他の方への変換も可能で、Double.valueOf(String).doubleValue()でdouble型に変換ができる。
		}
	}
}
