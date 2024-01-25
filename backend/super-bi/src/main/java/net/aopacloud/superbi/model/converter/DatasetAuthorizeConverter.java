package net.aopacloud.superbi.model.converter;

import net.aopacloud.superbi.model.dto.DatasetAuthorizeDTO;
import net.aopacloud.superbi.model.entity.DatasetAuthorize;
import net.aopacloud.superbi.model.vo.DatasetAuthorizeVO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(unmappedTargetPolicy = ReportingPolicy.IGNORE, componentModel = "spring")
public interface DatasetAuthorizeConverter{

    @Mapping(target = "rows", expression = "java(net.aopacloud.superbi.util.JSONUtils.parseObject(dto.getRowParam(), DatasetAuthorizeVO.Rows.class))")
    DatasetAuthorizeVO toVO(DatasetAuthorizeDTO dto);

    List<DatasetAuthorizeVO> toVOList(List<DatasetAuthorizeDTO> dtoList);

    @Mapping(target = "rowParam", expression = "java(net.aopacloud.superbi.util.JSONUtils.toJsonString(vo.getRows()))")
    DatasetAuthorizeDTO toDTO(DatasetAuthorizeVO vo);

    List<DatasetAuthorizeDTO> toDTOList(List<DatasetAuthorizeVO> voList);

    DatasetAuthorizeDTO entityToDTO(DatasetAuthorize entity);

    List<DatasetAuthorizeDTO> entityToDTOList(List<DatasetAuthorize> entityList);

    DatasetAuthorize toEntity(DatasetAuthorizeDTO dto);

}
