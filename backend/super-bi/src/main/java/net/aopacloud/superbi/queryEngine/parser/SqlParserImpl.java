package net.aopacloud.superbi.queryEngine.parser;

import com.alibaba.druid.DbType;
import com.alibaba.druid.sql.SQLUtils;
import com.alibaba.druid.sql.ast.SQLExpr;
import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.parser.ParserException;
import com.alibaba.druid.util.JdbcConstants;
import lombok.extern.slf4j.Slf4j;
import net.aopacloud.superbi.queryEngine.parser.visitor.SelectVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
public class SqlParserImpl implements SqlParser {
    @Override
    public SqlColumns parseSqlColumns(String sql) {

        SQLExpr sqlExpr = SQLUtils.toSQLExpr(sql, DbType.clickhouse);

        SelectVisitor visitor = new SelectVisitor();
        sqlExpr.accept(visitor);

        List<String> aggColumnNames = new ArrayList<>();
        for (SQLAggregateExpr expr : visitor.getAggregateExprs()) {
            parseAggColumnNames(expr, aggColumnNames);
        }

        return SqlColumns.builder()
                .aggrColumns(aggColumnNames.stream().distinct().collect(Collectors.toList()))
                .useColumns(visitor.getSelectColumn().stream().distinct().collect(Collectors.toList()))
                .sqlPart(sql)
                .build();
    }

    @Override
    public CheckResult checkSql(String sql) {

        CheckResult result = CheckResult.ofDefault();
        try {
            SQLUtils.parseStatements(sql, JdbcConstants.CLICKHOUSE);
            result.toPass();
        } catch (ParserException e) {
            result.setError(e.getMessage());
            log.error(" parse sql {} error", sql, e);
        }

        return result;
    }

    private void parseAggColumnNames(SQLAggregateExpr sqlAggregateExpr, List<String> columnNames) {

        SelectVisitor tmp = new SelectVisitor();
        sqlAggregateExpr.accept(tmp);
        columnNames.addAll(tmp.getSelectColumn());
        for (SQLAggregateExpr expr : tmp.getAggregateExprs()
                .stream()
                .filter(x -> x != sqlAggregateExpr)
                .collect(Collectors.toList())) {
            parseAggColumnNames(expr, columnNames);
        }
    }
}
