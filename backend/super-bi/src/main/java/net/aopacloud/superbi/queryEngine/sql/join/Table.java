package net.aopacloud.superbi.queryEngine.sql.join;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import lombok.Builder;
import lombok.Data;
import net.aopacloud.superbi.constant.BiConsist;
import net.aopacloud.superbi.model.domain.MetaConfigContent;
import net.aopacloud.superbi.model.dto.DatasetDTO;
import net.aopacloud.superbi.queryEngine.sql.AbstractSqlAssembler;
import net.aopacloud.superbi.queryEngine.sql.Segment;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Builder(toBuilder = true)
@Data
public class Table {

    private MetaConfigContent configContent;

    private String table;

    private List<Segment> pushDownFilters;

    private AbstractSqlAssembler sqlAssembler;

    private DatasetDTO dataset;


    public String produce() {

        if (!Strings.isNullOrEmpty(table)) {
           return table;
        }

        if (configContent.isSingleTable()) {
            return configContent.getTables().get(0).getTableIdentifier();
        }

        TableJoinSQLGenerator tableJoinSQLGenerator = new TableJoinSQLGenerator(configContent, dataset, sqlAssembler, pushDownFilters);
        return String.format("(%s) %s", tableJoinSQLGenerator.produceTableSql(), BiConsist.JOIN_TABLE_ALIAS);
    }

    public Table clone(){
        TableBuilder builder = Table.builder()
                .configContent(configContent)
                .table(table)
                .sqlAssembler(sqlAssembler)
                .dataset(dataset);
        if (Objects.nonNull(pushDownFilters)) {
            builder.pushDownFilters(pushDownFilters);
        }
        return builder.build();
    }

    public void replacePushDownFilter(Segment filter) {
        if (Objects.isNull(pushDownFilters)) {
            pushDownFilters = Lists.newArrayList();
        }
        List<Segment> replacedFilters = pushDownFilters.stream().filter(item -> !item.getName().equals(filter.getName()))
                .collect(Collectors.toList());

        replacedFilters.add(filter);

        pushDownFilters = replacedFilters;
    }


}
