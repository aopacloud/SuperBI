package net.aopacloud.superbi.queryEngine.sql.aggregator;

public class Quantile implements Aggregator{

    public static final String name = "QUANTILE";

    private String param;

    public Quantile(String param){
        if(param.length() < 2) {
            this.param = String.format("0%s", param);
        } else {
            this.param = param;
        }
    }

    @Override
    public String getFunction() {
        if("50".equals(param)) {
            return "medianExact({})";
        }
        return String.format("quantileExact(0.%s)({})", param);
    }
}
