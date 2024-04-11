package edu.hust.controller;

import edu.hust.pojo.Bulletin;
import edu.hust.pojo.Result;
import edu.hust.service.BulletinService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/bulletin")
public class BulletinController {
    private final BulletinService bulletinService;

    public BulletinController(BulletinService bulletinService) {
        this.bulletinService = bulletinService;
    }

    /**
     * 分页查询公告
     */
    @GetMapping("/list")
    public Result listBulletin(Integer page, Integer limit) {
        Map<String, Object> result = bulletinService.listBulletinByPage(page, limit);
        return Result.success("公告查询成功", result);
    }

    /**
     * 获取公告
     */
    @GetMapping("/{id}")
    public Result getBulletinById(@PathVariable Integer id) {
        Map<String, Object> map = bulletinService.getBulletinById(id);
        return Result.success("公告获取成功", map);
    }

    /**
     * 添加公告
     */
    @PostMapping("/add")
    public Result addBulletin(@RequestBody Bulletin bulletin) {
        Integer id = bulletinService.addBulletin(bulletin);
        return Result.success("公告添加成功", id);
    }

    /**
     * 删除公告
     */
    @GetMapping("/delete/{id}")
    public Result deleteBulletin(@PathVariable Integer id) {
        bulletinService.deleteBulletin(id);
        return Result.success("公告删除成功");
    }

    /**
     * 更新公告
     */
    @PostMapping("/update")
    public Result updateBulletin(@RequestBody Bulletin bulletin) {
        bulletinService.updateBulletin(bulletin);
        return Result.success("公告更新成功");
    }
}
