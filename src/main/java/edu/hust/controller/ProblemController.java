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
     * 分页条件查询
     */
    @GetMapping("/listByCondition")
    public Result listProblemByCondition(Integer page, Integer limit, String title, String tag, Byte difficulty) {
        Map<String, Object> res = problemService.listProblemByPageWithCondition(page, limit, title, tag, difficulty);
        return Result.success("题目列表获取成功", res);
    }

    /**
     * 删除题目
     */
    @GetMapping("/delete/{id}")
    public Result deleteProblem(@PathVariable Integer id) {
        problemService.deleteProblem(id);

        return Result.success("题目删除成功");
    }

    /**
     * 更新题目
     */
    @PostMapping("/update")
    public Result updateProblem(@RequestBody Problem problem) {
        problemService.updateProblem(problem);

        return Result.success("题目更新成功");
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
     * 提交代码
     */
    @PostMapping("/submitCode")
    public Result submitCode(@RequestBody ProblemSolution problemSolution) {
        List<Map<String, Object>> res = problemService.judge(problemSolution.getProblemId(), problemSolution.getCode(), problemSolution.getLanguage());

        return Result.success("提交成功", res);
    }


    /**
     * 查询指定时间范围内的提交记录
     */
    @GetMapping("/recordByTimeRange")
    public Result listProblemSolveRecord(Long startTime, Long endTime) {
        Map<String, Object> recordMap = problemSolveService.listProblemSolveRecordNumber(startTime, endTime);
        return Result.success("提交记录获取成功", recordMap);
    }

    /**
     * 查询最近通过的提交记录
     */
    @GetMapping("/recentAcceptRecord")
    public Result listRecentAcceptProblemSolveRecord(Integer limit) {
        List<Map<String, Object>> problemSolveRecords = problemSolveService.listRecentAcceptProblemSolveRecord(limit);
        return Result.success("最近通过的提交记录获取成功", problemSolveRecords);
    }

    /**
     * 分页条件查询提交记录
     */
    @GetMapping("/recordByCondition")
    public Result listProblemSolveRecordByCondition(Integer page, Integer limit, Integer problemId, Integer userId, Byte status) {
        Map<String, Object> res = problemSolveService.listProblemSolveRecordByPage(page, limit, problemId, userId, status);
        return Result.success("提交记录获取成功", res);
    }
}
