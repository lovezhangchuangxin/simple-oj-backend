package edu.hust.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import edu.hust.exception.ExceptionCodeEnum;
import edu.hust.exception.HustOjException;
import edu.hust.mapper.ClassUserMapper;
import edu.hust.pojo.ClassUser;
import edu.hust.service.ClassUserService;
import edu.hust.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ClassUserServiceImpl implements ClassUserService {
    private final ClassUserMapper classUserMapper;

    public ClassUserServiceImpl(ClassUserMapper classUserMapper) {
        this.classUserMapper = classUserMapper;
    }

    /**
     * 添加班级用户
     */
    @Override
    public void addClassUser(Integer classId, Integer userId) {
        // 先看是否已经存在
        ClassUser classUser;
        classUser = classUserMapper.selectOne(new LambdaQueryWrapper<ClassUser>()
                .eq(ClassUser::getClassId, classId)
                .eq(ClassUser::getUserId, userId));
        if (classUser != null) {
            throw new HustOjException(ExceptionCodeEnum.CLASS_USER_EXIST);
        }
        try {
            classUser = new ClassUser();
            classUser.setClassId(classId);
            classUser.setUserId(userId);
            classUserMapper.insert(classUser);
        } catch (Exception e) {
            throw new HustOjException(ExceptionCodeEnum.CLASS_USER_ADD_ERROR);
        }
    }

    /**
     * 删除班级用户
     */
    @Override
    public void deleteClassUser(Integer classId, Integer userId) {
        Integer currentUserId = JwtUtils.getUserId();
        boolean isAdmin = JwtUtils.isAdmin();
        if (!isAdmin && !currentUserId.equals(userId)) {
            throw new HustOjException(ExceptionCodeEnum.PERMISSION_DENIED);
        }
        // 先查询后删除
        ClassUser classUser = classUserMapper.selectOne(new LambdaQueryWrapper<ClassUser>()
                .eq(ClassUser::getClassId, classId)
                .eq(ClassUser::getUserId, userId));
        if (classUser == null) {
            throw new HustOjException(ExceptionCodeEnum.CLASS_USER_NOT_EXIST);
        }
        try {
            classUserMapper.deleteById(classUser.getId());
        } catch (Exception e) {
            throw new HustOjException(ExceptionCodeEnum.CLASS_USER_DELETE_ERROR);
        }
    }
}
