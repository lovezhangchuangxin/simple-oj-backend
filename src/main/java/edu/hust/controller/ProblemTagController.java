package edu.hust.controller;

import edu.hust.pojo.ProblemTag;
import edu.hust.pojo.Result;
import edu.hust.service.ProblemTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/problemTag")
public class ProblemTagController {
    private final ProblemTagService problemTagService;

    public ProblemTagController(ProblemTagService problemTagService) {
        this.problemTagService = problemTagService;
    }

    /**
     * 查询所有标签
     */
    @GetMapping("/list")
    public Result listAllTag() {
        return Result.success("查询成功", problemTagService.listAllTag());
    }

    /**
     * 查询问题对应的标签
     */
    @GetMapping("/problem/{problemId}")
    public Result queryTagByProblem(@PathVariable Integer problemId) {
        String tags = problemTagService.queryTagByProblem(problemId);
        return Result.success("查询成功", tags);
    }

    /**
     * 查询标签对应的问题
     */
    @GetMapping("/tag")
    public Result queryProblemByTag(String tag, Integer page, Integer limit) {
        return Result.success("查询成功", problemTagService.queryProblemByTag(tag, page, limit));
    }

    /**
     * 保存问题标签
     */
    @PostMapping("/save")
    public Result saveProblemTag(@RequestBody ProblemTag problemTag) {
        problemTagService.saveProblemTag(problemTag.getProblemId(), problemTag.getTag());
        return Result.success("保存成功");
    }

    /**
     * 删除问题标签
     */
    @PostMapping("/delete")
    public Result deleteProblemTag(@RequestBody ProblemTag problemTag) {
        problemTagService.deleteProblemTag(problemTag.getProblemId(), problemTag.getTag());
        return Result.success("删除成功");
    }
}
