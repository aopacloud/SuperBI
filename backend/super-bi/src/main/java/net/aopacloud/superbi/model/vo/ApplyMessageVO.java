package net.aopacloud.superbi.model.vo;

import lombok.Data;

@Data
public class ApplyMessageVO {

    private int applyingCount;

    private int reviewCount;

    private int operationCount;

    public int getTotal() {
        return applyingCount + reviewCount + operationCount;
    }

}
