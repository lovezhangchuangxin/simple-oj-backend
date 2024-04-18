package edu.hust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.hust.exception.ExceptionCodeEnum;
import edu.hust.exception.HustOjException;
import edu.hust.mapper.ProblemMapper;
import edu.hust.mapper.ProblemNoteMapper;
import edu.hust.mapper.UserMapper;
import edu.hust.pojo.Problem;
import edu.hust.pojo.ProblemNote;
import edu.hust.pojo.User;
import edu.hust.service.ProblemNoteService;
import edu.hust.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProblemNoteServiceImpl implements ProblemNoteService {
    @Value("${problem.path}")
    private String path;

    private final ProblemNoteMapper problemNoteMapper;

    private final ProblemMapper problemMapper;

    private final UserMapper userMapper;

    public ProblemNoteServiceImpl(ProblemNoteMapper problemNoteMapper, ProblemMapper problemMapper, UserMapper userMapper) {
        this.problemNoteMapper = problemNoteMapper;
        this.problemMapper = problemMapper;
        this.userMapper = userMapper;
    }

    /**
     * 根据 id 查询题解
     */
    @Override
    public ProblemNote getProblemNoteById(Integer id) {
        ProblemNote problemNote = problemNoteMapper.selectById(id);
        if (problemNote == null) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOTE_NOT_EXIST);
        }

        Problem problem = problemMapper.selectById(problemNote.getProblemId());
        if (problem == null) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOT_EXIST);
        }

        problemNote.setContent(readProblemNoteFromFile(problemNote, problem));
        return problemNote;
    }

    /**
     * 查询自己的指定题目的题解
     */
    @Override
    public ProblemNote getMyProblemNoteByProblemId(Integer problemId) {
        Integer userId = JwtUtils.getUserId();
        ProblemNote problemNote = problemNoteMapper.selectOne(new LambdaQueryWrapper<ProblemNote>()
                .eq(ProblemNote::getProblemId, problemId)
                .eq(ProblemNote::getAuthorId, userId));
        if (problemNote == null) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOTE_NOT_EXIST);
        }

        Problem problem = problemMapper.selectById(problemNote.getProblemId());
        if (problem == null) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOT_EXIST);
        }

        problemNote.setContent(readProblemNoteFromFile(problemNote, problem));
        return problemNote;
    }

    /**
     * 分页查询题目题解
     */
    @Override
    public Map<String, Object> listProblemNoteByPage(Integer problemId, Byte status, Integer page, Integer limit) {
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOT_EXIST);
        }

        List<ProblemNote> problemNotes = problemNoteMapper.selectList(new LambdaQueryWrapper<ProblemNote>()
                .eq(ProblemNote::getProblemId, problemId)
                .eq(status != null, ProblemNote::getStatus, status)
                .orderByDesc(ProblemNote::getCollection)
                .last("limit " + page * limit + "," + limit));
        Map<Integer, User> userMap = new HashMap<>();
        for (ProblemNote problemNote : problemNotes) {
            problemNote.setContent(readProblemNoteFromFile(problemNote, problem));
            // 只选取用户名，用户头像
            User user = userMapper.selectOne(new LambdaQueryWrapper<User>().select(User::getUsername, User::getAvatar, User::getRole).eq(User::getId, problemNote.getAuthorId()));
            if (user != null) {
                userMap.put(problemNote.getAuthorId(), user);
            }
        }
        Long total = problemNoteMapper.selectCount(new LambdaQueryWrapper<ProblemNote>().eq(ProblemNote::getProblemId, problemId));

        Map<String, Object> map = new HashMap<>();
        map.put("data", problemNotes);
        map.put("total", total);
        map.put("userMap", userMap);
        return map;
    }

    /**
     * 添加题解
     */
    @Override
    public void addProblemNote(ProblemNote problemNote) {
        Integer userId = JwtUtils.getUserId();
        problemNote.setId(null);
        problemNote.setAuthorId(userId);
        problemNote.setCreateTime(null);
        problemNote.setUpdateTime(null);
        problemNote.setStatus((byte) 0);
        problemNote.setCollection(0);

        // 查询题目是否存在
        Problem problem = problemMapper.selectById(problemNote.getProblemId());
        if (problem == null) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOT_EXIST);
        }

        // 查询用户是否已经存在该题目的题解
        ProblemNote existProblemNote = problemNoteMapper.selectOne(new LambdaQueryWrapper<ProblemNote>()
                .eq(ProblemNote::getProblemId, problemNote.getProblemId())
                .eq(ProblemNote::getAuthorId, userId));

        if (existProblemNote != null) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOTE_EXIST);
        }

        // 先写文件
        writeProblemNoteToFile(problemNote, problem, problemNote.getContent());
        // 插入题解
        problemNoteMapper.insert(problemNote);
    }

    /**
     * 删除题解
     */
    @Override
    public void deleteProblemNote(Integer id) {
        Integer userId = JwtUtils.getUserId();
        ProblemNote problemNote = problemNoteMapper.selectById(id);
        // 题解不存在或者不是自己的
        if (!isMyProblemNote(problemNote)) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOTE_NOT_EXIST);
        }

        Problem problem = problemMapper.selectById(problemNote.getProblemId());
        if (problem == null) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOT_EXIST);
        }

        // 删除文件
        deleteProblemNoteFile(problemNote, problem);
        // 删除题解
        problemNoteMapper.deleteById(id);
    }

    /**
     * 更新题解
     */
    @Override
    public void updateProblemNote(ProblemNote problemNote) {
        Integer userId = JwtUtils.getUserId();
        problemNote.setAuthorId(userId);
        problemNote.setCreateTime(null);
        problemNote.setUpdateTime(null);

        ProblemNote oldProblemNote = problemNoteMapper.selectById(problemNote.getId());
        if (!isMyProblemNote(oldProblemNote)) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOTE_NOT_EXIST);
        }

        // 是否是管理员
        User user = userMapper.selectById(userId);
        boolean isAdmin = user.getRole() == 1;
        if (!isAdmin) {
            problemNote.setStatus(oldProblemNote.getStatus());
        }
        problemNote.setCollection(oldProblemNote.getCollection());

        Problem problem = problemMapper.selectById(oldProblemNote.getProblemId());
        if (problem == null) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOT_EXIST);
        }

        // 写入文件
        writeProblemNoteToFile(problemNote, problem, problemNote.getContent());
        // 更新题解
        problemNoteMapper.updateById(problemNote);
    }

    /**
     * 判断题解是否是自己的
     */
    public boolean isMyProblemNote(ProblemNote problemNote) {
        Integer userId = JwtUtils.getUserId();
        return problemNote != null && problemNote.getAuthorId().equals(userId);
    }

    /**
     * 将题解写入文件
     */
    public void writeProblemNoteToFile(ProblemNote problemNote, Problem problem, String content) {
        try {
            String path = getProblemNotePath(problemNote, problem);
            FileUtils.writeStringToFile(new File(path), content, "UTF-8");
        } catch (IOException e) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOTE_WRITE_ERROR);
        }
    }

    /**
     * 读取题解文件
     */
    public String readProblemNoteFromFile(ProblemNote problemNote, Problem problem) {
        try {
            String path = getProblemNotePath(problemNote, problem);
            return FileUtils.readFileToString(new File(path), "UTF-8");
        } catch (IOException e) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOTE_READ_ERROR);
        }
    }

    /**
     * 删除题解文件
     */
    public void deleteProblemNoteFile(ProblemNote problemNote, Problem problem) {
        String path = getProblemNotePath(problemNote, problem);
        File file = new File(path);
        if (!file.delete()) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOTE_DELETE_ERROR);
        }
    }

    /**
     * 获取题解的路径
     */
    public String getProblemNotePath(ProblemNote problemNote, Problem problem) {
        return path + "/" + problem.getAuthorId() + "/" + problemNote.getProblemId() + "/note/" + problemNote.getAuthorId() + ".md";
    }
}
