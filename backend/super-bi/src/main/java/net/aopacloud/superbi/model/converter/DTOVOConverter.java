package net.aopacloud.superbi.model.converter;

import java.util.List;

public interface DTOVOConverter<VO, DTO> {

    VO toVO(DTO dto);

    List<VO> toVOList(List<DTO> dtoList);

    DTO toDTO(VO vo);

    List<DTO> toDTOList(List<VO> voList);

}
