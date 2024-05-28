package net.aopacloud.superbi.queryEngine.sql.aggregator;

public class Aggregators {
    public static Aggregator of(String name){
        switch (name) {
            case "EMPTY":
                return new Empty();
            case "SUM":
                return new Summation();
            case "AVG":
                return new Average();
            case "MID":
                return new Mid();
            case "MAX":
                return new Maximum();
            case "MIN":
                return new Minimum();
            case "COUNT":
                return new Count();
            case "COUNT_DISTINCT":
                return new CountDistinct();
            default:
                if(name.startsWith(Quantile.name)){
                    return new Quantile(name.substring(Quantile.name.length() + 1, name.length()));
                } else {
                    throw new IllegalArgumentException("");
                }
        }
    }
}
