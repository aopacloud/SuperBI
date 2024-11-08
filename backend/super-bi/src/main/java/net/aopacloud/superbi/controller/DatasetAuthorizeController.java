package net.aopacloud.superbi.controller;

import com.github.pagehelper.PageInfo;
import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.utils.PageUtils;
import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.common.core.web.page.PageVO;
import net.aopacloud.superbi.model.converter.DatasetAuthorizeConverter;
import net.aopacloud.superbi.model.dto.DatasetAuthorizeDTO;
import net.aopacloud.superbi.model.dto.SysUserDTO;
import net.aopacloud.superbi.model.query.DatasetAuthorizeQuery;
import net.aopacloud.superbi.model.query.DatasetAuthorizeSaveVO;
import net.aopacloud.superbi.model.vo.DatasetAuthorizeVO;
import net.aopacloud.superbi.service.DatasetAuthorizeService;
import net.aopacloud.superbi.service.SysUserService;
import org.assertj.core.util.Strings;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Dataset Authorize
 *
 * @author: hudong
 * @date: 2023/9/6
 * @description:
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("datasetAuthorize")
public class DatasetAuthorizeController {

    private final DatasetAuthorizeService datasetAuthorizeService;

    private final DatasetAuthorizeConverter converter;

    private final SysUserService sysUserService;

    /**
     * get dataset authorized user list
     *
     * @param query
     * @return
     */
    @GetMapping("/user")
    public RestApiResponse<PageVO<DatasetAuthorizeVO>> searchUserAuthorize(DatasetAuthorizeQuery query) {

        if (!Strings.isNullOrEmpty(query.getKeyword())) {
            List<SysUserDTO> sysUserDTOS = sysUserService.filter(query.getKeyword());
            if (!sysUserDTOS.isEmpty()) {
                query.setSearchUsers(sysUserDTOS.stream().map(SysUserDTO::getUsername).collect(Collectors.toList()));
            }
        }

        PageUtils.startPage();
        List<DatasetAuthorizeDTO> authorizes = datasetAuthorizeService.searchUserAuthorize(query);
        PageInfo pageInfo = new PageInfo(authorizes);
        PageVO pageVO = new PageVO(converter.toVOList(authorizes), pageInfo.getTotal());

        return RestApiResponse.success(pageVO);
    }

    /**
     * get dataset authorized role list
     *
     * @param query
     * @return
     */
    @GetMapping("/role")
    public RestApiResponse<PageVO<DatasetAuthorizeVO>> searchRoleAuthorize(DatasetAuthorizeQuery query) {

        PageUtils.startPage();
        List<DatasetAuthorizeDTO> authorizes = datasetAuthorizeService.searchRoleAuthorize(query);
        PageInfo pageInfo = new PageInfo(authorizes);
        PageVO pageVO = new PageVO(converter.toVOList(authorizes), pageInfo.getTotal());

        return RestApiResponse.success(pageVO);
    }

    /**
     * save dataset authorize
     *
     * @param datasetAuthorizeVO
     * @return
     */
    @PostMapping
    public RestApiResponse<List<DatasetAuthorizeVO>> newAuthorize(@RequestBody DatasetAuthorizeSaveVO datasetAuthorizeVO) {

        List<DatasetAuthorizeDTO> datasetAuthorizeDTOS = datasetAuthorizeService.save(datasetAuthorizeVO);

        return RestApiResponse.success(converter.toVOList(datasetAuthorizeDTOS));
    }

    /**
     * update dataset authorize
     *
     * @param datasetAuthorizeVO
     * @param id                 authorize id
     * @return
     */
    @PutMapping("/{id}")
    public RestApiResponse<DatasetAuthorizeVO> updateAuthorize(@RequestBody DatasetAuthorizeVO datasetAuthorizeVO, @PathVariable Long id) {
        datasetAuthorizeVO.setId(id);
        DatasetAuthorizeDTO datasetAuthorizeDTO = datasetAuthorizeService.update(converter.toDTO(datasetAuthorizeVO));

        return RestApiResponse.success(converter.toVO(datasetAuthorizeDTO));
    }

    /**
     * update dataset authorize permission
     *
     * @param datasetAuthorizeVO
     * @param id
     * @return
     */
    @PutMapping("/{id}/permission")
    public RestApiResponse<DatasetAuthorizeVO> updatePermission(@RequestBody DatasetAuthorizeVO datasetAuthorizeVO, @PathVariable Long id) {
        datasetAuthorizeVO.setId(id);
        DatasetAuthorizeDTO datasetAuthorizeDTO = datasetAuthorizeService.updatePermission(converter.toDTO(datasetAuthorizeVO));

        return RestApiResponse.success(converter.toVO(datasetAuthorizeDTO));
    }

    /**
     * delete dataset authorize
     *
     * @param id authorize id
     * @return deleted dataset authorize
     */
    @DeleteMapping("/{id}")
    public RestApiResponse<DatasetAuthorizeVO> deleteAuthorize(@PathVariable Long id) {

        DatasetAuthorizeDTO datasetAuthorizeDTO = datasetAuthorizeService.delete(id);

        return RestApiResponse.success(converter.toVO(datasetAuthorizeDTO));
    }

    /**
     * renew dataset authorize
     *
     * @param datasetAuthorizeVO
     * @return
     */
    @PutMapping("/{id}/renewed")
    public RestApiResponse<DatasetAuthorizeVO> renewedAuthorize(@RequestBody DatasetAuthorizeVO datasetAuthorizeVO, @PathVariable Long id) {
        datasetAuthorizeVO.setId(id);
        DatasetAuthorizeDTO datasetAuthorizeDTO = datasetAuthorizeService.renewed(converter.toDTO(datasetAuthorizeVO));

        return RestApiResponse.success(converter.toVO(datasetAuthorizeDTO));
    }

    /**
     * get specified dataset authorize
     *
     * @param id authorize id
     * @return
     */
    @GetMapping("/{id}")
    public RestApiResponse<DatasetAuthorizeVO> findOne(@PathVariable Long id) {
        DatasetAuthorizeDTO datasetAuthorizeDTO = datasetAuthorizeService.findOne(id);
        return RestApiResponse.success(converter.toVO(datasetAuthorizeDTO));
    }

    /**
     * get latest dataset authorize by dataset id and username
     *
     * @param datasetId
     * @param username
     * @return
     */
    @GetMapping("/lastOne")
    public RestApiResponse<DatasetAuthorizeVO> findLastOne(Long datasetId, String username) {
        DatasetAuthorizeDTO datasetAuthorizeDTO = datasetAuthorizeService.findLastOne(datasetId, username);
        return Optional.ofNullable(datasetAuthorizeDTO).map(authorize -> RestApiResponse.success(converter.toVO(authorize))).orElse(RestApiResponse.success());
    }
}
