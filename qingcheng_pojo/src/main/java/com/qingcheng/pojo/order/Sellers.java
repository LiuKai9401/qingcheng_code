package com.qingcheng.pojo.order;

import java.io.Serializable;
import java.util.List;

/**
 * 订单组合类
 */
public class Sellers implements Serializable {

    //主订单
    private Order order;
    //订单详细行
    private List<OrderItem> orderItemList;

    public Order getOrder() {
        return order;
    }

    public void setOrder(Order order) {
        this.order = order;
    }

    public List<OrderItem> getOrderItemList() {
        return orderItemList;
    }

    public void setOrderItemList(List<OrderItem> orderItemList) {
        this.orderItemList = orderItemList;
    }
}
