package edu.hust.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`class`")
public class Class {
    @TableId(type = IdType.AUTO)
    private Integer id; // 班级id
    private String name; // 班级名
    private String description; // 班级简介
    private Integer creatorId; // 创建者id
    private Integer parentId; // 父班级id
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间
}
