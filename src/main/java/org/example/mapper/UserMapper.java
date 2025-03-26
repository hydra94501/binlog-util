package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.example.entity.Comment;
import org.example.entity.User;

import java.util.List;

/**
 * @author Alan_   2025/3/26 13:14
 */
public interface UserMapper extends BaseMapper<User> {

    @Select(" SELECT \n" +
            "    platform_user_id,\n" +
            "    id,\n" +
            "    tid,\n" +
            "    platform_uid,\n" +
            "    head_img_enc_big,\n" +
            "    head_img_enc_small,\n" +
            "    nickname,\n" +
            "    username,\n" +
            "    wechat_union_id,\n" +
            "    wechat_nickname,\n" +
            "    sex,\n" +
            "    password,\n" +
            "    mobile,\n" +
            "    type,\n" +
            "    status,\n" +
            "    last_login_time,\n" +
            "    register_ip,\n" +
            "    device_id,\n" +
            "    model,\n" +
            "    push_id,\n" +
            "    os_type,\n" +
            "    register_from,\n" +
            "    bind_mobile,\n" +
            "    real_name,\n" +
            "    same_ip,\n" +
            "    login_ip,\n" +
            "    user_group_id,\n" +
            "    owner,\n" +
            "    remark,\n" +
            "    create_time,\n" +
            "    update_time,\n" +
            "    is_upload_author,\n" +
            "    video_count,\n" +
            "    fan_count,\n" +
            "    concern_count,\n" +
            "    love_count,\n" +
            "    platform_avatar_url,\n" +
            "    platform_avatar_url_big,\n" +
            "    platform_avatar_url_small,\n" +
            "    avatar_url_enc_big,\n" +
            "    avatar_url_enc,\n" +
            "    avatar_url_enc_small,\n" +
            "    role,\n" +
            "    vip_end_time,\n" +
            "    role_name,\n" +
            "    is_live_author,\n" +
            "    can_bullet,\n" +
            "    register_from_admin_id,\n" +
            "    register_from_admin_name,\n" +
            "    head_bak,\n" +
            "    deleted,\n" +
            "    is_blue_vip,\n" +
            "    blue_vip_name,\n" +
            "    show_sex,\n" +
            "    robot,\n" +
            "    batch_robot,\n" +
            "    balance,\n" +
            "    chat_history,\n" +
            "    is_game_author,\n" +
            "    total_points,\n" +
            "    redeemed_points,\n" +
            "    points,\n" +
            "    total_cash,\n" +
            "    withdraw_cash,\n" +
            "    cash,\n" +
            "    exchange_type\n" +
            "FROM t_user")
    List<User> selectAll();
}
