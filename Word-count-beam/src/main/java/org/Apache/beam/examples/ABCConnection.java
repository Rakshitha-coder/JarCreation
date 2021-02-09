package org.apache.beam.examples;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

public class ABCConnection {
   static int readData(Connection conn, String table_Name,String status) throws SQLException {
        Statement stmt=null;
		stmt = conn.createStatement();
	    String query = "select max(batch_id) as max from abc_table where table_name = '"+table_Name+"'and status = '"+status+"'";
        System.out.println(query);
        ResultSet rs = stmt.executeQuery(query);
		int val =0;
		while (rs.next()) {
			val = rs.getInt("max");
		}
		System.out.println(val);
		return val;
    }

    static void insertData(Connection conn, String table_name,Timestamp starttime) throws SQLException {
        String abc_insert_query = "insert into abc_table(table_name,start_time,status)  values('" + table_name + "','"
				+starttime + "','RUNNING' )";

		
		Statement stmt = null;
		stmt = conn.createStatement();

		System.out.println(abc_insert_query);
		stmt.executeUpdate(abc_insert_query);

        
    }
   public static void updateData(String tablename, Connection conn, Timestamp start_time, Timestamp endtime,
            String status) throws Exception {
        String abc_update_query = "UPDATE abc_table SET end_time ='" + endtime + "', status='" + status
				+ "' where table_name='" + tablename + "'and start_time='" + start_time + "'";

		System.out.println(abc_update_query);

		Statement stmt = null;
		stmt = conn.createStatement();

		System.out.println(abc_update_query);
		stmt.executeUpdate(abc_update_query);

    }
    static Connection connectSql(String url, String driverName, String userName, String password)
            throws Exception {
     Connection conn = null;
     Class.forName(driverName);

		

        conn = DriverManager.getConnection(url, userName, password);
        if(conn != null) {
          System.out.println("Connected database successfully...");
        }
        else{
            System.out.println("Connected database failed...");
        }
        return conn;
    }

    public static void closeConnection(Connection conn) throws SQLException {
        conn.close();
    }

    
}
