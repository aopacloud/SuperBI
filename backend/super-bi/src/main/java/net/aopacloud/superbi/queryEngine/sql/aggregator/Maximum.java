package net.aopacloud.superbi.queryEngine.sql.aggregator;

public class Maximum implements Aggregator{
    @Override
    public String getFunction() {
        return "MAX({})";
    }
}
