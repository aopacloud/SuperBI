package net.aopacloud.superbi.model.query;

import net.aopacloud.superbi.common.core.context.SecurityContextHolder;
import lombok.Data;

import java.util.Set;

/**
 * @author: hu.dong
 * @date: 2022/1/10
 **/
@Data
public class ReportQuery extends BaseQuery {

    private String creator;

    private int hasPermission;

    private int favorite;

    private Set<Long> datasetIds;

    private String username = SecurityContextHolder.getUserName();
}
