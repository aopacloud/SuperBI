package net.aopacloud.superbi.queryEngine.sql.aggregator;

public class Maximum implements Aggregator{
    @Override
    public String getFunction() {
        return "MAX({})";
    }
//    @Override
//    public String replaceIfFunction(String expression) {
//
//        String field = unfoldFunction("MAX", expression);
//
//        return String.format("maxIf(%s,isFinite(%s))", field, field);
//    }
}
