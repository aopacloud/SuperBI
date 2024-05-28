package net.aopacloud.superbi.queryEngine.sql.aggregator;

public class Minimum implements Aggregator{
    @Override
    public String getFunction() {
        return "MIN({})";
    }
}
