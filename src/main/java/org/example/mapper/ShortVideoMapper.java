package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.example.entity.ShortVideo;
import org.example.entity.TableColVo;

import java.util.List;

public interface ShortVideoMapper extends BaseMapper<ShortVideo> {

    @Select("SELECT column_name,ordinal_position-1 as ordinalPosition FROM information_schema.columns WHERE table_schema = 'shortVideo' AND table_name=#{tableName}")
    List<TableColVo>  getTableSchemaByName(@Param("tableName")String tableName);


    @Select("SELECT \n" +
            "    id,\n" +
            "    tid,\n" +
            "    title,\n" +
            "    owner_id,\n" +
            "    owner_nickname,\n" +
            "    owner_head_pic_small,\n" +
            "    owner_head_pic_big,\n" +
            "    video_duration,\n" +
            "    ori_video_url,\n" +
            "    cut_status,\n" +
            "    cover_url,\n" +
            "    cover_type,\n" +
            "    m3u8_content,\n" +
            "    audit_status,\n" +
            "    show_satus,\n" +
            "    video_from,\n" +
            "    public_id,\n" +
            "    favrit_counts,\n" +
            "    feedback_counts,\n" +
            "    view_counts,\n" +
            "    share_counts,\n" +
            "    last_comment_id,\n" +
            "    love_counts,\n" +
            "    label_ids,\n" +
            "    labels,\n" +
            "    summary,\n" +
            "    can_feedback,\n" +
            "    can_audio,\n" +
            "    can_bullet,\n" +
            "    UNIX_TIMESTAMP(create_time) * 1000  as create_time,\n" +
            "    UNIX_TIMESTAMP(audit_time) * 1000   as audit_time,\n" +
            "    audit_admin_user_id,\n" +
            "    audit_admin_user_name,\n" +
            "    UNIX_TIMESTAMP(update_time) * 1000  as update_time,\n" +
            "    store_key,\n" +
            "    play_key,\n" +
            "    UNIX_TIMESTAMP(play_key_end_time) * 1000  as play_key_end_time,\n" +
            "    is_office,\n" +
            "    remark,\n" +
            "    is_public,\n" +
            "    size_bytes,\n" +
            "    have_delete_request,\n" +
            "    public_tid,\n" +
            "    public_pid,\n" +
            "    width,\n" +
            "    height,\n" +
            "    cut_server,\n" +
            "    number,\n" +
            "    UNIX_TIMESTAMP(publish_time) * 1000 as publish_time FROM t_short_video")
    List<ShortVideo> selectAll();
}
