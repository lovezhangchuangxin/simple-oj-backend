package edu.hust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.hust.exception.ExceptionCodeEnum;
import edu.hust.exception.HustOjException;
import edu.hust.mapper.ProblemMapper;
import edu.hust.mapper.ProblemTagMapper;
import edu.hust.pojo.Problem;
import edu.hust.pojo.ProblemTag;
import edu.hust.service.ProblemTagService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProblemTagServiceImpl implements ProblemTagService {
    private final ProblemTagMapper problemTagMapper;
    private final ProblemMapper problemMapper;

    public ProblemTagServiceImpl(ProblemTagMapper problemTagMapper, ProblemMapper problemMapper) {
        this.problemTagMapper = problemTagMapper;
        this.problemMapper = problemMapper;
    }

    /**
     * 查询所有标签
     */
    @Override
    public List<String> listAllTag() {
        // 查询所有标签，去重
        List<ProblemTag> problemTags = problemTagMapper.selectList(new LambdaQueryWrapper<ProblemTag>().select(ProblemTag::getTag).groupBy(ProblemTag::getTag));
        return problemTags.stream().map(ProblemTag::getTag).toList();
    }

    /**
     * 查询问题对应的标签
     */
    @Override
    public String queryTagByProblem(Integer problemId) {
        List<ProblemTag> problemTags = problemTagMapper.selectList(new LambdaQueryWrapper<ProblemTag>().eq(ProblemTag::getProblemId, problemId));
        return problemTags.stream().map(ProblemTag::getTag).reduce((a, b) -> a + " " + b).orElse(" ");
    }


    /**
     * 分页查询标签对应的问题
     */
    @Override
    public Map<String, Object> queryProblemByTag(String tag, Integer page, Integer limit) {
        Map<String, Object> map = new HashMap<>();
        List<ProblemTag> problemTags = problemTagMapper.selectList(new LambdaQueryWrapper<ProblemTag>().eq(ProblemTag::getTag, tag).last("limit " + (page - 1) * limit + "," + limit));
        List<Integer> problemIds = problemTags.stream().map(ProblemTag::getProblemId).toList();
        // 查询问题
        List<Problem> problems = new ArrayList<>();
        if (!problemIds.isEmpty()) {
            problems = problemMapper.selectBatchIds(problemIds);
        }
        map.put("problems", problems);
        // 查询总数
        Long total = problemTagMapper.selectCount(new LambdaQueryWrapper<ProblemTag>().eq(ProblemTag::getTag, tag));
        map.put("total", total);
        return map;
    }

    /**
     * 保存问题标签
     */
    @Override
    public void saveProblemTag(Integer problemId, String tag) {
        if (tag.equals(" ")) {
            return;
        }
        // 先查询是否已经存在
        ProblemTag problemTag;
        problemTag = problemTagMapper.selectOne(new LambdaQueryWrapper<ProblemTag>().eq(ProblemTag::getProblemId, problemId).eq(ProblemTag::getTag, tag));
        if (problemTag != null) {
            return;
        }
        problemTag = new ProblemTag();
        problemTag.setProblemId(problemId);
        problemTag.setTag(tag);
        problemTagMapper.insert(problemTag);
        // 更新问题的标签
        updateTag(problemId, queryTagByProblem(problemId));
    }

    /**
     * 删除问题标签
     */
    @Override
    public void deleteProblemTag(Integer problemId, String tag) {
        if (tag.equals(" ")) {
            return;
        }
        problemTagMapper.delete(new LambdaQueryWrapper<ProblemTag>().eq(ProblemTag::getProblemId, problemId).eq(ProblemTag::getTag, tag));
        // 更新问题的标签
        updateTag(problemId, queryTagByProblem(problemId));
    }

    /**
     * 更新标签
     */
    public void updateTag(Integer problemId, String tag) {
        Problem problem = problemMapper.selectById(problemId);
        if (problem == null) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOT_EXIST);
        }
        problem.setTag(tag);
        problemMapper.updateById(problem);
    }
}
