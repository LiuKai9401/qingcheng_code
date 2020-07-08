package com.qingcheng.service.order;

import com.qingcheng.pojo.order.CategoryReport;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 *  categoryReport业务逻辑层
 */
public interface CategoryReportService {

    public List<CategoryReport> categoryReport(LocalDate date);

    public void createData(LocalDate date);

    public List<Map> categoryCount(String startDate, String endDate);
}
