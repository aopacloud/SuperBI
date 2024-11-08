package net.aopacloud.superbi.queryEngine.sql.aggregator;

public class Average implements Aggregator{

    @Override
    public String getFunction() {
        return "AVG({})";
    }

//    @Override
//    public String replaceIfFunction(String expression) {
//
//        String field = unfoldFunction("AVG", expression.trim());
//
//        return String.format("avgIf(%s,isFinite(toFloat64(%s)))", field, field);
//    }
}
