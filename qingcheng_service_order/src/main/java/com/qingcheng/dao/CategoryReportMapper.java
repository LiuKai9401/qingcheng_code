package com.qingcheng.dao;

import com.qingcheng.pojo.order.CategoryReport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.time.LocalDate;
import java.util.List;

public interface CategoryReportMapper extends Mapper<CategoryReport> {

    @Select("SELECT " +
            " b.category_id1 categoryId1, " +
            " b.category_id2 categoryId2, " +
            " b.category_id3 categoryId3, " +
            " DATE_FORMAT( a.create_time, '%Y-%m-%d' ) countDate, " +
            " SUM( b.num ) num, " +
            " SUM( b.pay_money ) money " +
            "FROM " +
            " tb_order a, " +
            " tb_order_item b  " +
            "WHERE " +
            " a.id = b.order_id  " +
            " AND DATE_FORMAT( a.create_time, '%Y-%m-%d' )= #{date}  " +
            " AND a.is_delete = '0'  " +
            " AND a.pay_status = '1'  " +
            "GROUP BY " +
            " b.category_id1, " +
            " b.category_id2, " +
            " b.category_id3, " +
            " a.create_time")
    public List<CategoryReport> categoryReport(@Param("date") LocalDate date);
}
