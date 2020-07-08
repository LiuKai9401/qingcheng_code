package com.qingcheng.dao;

import com.qingcheng.pojo.order.CategoryReport;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

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

    @Select(" SELECT " +
            " a.category_id1 categoryId1, " +
            " b.NAME categoryName, " +
            " SUM( a.num ) num, " +
            " SUM( a.money ) money  " +
            "FROM " +
            " tb_category_report a, " +
            " v_category b  " +
            "WHERE " +
            " a.category_id1 = b.id  " +
            " AND a.count_date >= #{startDate}  " +
            " AND a.count_date <= #{endDate}  " +
            "GROUP BY " +
            " a.category_id1")
    public List<Map> categoryCount(@Param("startDate") String startDate,@Param("endDate") String endDate);
}
