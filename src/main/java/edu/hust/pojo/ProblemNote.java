package edu.hust.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`problem_note`")
public class ProblemNote {
    @TableId(type = IdType.AUTO)
    private Integer id; // 题解id
    private String title; // 题解标题
    private Integer problemId; // 题目id
    private Integer authorId; // 作者id
    private Byte status; // 状态 0 待审核 1 通过 2 未通过
    private Integer collection; // 收藏数
    @TableField(exist = false)
    private String content; // 题解内容
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间
}
