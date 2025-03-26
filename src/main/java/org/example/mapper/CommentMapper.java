package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.example.entity.Comment;
import org.example.entity.Label;

import java.util.List;

/**
 * @author Alan_   2025/3/26 13:14
 */
public interface CommentMapper extends BaseMapper<Comment> {

    @Select(" SELECT \n" +
            "    id,\n" +
            "    user_id,\n" +
            "    video_id,\n" +
            "    video_owner_id,\n" +
            "    comment_text,\n" +
            "    comment_pic,\n" +
            "    love_count,\n" +
            "    reply_count,\n" +
            "    create_time,\n" +
            "    is_top,\n" +
            "    sortid,\n" +
            "    spam_key,\n" +
            "    tid,\n" +
            "    is_delete,\n" +
            "    admin_tid,\n" +
            "    admin_uid\n" +
            "FROM t_comment")
    List<Comment> selectAll();
}
