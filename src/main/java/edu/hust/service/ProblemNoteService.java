package edu.hust.service;

import edu.hust.pojo.ProblemNote;

import java.util.Map;

public interface ProblemNoteService {
    ProblemNote getProblemNoteById(Integer id);

    ProblemNote getMyProblemNoteByProblemId(Integer problemId);

    Map<String, Object> listProblemNoteByPage(Integer problemId, Byte status, Integer page, Integer limit);

    void addProblemNote(ProblemNote problemNote);

    void deleteProblemNote(Integer id);

    void updateProblemNote(ProblemNote problemNote);
}
