package net.aopacloud.superbi.queryEngine.parser;

import lombok.Data;

@Data
public class CheckResult {

    private boolean pass;

    private boolean withAggregator;

    private String sql;

    private String error;

    private CheckResult(boolean pass, boolean withAggregator) {
        this.pass = pass;
        this.withAggregator = withAggregator;
    }

    public static CheckResult ofDefault(){
        return new CheckResult(false, false);
    }

    public void toPass() {
        this.pass = true;
    }

    public void fail() {
        this.pass = false;
    }


}
