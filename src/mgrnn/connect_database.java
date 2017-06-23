package mgrnn;

import java.util.*;
import java.sql.*;
import java.text.*;

public class connect_database {
  Connection con;
  private final int Administrator = 3;
  private final int Postgres = 4;
  private int UserAttribute = Administrator;
  final String head_url = "jdbc:postgresql://";
  final boolean DEBUG = false;

  DateFormat df = DateFormat.getDateInstance(1,Locale.JAPAN);
  Calendar today = Calendar.getInstance();
  java.util.Date date;
  java.sql.Time sql_time;


  public connect_database(String URL, String dbname, String username, String password) throws Exception {
    this.date = new java.util.Date();
    today.setTime(date);
    sql_time = new java.sql.Time(today.getTimeInMillis());
    System.out.println("sqltime is " + sql_time.toString());

    Class.forName("org.postgresql.Driver");

    String detailed_url = this.head_url + URL + "/" + dbname; //これで良いかしら
    System.out.println(detailed_url);
    System.out.println("username = " + username);
    System.out.println("password = " + password);
    this.con = DriverManager.getConnection(detailed_url, username, password);
    if (username.equals("admin")) {
      this.UserAttribute = this.Administrator;
      System.out.println("Administrator");
    }else{
      this.UserAttribute = this.Postgres;
      System.out.println("Postgres");
    }
  }

  public Connection getConnection() {
    return this.con;
  }


  public void close_connection() {
    try {
      this.con.close();
    }catch(SQLException ex) {
      ex.printStackTrace();
    }
  }

/**
 * @return the userAttribute
 */
public int getUserAttribute() {
	return UserAttribute;
}

/**
 * @return the today
 */
public Calendar getToday() {
	return today;
}

/**
 * @return the date
 */
public java.util.Date getDate() {
	return date;
}

/**
 * @return the sql_time
 */
public java.sql.Time getSql_time() {
	return sql_time;
}

}