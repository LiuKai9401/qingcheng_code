package com.qingcheng.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.qingcheng.dao.CategoryReportMapper;
import com.qingcheng.pojo.order.CategoryReport;
import com.qingcheng.service.order.CategoryReportService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service(interfaceClass = CategoryReportService.class)
public class CategoryReportServiceImpl implements CategoryReportService {

    @Autowired
    private CategoryReportMapper categoryReportMapper;

    /**
     * 查询昨天一天销售数量与销售额分析表
     * @param date
     * @return
     */
    @Override
    public List<CategoryReport> categoryReport(LocalDate date) {
        return categoryReportMapper.categoryReport(date);
    }

    /**
     * 定时任务将上一天的销售结果插入:categoryReport表中
     */
    @Transactional
    public void createData(LocalDate date){
        List<CategoryReport> categoryReportList = categoryReport(date);
        for (CategoryReport categoryReport : categoryReportList) {
            categoryReportMapper.insert(categoryReport);
        }
    }
}
