package TransrateTimeStamp;
import java.sql.Timestamp;
import java.util.StringTokenizer;

public class TransrateTimeStamp {
	private final static boolean DEBUG = false;
	public static TimeData GetHour(Timestamp date) {
		String date_str = date.toString();
		
		StringTokenizer stk1 = new StringTokenizer(date_str, " ");
		String second_token=null;
		TimeData result = new TimeData();
		while (stk1.hasMoreTokens()) {
			stk1.nextToken();//yy-mm-dd
			second_token = stk1.nextToken();//hh:mm:ss
		}
		if (DEBUG) {
			System.out.println("TransrateTimeStamp." + "SecondToken = " + second_token);
		}
		StringTokenizer stk2 = new StringTokenizer(second_token, ":");
		String hour_token = stk2.nextToken();
		if (DEBUG) {
			System.out.println("TransrateTimeStamp." + "hourToken = " + hour_token);
		}
		String minute_token = stk2.nextToken();
		if (DEBUG) {
			System.out.println("TransrateTimeStamp." + "minuteToken = " + minute_token);
		}
		String sec_token = stk2.nextToken();
		if (DEBUG) {
			System.out.println("TransrateTimeStamp." + "secondToken = " + sec_token);
		}
		result.hour = Integer.valueOf(hour_token);
		result.minute = Integer.valueOf(minute_token);
		result.second = Double.valueOf(sec_token);
		return result;
	}
	

}
