package edu.hust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.hust.mapper.ProblemMapper;
import edu.hust.mapper.ProblemSolveRecordMapper;
import edu.hust.mapper.UserMapper;
import edu.hust.pojo.Problem;
import edu.hust.pojo.ProblemSolveRecord;
import edu.hust.pojo.User;
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
    private final ProblemSolveRecordMapper problemSolveRecordMapper;
    private final ProblemMapper problemMapper;
    private final UserMapper userMapper;

    public ProblemSolveServiceImpl(ProblemSolveRecordMapper problemSolveRecordMapper, ProblemMapper problemMapper, UserMapper userMapper) {
        this.problemSolveRecordMapper = problemSolveRecordMapper;
        this.problemMapper = problemMapper;
        this.userMapper = userMapper;
    }


    /**
     * 查询指定题目的提交记录
     */
    @Override
    public List<ProblemSolveRecord> listProblemSolveRecord(Integer problemId) {
        Integer userId = JwtUtils.getUserId();
        return problemSolveRecordMapper.selectList(
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
        List<ProblemSolveRecord> problemSolveRecords = problemSolveRecordMapper.selectList(
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
        return problemSolveRecordMapper.selectList(
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
    public Map<String, Object> listProblemSolveRecordNumber(Long startTime, Long endTime) {
        List<ProblemSolveRecord> problemSolveRecords = listProblemSolveRecordByTime(startTime, endTime);
        Map<String, Object> res = new HashMap<>();
        Map<String, Integer> collect = new HashMap<>();
        int accept = 0, other = 0;
        for (ProblemSolveRecord problemSolveRecord : problemSolveRecords) {
            String date = problemSolveRecord.getCreateTime().toLocalDate().toString();
            collect.put(date, collect.getOrDefault(date, 0) + 1);
            if (problemSolveRecord.getStatus() == 1) {
                accept++;
            } else {
                other++;
            }
        }
        res.put("accept", accept);
        res.put("other", other);
        res.put("collect", collect);
        return res;
    }

    /**
     * 查询最近通过的提交记录
     */
    @Override
    public List<Map<String, Object>> listRecentAcceptProblemSolveRecord(Integer limit) {
        Integer userId = JwtUtils.getUserId();
        List<ProblemSolveRecord> problemSolveRecords = problemSolveRecordMapper.selectList(
                new LambdaQueryWrapper<ProblemSolveRecord>()
                        .eq(ProblemSolveRecord::getUserId, userId)
                        .eq(ProblemSolveRecord::getStatus, 1)
                        .orderByDesc(ProblemSolveRecord::getCreateTime)
                        .last("limit " + limit)
        );

        List<Map<String, Object>> res = problemSolveRecords.stream().map(problemSolveRecord -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", problemSolveRecord.getId());
            map.put("problemId", problemSolveRecord.getProblemId());
            // 题目
            Problem problem = problemMapper.selectById(problemSolveRecord.getProblemId());
            map.put("title", problem.getTitle());
            map.put("time", problemSolveRecord.getTimeCost());
            map.put("memory", problemSolveRecord.getMemoryCost());
            map.put("createTime", problemSolveRecord.getCreateTime());
            return map;
        }).toList();

        return res;
    }

    /**
     * 分页条件查询提交记录
     */
    @Override
    public Map<String, Object> listProblemSolveRecordByPage(Integer page, Integer limit, Integer problemId, Integer userId, Byte status) {
        Map<String, Object> map = new HashMap<>();
        LambdaQueryWrapper<ProblemSolveRecord> wrapper = new LambdaQueryWrapper<>();
        if (problemId != null) {
            wrapper.eq(ProblemSolveRecord::getProblemId, problemId);
        }
        if (userId != null) {
            wrapper.eq(ProblemSolveRecord::getUserId, userId);
        }
        if (status != null) {
            wrapper.eq(ProblemSolveRecord::getStatus, status);
        }
        Long total = problemSolveRecordMapper.selectCount(wrapper);
        map.put("total", total);
        List<ProblemSolveRecord> problemSolveRecords = problemSolveRecordMapper.selectList(
                wrapper.orderByDesc(ProblemSolveRecord::getCreateTime)
                        .last("limit " + page * limit + "," + limit)
        );
        map.put("data", problemSolveRecords);
        Map<Integer, Object> userMap = new HashMap<>();
        Map<Integer, Object> problemMap = new HashMap<>();
        for (ProblemSolveRecord problemSolveRecord : problemSolveRecords) {
            User user = userMapper.selectById(problemSolveRecord.getUserId());
            if (user != null) {
                // 用户名和邮箱
                userMap.put(user.getId(), Map.of("username", user.getUsername(), "email", user.getEmail()));
            }
            Problem problem = problemMapper.selectById(problemSolveRecord.getProblemId());
            if (problem != null) {
                problemMap.put(problem.getId(), Map.of("title", problem.getTitle(), "difficulty", problem.getDifficulty()));
            }
        }
        map.put("userMap", userMap);
        map.put("problemMap", problemMap);
        return map;
    }

    /**
     * 生成一条提交记录
     */
    @Override
    public void saveProblemSolveRecord(Integer problemId, String language, boolean accept, int time, int memory) {
        ProblemSolveRecord problemSolveRecord = new ProblemSolveRecord();
        problemSolveRecord.setProblemId(problemId);
        problemSolveRecord.setUserId(JwtUtils.getUserId());
        problemSolveRecord.setLanguage(language);
        problemSolveRecord.setStatus(accept ? (byte) 1 : (byte) 2);
        problemSolveRecord.setTimeCost((short) time);
        problemSolveRecord.setMemoryCost((short) memory);
        problemSolveRecordMapper.insert(problemSolveRecord);
    }
}
