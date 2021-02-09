package org.apache.beam.examples;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CDCManipulation {
	public static Connection conn = null;

	public static Connection connectCDC(String urlcdc, String user, String password, String cdcdriver)
			throws  SQLException, ClassNotFoundException {

		Class.forName(cdcdriver);

		// STEP 3: Open a connection

		conn = DriverManager.getConnection(urlcdc, user, password);

		if (conn != null) {
			System.out.println("Connected database successfully...");
		} else {
			System.out.println("Connected database failed...");
		}
		return conn;

	}

	public static String readCDC(String querycdc) throws SQLException {
		
		Statement stmt = null;
		stmt = conn.createStatement();

		ResultSet rs = stmt.executeQuery(querycdc);
		String val1 = "";
		while (rs.next()) {
			// Retrieve by column name
			val1 = rs.getString("current_value");
		}
		System.out.println(val1);
		
		return val1;

	}

	public static void writeCDC(String sql) throws SQLException {
		Statement stmt = null;
		stmt = conn.createStatement();

		System.out.println(sql);
		stmt.executeUpdate(sql);
	}

	public static void closeConnectCDC() throws SQLException {

		conn.close();

	}

}
