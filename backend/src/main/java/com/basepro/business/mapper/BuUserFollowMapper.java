package com.basepro.business.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.basepro.business.entity.BuUserFollow;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;

public interface BuUserFollowMapper extends BaseMapper<BuUserFollow> {

    @Delete("DELETE FROM bu_user_follow WHERE id = #{id}")
    void physicalDeleteById(@Param("id") Long id);

    @Delete("DELETE FROM bu_user_follow WHERE follower_id = #{followerId} AND followee_id = #{followeeId}")
    void physicalDeleteByPair(@Param("followerId") Long followerId, @Param("followeeId") Long followeeId);
}
