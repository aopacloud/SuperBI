package net.aopacloud.superbi.queryEngine.sql.aggregator;

public class Minimum implements Aggregator{
    @Override
    public String getFunction() {
        return "MIN({})";
    }

//    @Override
//    public String replaceIfFunction(String expression) {
//
//        String field = unfoldFunction("MIN", expression);
//
//        return String.format("minIf(%s,isFinite(%s))", field, field);
//    }
}
