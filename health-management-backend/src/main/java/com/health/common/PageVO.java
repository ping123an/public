package com.health.common;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 通用分页响应结构。
 *
 * @param <T> 分页记录类型
 */
@Data
public class PageVO<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 当前页数据 */
    private List<T> records;

    /** 总记录数 */
    private long total;

    /** 当前页码 */
    private long current;

    /** 每页条数 */
    private long size;

    /** 总页数 */
    private long pages;

    /**
     * 由 MyBatis-Plus 分页对象 + 转换函数构建分页响应。
     */
    public static <E, T> PageVO<T> of(IPage<E> page, Function<E, T> converter) {
        PageVO<T> vo = new PageVO<>();
        List<E> source = page.getRecords();
        vo.setRecords(source == null ? Collections.emptyList()
                : source.stream().map(converter).collect(Collectors.toList()));
        vo.setTotal(page.getTotal());
        vo.setCurrent(page.getCurrent());
        vo.setSize(page.getSize());
        vo.setPages(page.getPages());
        return vo;
    }
}
