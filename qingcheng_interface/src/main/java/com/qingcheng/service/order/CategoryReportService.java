package com.qingcheng.service.order;

import com.qingcheng.pojo.order.CategoryReport;

import java.time.LocalDate;
import java.util.List;

/**
 *  categoryReport业务逻辑层
 */
public interface CategoryReportService {

    public List<CategoryReport> categoryReport(LocalDate date);

    public void createData(LocalDate date);
}
