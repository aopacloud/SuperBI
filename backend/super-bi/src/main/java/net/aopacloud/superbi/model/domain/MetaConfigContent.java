package net.aopacloud.superbi.model.domain;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import lombok.Data;
import org.assertj.core.util.Strings;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Data
public class MetaConfigContent {

    private List<TableDescriptor> tables;

    private List<TableJoinDescriptor> joinDescriptors;

    public String getRefTables() {

        List<String> tables = this.tables.stream().map(TableDescriptor::getTableIdentifier).collect(Collectors.toList());
        return Joiner.on(", ").join(tables);
    }

    public boolean isSingleTable() {
        return this.tables.size() == 1;
    }

    public Set<String> getAllJoinColumnsByAlias(String tableAlias) {
        if (Strings.isNullOrEmpty(tableAlias)) {
            return Sets.newHashSet();
        }
        return joinDescriptors.stream().flatMap(join -> join.getJoinFieldByAlias(tableAlias).stream()).collect(Collectors.toSet());
    }
}
