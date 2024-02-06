package net.aopacloud.superbi.service;

import net.aopacloud.superbi.model.dto.FavoriteDTO;

import java.util.List;

/**
 * @Author shinnie
 * @Description
 * @Date 18:32 2023/9/13
 */
public interface FavoriteService {

    List<FavoriteDTO> query(String position);

    FavoriteDTO favorite(FavoriteDTO favoriteDTO);

    FavoriteDTO unFavorite(Integer id);

    FavoriteDTO unFavorite(FavoriteDTO favoriteDTO);


}
