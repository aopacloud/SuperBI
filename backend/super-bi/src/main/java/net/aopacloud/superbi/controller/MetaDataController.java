package net.aopacloud.superbi.controller;

import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.converter.DatabaseConverter;
import net.aopacloud.superbi.model.converter.TableConverter;
import net.aopacloud.superbi.model.dto.DatabaseDTO;
import net.aopacloud.superbi.model.dto.TableSchemaDTO;
import net.aopacloud.superbi.model.query.DatabaseQuery;
import net.aopacloud.superbi.model.vo.DatabaseVO;
import net.aopacloud.superbi.model.vo.TableSchemaVO;
import net.aopacloud.superbi.service.MetaDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author: hudong
 * @date: 2023/10/23
 * @description:
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("metaData")
public class MetaDataController {

    private final MetaDataService metaDataService;

    private final DatabaseConverter databaseConverter;

    private final TableConverter tableConverter;

    /**
     * get all database
     * @param query
     * @return
     */
    @GetMapping("/database")
    public RestApiResponse<List<DatabaseVO>> listAllDb(DatabaseQuery query) {
        List<DatabaseDTO> databaseDTOS = metaDataService.listAllDatabase(query.getWorkspaceId());
        return RestApiResponse.success(databaseConverter.toVOList(databaseDTOS));
    }


    /**
     * list all table by specified database
     * @return
     */
    @GetMapping("/table")
    public RestApiResponse<List<TableSchemaVO>> listAllTable(DatabaseVO databaseVO) {
        List<TableSchemaDTO> tableSchemaDTOS = metaDataService.listAllTable(databaseConverter.toDTO(databaseVO));
        return RestApiResponse.success(tableConverter.toVOList(tableSchemaDTOS));
    }


    /**
     * get table fields
     * @param tableSchemaVO
     * @return
     */
    @GetMapping("/table/detail")
    public RestApiResponse<TableSchemaVO> getTableField(TableSchemaVO tableSchemaVO) {
        TableSchemaDTO tableSchemaDTO = metaDataService.getTableSchema(tableConverter.toDTO(tableSchemaVO));
        return RestApiResponse.success(tableConverter.toVO(tableSchemaDTO));
    }

}
