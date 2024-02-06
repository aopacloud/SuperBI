package net.aopacloud.superbi.model.dto;

import lombok.Data;

import java.util.Objects;

/**
 * @author: hudong
 * @date: 2023/9/11
 * @description:
 */
@Data
public class QueryPrivilege {

    private boolean pass;

    private String rowPrivileges;

    public boolean hasRowPrivilege() {
        return Objects.nonNull(rowPrivileges) && !rowPrivileges.trim().isEmpty();
    }

    public void deny() {
        this.pass = Boolean.FALSE;
    }

    public void pass() {
        this.pass = Boolean.TRUE;
    }


}
