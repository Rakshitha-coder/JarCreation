package org.apache.beam.examples;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import org.apache.beam.sdk.io.jdbc.JdbcIO.RowMapper;

public class RowToString implements RowMapper<String> {

	
	
			private static final long serialVersionUID = 1L;
			public String delimeter;
			public RowToString(String delimeter) {
				this.delimeter=delimeter;
			}

			@Override
			public String mapRow(ResultSet resultSet) throws Exception {
				
				//Retrieving metadata to get no of columns
				ResultSetMetaData rsmd = resultSet.getMetaData();
				int columnsNumber = rsmd.getColumnCount();
				String columnValue = "";
				
				//Retrieving columns from the results for all rows
				do {
					for (int i = 1; i <= columnsNumber; i++) {

						if (i == columnsNumber)
							columnValue += resultSet.getString(i);
						else
							columnValue += resultSet.getString(i) + delimeter;

					}
					columnValue += "\n";
					
				}while (resultSet.next());
				
				return columnValue;
			}
	}


