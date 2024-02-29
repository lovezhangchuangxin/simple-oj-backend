package edu.hust.controller;

import edu.hust.exception.ExceptionCodeEnum;
import edu.hust.exception.HustOjException;
import edu.hust.pojo.Problem;
import edu.hust.pojo.ProblemSolution;
import edu.hust.pojo.Result;
import edu.hust.sandbox.CodeExecuteResult;
import edu.hust.service.ProblemService;
import edu.hust.service.ProblemSolveService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/problem")
public class ProblemController {
    private final ProblemService problemService;

    public final ProblemSolveService problemSolveService;

    public ProblemController(ProblemService problemService, ProblemSolveService problemSolveService) {
        this.problemService = problemService;
        this.problemSolveService = problemSolveService;
    }

    /**
     * 创建题目
     */
    @PostMapping("/create")
    public Result createProblem(@RequestBody Problem problem) {
        log.info("problem: {}", problem);

        boolean ok = problemService.createProblem(problem);

        if (ok) {
            return Result.success("题目创建成功");
        }

        throw new HustOjException(ExceptionCodeEnum.PROBLEM_CREATE_ERROR);
    }

    /**
     * 获取题目
     */
    @GetMapping("/{id}")
    public Result getProblem(@PathVariable Integer id) {
        Problem problem = problemService.getProblemById(id);

        return Result.success("题目获取成功", problem);
    }

    /**
     * 分页获取题目列表
     */
    @GetMapping("/list")
    public Result listProblem(Integer page, Integer limit) {
        Map<String, Object> problemMap = problemService.listProblemByPageWithStatus(page, limit);

        return Result.success("题目列表获取成功", problemMap);
    }

    /**
     * 提交代码
     */
    @PostMapping("/submit")
    public Result submitProblem(@RequestBody ProblemSolution problemSolution) {
        List<CodeExecuteResult> codeExecuteResults = problemService.executeCode(problemSolution.getProblemId(), problemSolution.getCode(), problemSolution.getLanguage());

        return Result.success("提交成功", codeExecuteResults);
    }

    /**
     * 查询指定时间范围内的提交记录
     */
    @GetMapping("/recordByTimeRange")
    public Result listProblemSolveRecord(Long startTime, Long endTime) {
        Map<String, Integer> recordMap = problemSolveService.listProblemSolveRecordNumber(startTime, endTime);
        return Result.success("提交记录获取成功", recordMap);
    }
}
