package com.qingcheng.controller.task;

import com.alibaba.dubbo.config.annotation.Reference;
import com.qingcheng.service.order.OrderConfigService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;


@Component
public class OrderTask {

    @Reference
    private OrderConfigService orderConfigService;

    @Scheduled(cron = "0 0/2 * * * ?")
    public void taskTest (){
        System.out.println("每隔两分钟执行一次任务"+new Date());
        orderConfigService.orderTimeOutLogic();
    }
}
