package net.aopacloud.superbi.queryEngine.sql.aggregator;

public class Count implements Aggregator{
    @Override
    public String getFunction() {
        return "COUNT({})";
    }
}
