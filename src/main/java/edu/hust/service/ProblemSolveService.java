package edu.hust.service;

import edu.hust.pojo.ProblemSolveRecord;
import edu.hust.sandbox.CodeExecuteResult;

import java.util.List;
import java.util.Map;

public interface ProblemSolveService {
    List<ProblemSolveRecord> listProblemSolveRecord(Integer problemId);

    List<Integer> listProblemSolveRecord(List<Integer> problemIdList);

    List<ProblemSolveRecord> listProblemSolveRecordByTime(Long startTime, Long endTime);

    Map<String, Integer> listProblemSolveRecordNumber(Long startTime, Long endTime);

    void saveProblemSolveRecord(Integer problemId, String language, boolean accept, List<CodeExecuteResult> codeExecuteResults);
}
