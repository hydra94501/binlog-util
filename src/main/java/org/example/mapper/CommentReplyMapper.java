package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.example.entity.CommentReply;

import java.util.List;

/**
 * @author Alan_   2025/3/26 14:16
 */
public interface CommentReplyMapper extends BaseMapper<CommentReply> {
    @Select(" SELECT \n" +
            "    id,\n" +
            "    comment_id,\n" +
            "    video_id,\n" +
            "    comment_owner_id,\n" +
            "    user_id,\n" +
            "    reply_text,\n" +
            "    reply_pic,\n" +
            "    love_count,\n" +
            "    create_time,\n" +
            "    to_reply_id,\n" +
            "    to_user_id,\n" +
            "    to_user_nickname,\n" +
            "    admin_tid,\n" +
            "    admin_uid,\n" +
            "    spam_key\n" +
            "FROM t_comment_reply")
    List<CommentReply> selectAll();
}
