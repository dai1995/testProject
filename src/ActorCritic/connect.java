package ActorCritic;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;


public class connect {
	public connect()  {
		try{
			Class.forName("org.postgresql.Driver");
			
			String url = "jdbc:postgresql://192.168.254.7:5432/e-farm";
			Connection con = DriverManager.getConnection(url);
			Statement stmt = con.createStatement();
			
			ResultSet rs = stmt.executeQuery("select * from owner;");
			while (rs.next()) {
				System.out.print(" " + rs.getString(1));
				System.out.print(" " + rs.getString(2));
				System.out.println(" " + rs.getString(3));
			}
			
		}catch(SQLException sqlex) {
			sqlex.printStackTrace();
		}catch(ClassNotFoundException ex) {
			ex.printStackTrace();
			System.exit(1);
		}
	}
	//main関数-------------------------------------------------------------------------------------
	/*public static void main (String argv[]) {
		try {
			new connect();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}*/
	//インスタンス化---------------------------------------------------------------------------------
	public Connection con;
	
	
	//Connectionを返す関数--------------------------------------------------------------------------
	public Connection getCon() {
		return con;
	}
}
