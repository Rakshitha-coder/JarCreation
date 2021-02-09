package org.apache.beam.examples;

import org.apache.beam.sdk.schemas.Schema;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.values.Row;

public class RowParDo  extends DoFn<String, Row> {
 
        private static final long serialVersionUID = 1L;
        String CSV_HEADER;
        Schema schema;
        String delimiter;

        public RowParDo(String CSV_HEADER, Schema schema, String delimiter) {
            this.CSV_HEADER = CSV_HEADER;
            this.schema = schema;
            this.delimiter = delimiter;
        }

        @ProcessElement
        public void processElement(ProcessContext c) {
            if (!c.element().equalsIgnoreCase(CSV_HEADER)) {
                String[] vals = c.element().replace(delimiter, ",").split(",");
                if(vals.length==1 && vals[0]!=" "){
                }
                else{
                Row appRow = Row.withSchema(schema).addValues(vals).build();
                c.output(appRow);
                }
                }

            }

}