package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSONArray;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.OrderItemMapper;
import com.qingcheng.dao.OrderLogMapper;
import com.qingcheng.dao.OrderMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.order.Order;
import com.qingcheng.pojo.order.OrderItem;
import com.qingcheng.pojo.order.OrderLog;
import com.qingcheng.pojo.order.Sellers;
import com.qingcheng.service.order.OrderService;
import org.aspectj.weaver.ast.Or;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import src.com.qingcheng.util.IdWorker;
import sun.applet.Main;
import tk.mybatis.mapper.entity.Example;

import java.util.*;

@Service(interfaceClass = OrderService.class)
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderItemMapper orderItemMapper;

    @Autowired
    private OrderLogMapper orderLogMapper;

    @Autowired
    private IdWorker idWorker;



    /**
     * 返回全部记录
     * @return
     */
    public List<Order> findAll() {
        return orderMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Order> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Order> orders = (Page<Order>) orderMapper.selectAll();
        return new PageResult<Order>(orders.getTotal(),orders.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Order> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return orderMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Order> findPage(Map<String, Object> searchMap, int page, int size) {
        String ordeBy="consign_time desc";
        PageHelper.startPage(page,size,ordeBy);
        Example example = createExample(searchMap);
        Page<Order> orders = (Page<Order>) orderMapper.selectByExample(example);
        return new PageResult<Order>(orders.getTotal(),orders.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Order findById(String id) {
        return orderMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param order
     */
    public void add(Order order) {
        orderMapper.insert(order);
    }

    /**
     * 修改
     * @param order
     */
    public void update(Order order) {
        orderMapper.updateByPrimaryKeySelective(order);
    }

    /**
     *  删除
     * @param id
     */
    public void delete(String id) {
        orderMapper.deleteByPrimaryKey(id);
    }

    /**
     * 查询主订单与订单行明细
     * @param id
     * @return
     */
    public Sellers findSellersById(String id) {
        //1.创建组合实体类返回
        Sellers sellers = new Sellers();
        //2.查询主订单数据，根据id查询
        Order order = orderMapper.selectByPrimaryKey(id);
        //3.查询子订单数据，根据主订单id查询
        Example example = new Example(OrderItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId",id);
        List<OrderItem> orderItems = orderItemMapper.selectByExample(example);
        //4.封装组合实体类
        sellers.setOrder(order);
        sellers.setOrderItemList(orderItems);
        return sellers;
    }

    /**
     * 批量处理发货
     * @param orders
     */
    @Transactional
    public void batchSendGoods(List<Order> orders) {
        for (Order order : orders) {
            //1.判断运单号和物流公司是否为空
            if (order.getShippingName()==null || order.getShippingCode()==null){
                throw new RuntimeException("请选择快递公司和填写快递单号!");
            }

            //2.当不为空时，更新订单状态
            order.setOrderStatus("3");//订单状态  已发货
            order.setConsignStatus("2");//发货状态  已发货
            order.setConsignTime(new Date());//发货时间
            orderMapper.updateByPrimaryKeySelective(order);

            //3.记录订单日志
            OrderLog orderLog = new OrderLog();
            orderLog.setId(idWorker.nextId()+"");//日志id
            orderLog.setOperater(order.getUsername());//操作人员
            orderLog.setOperateTime(order.getConsignTime());//操作人员时间
            orderLog.setOrderId(order.getId());//订单id
            orderLog.setOrderStatus(order.getOrderStatus());//订单状态
            orderLog.setPayStatus(order.getPayStatus());//支付状态
            orderLog.setConsignStatus(order.getConsignStatus());//发货状态
            orderLogMapper.insert(orderLog);
        }
    }

    /**
     * 订单合并
     * @param mainOrder
     * @param followOrder
     */
    @Override
    @Transactional
    public void mergeOrder(String mainOrder, String followOrder) {
        //1.先判断合并的订单是否为同一张
        if (mainOrder.equals(followOrder)){
            throw new RuntimeException("合并订单不能为同一张！");
        }

        //2.当不是同一张时，先查询两种订单数据
        Order MainOrder = orderMapper.selectByPrimaryKey(mainOrder);//主订单
        Order FollowOrder = orderMapper.selectByPrimaryKey(followOrder);//子订单
        if (MainOrder==null || FollowOrder==null){
            throw new RuntimeException("主订单或者子订单不存在！");
        }

        //3.先查询出从表所有子订单
        Example  example = new Example(OrderItem.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("orderId",followOrder);
        List<OrderItem> orderItems = orderItemMapper.selectByExample(example);
        if (orderItems.size()>0){
            //4.遍历循环添加到主订单中
            for (OrderItem orderItem : orderItems) {
                //4.1 设置为主表订单,并添加进入主表中
                orderItem.setOrderId(mainOrder);
                orderItem.setId(idWorker.nextId()+"");
                orderItemMapper.insert(orderItem);
            }
            //5.子表标记为逻辑删除
            FollowOrder.setIsDelete("1");
            orderMapper.updateByPrimaryKeySelective(FollowOrder);

            //6.查询主订单的订单明细
            example.clear();
            Example.Criteria criteria1 = example.createCriteria();
            criteria1.andEqualTo("orderId",mainOrder);
            List<OrderItem> mainOrderItems = orderItemMapper.selectByExample(example);
                int totalNum=0;
                int totalMoney=0;
                int payMoney=0;
                int postFee=0;
                for (OrderItem orderItem : mainOrderItems) {
                    //数量总计
                    totalNum+= orderItem.getNum();
                    //总金额总计
                    totalMoney+= orderItem.getMoney();
                    //实付金额
                    payMoney += orderItem.getPayMoney();
                    //运费金额
                    postFee += orderItem.getPostFee();
                }
                //6.1 更新主订单的数量
                MainOrder.setTotalNum(totalNum);
                //6.2 更新主订单总金额
                MainOrder.setTotalMoney(totalMoney);
                //6.3 更新主订单邮费
                MainOrder.setPostFee(postFee);
                //6.4 更新实付金额=总金额 - 优惠金额 + 运费
                MainOrder.setPayMoney(totalMoney-MainOrder.getPreMoney()+postFee);
                orderMapper.updateByPrimaryKeySelective(MainOrder);
        }else {
            throw new RuntimeException("从表不存在子订单，请检查从表！");
        }

    }

    /**
     * 订单拆分
     * @param mapList
     */
    @Override
    @Transactional
    public void splitOrder(List<Map<String, Object>> mapList) {
        List<OrderItem> newOrderItemList = new ArrayList<OrderItem>();
        String orderId=null;
        for (Map<String, Object> map : mapList) {
           //1. 判断当前num是否有值，如果无值表示无需拆分
            if (map.get("num")!=null && !"".equals(map.get("num"))){
                //2.获取订单行的id
                String id = (String) map.get("id");
                //3.获取拆分数量
                int num = (int) map.get("num");
                OrderItem orderItem = orderItemMapper.selectByPrimaryKey(id);
                //4.获取订单id
                orderId = orderItem.getOrderId();
                //5.判断拆分数量是否大于订单数量
                if (num<orderItem.getNum()){
                    //5.1 使用当前订单数量-拆分订单数量,总金额= 数量*单价
                    orderItem.setNum(orderItem.getNum()-num);
                    orderItem.setMoney(orderItem.getNum()*orderItem.getPrice());
                    orderItemMapper.updateByPrimaryKeySelective(orderItem);
                    //5.2 把之前查询出的订单行信息，重新设置数量，并更新金额=数量*单价,并添加进入新集合中
                    orderItem.setId(null);
                    orderItem.setNum(num);
                    orderItem.setMoney(orderItem.getNum()*orderItem.getPrice());
                    newOrderItemList.add(orderItem);
                }else {
                    throw new RuntimeException("拆分数量大于订单数量，不允许拆分订单！");
                }
            }
        }

        if (orderId!=null){
            //1.获取之前订单信息
            Order order = orderMapper.selectByPrimaryKey(orderId);
            //1.1 设置总数量为0，因为数量需要重新累积
            order.setTotalNum(0);
            //1.2 设置运费为0
            order.setPostFee(0);
            //1.3 设置总金额为0,
            order.setTotalMoney(0);
            //2. 查询所有子订单
            Example example = new Example(OrderItem.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("orderId",orderId);
            List<OrderItem> oldOrderItemList = orderItemMapper.selectByExample(example);
            for (OrderItem orderItem : oldOrderItemList) {
                    //2.1 汇总数量
                    order.setTotalNum(order.getTotalNum()+orderItem.getNum());
                    //2.2 汇总运费
                    order.setPostFee(order.getPostFee()+orderItem.getPostFee());
                    //2.3 设置总金额
                    order.setTotalMoney(order.getTotalMoney()+orderItem.getMoney());
            }
            //3. 更新订单数据,实付金额=总金额 - 优惠金额 - 运费
            order.setPayMoney(order.getTotalMoney()-order.getPreMoney()+order.getPostFee());
            orderMapper.updateByPrimaryKeySelective(order);
            //4. 把之前查询出的订单信息，重新设置id和总金额，总数量，总实付
            order.setId(idWorker.nextId()+"");
            order.setTotalMoney(0);
            order.setPostFee(0);
            order.setTotalMoney(0);
            order.setPayMoney(0);
            orderMapper.insert(order);
            for (OrderItem orderItem : newOrderItemList) {
                //4.1 设置从表的id和订单id
                orderItem.setId(idWorker.nextId()+"");
                //4.2设置订单行的所属订单id
                orderItem.setOrderId(order.getId());
                //4.3 插入订单行
                orderItemMapper.insert(orderItem);
                //4.4 设置总金额
                order.setTotalMoney(order.getTotalMoney()+orderItem.getMoney());
                //4.5 汇总数量
                order.setTotalNum(order.getTotalNum()+orderItem.getNum());
                //4.6 汇总运费
                order.setPostFee(order.getPostFee()+orderItem.getPostFee());
            }
            //5.实付金额=总金额 - 优惠金额 - 运费
            order.setPayMoney(order.getTotalMoney()-order.getPreMoney()+order.getPostFee());
            order.setId(idWorker.nextId()+"");
            //6.生成新订单
            orderMapper.insert(order);
        }
    }


    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Order.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 根据数组ids查询
            if (searchMap.get("ids")!=null){
                criteria.andIn("id", (List<String>) searchMap.get("ids"));
            }
            // 订单id
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
            }
            // 支付类型，1、在线支付、0 货到付款
            if(searchMap.get("payType")!=null && !"".equals(searchMap.get("payType"))){
                criteria.andLike("payType","%"+searchMap.get("payType")+"%");
            }
            // 物流名称
            if(searchMap.get("shippingName")!=null && !"".equals(searchMap.get("shippingName"))){
                criteria.andLike("shippingName","%"+searchMap.get("shippingName")+"%");
            }
            // 物流单号
            if(searchMap.get("shippingCode")!=null && !"".equals(searchMap.get("shippingCode"))){
                criteria.andLike("shippingCode","%"+searchMap.get("shippingCode")+"%");
            }
            // 用户名称
            if(searchMap.get("username")!=null && !"".equals(searchMap.get("username"))){
                criteria.andLike("username","%"+searchMap.get("username")+"%");
            }
            // 买家留言
            if(searchMap.get("buyerMessage")!=null && !"".equals(searchMap.get("buyerMessage"))){
                criteria.andLike("buyerMessage","%"+searchMap.get("buyerMessage")+"%");
            }
            // 是否评价
            if(searchMap.get("buyerRate")!=null && !"".equals(searchMap.get("buyerRate"))){
                criteria.andLike("buyerRate","%"+searchMap.get("buyerRate")+"%");
            }
            // 收货人
            if(searchMap.get("receiverContact")!=null && !"".equals(searchMap.get("receiverContact"))){
                criteria.andLike("receiverContact","%"+searchMap.get("receiverContact")+"%");
            }
            // 收货人手机
            if(searchMap.get("receiverMobile")!=null && !"".equals(searchMap.get("receiverMobile"))){
                criteria.andLike("receiverMobile","%"+searchMap.get("receiverMobile")+"%");
            }
            // 收货人地址
            if(searchMap.get("receiverAddress")!=null && !"".equals(searchMap.get("receiverAddress"))){
                criteria.andLike("receiverAddress","%"+searchMap.get("receiverAddress")+"%");
            }
            // 订单来源：1:web，2：app，3：微信公众号，4：微信小程序  5 H5手机页面
            if(searchMap.get("sourceType")!=null && !"".equals(searchMap.get("sourceType"))){
                criteria.andLike("sourceType","%"+searchMap.get("sourceType")+"%");
            }
            // 交易流水号
            if(searchMap.get("transactionId")!=null && !"".equals(searchMap.get("transactionId"))){
                criteria.andLike("transactionId","%"+searchMap.get("transactionId")+"%");
            }
            // 订单状态
            if(searchMap.get("orderStatus")!=null && !"".equals(searchMap.get("orderStatus"))){
                criteria.andEqualTo("orderStatus",searchMap.get("orderStatus"));
            }
            // 支付状态
            if(searchMap.get("payStatus")!=null && !"".equals(searchMap.get("payStatus"))){
                criteria.andLike("payStatus","%"+searchMap.get("payStatus")+"%");
            }
            // 发货状态
            if(searchMap.get("consignStatus")!=null && !"".equals(searchMap.get("consignStatus"))){
                criteria.andLike("consignStatus","%"+searchMap.get("consignStatus")+"%");
            }
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andLike("isDelete","%"+searchMap.get("isDelete")+"%");
            }

            // 数量合计
            if(searchMap.get("totalNum")!=null ){
                criteria.andEqualTo("totalNum",searchMap.get("totalNum"));
            }
            // 金额合计
            if(searchMap.get("totalMoney")!=null ){
                criteria.andEqualTo("totalMoney",searchMap.get("totalMoney"));
            }
            // 优惠金额
            if(searchMap.get("preMoney")!=null ){
                criteria.andEqualTo("preMoney",searchMap.get("preMoney"));
            }
            // 邮费
            if(searchMap.get("postFee")!=null ){
                criteria.andEqualTo("postFee",searchMap.get("postFee"));
            }
            // 实付金额
            if(searchMap.get("payMoney")!=null ){
                criteria.andEqualTo("payMoney",searchMap.get("payMoney"));
            }

        }
        return example;
    }

}
