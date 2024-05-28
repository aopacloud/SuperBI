package net.aopacloud.superbi.queryEngine.sql.aggregator;

public class Empty implements Aggregator{
    @Override
    public String getFunction() {
        return "{}";
    }
}
