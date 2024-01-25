package net.aopacloud.superbi.model.vo;

import net.aopacloud.superbi.model.entity.Folder;
import lombok.Data;
import org.springframework.beans.BeanUtils;


@Data
public class FolderVO extends Folder {

    private String absolutePath;

    public static FolderVO from(Folder folder , String absolutePath) {

        FolderVO vo = new FolderVO();
        BeanUtils.copyProperties(folder, vo);
        vo.setAbsolutePath(absolutePath);

        return vo;
    }

}
