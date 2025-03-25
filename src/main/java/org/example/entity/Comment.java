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
import org.example.service.Identifiable;


/**
 * 视频评论表
 * @author Alan_   2025/3/25 13:00
 */
@Data
@Accessors(chain = true)
@TableName(value = "t_comment")
@EqualsAndHashCode(callSuper = false)
public class Comment extends Model<Comment> implements Identifiable {
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField(value = "user_id")
    private Long userId;

    @TableField(value = "video_id")
    private Long videoId;

    @TableField(value = "video_owner_id")
    private Long videoOwnerId;

    /**
     * 评论文本内容
     */
    @TableField(value = "comment_text")
    private String commentText;

    /**
     * 多个图片使用英文逗号分隔
     */
    @TableField(value = "comment_pic")
    private String commentPic;

    /**
     * 评论收到的赞的数量
     */
    @TableField(value = "love_count")
    private Integer loveCount;

    /**
     * 评论收到的回复数量
     */
    @TableField(value = "reply_count")
    private Integer replyCount;

    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 是否置顶YESNO
     */
    @TableField(value = "is_top")
    private String isTop;

    /**
     * 排序ID
     */
    @TableField(value = "sortid")
    private Integer sortid;

    /**
     * userId+content哈希值
     */
    @TableField(value = "spam_key")
    private Long spamKey;

    /**
     * 租户id
     */
    @TableField(value = "tid")
    private Long tid;

    @TableField(value = "is_delete")
    private Byte isDelete;

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

    @Override
    public String index() {
        return "comment_index";
    }

    @Override
    public Identifiable toEsDocument() {
        return this;
    }
}