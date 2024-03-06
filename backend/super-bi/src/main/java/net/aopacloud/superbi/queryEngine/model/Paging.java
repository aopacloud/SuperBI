package net.aopacloud.superbi.queryEngine.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.aopacloud.superbi.constant.BiConsist;

/**
 * @author: hudong
 * @date: 2023/8/14
 * @description:
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Paging {

    private Long limit;

    private Long offset;

    public boolean hasOffset() {
        return offset != null && offset.equals(0L);
    }

    public boolean hasLimit() {
        return limit != null && !limit.equals(0L);
    }

    public Paging(Long limit) {
        this.limit = limit;
    }

    public static Paging getDefault() {
        return new Paging(BiConsist.DEFAULT_QUERY_NUM);
    }

}