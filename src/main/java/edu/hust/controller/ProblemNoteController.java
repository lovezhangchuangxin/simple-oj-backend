package edu.hust.controller;

import edu.hust.pojo.ProblemNote;
import edu.hust.pojo.Result;
import edu.hust.service.ProblemNoteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/problemNote")
public class ProblemNoteController {
    private final ProblemNoteService problemNoteService;

    public ProblemNoteController(ProblemNoteService problemNoteService) {
        this.problemNoteService = problemNoteService;
    }

    /**
     * 根据 id 查询题解
     */
    @GetMapping("/id")
    public Result getProblemNoteById(Integer id) {
        return Result.success("查询成功", problemNoteService.getProblemNoteById(id));
    }

    /**
     * 查询自己的指定题目的题解
     */
    @GetMapping("/my")
    public Result listMyProblemNoteByProblemId(Integer problemId) {
        return Result.success("查询成功", problemNoteService.getMyProblemNoteByProblemId(problemId));
    }

    /**
     * 分页查询题目题解
     */
    @GetMapping("/list")
    public Result listProblemNoteByPage(Integer problemId, Byte status, Integer page, Integer limit) {
        return Result.success("查询成功", problemNoteService.listProblemNoteByPage(problemId, status, page, limit));
    }

    /**
     * 添加题解
     */
    @PostMapping("/add")
    public Result addProblemNote(@RequestBody ProblemNote problemNote) {
        problemNoteService.addProblemNote(problemNote);
        return Result.success("添加成功");
    }

    /**
     * 删除题解
     */
    @GetMapping("/delete")
    public Result deleteProblemNote(Integer id) {
        problemNoteService.deleteProblemNote(id);
        return Result.success("删除成功");
    }

    /**
     * 更新题解
     */
    @PostMapping("/update")
    public Result updateProblemNote(@RequestBody ProblemNote problemNote) {
        problemNoteService.updateProblemNote(problemNote);
        return Result.success("更新成功");
    }
}
