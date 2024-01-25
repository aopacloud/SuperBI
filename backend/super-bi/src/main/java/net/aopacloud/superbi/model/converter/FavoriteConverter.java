package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.FavoriteDTO;
import net.aopacloud.superbi.model.entity.Favorite;
import net.aopacloud.superbi.model.vo.FavoriteVO;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * @Author shinnie
 * @Description
 * @Date 12:16 2023/9/13
 */
@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")

public interface FavoriteConverter extends BaseConverter<FavoriteVO, FavoriteDTO, Favorite> {
}
