package org.apache.beam.examples;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;

public class ABCManipulation {

	public static void insertABC(String table, Connection conn, Timestamp instant) throws SQLException {

		String abc_write_query = "insert into abc_table(table_name,start_time,status)  values('" + table + "','"
				+ instant + "','RUNNING' )";

		System.out.println(abc_write_query);

		Statement stmt = null;
		stmt = conn.createStatement();

		System.out.println(abc_write_query);
		stmt.executeUpdate(abc_write_query);

	}

	public static String readABC(String table, Connection conn, Timestamp instant) throws SQLException {

		String abc_read_query = "select batch_id from abc_table where table_name='" + table + "' and start_time='"
				+ instant + "'";
		Statement stmt = null;
		stmt = conn.createStatement();

		ResultSet rs = stmt.executeQuery(abc_read_query);
		String val1 = "";
		while (rs.next()) {

			// Retrieve by column name
			val1 = rs.getString("batch_id");
		}
		System.out.println(val1);

		return val1;
	}

	public static void updateABC(String output, Connection conn, Timestamp instant, String string) throws SQLException {

		Timestamp now = Timestamp.from(Instant.now());

		String abc_update_query = "UPDATE abc_table SET end_time ='" + now + "', status='" + string
				+ "' where table_name='" + output + "'and start_time='" + instant + "'";

		System.out.println(abc_update_query);

		Statement stmt = null;
		stmt = conn.createStatement();

		System.out.println(abc_update_query);
		stmt.executeUpdate(abc_update_query);

	}

}
