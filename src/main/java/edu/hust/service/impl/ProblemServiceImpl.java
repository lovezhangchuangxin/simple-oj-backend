package edu.hust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.google.gson.Gson;
import edu.hust.exception.ExceptionCodeEnum;
import edu.hust.exception.HustOjException;
import edu.hust.mapper.ProblemMapper;
import edu.hust.pojo.Problem;
import edu.hust.pojo.ProblemSample;
import edu.hust.sandbox.CodeExecuteResult;
import edu.hust.sandbox.CodeResult;
import edu.hust.sandbox.DockerSandbox;
import edu.hust.service.ProblemService;
import edu.hust.service.ProblemSolveService;
import edu.hust.utils.JudgerUtils;
import edu.hust.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ProblemServiceImpl implements ProblemService {
    @Value("${problem.path}")
    private String path;
    @Value("${problem.code-path}")
    private String codePath;

    private final List<Integer> memoryLimitList = List.of(32, 64, 128, 256, 512);
    private final List<Integer> timeLimitList = List.of(500, 1000, 1500, 2000);

    private final ProblemMapper problemMapper;

    public final ProblemSolveService problemSolveService;

    public final DockerSandbox dockerSandbox;

    public ProblemServiceImpl(ProblemMapper problemMapper, ProblemSolveService problemSolveService, DockerSandbox dockerSandbox) {
        this.problemMapper = problemMapper;
        this.problemSolveService = problemSolveService;
        this.dockerSandbox = dockerSandbox;
    }

    /**
     * 获取题目
     */
    @Override
    public Problem getProblemById(Integer id) {
        Problem problem = problemMapper.selectById(id);

        if (problem == null) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOT_EXIST);
        }

        // 读取题目文件夹
        try {
            readProblemFromFile(problem);
            readTestCaseFromFile(problem, false);
        } catch (IOException e) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_READ_ERROR);
        }

        return problem;
    }

    /**
     * 执行代码
     *
     * @deprecated
     */
    @Override
    public List<CodeExecuteResult> executeCode(Integer id, String code, String language) {
        // 先把用户代码字符串写入文件
        saveUserCode(id, code, language);
        // 创建题目对象
        Problem problem = problemMapper.selectById(id);

        if (problem == null) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOT_EXIST);
        }

        try {
            readTestCaseFromFile(problem, true);
        } catch (IOException e) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_READ_ERROR);
        }

        // 将输入样例写入用户代码文件夹
        writeSamplesToCodeFolder(id, problem.getSampleGroup());
        // 用户代码路径
        String userCodePath = generateUserCodePath(id);
        // 执行代码
        List<CodeExecuteResult> codeExecuteResults = dockerSandbox.executeCode(userCodePath, language, problem.getSampleGroup().size(), problem.getTimeLimit(), problem.getMemoryLimit());
        // 删除用户代码文件夹
        deleteUserCodeFolder(id);
        // 判断代码执行结果
        boolean accept = judgeCodeExecuteResult(problem, codeExecuteResults);
        // 下面代码是错误的，没有使用的
        if (accept) {
            // 保存执行记录
            problemSolveService.saveProblemSolveRecord(id, language, accept, 0, 0);
        } else {
            // 保存执行记录
            problemSolveService.saveProblemSolveRecord(id, language, accept, 0, 0);
        }
        return codeExecuteResults;
    }

    /**
     * 判断代码执行结果
     */
    public boolean judgeCodeExecuteResult(Problem problem, List<CodeExecuteResult> codeExecuteResults) {
        boolean accept = true;
        for (int i = 0; i < problem.getSampleGroup().size(); i++) {
            ProblemSample problemSample = problem.getSampleGroup().get(i);
            CodeExecuteResult codeExecuteResult = codeExecuteResults.get(i);
            // 超时
            if (codeExecuteResult.getTimeout()) {
                codeExecuteResult.setStatus((byte) 3);
                accept = false;
                continue;
            }
            // 超内存
            if (codeExecuteResult.getMemory() > problem.getMemoryLimit()) {
                codeExecuteResult.setStatus((byte) 4);
                accept = false;
                continue;
            }
            // 答案错误
            if (!problemSample.getOut().equals(codeExecuteResult.getOutput())) {
                codeExecuteResult.setStatus((byte) 2);
                accept = false;
                continue;
            }
            codeExecuteResult.setStatus((byte) 1);
        }

        return accept;
    }

    /**
     * 判题
     */
    @Override
    public List<Map<String, Object>> judge(Integer id, String code, String language) {
        if (!language.equals("c") && !language.equals("cpp")) {
            throw new HustOjException(ExceptionCodeEnum.LANGUAGE_NOT_EXIST);
        }
        // 先把用户代码字符串写入文件
        saveUserCode(id, code, language);
        // 编译代码
        String codePath = generateUserCodePath(id) + "/" + "Main" + generateFileExt(language);
        String exePath = generateUserCodePath(id) + "/" + "Main";
        JudgerUtils.compile(language, codePath, exePath);
        // 获取题目对象
        Problem problem = problemMapper.selectById(id);
        // 结果
        List<Map<String, Object>> resMap = new ArrayList<>();
        // 正确的个数
        int acceptCount = 0;
        int time = 1, memory = 1;
        // 读取测试用例
        for (int i = 1; i <= problem.getSampleCount(); i++) {
            Map<String, Object> map = new HashMap<>();
            String inputPath = "problemSet/" + problem.getAuthorId() + "/" + problem.getId() + "/in/" + i + ".in";
            String outputPath = "code/" + id + "/" + i + ".out";
            String error_path = "code/" + id + "/" + i + ".err";
            File answerFile = new File(generateTestCasePath(problem, i, false));
            CodeResult stat = JudgerUtils.execute(language, "code/" + id + "/Main", inputPath, outputPath, error_path, problem.getTimeLimit(), problem.getMemoryLimit() * 1024 * 1024);
            map.put("stat", stat);
            if (stat.result != 0) {
                continue;
            }
            String output, answer;
            try {
                output = FileUtils.readFileToString(new File(outputPath), "UTF-8");
                answer = FileUtils.readFileToString(answerFile, "UTF-8");
            } catch (IOException e) {
                throw new HustOjException(ExceptionCodeEnum.PROBLEM_READ_ERROR);
            }

            if (!output.equals(answer)) {
                // 6 表示答案错误
                stat.setResult(6);
                map.put("output", output);
            } else {
                acceptCount++;
                time += stat.real_time;
                memory += stat.memory;
            }
            resMap.add(map);
        }
        boolean accept = acceptCount == problem.getSampleCount();
        if (acceptCount == 0) acceptCount = 1;
        // 获取平均时间和内存
        time /= acceptCount;
        memory /= (acceptCount * 1024 * 1024);
        // 保存执行记录
        problemSolveService.saveProblemSolveRecord(id, language, accept, time, memory);
        // 修改题目通过情况
        if (accept) {
            if (problem.getAcceptCount() == null) {
                problem.setAcceptCount(1);
            } else {
                problem.setAcceptCount(problem.getAcceptCount() + 1);
            }
        }
        if (problem.getSubmitCount() == null) {
            problem.setSubmitCount(1);
        } else {
            problem.setSubmitCount(problem.getSubmitCount() + 1);
        }
        problemMapper.updateById(problem);
        // 删除用户代码文件夹
        deleteUserCodeFolder(id);
        return resMap;
    }

    /**
     * 查询用户自己发布的题目
     */
    public List<Problem> listProblemByUserId() {
        Integer userId = JwtUtils.getUserId();
        return problemMapper.selectList(new LambdaQueryWrapper<Problem>().eq(Problem::getAuthorId, userId));
    }

    /**
     * 分页查询题目基础信息
     */
    public List<Problem> listProblemByPage(Integer page, Integer limit) {
        return problemMapper.selectList(new LambdaQueryWrapper<Problem>()
                .select(Problem::getId, Problem::getTitle, Problem::getTag, Problem::getSubmitCount, Problem::getAcceptCount, Problem::getDifficulty)
                .last("limit " + page * limit + "," + limit));
    }

    /**
     * 分页查询题目基础信息（带 status）
     */
    @Override
    public Map<String, Object> listProblemByPageWithStatus(Integer page, Integer limit) {
        List<Problem> problems = listProblemByPage(page, limit);
        List<Integer> ids = problemSolveService.listProblemSolveRecord(problems.stream().map(Problem::getId).toList());
        Map<String, Object> map = new HashMap<>();
        map.put("problems", problems);
        map.put("ids", ids);
        // 题目总数
        map.put("total", problemMapper.selectCount(null));
        return map;
    }

    /**
     * 分页条件查询题目基础信息
     */
    @Override
    public Map<String, Object> listProblemByPageWithCondition(Integer page, Integer limit, String title, String tag, Byte difficulty) {
        Map<String, Object> map = new HashMap<>();
        LambdaQueryWrapper<Problem> wrapper = new LambdaQueryWrapper<>();
        if (title != null && !title.isEmpty()) {
            wrapper.like(Problem::getTitle, title);
        }
        if (tag != null && !tag.isEmpty()) {
            wrapper.like(Problem::getTag, tag);
        }
        if (difficulty != null && difficulty != 0) {
            wrapper.eq(Problem::getDifficulty, difficulty);
        }
        // 题目总数
        map.put("total", problemMapper.selectCount(wrapper));
        List<Problem> problems = problemMapper.selectList(wrapper
                .select(Problem::getId, Problem::getTitle, Problem::getTag, Problem::getSubmitCount, Problem::getAcceptCount, Problem::getDifficulty)
                .last("limit " + page * limit + "," + limit));
        List<Integer> ids = problems.isEmpty() ? List.of() : problemSolveService.listProblemSolveRecord(problems.stream().map(Problem::getId).toList());
        map.put("problems", problems);
        map.put("ids", ids);
        return map;
    }


    /**
     * 创建题目
     */
    @Override
    public boolean createProblem(Problem problem) {
        Integer userId = JwtUtils.getUserId();
        // 防止用户删除其他数据
        problem.setId(null);
        problem.setAuthorId(userId);
        problem.setSampleCount((byte) problem.getSampleGroup().size());
        problem.setCreateTime(null);
        problem.setUpdateTime(null);
        problem.setSubmitCount(null);
        problem.setAcceptCount(null);
        problem.setAcceptNote(true);

        validateMemoryAndTime(problem);

        try {
            problemMapper.insert(problem);
            createProblemFile(problem);
        } catch (IOException e) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_CREATE_ERROR);
        }

        return true;
    }

    /**
     * 删除题目
     */
    @Override
    public void deleteProblem(Integer id) {
        Integer userId = JwtUtils.getUserId();

        Problem problem = problemMapper.selectById(id);

        // 只能删除自己的题目
        if (problem == null || !problem.getAuthorId().equals(userId)) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOT_EXIST);
        }

        problemMapper.deleteById(id);
        deleteProblemFile(problem);
    }

    /**
     * 更新题目
     */
    @Override
    public void updateProblem(Problem problem) {
        Integer userId = JwtUtils.getUserId();
        Problem oldProblem = problemMapper.selectById(problem.getId());

        // 只能更新自己的题目，除非是管理员
        boolean isAdmin = JwtUtils.isAdmin();
        System.out.println("是否管理员：" + isAdmin);
        if (oldProblem == null || (!isAdmin && !oldProblem.getAuthorId().equals(userId))) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_NOT_EXIST);
        }

        problem.setSubmitCount(null);
        problem.setAcceptCount(null);
        // 是否修改了题目的内容
        boolean isChangeProblemContent = problem.getSampleCount() != null && problem.getSampleGroup() != null;
        if (isChangeProblemContent) {
            validateMemoryAndTime(problem);
            problem.setSampleCount((byte) problem.getSampleGroup().size());
        }

        try {
            problemMapper.updateById(problem);
            if (isChangeProblemContent) {
                createProblemFile(problem);
            }
        } catch (IOException e) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_UPDATE_ERROR);
        }
    }

    /**
     * 创建题目文件
     */
    public void createProblemFile(Problem problem) throws IOException {
        writeProblemToFile(problem);
        writeTestCaseToFile(problem);
    }


    /**
     * 将题目内容写入文件
     */
    public void writeProblemToFile(Problem problem) throws IOException {
        String problemPath = generateProblemPath(problem);
        String problemContent = generateProblemContent(problem);
        File file = new File(problemPath);
        FileUtils.writeStringToFile(file, problemContent, "UTF-8");
    }

    /**
     * 从文件中读取题目内容
     */
    public void readProblemFromFile(Problem problem) throws IOException {
        String problemPath = generateProblemPath(problem);
        File file = new File(problemPath);
        String content = FileUtils.readFileToString(file, "UTF-8");
        parseProblemContent(problem, content);
    }

    /**
     * 将测试用例写入文件
     */
    public void writeTestCaseToFile(Problem problem) throws IOException {
        List<ProblemSample> problemSamples = problem.getSampleGroup();

        for (int i = 0; i < problemSamples.size(); ) {
            ProblemSample problemSample = problemSamples.get(i++);
            // 从 1 开始
            String inputPath = generateTestCasePath(problem, i, true);
            String outputPath = generateTestCasePath(problem, i, false);
            File inputfile = new File(inputPath);
            File outputfile = new File(outputPath);

            FileUtils.writeStringToFile(inputfile, problemSample.getIn(), "UTF-8");
            FileUtils.writeStringToFile(outputfile, problemSample.getOut(), "UTF-8");
        }
    }

    /**
     * 从文件中读取测试用例
     */
    public void readTestCaseFromFile(Problem problem, boolean all) throws IOException {
        List<ProblemSample> problemSamples = new ArrayList<>();
        // 暂时最多读三个样例
        int count = all ? problem.getSampleCount() : Math.min(problem.getSampleCount(), 3);

        for (int i = 0; i++ < count; ) {
            ProblemSample problemSample = new ProblemSample();
            // 从 1 开始
            String inputPath = generateTestCasePath(problem, i, true);
            String outputPath = generateTestCasePath(problem, i, false);
            File inputfile = new File(inputPath);
            File outputfile = new File(outputPath);
            problemSample.setIn(FileUtils.readFileToString(inputfile, "UTF-8"));
            problemSample.setOut(FileUtils.readFileToString(outputfile, "UTF-8"));
            problemSamples.add(problemSample);
        }

        problem.setSampleGroup(problemSamples);
    }


    /**
     * 删除题目文件夹
     */
    public void deleteProblemFile(Problem problem) {
        // 题目文件夹路径
        String problemFolderPath = generateProblemFolder(problem);
        // 删除该文件夹
        File file = new File(problemFolderPath);
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            throw new HustOjException(ExceptionCodeEnum.PROBLEM_DELETE_ERROR);
        }
    }

    /**
     * 生成题目内容
     */
    public String generateProblemContent(Problem problem) {
        Map<String, String> map = new HashMap<>();
        map.put("description", problem.getDescription());
        map.put("inputFormat", problem.getInputFormat());
        map.put("outputFormat", problem.getOutputFormat());
        map.put("hint", problem.getHint());

        Gson gson = new Gson();
        return gson.toJson(map);
    }

    /**
     * 解析题目内容
     */
    public Problem parseProblemContent(Problem problem, String content) {
        Gson gson = new Gson();
        Map<String, String> map = gson.fromJson(content, Map.class);

        problem.setDescription(map.get("description"));
        problem.setInputFormat(map.get("inputFormat"));
        problem.setOutputFormat(map.get("outputFormat"));
        problem.setHint(map.get("hint"));

        return problem;
    }

    /**
     * 生成题目文件夹路径
     */
    public String generateProblemFolder(Problem problem) {
        return path + "/" + problem.getAuthorId() + "/" + problem.getId();
    }

    /**
     * 生成题目路径
     */
    public String generateProblemPath(Problem problem) {
        return generateProblemFolder(problem) + "/" + problem.getTitle() + ".json";
    }

    /**
     * 生成测试用例路径
     */
    public String generateTestCasePath(Problem problem, int number, boolean input) {
        return generateProblemFolder(problem) + "/" + (input ? "in" : "out") + "/" + number + (input ? ".in" : ".out");
    }

    /**
     * 校验题目的内存时间参数
     */
    public void validateMemoryAndTime(Problem problem) {
        Integer memoryLimit = problem.getMemoryLimit();
        Integer timeLimit = problem.getTimeLimit();

        if (!memoryLimitList.contains(memoryLimit) || !timeLimitList.contains(timeLimit)) {
            throw new HustOjException(ExceptionCodeEnum.TIME_OR_MEMORY_ERROR);
        }
    }

    /**
     * 保存用户代码
     */
    public void saveUserCode(Integer id, String code, String language) {
        String codeFilePath = generateUserCodePath(id) + "/" + "Main" + generateFileExt(language);
        File file = new File(codeFilePath);
//        code = dockerSandbox.redirectInput(code);
        try {
            FileUtils.writeStringToFile(file, code, "UTF-8");
        } catch (IOException e) {
            throw new HustOjException(ExceptionCodeEnum.USER_CODE_SAVE_ERROR);
        }
    }

    /**
     * 删除用户代码文件夹
     */
    public void deleteUserCodeFolder(Integer id) {
        String userCodePath = generateUserCodePath(id);
        File file = new File(userCodePath);
        try {
            FileUtils.deleteDirectory(file);
        } catch (IOException e) {
            throw new HustOjException(ExceptionCodeEnum.USER_CODE_DELETE_ERROR);
        }
    }

    /**
     * 生成用户代码存放路径
     *
     * @param id 用户 id
     */
    public String generateUserCodePath(Integer id) {
        return codePath + "/" + id;
    }

    /**
     * 将题目的输入样例写入到用户代码文件夹中
     */
    public void writeSamplesToCodeFolder(Integer id, List<ProblemSample> problemSamples) {
        for (int i = 0; i < problemSamples.size(); ) {
            ProblemSample problemSample = problemSamples.get(i++);
            String inputPath = generateUserCodePath(id) + "/" + i + ".in";
            File inputfile = new File(inputPath);

            try {
                FileUtils.writeStringToFile(inputfile, problemSample.getIn(), "UTF-8");
            } catch (IOException e) {
                throw new HustOjException(ExceptionCodeEnum.USER_CODE_SAVE_ERROR);
            }
        }
    }

    /**
     * 根据语言生成文件扩展名
     *
     * @param language 语言
     */
    public String generateFileExt(String language) {
        return switch (language) {
            case "c" -> ".c";
            case "cpp" -> ".cpp";
            case "java" -> ".java";
            default -> throw new HustOjException(ExceptionCodeEnum.LANGUAGE_NOT_EXIST);
        };
    }
}
