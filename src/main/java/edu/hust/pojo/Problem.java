package edu.hust.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@TableName("`problem`")
public class Problem {
    @TableId(type = IdType.AUTO)
    private Integer id; // 题目id
    private Integer authorId; // 作者id
    private String title; // 题目标题
    private String tag; // 题目标签
    @TableField(exist = false)
    private String description; // 题目描述
    @TableField(exist = false)
    private String inputFormat; // 输入格式
    @TableField(exist = false)
    private String outputFormat; // 输出格式
    @TableField(exist = false)
    private List<ProblemSample> sampleGroup; // 题目样例
    private Byte sampleCount; // 样例数
    @TableField(exist = false)
    private String hint; // 提示
    private Integer memoryLimit; // 内存限制
    private Integer timeLimit; // 时间限制
    private Integer submitCount; // 提交数
    private Integer acceptCount; // 通过数
    private Byte difficulty; // 难度 0 无 1 简单 2 中等 3 困难
    private Boolean acceptNote; // 是否接受题解
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime; // 创建时间
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime; // 更新时间
}
