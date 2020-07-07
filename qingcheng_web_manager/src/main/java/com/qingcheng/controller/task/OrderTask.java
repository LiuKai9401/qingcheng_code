package com.qingcheng.controller.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.order.CategoryReportService;
import com.qingcheng.service.order.OrderConfigService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Date;


@Component
public class OrderTask {

    @Reference
    private OrderConfigService orderConfigService;

    @Reference
    private CategoryReportService categoryReportService;

    @Scheduled(cron = "0 0/2 * * * ?")
    public void taskTest (){
        System.out.println("每隔两分钟执行一次任务"+new Date());
        orderConfigService.orderTimeOutLogic();
    }

    /**
     * 凌晨1点将昨天的销售统计表插入categoryReport中
     */
    @Scheduled(cron ="0 * * * * ?")
    public void createCategoryReport(){
        System.out.println("运行获取昨天销售结果");
        LocalDate date = LocalDate.now().minusDays(1);
        //LocalDate date = LocalDate.of(2019,04,15);
        categoryReportService.createData(date);
    }
}
