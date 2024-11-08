package net.aopacloud.superbi.queryEngine.sql.aggregator;

import com.google.common.base.Strings;

public class Summation implements Aggregator{
    @Override
    public String getFunction() {
        return "SUM({})";
    }


//    @Override
//    public String replaceIfFunction(String expression) {
//
//        String field = unfoldFunction("SUM", expression.trim());
//
//        return String.format("sumIf(%s,isFinite(toFloat64(%s)))", field, field);
//    }


}
