package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.FolderDTO;
import net.aopacloud.superbi.model.entity.Folder;
import net.aopacloud.superbi.model.vo.FolderVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Author shinnie
 * @Description
 * @Date 16:26 2023/8/10
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface FolderConverter extends BaseConverter<FolderVO, FolderDTO, Folder> {


}
