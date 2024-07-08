package edu.hust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.hust.exception.ExceptionCodeEnum;
import edu.hust.exception.HustOjException;
import edu.hust.mapper.ClassMapper;
import edu.hust.mapper.ClassUserMapper;
import edu.hust.mapper.UserMapper;
import edu.hust.pojo.Class;
import edu.hust.pojo.ClassUser;
import edu.hust.pojo.User;
import edu.hust.service.ClassService;
import edu.hust.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClassServiceImpl implements ClassService {
    private final ClassMapper classMapper;

    private final UserMapper userMapper;

    private final ClassUserMapper classUserMapper;

    public ClassServiceImpl(ClassMapper classMapper, UserMapper userMapper, ClassUserMapper classUserMapper) {
        this.classMapper = classMapper;
        this.userMapper = userMapper;
        this.classUserMapper = classUserMapper;
    }

    /**
     * 分页查询班级
     */
    @Override
    public Map<String, Object> listClassByPage(Integer page, Integer limit) {
        Map<String, Object> result = new HashMap<>();
        List<Class> classes = classMapper.selectList(new LambdaQueryWrapper<Class>()
                .select(Class::getId, Class::getName, Class::getCreatorId, Class::getParentId, Class::getCreateTime, Class::getUpdateTime)
                .orderByDesc(Class::getUpdateTime)
                .last("limit " + page * limit + "," + limit));
        // 查询创建者id对应的创建者名
        List<Integer> ids = new ArrayList<>();
        classes.forEach(aClass -> ids.add(aClass.getCreatorId()));
        List<User> creators = new ArrayList<>();
        if (!ids.isEmpty()) {
            creators = userMapper.selectBatchIds(ids);
        }
        Map<Integer, String> creatorMap = new HashMap<>();
        creators.forEach(user -> creatorMap.put(user.getId(), user.getUsername()));

        result.put("data", classes);
        result.put("total", classMapper.selectCount(null));
        result.put("user", creatorMap);
        return result;
    }

    /**
     * 创建班级
     */
    @Override
    public void createClass(Class aClass) {
        Integer userId = JwtUtils.getUserId();
        aClass.setId(null);
        aClass.setCreatorId(userId);
        aClass.setCreateTime(null);
        aClass.setUpdateTime(null);

        if (aClass.getName() == null || aClass.getName().length() > 30) {
            throw new HustOjException(ExceptionCodeEnum.CLASS_NAME_ERROR);
        }

        classMapper.insert(aClass);
    }

    /**
     * 删除班级
     */
    @Override
    public void deleteClass(Integer id) {
        Integer userId = JwtUtils.getUserId();
        Class aClass = classMapper.selectById(id);
        boolean isAdmin = JwtUtils.isAdmin();
        if (!isAdmin && !userId.equals(aClass.getCreatorId())) {
            // 不是创建者
            throw new HustOjException(ExceptionCodeEnum.CLASS_DELETE_ERROR);
        }
        classMapper.deleteById(id);
    }

    /**
     * 更新班级
     */
    @Override
    public void updateClass(Class aClass) {
        Integer userId = JwtUtils.getUserId();
        Class oldClass = classMapper.selectById(aClass.getId());
        boolean isAdmin = JwtUtils.isAdmin();
        if (!isAdmin && !userId.equals(oldClass.getCreatorId())) {
            // 不是创建者
            throw new HustOjException(ExceptionCodeEnum.CLASS_UPDATE_ERROR);
        }
        aClass.setCreatorId(null);
        aClass.setCreateTime(null);
        aClass.setUpdateTime(null);
        classMapper.updateById(aClass);
    }

    /**
     * 分页条件查询
     */
    @Override
    public Map<String, Object> listClassByPageWithCondition(Integer page, Integer limit, String name, String creator, Integer id) {
        Map<String, Object> result = new HashMap<>();
        LambdaQueryWrapper<Class> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(Class::getName, name);
        }
        if (creator != null && !creator.isEmpty()) {
            List<User> users = userMapper.selectList(new LambdaQueryWrapper<User>().like(User::getUsername, creator));
            List<Integer> userIds = new ArrayList<>();
            users.forEach(user -> userIds.add(user.getId()));
            if (!userIds.isEmpty()) {
                wrapper.in(Class::getCreatorId, userIds);
            } else {
                result.put("total", 0);
                result.put("data", new ArrayList<>());
                result.put("user", new HashMap<>());
                return result;
            }
        }
        if (id != null) {
            wrapper.eq(Class::getId, id);
        }
        // 班级总数
        result.put("total", classMapper.selectCount(wrapper));
        List<Class> classes = classMapper.selectList(wrapper
                .select(Class::getId, Class::getName, Class::getCreatorId, Class::getDescription, Class::getParentId, Class::getCreateTime, Class::getUpdateTime)
                .orderByDesc(Class::getUpdateTime)
                .last("limit " + page * limit + "," + limit));
        // 查询创建者id对应的创建者名
        List<Integer> ids = new ArrayList<>();
        classes.forEach(aClass -> ids.add(aClass.getCreatorId()));
        List<User> creators = new ArrayList<>();
        if (!ids.isEmpty()) {
            creators = userMapper.selectBatchIds(ids);
        }
        Map<Integer, String> creatorMap = new HashMap<>();
        creators.forEach(user -> creatorMap.put(user.getId(), user.getUsername()));

        result.put("data", classes);
        result.put("user", creatorMap);

        return result;
    }

    /**
     * 查询一个班级中所有的班级和用户
     */
    @Override
    public Map<String, Object> listClassUserByClassId(Integer classId) {
        /**
         * result 的结果为：
         * {
         *    class: {
         *        id: classId,
         *        name: "className",
         *        creatorId: creatorId,
         *        parentId: parentId,
         *        description: description,
         *        createTime: createTime,
         *        updateTime: updateTime,
         *        users: [userId1, userId2, ...],
         *        classes: [{...}, {...}, ...],
         *    },
         *    user: {
         *        userId1: {
         *            username: "username",
         *            avatar: "http://xxx.com/xxx.jpg",
         *        },
         *    }
         * }
         * 其中 class 是一个递归定义的结构
         */
        Map<String, Object> result = new HashMap<>();
        Set<Integer> userIdSet = new HashSet<>();
        Map<String, Object> classMap = listClassUserByClass(classMapper.selectById(classId), userIdSet);
        result.put("class", classMap);
        // TODO: 先实现了，再优化
        List<User> users = userMapper.selectBatchIds(userIdSet);
        Map<Integer, Object> userMap = new HashMap<>();
        users.forEach(user -> {
            Map<String, Object> userDetail = new HashMap<>();
            userDetail.put("username", user.getUsername());
            userDetail.put("avatar", user.getAvatar());
            userMap.put(user.getId(), userDetail);
        });
        result.put("user", userMap);

        return result;
    }

    /**
     * 查询用户所属的班级
     */
    @Override
    public List<Class> getClassesByUserId(Integer userId) {
        List<Class> classes = new ArrayList<>();
        List<ClassUser> classUsers = classUserMapper.selectList(new LambdaQueryWrapper<ClassUser>()
                .eq(ClassUser::getUserId, userId));
        for (ClassUser classUser : classUsers) {
            Class aClass = classMapper.selectById(classUser.getClassId());
            if (aClass != null) {
                classes.add(aClass);
            }
        }
        return classes;
    }

    /**
     * 查询一个班级中所有的班级和用户
     */
    public Map<String, Object> listClassUserByClass(Class aclass, Set<Integer> userIdSet) {
        /**
         * result 的结果为：
         * {
         *     id: classId,
         *     name: "className",
         *     creatorId: creatorId,
         *     parentId: parentId,
         *     description: description,
         *     createTime: createTime,
         *     updateTime: updateTime,
         *     users: [userId1, userId2, ...],
         *     classes: [{...}, {...}, ...],
         * }
         * 递归定义的结构
         */
        Map<String, Object> result = new HashMap<>();
        result.put("id", aclass.getId());
        result.put("name", aclass.getName());
        result.put("creatorId", aclass.getCreatorId());
        result.put("parentId", aclass.getParentId());
        result.put("description", aclass.getDescription());
        result.put("createTime", aclass.getCreateTime());
        result.put("updateTime", aclass.getUpdateTime());
        List<Integer> userIds = listUserByClassId(aclass.getId());
        userIdSet.addAll(userIds);
        result.put("users", userIds);

        List<Class> classes = listClassByParentId(aclass.getId());
        List<Map<String, Object>> classList = new ArrayList<>();
        for (Class c : classes) {
            classList.add(listClassUserByClass(c, userIdSet));
        }
        result.put("classes", classList);
        return result;
    }

    /**
     * 查询班级中的班级
     */
    public List<Class> listClassByParentId(Integer parentId) {
        return classMapper.selectList(new LambdaQueryWrapper<Class>().eq(Class::getParentId, parentId));
    }

    /**
     * 查询班级中的用户
     */
    public List<Integer> listUserByClassId(Integer classId) {
        return classUserMapper.selectList(new LambdaQueryWrapper<ClassUser>().eq(ClassUser::getClassId, classId))
                .stream().map(ClassUser::getUserId).collect(Collectors.toList());
    }
}
