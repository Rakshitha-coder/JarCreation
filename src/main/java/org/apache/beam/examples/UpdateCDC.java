package org.apache.beam.examples;

import org.apache.beam.sdk.transforms.*;

public class UpdateCDC extends DoFn<String, Void> {

	private static final long serialVersionUID = 1L;
	public String max, deltacol, table, urlcdc, usercdc, passwordcdc, drivercdc;
	String instant;
	int count = 0;

	public UpdateCDC(String max, String deltacol, String table, String instant, String urlcdc, String user,
			String password, String cdcdriver) {
		this.max = max;
		this.deltacol = deltacol;
		this.table = table;
		this.instant = instant;
		this.urlcdc = urlcdc;
		this.usercdc = user;
		this.passwordcdc = password;
		this.drivercdc = cdcdriver;
	}

	@ProcessElement
	public void processElement(ProcessContext c) throws Exception {
		
		if (c.element().isEmpty() || c.element().equals("\n") || count > 1) {
			return;
		}
		
		max += c.element();
		System.out.println(max);
		
		// Update deltacol from cdc
		String cdc_write_query = "UPDATE cdc_table SET current_value ='" + max + "', last_modified_time_stamp='"
				+ instant + "' where table_name='" + table + "'and delta_col='" + deltacol + "'";
		System.out.println(cdc_write_query);
		
		count++;
		CDCManipulation.connectCDC(urlcdc, usercdc, passwordcdc, drivercdc);

		CDCManipulation.writeCDC(cdc_write_query);

		System.out.println(cdc_write_query);


	}
}
