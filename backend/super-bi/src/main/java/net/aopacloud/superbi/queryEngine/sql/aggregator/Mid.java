package net.aopacloud.superbi.queryEngine.sql.aggregator;

public class Mid implements Aggregator{
    @Override
    public String getFunction() {
        return "MID({})";
    }
}
