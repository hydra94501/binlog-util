package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.example.service.Identifiable;

/**
 * 评论回复
 * @author Alan_
 */
@Data
@Accessors(chain = true)
@TableName(value = "t_comment_reply")
@EqualsAndHashCode(callSuper = false)
public class CommentReply extends Model<CommentReply> implements Identifiable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 评论ID
     */
    @TableField(value = "comment_id")
    private Long commentId;

    @TableField(value = "video_id")
    private Long videoId;

    @TableField(value = "comment_owner_id")
    private Long commentOwnerId;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "reply_text")
    private String replyText;

    /**
     * 多个图片使用英文逗号分隔
     */
    @TableField(value = "reply_pic")
    private String replyPic;

    /**
     * 本回复收到的赞的数量
     */
    @TableField(value = "love_count")
    private Integer loveCount;

    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 被回复的id
     */
    @TableField(value = "to_reply_id")
    private Long toReplyId;

    /**
     * 被回复的用户id
     */
    @TableField(value = "to_user_id")
    private Long toUserId;

    /**
     * 被回复的用户昵称
     */
    @TableField(value = "to_user_nickname")
    private String toUserNickname;

    /**
     * 操作的管理员平台id
     */
    @TableField(value = "admin_tid")
    private Long adminTid;

    /**
     * 操作的管理员用户id
     */
    @TableField(value = "admin_uid")
    private Long adminUid;

    @TableField(value = "spam_key")
    private Long spamKey;

    @Override
    public String index() {
        return "comment_reply_index";
    }

    @Override
    public Identifiable toEsDocument() {
        return this;
    }
}