package net.aopacloud.superbi.queryEngine.parser;

import lombok.Builder;
import lombok.Data;
import org.assertj.core.util.Sets;

import java.util.List;
import java.util.Set;

@Data
@Builder(toBuilder = true)
public class SqlColumns {

    private List<String> useColumns;

    private List<String> aggrColumns;

    private String sqlPart;

    public Set<String> groupByColumns() {
        Set<String> tmp = Sets.newHashSet(useColumns);
        tmp.removeAll(aggrColumns);
        return tmp;
    }
}
