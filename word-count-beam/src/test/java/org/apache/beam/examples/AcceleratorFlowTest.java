package org.apache.beam.examples;

import java.beans.PropertyVetoException;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.PipelineResult.State;
import org.apache.beam.sdk.coders.StringUtf8Coder;
import org.apache.beam.sdk.io.jdbc.JdbcIO;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.*;
import org.apache.beam.sdk.options.PipelineOptionsFactory;
import java.sql.Connection;
import java.sql.Timestamp;
import java.time.Instant;
import org.apache.beam.sdk.options.Validation.Required;
import org.apache.beam.sdk.transforms.*;
import java.util.Properties;
import java.io.FileInputStream;

public class AcceleratorFlow {

	public static String query, driver, url, user, password, output, header, drivercdc, urlcdc, load_type, table,
			deltacol, max = "", delimeter, usercdc, passwordcdc,outputtype,bq_load_type;

	public interface MyOptions extends PipelineOptions {
		@Validation.Required
		String getAbcProperties();

		void setAbcProperties(String value);

		@Required
		String getRdbmsProperties();

		void setRdbmsProperties(String value);

		@Required
		String getTableProperties();

		void setTableProperties(String value);
	}

	public static void config(String abc_prop, String rdbms_prop, String table_prop) throws Exception {

		// Reading config files for rdms and query details
		Properties prop = new Properties();
		String propfile = table_prop;

		String rdmsfile = rdbms_prop;

		String cdcfile = abc_prop;

		FileInputStream input = new FileInputStream(propfile);
		// loading properties in the file
		prop.load(input);

		// Retrieving values for the keys required
        query = prop.getProperty("query");
        outputtype = prop.getProperty("output_type");
        bq_load_type= prop.getProperty("bq_load_type");
		output = prop.getProperty("outputfile");
		header = prop.getProperty("header");
		deltacol = prop.getProperty("delta_col");
		table = prop.getProperty("table_name");

		load_type = prop.getProperty("load_type");
		delimeter = prop.getProperty("delimiter");

		FileInputStream rdms = new FileInputStream(rdmsfile);
		prop.load(rdms);
		driver = prop.getProperty("driver");
		url = prop.getProperty("url");
		user = prop.getProperty("user");
		password = prop.getProperty("password");

		FileInputStream cdc = new FileInputStream(cdcfile);
		prop.load(cdc);
		drivercdc = prop.getProperty("driverabc");
		urlcdc = prop.getProperty("urlabc");
		usercdc = prop.getProperty("userabc");
		passwordcdc = prop.getProperty("passwordabc");

	}

	public static void main(String[] args) throws PropertyVetoException, Exception {

		PipelineOptionsFactory.register(MyOptions.class);
		MyOptions options = PipelineOptionsFactory.fromArgs(args).withValidation().create().as(MyOptions.class);
        Pipeline pipeline = Pipeline.create(options);
        State status=null;
        Timestamp instant = Timestamp.from(Instant.now());

		AcceleratorFlow.config(options.getAbcProperties(), options.getRdbmsProperties(), options.getTableProperties());

		// Connect to cdc table
		Connection conn = CDCManipulation.connectCDC(urlcdc, usercdc, passwordcdc, drivercdc);
		
		ABCManipulation.insertABC(table, conn, instant);
        String batch_id = ABCManipulation.readABC(table, conn, instant);
        

		if (load_type.toLowerCase().equals("full")) {	
			
			RdbmsPipeline.pipelineRun(pipeline, driver, url, user, password, query,
					output, "full_load", header, delimeter, args,outputtype,bq_load_type,batch_id,table);
			
			status = pipeline.run().waitUntilFinish();
            System.out.println(status.toString());
            

		} else if (load_type.toLowerCase().equals("delta")) {

			String cdc_read_query = "select current_value from cdc_table where table_name='" + table
					+ "'and delta_col='" + deltacol + "'";

			System.out.println(cdc_read_query);

			// Get deltacol from cdc
			String val1 = CDCManipulation.readCDC(cdc_read_query);

			if (val1.isEmpty())
				val1 = "0";

			query = query.replace("$value", "'" + val1.trim() + "'");

			System.out.println(query);

			System.out.println(val1);

			val1 = val1.replace(':', '-');
	
            RdbmsPipeline.pipelineRun(pipeline, driver, url, user, password, query,
					output, val1.trim(), header, delimeter, args,outputtype,bq_load_type,batch_id,table);

			// Getting max value for delta col
			String querymax = "select max(" + deltacol + ") from " + table + "";
			System.out.println(querymax);

			pipeline.apply(JdbcIO.<String>read()
					.withDataSourceConfiguration(JdbcIO.DataSourceConfiguration.create(driver, url).withUsername(user)
							.withPassword(password))
					.withQuery(querymax).withCoder(StringUtf8Coder.of()).withRowMapper(new RowToString(delimeter)))
					.apply("UpdateCDC", ParDo.of(new UpdateCDC(max, deltacol, table, instant.toString(),
							urlcdc, usercdc, passwordcdc, drivercdc)));

			status = pipeline.run().waitUntilFinish();
			System.out.println(status.toString());

			

        }
        
        if (status.toString().equals("DONE"))
				ABCManipulation.updateABC(table, conn, instant, "SUCCESS");
			else
                ABCManipulation.updateABC(table, conn, instant, "FAILURE");
        CDCManipulation.closeConnectCDC();


	}
}
