package edu.hust.service;

import java.util.List;
import java.util.Map;

public interface ProblemTagService {
    List<String> listAllTag();

    String queryTagByProblem(Integer problemId);

    Map<String, Object> queryProblemByTag(String tag, Integer page, Integer limit);

    void saveProblemTag(Integer problemId, String tag);

    void deleteProblemTag(Integer problemId, String tag);
}
