package com.bite.common.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.bite.common.core.constants.Constants;
import com.bite.common.core.utils.ThreadLocalUtils;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class MyMetaObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        Long userId = ThreadLocalUtils.get(Constants.USER_ID, Long.class);
        this.strictInsertFill(metaObject, "createBy", Long.class, userId);
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        Long userId = ThreadLocalUtils.get(Constants.USER_ID, Long.class);
        this.strictUpdateFill(metaObject, "updateBy", Long.class, userId);
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
