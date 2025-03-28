package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.example.entity.Comment;
import org.example.entity.Label;
import org.example.pojo.vo.CommentVideoVo;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alan_   2025/3/26 13:14
 */
public interface CommentMapper extends BaseMapper<Comment> {


    List<CommentVideoVo> selectAll();



}
