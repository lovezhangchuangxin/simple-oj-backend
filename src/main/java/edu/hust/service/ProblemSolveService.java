package edu.hust.service;

import edu.hust.pojo.ProblemSolveRecord;

import java.util.List;
import java.util.Map;

public interface ProblemSolveService {
    List<ProblemSolveRecord> listProblemSolveRecord(Integer problemId);

    List<Integer> listProblemSolveRecord(List<Integer> problemIdList);

    List<ProblemSolveRecord> listProblemSolveRecordByTime(Long startTime, Long endTime);

    Map<String, Object> listProblemSolveRecordNumber(Long startTime, Long endTime);

    List<Map<String, Object>> listRecentAcceptProblemSolveRecord(Integer limit);

    Map<String, Object> listProblemSolveRecordByPage(Integer page, Integer limit, Integer problemId, Integer userId, Byte status);

    void saveProblemSolveRecord(Integer problemId, String language, boolean accept, int time, int memory);
}
