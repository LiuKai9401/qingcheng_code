package com.qingcheng.controller.order;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.pojo.order.CategoryReport;
import com.qingcheng.service.order.CategoryReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

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
}
