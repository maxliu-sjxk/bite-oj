package com.bite.common.core.controller;

import cn.hutool.core.collection.CollectionUtil;
import com.bite.common.core.domain.R;
import com.bite.common.core.domain.TableDataInfo;
import com.github.pagehelper.PageInfo;

import java.util.List;

public class BaseController {
    public R<Void> toR(int rows) {
        return rows > 0 ? R.ok() : R.fail();
    }

    public R<Void> toR(boolean result) {
        return result ? R.ok() : R.fail();
    }

    /**
     * 查询后对查询结果进行处理，包含获取符合查询条件的记录总数
     * @param list
     * @return
     */
    public TableDataInfo getTableDataInfo(List<?> list) {
        if (CollectionUtil.isEmpty(list)) {
            return TableDataInfo.empty();
        }
        //mybatis PageHelper计算返回符合查询条件的记录总数
        long total = new PageInfo<>(list).getTotal();
        return TableDataInfo.success(list, total);
    }
}
