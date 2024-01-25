package net.aopacloud.superbi.controller;

import net.aopacloud.superbi.common.core.web.domain.RestApiResponse;
import net.aopacloud.superbi.model.converter.FavoriteConverter;
import net.aopacloud.superbi.model.dto.FavoriteDTO;
import net.aopacloud.superbi.model.vo.FavoriteVO;
import net.aopacloud.superbi.service.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Favorite
 * @Author shinnie
 * @Description
 * @Date 18:32 2023/9/13
 */
@RestController
@RequestMapping("favorite")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    private final FavoriteConverter favoriteConverter;

    /**
     * query favorite list
     *
     * @param position
     * @return
     */
    @GetMapping
    public RestApiResponse<List<FavoriteVO>> query(String position) {
        List<FavoriteDTO> query = favoriteService.query(position);
        return RestApiResponse.success(favoriteConverter.toVOList(query));
    }

    /**
     * favorite
     *
     * @param favoriteVO
     * @return
     */
    @PostMapping
    public RestApiResponse<FavoriteVO> favorite(@RequestBody FavoriteVO favoriteVO) {
        FavoriteDTO favorite = favoriteService.favorite(favoriteConverter.toDTO(favoriteVO));
        return RestApiResponse.success(favoriteConverter.toVO(favorite));
    }

    /**
     * cancel favorite
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    @Deprecated
    public RestApiResponse<FavoriteVO> unFavorite(@PathVariable Integer id) {

        FavoriteDTO favoriteDTO = favoriteService.unFavorite(id);
        return RestApiResponse.success(favoriteConverter.toVO(favoriteDTO));
    }

    /**
     * cancel favorite
     *
     * @param favoriteVO
     * @return
     */
    @DeleteMapping("/cancel")
    public RestApiResponse<FavoriteVO> cancel(@RequestBody FavoriteVO favoriteVO) {
        FavoriteDTO favoriteDTO = favoriteService.unFavorite(favoriteConverter.toDTO(favoriteVO));
        return RestApiResponse.success(favoriteConverter.toVO(favoriteDTO));
    }

}
