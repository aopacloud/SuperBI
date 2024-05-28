package net.aopacloud.superbi.queryEngine.sql.aggregator;

public class Summation implements Aggregator{
    @Override
    public String getFunction() {
        return "SUM({})";
    }
}
