package edu.hust.pojo;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("`problem_solve_record`")
public class ProblemSolveRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer problemId;
    private Integer userId;
    private Byte status; // 1: AC, 2: WA, 3: TLE, 4: MLE, 5: RE, 6: CE
    private Short timeCost; // 题目耗时 ms
    private Short memoryCost; // 题目内存 KB
    private String language; // 解题语言
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
