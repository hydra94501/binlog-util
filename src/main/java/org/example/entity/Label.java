package org.example.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.example.service.Identifiable;

/**
 * <p>
 * 视频标签表
 * </p>
 *
 * @author auto generate
 * @since 2023-11-21
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@TableName("t_label")
public class Label extends Model<Label> implements Identifiable {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @TableField("tid")
    private Long tid;

    /**
     * 标签名称
     */
    @TableField("label")
    private String label;

    /**
     * 排序
     */
    @TableField("number")
    private Long number;

    @TableField("create_time")
    private Long createTime;
    /**
     * 公共标记YESNO
     */
    @TableField("is_public")
    private String isPublic;

    private Long viewCount;
    private Integer videoCount;
    private Long loveCount;
    private Long favCount;
    private Long shareCount;

    @Override
    public String index() {
        return "label_index";
    }

    @Override
    public Identifiable toEsDocument() {
        return this;
    }

}
