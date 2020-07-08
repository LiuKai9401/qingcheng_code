package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.order.CategoryReport;
import com.qingcheng.service.order.CategoryReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/categoryReport")
public class CategoryReportController {

    @Reference
    private CategoryReportService categoryReportService;

    /**
     * 查询昨天销售数量及销售额
     * @return
     */
    @GetMapping("/yesterday")
    public List<CategoryReport> yesterday(){
        LocalDate date = LocalDate.of(2019,04,15);
        return categoryReportService.categoryReport(date);
    }

    /**
     * 根据日期统计一级类目集合
     * @param startDate
     * @param endDate
     * @return
     */
    @GetMapping("/categoryCount")
    public List<Map> categoryCount(String startDate, String endDate){
        return categoryReportService.categoryCount(startDate,endDate);
    }
}
