package org.example.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.example.entity.Comment;
import org.example.pojo.vo.CommentVideoVo;

import java.util.List;

/**
 * @author Alan_   2025/3/26 13:14
 */
public interface CommentMapper extends BaseMapper<Comment> {

    IPage<CommentVideoVo> selectAll(Page<CommentVideoVo> page);


}
