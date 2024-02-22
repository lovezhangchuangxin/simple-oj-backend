package edu.hust.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 默认值填充拦截器
 */
@Slf4j
@Component
public class FieldMetaObjectHandler implements MetaObjectHandler {
    /**
     * 新增时间
     **/
    private final static String TIME_CREATE = "createTime";
    /**
     * 更新时间
     **/
    private final static String TIME_UPDATE = "updateTime";

    /**
     * 新增默认填充器
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        // 添加时间、最后更新时间
        this.strictInsertFill(metaObject, TIME_CREATE, LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, TIME_UPDATE, LocalDateTime.class, LocalDateTime.now());
    }


    /**
     * 更新默认填充器
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        // 最后更新时间
        this.strictInsertFill(metaObject, TIME_UPDATE, LocalDateTime.class, LocalDateTime.now());
    }
}