package org.apache.beam.examples;
import com.google.api.services.bigquery.model.TableFieldSchema;
import com.google.api.services.bigquery.model.TableSchema;
import java.util.ArrayList;
import java.util.List;
import com.google.cloud.bigquery.BigQuery;
import com.google.cloud.bigquery.BigQueryOptions;

public class BigQueryDetails{
     static TableSchema getBQschema(String header) {
         String[] columnNames = header.split(",");
              List<TableFieldSchema> fields = new ArrayList<>();
              
             for(int i=0;i<columnNames.length; i++) {
               fields.add(new TableFieldSchema().setName(columnNames[i]).setType("STRING"));

             }
      TableSchema  tableschema=new TableSchema().setFields(fields);   
      return tableschema;
    }
    static String getHeaderFromBQTable(String[] bq_inputpath) {
        
        BigQuery bigquery = BigQueryOptions.getDefaultInstance().getService();

        String bq_datasetName = bq_inputpath[1];
        String bq_tableName = bq_inputpath[2];
        com.google.cloud.bigquery.Table table = bigquery.getTable(bq_datasetName, bq_tableName);
        //null table is not created 
        com.google.cloud.bigquery.Schema bqschema = table.getDefinition().getSchema();
        int columnsize = bqschema.getFields().size();
        int j = 0;
        String HEADERS = "";
        for (; j < columnsize - 1; j++) {
            HEADERS += bqschema.getFields().get(j).getName()+",";
        }

        HEADERS += bqschema.getFields().get(j).getName();
      
        return HEADERS;
    }
}