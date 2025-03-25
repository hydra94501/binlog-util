package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Select;
import org.example.entity.Label;
import org.example.entity.ShortVideo;

import java.util.List;

public interface LabelMapper extends BaseMapper<Label> {

    @Select(" SELECT \n" +
            "    id,\n" +
            "    tid,\n" +
            "    label,\n" +
            "    number,\n" +
            "    is_public,\n" +
            "    UNIX_TIMESTAMP(create_time)*1000 as create_time,\n" +
            "    view_count,\n" +
            "    video_count,\n" +
            "    love_count,\n" +
            "    fav_count,\n" +
            "    share_count\n" +
            "FROM t_label")
    List<Label> selectAll();
}
