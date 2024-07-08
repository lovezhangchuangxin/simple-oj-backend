package edu.hust.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`class_user`")
public class ClassUser {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer classId; // 班级id
    private Integer userId; // 用户id
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间
}
