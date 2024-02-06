package net.aopacloud.superbi.model.converter;

import java.util.List;

public interface BaseConverter<VO, DTO, ENTITY> {

    VO toVO(DTO dto);

    List<VO> toVOList(List<DTO> dtoList);

    DTO toDTO(VO vo);

    List<DTO> toDTOList(List<VO> voList);

    DTO entityToDTO(ENTITY entity);

    List<DTO> entityToDTOList(List<ENTITY> entityList);

    ENTITY toEntity(DTO dto);

//    List<ENTITY> toEntityList(List<DTO> dtoList);
}
