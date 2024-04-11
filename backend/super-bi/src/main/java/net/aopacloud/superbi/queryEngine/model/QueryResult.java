package net.aopacloud.superbi.queryEngine.model;

import com.google.common.collect.Lists;
import lombok.Data;
import net.aopacloud.superbi.enums.QueryStatusEnum;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/8/23
 * @description:
 */
@Data
public class QueryResult {

    private QueryStatusEnum status = QueryStatusEnum.FAILED;

    private List<Object[]> rows = Lists.newArrayList();

    private List<String> fieldNames = Lists.newArrayList();

    private List<QueryColumn> columns = Lists.newArrayList();

    private List<Object[]> summaryRows = Lists.newArrayList();

    private String sql;

    private String errorLog;

    private long elapsed;

    private String queryId;

    public void add(Object[] row) {
        rows.add(row);
    }

    public int getTotal() {
        return rows.size();
    }

    public boolean isEmpty() {
        return rows.isEmpty();
    }

    public static QueryResult noPrivilege() {
        QueryResult result = new QueryResult();
        result.setStatus(QueryStatusEnum.NO_PRIVILEGE);
        return result;
    }
}
