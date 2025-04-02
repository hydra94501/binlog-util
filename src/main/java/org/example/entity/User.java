package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.math.BigDecimal;
import java.util.Date;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;
import org.example.service.Identifiable;


/**
 * 用户信息表
 * @author Alan_
 */
@Data
@Accessors(chain = true)
@TableName(value = "t_user")
@EqualsAndHashCode(callSuper = false)
public class User extends Model<User> implements Identifiable {
    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 平台用户ID
     */
    @TableField(value = "platform_user_id")
    private Long platformUserId;

    /**
     * 租户id
     */
    @TableField(value = "tid")
    private Long tid;

    @TableField(value = "platform_uid")
    private String platformUid;

    @TableField(value = "head_img_enc_big")
    private String headImgEncBig;

    @TableField(value = "head_img_enc_small")
    private String headImgEncSmall;

    /**
     * 昵称
     */
    @TableField(value = "nickname")
    private String nickname;

    /**
     * 用户名
     */
    @TableField(value = "username")
    private String username;

    /**
     * 微信开放平台unionid
     */
    @TableField(value = "wechat_union_id")
    private String wechatUnionId;

    /**
     * 微信昵称
     */
    @TableField(value = "wechat_nickname")
    private String wechatNickname;

    /**
     * 性别(0:未知;1:男;2:女)
     */
    @TableField(value = "sex")
    private Byte sex;

    /**
     * 密码
     */
    @TableField(value = "`password`")
    private String password;

    /**
     * 手机号码
     */
    @TableField(value = "mobile")
    private String mobile;

    /**
     * 类型
     */
    @TableField(value = "`type`")
    private Byte type;

    /**
     * 状态
     */
    @TableField(value = "`status`")
    private Byte status;

    /**
     * 最近登录时间
     */
    @TableField(value = "last_login_time")
    private Date lastLoginTime;

    /**
     * IP地址
     */
    @TableField(value = "register_ip")
    private String registerIp;

    /**
     * 手机的唯一设备ID
     */
    @TableField(value = "device_id")
    private String deviceId;

    /**
     * 手机型号
     */
    @TableField(value = "model")
    private String model;

    /**
     * 安卓推送标识
     */
    @TableField(value = "push_id")
    private String pushId;

    /**
     * 0-安卓，1-iOS
     */
    @TableField(value = "os_type")
    private Byte osType;

    /**
     * 注册来源
     */
    @TableField(value = "register_from")
    private String registerFrom;

    /**
     * 是否绑定手机，1-是，0-否
     */
    @TableField(value = "bind_mobile")
    private Byte bindMobile;

    /**
     * 真实姓名
     */
    @TableField(value = "real_name")
    private String realName;

    /**
     * 是否有相同的IP地址：1有；0:无
     */
    @TableField(value = "same_ip")
    private Byte sameIp;

    /**
     * 登录IP地址
     */
    @TableField(value = "login_ip")
    private String loginIp;

    /**
     * 用户组ID
     */
    @TableField(value = "user_group_id")
    private Long userGroupId;

    /**
     * 所属者
     */
    @TableField(value = "`owner`")
    private String owner;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 是否为视频作者YESNO
     */
    @TableField(value = "is_upload_author")
    private String isUploadAuthor;

    /**
     * 视频作品数量
     */
    @TableField(value = "video_count")
    private Integer videoCount;

    /**
     * 粉丝数量
     */
    @TableField(value = "fan_count")
    private Integer fanCount;

    /**
     * 关注作者数量
     */
    @TableField(value = "concern_count")
    private Integer concernCount;

    /**
     * 获赞数量
     */
    @TableField(value = "love_count")
    private Integer loveCount;

    @TableField(value = "platform_avatar_url")
    private String platformAvatarUrl;

    @TableField(value = "platform_avatar_url_big")
    private String platformAvatarUrlBig;

    @TableField(value = "platform_avatar_url_small")
    private String platformAvatarUrlSmall;

    @TableField(value = "avatar_url_enc_big")
    private String avatarUrlEncBig;

    @TableField(value = "avatar_url_enc")
    private String avatarUrlEnc;

    @TableField(value = "avatar_url_enc_small")
    private String avatarUrlEncSmall;

    /**
     * 角色，user、vip1、vip2、vip3、vip4、vip5
     */
    @TableField(value = "`role`")
    private String role;

    /**
     * VIP特权截止时间
     */
    @TableField(value = "vip_end_time")
    private Date vipEndTime;

    /**
     * 角色名称
     */
    @TableField(value = "role_name")
    private String roleName;

    /**
     * 是否主播
     */
    @TableField(value = "is_live_author")
    private String isLiveAuthor;

    /**
     * 是否开启弹幕YESNO
     */
    @TableField(value = "can_bullet")
    private String canBullet;

    /**
     * 后台添加的管理员id
     */
    @TableField(value = "register_from_admin_id")
    private Long registerFromAdminId;

    /**
     * 后台添加的管理员名
     */
    @TableField(value = "register_from_admin_name")
    private String registerFromAdminName;

    /**
     * 头像备份
     */
/*    @TableField(value = "head_bak")
    private String headBak;*/

    /**
     * 逻辑删除
     */
    @TableField(value = "deleted")
    private Byte deleted;

    /**
     * 是否蓝V认证
     */
    @TableField(value = "is_blue_vip")
    private Byte isBlueVip;

    /**
     * 蓝V认证名称
     */
    @TableField(value = "blue_vip_name")
    private String blueVipName;

    /**
     * 是否显示性别
     */
    @TableField(value = "show_sex")
    private Byte showSex;

    /**
     * 是否机器人
     */
    @TableField(value = "robot")
    private Byte robot;

    /**
     * 是否批量添加机器人
     */
    @TableField(value = "batch_robot")
    private Byte batchRobot;

    /**
     * 余额
     */
    @TableField(value = "balance")
    private BigDecimal balance;

    /**
     * 配置聊天室历史消息
     */
    @TableField(value = "chat_history")
    private Boolean chatHistory;

    /**
     * 是否有游戏挂载
     */
    @TableField(value = "is_game_author")
    private String isGameAuthor;

    /**
     * 累计获得积分
     */
    @TableField(value = "total_points")
    private BigDecimal totalPoints;

    /**
     * 已兑换积分
     */
    @TableField(value = "redeemed_points")
    private BigDecimal redeemedPoints;

    /**
     * 积分余额
     */
    @TableField(value = "points")
    private BigDecimal points;

    /**
     * 累计兑换现金（元）
     */
    @TableField(value = "total_cash")
    private BigDecimal totalCash;

    /**
     * 已提现现金（元）
     */
    @TableField(value = "withdraw_cash")
    private BigDecimal withdrawCash;

    /**
     * 现金余额（元）
     */
    @TableField(value = "cash")
    private BigDecimal cash;

    /**
     * 兑换方式（1：自动兑换 2：手动兑换）
     */
    @TableField(value = "exchange_type")
    private Integer exchangeType;

    @Override
    public String index() {
        return "user_index";
    }

    @Override
    public Identifiable toEsDocument() {
        return this;
    }
}