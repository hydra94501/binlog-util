package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.example.document.ShortVideoDocument;
import org.example.service.Identifiable;
import org.springframework.beans.BeanUtils;

/**
 * <p>
 * 视频主表
 * </p>
 *
 * @author auto generate
 * @since 2023-11-21
 */
@ToString
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_short_video")
public class ShortVideo extends Model<ShortVideo> implements Identifiable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


    @TableField("tid")
    private Long tid;

    /**
     * 视频标题
     */
    @TableField("title")
    private String title;

    /**
     * 上传用户ID
     */
    @TableField("owner_id")
    private Long ownerId;

    /**
     * 作者昵称
     */
    @TableField("owner_nickname")
    private String ownerNickname;


    @TableField(exist = false)
    private String ownerMobile;

    /**
     * 作者头像url地址小
     */
    @TableField("owner_head_pic_small")
    private String ownerHeadPicSmall;

    /**
     * 作者头像url地址大
     */
    @TableField("owner_head_pic_big")
    private String ownerHeadPicBig;

    /**
     * 视频时长秒
     */
    @TableField("video_duration")
    private String videoDuration;

    /**
     * 视频原始地址
     */
    @TableField("ori_video_url")
    private String oriVideoUrl;

    /**
     * 切片状态WAITTING、CUTTING、SUCCESS、FAILED
     */
    @TableField("cut_status")
    private String cutStatus;

    /**
     * 封面图片地址
     */
    @TableField("cover_url")
    private String coverUrl;

    /**
     * 封面类型 USERUPLOAD、CUTFIRSTFRAME
     */
    @TableField("cover_type")
    private String coverType;

    @TableField("m3u8_content")
    private String m3u8Content;

    /**
     * 审核状态 WAITTING、SUCCESS、FAILED
     */
    @TableField("audit_status")
    private String auditStatus;

    /**
     * 显示状态 ON OFF
     */
    @TableField("show_satus")
    private String showSatus;

    /**
     * 视频来源 PUBLICSHARED、ADMINUPLOAD、USERUPLOAD
     */
    @TableField("video_from")
    private String videoFrom;

    /**
     * 公共视频库ID(若video_from 为 PUBLICSHARED)
     */
    @TableField("public_id")
    private Long publicId;


    /**
     * 公共租户id
     */
    @TableField("public_tid")
    private Long publicTid;

    /**
     * 公共平台id
     */
    @TableField("public_pid")
    private Long publicPid;

    /**
     * 被收藏数量
     */
    @TableField("favrit_counts")
    private Integer favritCounts;

    /**
     * 评论数量
     */
    @TableField("feedback_counts")
    private Integer feedbackCounts;

    /**
     * 点赞数量
     */
    @TableField("love_counts")
    private Integer loveCounts;

    /**
     * 观看数量
     */
    @TableField("view_counts")
    private Integer viewCounts;

    /**
     * 分享数量
     */
    @TableField("share_counts")
    private Integer shareCounts;

    /**
     * 视频标签
     */
    @TableField("labels")
    private String labels;

    /**
     * 视频简介
     */
    @TableField("summary")
    private String summary;

    /**
     * 是否为公共视频库(平台)  YES/NO
     */
    @TableField("is_public")
    private String isPublic;

    /**
     * 开启评论YESNO
     */
    @TableField("can_feedback")
    private String canFeedback;

    /**
     * 开启听视频模式YESNO
     */
    @TableField("can_audio")
    private String canAudio;

    /**
     * 开启弹幕
     */
    @TableField("can_bullet")
    private String canBullet;

    /**
     * 上传时间
     */
    private Long createTime;

    private Long auditTime;


    @TableField("audit_admin_user_id")
    private Long auditAdminUserId;


    @TableField("audit_admin_user_name")
    private String auditAdminUserName;

    @TableField("store_key")
    private String storeKey;

    /**
     * 播放码,经常更换，此码能唯一关联m3u8内容,可以缓存到redis
     */
    @TableField("play_key")
    private String playKey;


    /**
     * 官方视频标记YESNO
     */
    @TableField("is_office")
    private String isOffice;

    @TableField("remark")
    private String remark;

    @TableField("last_comment_id")
    private Long lastCommentId;

    private String labelIds;

    /**
     * 权重排序
     */
    @TableField("number")
    private Long number;

    private Integer width;

    private Integer height;

    /**
     * 是否 有删除申请
     */
    private Boolean haveDeleteRequest;

    private Long sizeBytes;


    public ShortVideoDocument toEsDocument(){
        ShortVideoDocument document =  new ShortVideoDocument();
        BeanUtils.copyProperties(this,document);
        // 时间戳转换为北京时间
        if(document.getCreateTime()!=null) {
            document.setCreateTime(document.getCreateTime() - 28800000L);
        }
        if(document.getAuditTime()!=null) {
            document.setAuditTime(document.getAuditTime() - 28800000L);
        }
        return document;
    }

    @Override
    public String index() {
        return "short_video_index";
    }
}
