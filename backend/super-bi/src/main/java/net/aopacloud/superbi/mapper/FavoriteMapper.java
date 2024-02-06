package net.aopacloud.superbi.mapper;

import net.aopacloud.superbi.model.entity.Favorite;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface FavoriteMapper {

    Favorite selectByPrimaryKey(Integer id);

    List<Favorite> selectByPosition(@Param("position") String position, @Param("userName") String userName);

    int deleteByPrimaryKey(Integer id);

    int insert(Favorite row);

    int update(Favorite row);

    List<Favorite> selectByExample(Favorite favorite);
}