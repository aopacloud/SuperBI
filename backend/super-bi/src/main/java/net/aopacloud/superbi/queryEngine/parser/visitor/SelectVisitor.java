package net.aopacloud.superbi.queryEngine.parser.visitor;

import com.alibaba.druid.sql.ast.expr.SQLAggregateExpr;
import com.alibaba.druid.sql.ast.expr.SQLIdentifierExpr;
import com.alibaba.druid.sql.ast.expr.SQLPropertyExpr;
import com.alibaba.druid.sql.visitor.SQLASTVisitor;
import com.alibaba.druid.sql.visitor.SchemaStatVisitor;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jiangyihang
 */
public class SelectVisitor extends SchemaStatVisitor implements SQLASTVisitor {

    @Getter
    List<SQLAggregateExpr> aggregateExprs = new ArrayList<>();

    List<SQLIdentifierExpr> identifierExprs = new ArrayList<>();

    List<SQLPropertyExpr> sqlPropertyExprs = new ArrayList<>();

    @Override
    public boolean visit(SQLPropertyExpr x) {
        sqlPropertyExprs.add(x);
        return super.visit(x);
    }

    @Override
    public boolean visit(SQLIdentifierExpr x) {
        identifierExprs.add(x);
        return super.visit(x);
    }

    @Override
    public boolean visit(SQLAggregateExpr x) {
        aggregateExprs.add(x);
        return super.visit(x);
    }

    public List<String> getSelectColumn() {
        List<String> columnNames = new ArrayList<>();
        for (SQLIdentifierExpr identifierExpr : identifierExprs) {
            columnNames.add(identifierExpr.getName());
        }
        for (SQLPropertyExpr sqlPropertyExpr : sqlPropertyExprs) {
            columnNames.add(sqlPropertyExpr.getName());
        }
        return columnNames;
    }

}