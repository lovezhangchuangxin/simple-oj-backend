package edu.hust.service;

import edu.hust.pojo.Problem;
import edu.hust.sandbox.CodeExecuteResult;

import java.util.List;
import java.util.Map;

public interface ProblemService {
    Problem getProblemById(Integer id);

    List<CodeExecuteResult> executeCode(Integer id, String code, String language);

    List<Map<String, Object>> judge(Integer id, String code, String language);

    Map<String, Object> listProblemByPageWithStatus(Integer page, Integer limit);

    Map<String, Object> listProblemByPageWithCondition(Integer page, Integer limit, String title, String tag, Byte difficulty);

    boolean createProblem(Problem problem);

    void deleteProblem(Integer id);

    void updateProblem(Problem problem);
}
