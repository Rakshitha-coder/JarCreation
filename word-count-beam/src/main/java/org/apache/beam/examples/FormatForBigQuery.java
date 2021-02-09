package org.apache.beam.examples;

import com.google.api.services.bigquery.model.TableRow;

import org.apache.beam.sdk.transforms.DoFn;

public class FormatForBigQuery extends DoFn<String, TableRow> {

        private static final long serialVersionUID = 1L;

        String header,delimiter;
        public FormatForBigQuery(String header, String delimiter) {
            this.header = header;
            this.delimiter = delimiter;
        }
        
        @ProcessElement
        public void processElement(ProcessContext c) {
            String[] columnNames = header.split(delimiter);
            TableRow row = new TableRow();
            String[] parts = c.element().split(delimiter);
            System.out.println(columnNames.length+"........."+parts.length);
            
                for (int i = 0; i < parts.length; i++) {
                    row.set(columnNames[i], parts[i]);
                }
                c.output(row);
            

        }
    }