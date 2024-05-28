package net.aopacloud.superbi.queryEngine.sql.aggregator;

public class Average implements Aggregator{

    @Override
    public String getFunction() {
        return "AVG({})";
    }
}
