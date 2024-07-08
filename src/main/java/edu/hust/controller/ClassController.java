package edu.hust.controller;

import edu.hust.pojo.Class;
import edu.hust.pojo.ClassUser;
import edu.hust.pojo.Result;
import edu.hust.service.ClassService;
import edu.hust.service.ClassUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/class")
public class ClassController {
    private final ClassService classService;

    private final ClassUserService classUserService;

    public ClassController(ClassService classService, ClassUserService classUserService) {
        this.classService = classService;
        this.classUserService = classUserService;
    }

    /**
     * 分页查询班级
     */
    @GetMapping("/list")
    public Result listClass(Integer page, Integer limit) {
        Map<String, Object> result = classService.listClassByPage(page, limit);
        return Result.success("班级查询成功", result);
    }

    /**
     * 创建班级
     */
    @PostMapping("/create")
    public Result createClass(@RequestBody Class aClass) {
        classService.createClass(aClass);
        return Result.success("班级创建成功");
    }

    /**
     * 删除班级
     */
    @GetMapping("/delete/{id}")
    public Result deleteClass(@PathVariable Integer id) {
        classService.deleteClass(id);
        return Result.success("班级删除成功");
    }

    /**
     * 更新班级
     */
    @PostMapping("/update")
    public Result updateClass(@RequestBody Class aClass) {
        classService.updateClass(aClass);
        return Result.success("班级更新成功");
    }

    /**
     * 分页条件查询提交记录
     */
    @GetMapping("/recordByCondition")
    public Result listClassByCondition(Integer page, Integer limit, String name, String creator, Integer id) {
        Map<String, Object> res = classService.listClassByPageWithCondition(page, limit, name, creator, id);
        return Result.success("班级查询成功", res);
    }

    /**
     * 查询一个班级中所有的班级和用户
     */
    @GetMapping("/listClassUserByClassId/{classId}")
    public Result listClassUserByClassId(@PathVariable Integer classId) {
        Map<String, Object> res = classService.listClassUserByClassId(classId);
        return Result.success("班级查询成功", res);
    }

    /**
     * 添加班级用户
     */
    @PostMapping("/addClassUser")
    public Result addClassUser(@RequestBody ClassUser classUser) {
        classUserService.addClassUser(classUser.getClassId(), classUser.getUserId());
        return Result.success("添加成功");
    }

    /**
     * 删除班级用户
     */
    @GetMapping("/deleteClassUser/{classId}/{userId}")
    public Result deleteClassUser(@PathVariable Integer classId, @PathVariable Integer userId) {
        classUserService.deleteClassUser(classId, userId);
        return Result.success("删除成功");
    }

    /**
     * 查询用户所属的班级
     */
    @GetMapping("/getClassesByUserId/{userId}")
    public Result getClassesByUserId(@PathVariable Integer userId) {
        return Result.success("查询成功", classService.getClassesByUserId(userId));
    }
}
