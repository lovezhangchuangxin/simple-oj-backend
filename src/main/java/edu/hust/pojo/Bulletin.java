package edu.hust.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`bulletin`")
public class Bulletin {
    @TableId(type = IdType.AUTO)
    private Integer id; // 公告id
    private String title; // 公告标题
    @TableField(exist = false)
    private String content; // 公告内容
    private Integer authorId; // 作者id
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间
}
