package net.aopacloud.superbi.service.impl;

import lombok.RequiredArgsConstructor;
import net.aopacloud.superbi.common.core.context.LoginContextHolder;
import net.aopacloud.superbi.common.core.exception.ServiceException;
import net.aopacloud.superbi.i18n.LocaleMessages;
import net.aopacloud.superbi.i18n.MessageConsist;
import net.aopacloud.superbi.mapper.FavoriteMapper;
import net.aopacloud.superbi.model.converter.FavoriteConverter;
import net.aopacloud.superbi.model.dto.FavoriteDTO;
import net.aopacloud.superbi.model.entity.Favorite;
import net.aopacloud.superbi.service.FavoriteService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @Author shinnie
 * @Description
 * @Date 18:32 2023/9/13
 */
@Service
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService {

    private final FavoriteMapper favoriteMapper;

    private final FavoriteConverter favoriteConverter;

    @Override
    public List<FavoriteDTO> query(String position) {
        String userName = LoginContextHolder.getUsername();
        return favoriteConverter.entityToDTOList(favoriteMapper.selectByPosition(position, userName));
    }

    @Override
    public FavoriteDTO favorite(FavoriteDTO favoriteDTO) {
        Favorite favorite = favoriteConverter.toEntity(favoriteDTO);
        favorite.setUsername(LoginContextHolder.getUsername());
        favoriteMapper.insert(favorite);
        return favoriteDTO;
    }

    @Override
    public FavoriteDTO unFavorite(Integer id) {
        Favorite favorite = favoriteMapper.selectByPrimaryKey(id);
        if (favorite == null) {
            throw new ServiceException(LocaleMessages.getMessage(MessageConsist.UN_FAVORITE_ERROR));
        }
        favoriteMapper.deleteByPrimaryKey(id);
        return favoriteConverter.entityToDTO(favorite);
    }

    @Override
    public FavoriteDTO unFavorite(FavoriteDTO favoriteDTO) {
        String currentUser = LoginContextHolder.getUsername();

        Favorite favorite = favoriteConverter.toEntity(favoriteDTO);
        favorite.setUsername(currentUser);

        List<Favorite> favorites = favoriteMapper.selectByExample(favorite);

        favorites.stream().map(Favorite::getId).forEach(this::unFavorite);

        return favoriteDTO;
    }
}
