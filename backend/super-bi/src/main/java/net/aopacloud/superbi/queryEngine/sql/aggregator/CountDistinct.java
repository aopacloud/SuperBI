package net.aopacloud.superbi.queryEngine.sql.aggregator;

public class CountDistinct implements Aggregator{
    @Override
    public String getFunction() {
        return "COUNT(distinct {})";
    }
}
