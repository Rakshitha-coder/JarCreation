package org.apache.beam.examples;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.time.LocalDate;
import com.google.api.services.bigquery.model.TableRow;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.TextIO;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO;
import org.apache.beam.sdk.io.gcp.bigquery.BigQueryIO.Write.WriteDisposition;
import com.google.api.services.bigquery.model.TableSchema;

public class RdbmsPipeline {

	public static void pipelineRun(Pipeline pipeline, String driver, String url, String user, String password,
			String query, String output, String val1, String header, String delimiter, String[] args, String outputtype,
            String bq_load_type, String batch_id, String table) {

                LocalDate now = LocalDate.now();
                header=header+delimiter+"EDW_INGESTION_BATCH_ID";
		        System.out.println(output);
                        
                PCollection<String> result=  pipeline.apply(JdbcIO.<String>read()
                .withDataSourceConfiguration(
                        JdbcIO.DataSourceConfiguration.create(driver, url).withUsername(user).withPassword(password))
                .withQuery(query).withCoder(StringUtf8Coder.of()).withRowMapper(new JdbcIO.RowMapper<String>() {
                   
                    private static final long serialVersionUID = 1L;

                    public String mapRow(ResultSet resultSet) throws Exception {
                        ResultSetMetaData rsmd = resultSet.getMetaData();
                        int columnsNumber = rsmd.getColumnCount();
                        String columnValue = "";

                        // Retrieving columns from the results for all rows

                        for (int i = 1; i <= columnsNumber; i++) {
                            String data=resultSet.getString(i);
                            if(data.isEmpty()||data.length()==0)
                            data=null;
                            if (i == columnsNumber)
                                columnValue += data + delimiter + batch_id;
                            else
                                columnValue += data + delimiter;

                        }
                        return columnValue;
                    }
                }));

         if(outputtype.toLowerCase().equals("gcs")){

            output=output +table+ "/" + now + "/" + table + "_" + batch_id;
           
			result.apply("write_to_file",
                        TextIO.write().to(output).withSuffix(".csv").withoutSharding().withHeader(header));

         }
        else if(outputtype.toLowerCase().equals("bq")){

                     
            String[] parts=output.replace(".",":").split(":");

            header = BigQueryDetails.getHeaderFromBQTable(parts);
                
            PCollection<TableRow> final_result = result.apply("ConvertToTableRow",
                    ParDo.of(new FormatForBigQuery(header, delimiter)));
            
            TableSchema bq_schema = BigQueryDetails.getBQschema(header);
            WriteDisposition bigQueryIOPTransform = null;

            if (bq_load_type.toUpperCase().equals("TRUNCATE")) {
                bigQueryIOPTransform = BigQueryIO.Write.WriteDisposition.WRITE_TRUNCATE;
            } else if (bq_load_type.toUpperCase().equals("APPEND")) {
                bigQueryIOPTransform = BigQueryIO.Write.WriteDisposition.WRITE_APPEND;
            }
            final_result.apply("WriteToBigQuery",
                    BigQueryIO.writeTableRows().to(output).withSchema(bq_schema)
                            .withCreateDisposition(BigQueryIO.Write.CreateDisposition.CREATE_IF_NEEDED)
                                .withWriteDisposition(bigQueryIOPTransform));
                }

    }
  
}
