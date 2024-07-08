package edu.hust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.hust.exception.ExceptionCodeEnum;
import edu.hust.exception.HustOjException;
import edu.hust.mapper.BulletinMapper;
import edu.hust.mapper.UserMapper;
import edu.hust.pojo.Bulletin;
import edu.hust.pojo.User;
import edu.hust.service.BulletinService;
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
public class BulletinServiceImpl implements BulletinService {
    @Value("${bulletin.path}")
    private String path;

    private final BulletinMapper bulletinMapper;

    private final UserMapper userMapper;

    public BulletinServiceImpl(BulletinMapper bulletinMapper, UserMapper userMapper) {
        this.bulletinMapper = bulletinMapper;
        this.userMapper = userMapper;
    }

    /**
     * 分页查询查询公告
     */
    @Override
    public Map<String, Object> listBulletinByPage(Integer page, Integer limit) {
        Map<String, Object> result = new HashMap<>();
        List<Bulletin> bulletins = bulletinMapper.selectList(new LambdaQueryWrapper<Bulletin>()
                .select(Bulletin::getId, Bulletin::getTitle, Bulletin::getAuthorId, Bulletin::getCreateTime, Bulletin::getUpdateTime)
                .orderByDesc(Bulletin::getUpdateTime)
                .last("limit " + page * limit + "," + limit));
        // 查询作者id对应的作者名
        List<Integer> ids = new ArrayList<>();
        bulletins.forEach(bulletin -> ids.add(bulletin.getAuthorId()));
        List<User> authors = new ArrayList<>();
        if (!ids.isEmpty()) {
            authors = userMapper.selectBatchIds(ids);
        }
        Map<Integer, String> authorMap = new HashMap<>();
        authors.forEach(user -> authorMap.put(user.getId(), user.getUsername()));

        result.put("data", bulletins);
        result.put("total", bulletinMapper.selectCount(null));
        result.put("user", authorMap);
        return result;
    }

    /**
     * 添加公告
     */
    @Override
    public Integer addBulletin(Bulletin bulletin) {
        bulletin.setId(null);
        bulletin.setCreateTime(null);
        bulletin.setUpdateTime(null);
        boolean bool = bulletinMapper.insert(bulletin) > 0;
        if (bool) {
            try {
                // 查询插入的公告 id
                Integer id = bulletinMapper.selectOne(new LambdaQueryWrapper<Bulletin>()
                        .eq(Bulletin::getTitle, bulletin.getTitle())
                        .orderByDesc(Bulletin::getCreateTime)).getId();
                saveBulletinContent(id, bulletin.getContent());
                return id;
            } catch (IOException e) {
                throw new HustOjException(ExceptionCodeEnum.BULLETIN_ADD_ERROR);
            }
        }

        throw new HustOjException(ExceptionCodeEnum.BULLETIN_ADD_ERROR);
    }

    /**
     * 删除公告
     */
    @Override
    public void deleteBulletin(Integer id) {
        boolean bool = bulletinMapper.deleteById(id) > 0;
        if (bool) {
            // 删除文件
            if (deleteBulletinContent(id)) {
                return;
            }
        }
        throw new HustOjException(ExceptionCodeEnum.BULLETIN_DELETE_ERROR);
    }

    /**
     * 获取公告
     */
    @Override
    public Map<String, Object> getBulletinById(Integer id) {
        Map<String, Object> map = new HashMap<>();
        Bulletin bulletin = bulletinMapper.selectById(id);
        try {
            String content = getBulletinContent(id);
            bulletin.setContent(content);
        } catch (IOException e) {
            throw new HustOjException(ExceptionCodeEnum.BULLETIN_NOT_FOUND);
        }
        // 获取作者名
        User user = userMapper.selectById(bulletin.getAuthorId());
        map.put("data", bulletin);
        map.put("author", user.getUsername());
        return map;
    }

    /**
     * 更新公告
     */
    @Override
    public void updateBulletin(Bulletin bulletin) {
        bulletin.setCreateTime(null);
        bulletin.setUpdateTime(null);
        boolean bool = bulletinMapper.updateById(bulletin) > 0;
        if (bool) {
            try {
                saveBulletinContent(bulletin.getId(), bulletin.getContent());
                return;
            } catch (IOException e) {
                throw new HustOjException(ExceptionCodeEnum.BULLETIN_UPDATE_ERROR);
            }
        }

        throw new HustOjException(ExceptionCodeEnum.BULLETIN_UPDATE_ERROR);
    }

    /**
     * 获取公告路径
     */
    public String getBulletinPath(Integer id) {
        return path + "\\" + id + ".md";
    }

    /**
     * 获取公告内容
     */
    public String getBulletinContent(Integer id) throws IOException {
        String filePath = getBulletinPath(id);
        File file = new File(filePath);
        return FileUtils.readFileToString(file, "UTF-8");
    }

    /**
     * 保存公告内容
     */
    public void saveBulletinContent(Integer id, String content) throws IOException {
        String filePath = getBulletinPath(id);
        File file = new File(filePath);
        FileUtils.writeStringToFile(file, content, "UTF-8");
    }

    /**
     * 删除公告内容
     */
    public boolean deleteBulletinContent(Integer id) {
        String filePath = getBulletinPath(id);
        File file = new File(filePath);
        if (file.exists()) {
            return file.delete();
        }
        return false;
    }
}