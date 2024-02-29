package edu.hust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.hust.mapper.ProblemSolveRecordMapper;
import edu.hust.pojo.ProblemSolveRecord;
import edu.hust.sandbox.CodeExecuteResult;
import edu.hust.service.ProblemSolveService;
import edu.hust.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProblemSolveServiceImpl implements ProblemSolveService {
    private final ProblemSolveRecordMapper problemSolveRecordMapperMapper;

    public ProblemSolveServiceImpl(ProblemSolveRecordMapper problemSolveRecordMapperMapper) {
        this.problemSolveRecordMapperMapper = problemSolveRecordMapperMapper;
    }

    /**
     * 查询指定题目的提交记录
     */
    @Override
    public List<ProblemSolveRecord> listProblemSolveRecord(Integer problemId) {
        Integer userId = JwtUtils.getUserId();
        return problemSolveRecordMapperMapper.selectList(
                new LambdaQueryWrapper<ProblemSolveRecord>()
                        .eq(ProblemSolveRecord::getProblemId, problemId)
                        .eq(ProblemSolveRecord::getUserId, userId)
        );
    }

    /**
     * 查询一堆题目中完成的记录
     */
    @Override
    public List<Integer> listProblemSolveRecord(List<Integer> problemIdList) {
        Integer userId = JwtUtils.getUserId();
        List<ProblemSolveRecord> problemSolveRecords = problemSolveRecordMapperMapper.selectList(
                new LambdaQueryWrapper<ProblemSolveRecord>()
                        .select(ProblemSolveRecord::getProblemId)
                        .eq(ProblemSolveRecord::getUserId, userId)
                        .in(ProblemSolveRecord::getProblemId, problemIdList)
                        .eq(ProblemSolveRecord::getStatus, 1)
        );
        return problemSolveRecords.stream().map(ProblemSolveRecord::getProblemId).toList();
    }

    /**
     * 查询一段时间内的提交记录
     */
    @Override
    public List<ProblemSolveRecord> listProblemSolveRecordByTime(Long startTime, Long endTime) {
        // 将时间戳转为 datetime 格式
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String start = simpleDateFormat.format(startTime);
        String end = simpleDateFormat.format(endTime);

        Integer userId = JwtUtils.getUserId();
        return problemSolveRecordMapperMapper.selectList(
                new LambdaQueryWrapper<ProblemSolveRecord>()
                        .eq(ProblemSolveRecord::getUserId, userId)
                        .ge(ProblemSolveRecord::getCreateTime, start)
                        .le(ProblemSolveRecord::getCreateTime, end)
        );
    }

    /**
     * 查询一段时间内的每天的提交数
     */
    @Override
    public Map<String, Integer> listProblemSolveRecordNumber(Long startTime, Long endTime) {
        List<ProblemSolveRecord> problemSolveRecords = listProblemSolveRecordByTime(startTime, endTime);
        Map<String, Integer> collect = new HashMap<>();
        for (ProblemSolveRecord problemSolveRecord : problemSolveRecords) {
            String date = problemSolveRecord.getCreateTime().toLocalDate().toString();
            collect.put(date, collect.getOrDefault(date, 0) + 1);
        }
        return collect;
    }

    /**
     * 生成一条提交记录
     */
    @Override
    public void saveProblemSolveRecord(Integer problemId, String language, boolean accept, List<CodeExecuteResult> codeExecuteResults) {
        // 计算平均时间和内存
        int time = 0;
        int memory = 0;
        for (CodeExecuteResult codeExecuteResult : codeExecuteResults) {
            time += codeExecuteResult.getTime();
            memory += codeExecuteResult.getMemory();
        }
        time /= codeExecuteResults.size();
        memory /= codeExecuteResults.size();

        ProblemSolveRecord problemSolveRecord = new ProblemSolveRecord();
        problemSolveRecord.setProblemId(problemId);
        problemSolveRecord.setUserId(JwtUtils.getUserId());
        problemSolveRecord.setStatus(accept ? (byte) 1 : (byte) 2);
        problemSolveRecord.setLanguage(language);
        problemSolveRecord.setMemoryCost((short) memory);
        problemSolveRecord.setTimeCost((short) time);

        problemSolveRecordMapperMapper.insert(problemSolveRecord);
    }
}
