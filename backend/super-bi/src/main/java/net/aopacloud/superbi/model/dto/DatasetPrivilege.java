package net.aopacloud.superbi.model.dto;

import lombok.Builder;
import lombok.experimental.Accessors;
import net.aopacloud.superbi.enums.PrivilegeTypeEnum;
import com.google.common.collect.Sets;
import lombok.Data;

import java.util.Set;

/**
 * @author: hudong
 * @date: 2023/9/11
 * @description:
 */
@Data
@Accessors(chain = true)
public class DatasetPrivilege {

    private boolean pass = Boolean.FALSE;

    private PrivilegeTypeEnum privilegeType;

    private DatasetDTO dataset;

    private Set<String> columns = Sets.newHashSet();

    private Set<String> rows = Sets.newHashSet();

    public void addColumns(Set<String> columns) {
        this.columns.addAll(columns);
    }

    public void addRows(Set<String> rows) {
        this.rows.addAll(rows);
    }
}
