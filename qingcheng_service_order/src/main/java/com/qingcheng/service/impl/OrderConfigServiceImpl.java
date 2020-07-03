package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.OrderConfigMapper;
import com.qingcheng.dao.OrderLogMapper;
import com.qingcheng.dao.OrderMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.order.OrderConfig;
import com.qingcheng.pojo.order.OrderLog;
import com.qingcheng.service.order.OrderConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import src.com.qingcheng.util.IdWorker;
import tk.mybatis.mapper.entity.Example;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = OrderConfigService.class)
public class OrderConfigServiceImpl implements OrderConfigService {

    @Autowired
    private OrderConfigMapper orderConfigMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderLogMapper orderLogMapper;

    @Autowired
    private IdWorker idWorker;
    /**
     * 返回全部记录
     * @return
     */
    public List<OrderConfig> findAll() {
        return orderConfigMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<OrderConfig> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<OrderConfig> orderConfigs = (Page<OrderConfig>) orderConfigMapper.selectAll();
        return new PageResult<OrderConfig>(orderConfigs.getTotal(),orderConfigs.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<OrderConfig> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return orderConfigMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<OrderConfig> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<OrderConfig> orderConfigs = (Page<OrderConfig>) orderConfigMapper.selectByExample(example);
        return new PageResult<OrderConfig>(orderConfigs.getTotal(),orderConfigs.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public OrderConfig findById(Integer id) {
        return orderConfigMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param orderConfig
     */
    public void add(OrderConfig orderConfig) {
        orderConfigMapper.insert(orderConfig);
    }

    /**
     * 修改
     * @param orderConfig
     */
    public void update(OrderConfig orderConfig) {
        orderConfigMapper.updateByPrimaryKeySelective(orderConfig);
    }

    /**
     *  删除
     * @param id
     */
    public void delete(Integer id) {
        orderConfigMapper.deleteByPrimaryKey(id);
    }

    /**
     * 订单超时处理逻辑
     */
    @Transactional
    public void orderTimeOutLogic() {
        //1.先获取系统配置中的超时参数设置：因为只有一条配置记录
        OrderConfig orderConfig = orderConfigMapper.selectByPrimaryKey("1");
        //2.获取参数设置的订单超时时数
        Integer orderTimeout=orderConfig.getOrderTimeout();
        //3.获取当前系统数据前60分钟的时间: minusMinutes表示当前时间减去值
        LocalDateTime localDateTime = LocalDateTime.now().minusMinutes(orderTimeout);

        //4.查询订单数据库中，小于得到的超时时间点订单，表示未付款订单
        Example example = new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andLessThan("createTime",localDateTime);//创建时间小于超时时间订单
        criteria.andEqualTo("orderStatus","0");//订单状态=待付款
        criteria.andEqualTo("isDelete","0");//删除状态=未删除
        List<Order> orderList = orderMapper.selectByExample(example);
        for (Order order : orderList) {
            //记录订单变动日志
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId()+"");//设置日志id
            orderLog.setOperater("system");//设置操作员
            orderLog.setOperateTime(new Date());//设置操作时间
            orderLog.setOrderId(order.getId());//设置订单id
            orderLog.setOrderStatus("4");//设置订单状态
            orderLog.setPayStatus(order.getPayStatus());//设置付款状态
            orderLog.setConsignStatus(order.getConsignStatus());//设置订单发货状态
            orderLog.setRemarks("超时订单，系统自动关闭！");
            orderLogMapper.insert(orderLog);

            //更改订单状态
            order.setOrderStatus("4");//订单状态修改为：已关闭
            order.setCloseTime(new Date());//设置订单状态关闭时间
            orderMapper.updateByPrimaryKeySelective(order);
        }
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(OrderConfig.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){

            // ID
            if(searchMap.get("id")!=null ){
                criteria.andEqualTo("id",searchMap.get("id"));
            }
            // 正常订单超时时间（分）
            if(searchMap.get("orderTimeout")!=null ){
                criteria.andEqualTo("orderTimeout",searchMap.get("orderTimeout"));
            }
            // 秒杀订单超时时间（分）
            if(searchMap.get("seckillTimeout")!=null ){
                criteria.andEqualTo("seckillTimeout",searchMap.get("seckillTimeout"));
            }
            // 自动收货（天）
            if(searchMap.get("takeTimeout")!=null ){
                criteria.andEqualTo("takeTimeout",searchMap.get("takeTimeout"));
            }
            // 售后期限
            if(searchMap.get("serviceTimeout")!=null ){
                criteria.andEqualTo("serviceTimeout",searchMap.get("serviceTimeout"));
            }
            // 自动五星好评
            if(searchMap.get("commentTimeout")!=null ){
                criteria.andEqualTo("commentTimeout",searchMap.get("commentTimeout"));
            }

        }
        return example;
    }

}
