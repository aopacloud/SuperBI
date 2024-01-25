package net.aopacloud.superbi.common.core.web.page;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * @author: hu.dong
 * @date: 2021/10/20
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
@Accessors
public class PageVO<T> {

    private List<T> data;

    private int pageNum;

    private int pageSize;

    private long total;

    public PageVO(List<T> data, long total) {
        this.data = data;
        this.pageNum = 1;
        this.pageSize = data.size();
        this.total = total;
    }

}
