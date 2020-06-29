package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.*;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.*;
import com.qingcheng.service.goods.SpuService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import src.com.qingcheng.util.IdWorker;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.Map;

@Service(interfaceClass = SpuService.class)
public class SpuServiceImpl implements SpuService {

    @Autowired
    private SpuMapper spuMapper;
    @Autowired
    private SkuMapper skuMapper;

    @Autowired
    private IdWorker idWorker;

    @Autowired
    private CategoryMapper CategoryMapper;

    @Autowired
    private CategoryBrandMapper categoryBrandMapper;

    @Autowired
    private AuditMapper auditMapper;
    /**
     * 返回全部记录
     * @return
     */
    public List<Spu> findAll() {
        return spuMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Spu> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectAll();
        return new PageResult<Spu>(spus.getTotal(),spus.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Spu> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return spuMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Spu> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Spu> spus = (Page<Spu>) spuMapper.selectByExample(example);
        return new PageResult<Spu>(spus.getTotal(),spus.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Spu findById(String id) {
        return spuMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param spu
     */
    public void add(Spu spu) {
        spuMapper.insert(spu);
    }

    /**
     * 修改
     * @param spu
     */
    public void update(Spu spu) {
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     *  删除
     * @param id
     */
    public void delete(String id) {
        spuMapper.deleteByPrimaryKey(id);
    }

    /**
     * 保存商品信息&更新商品信息
     * @param goods
     */
    @Override
    @Transactional
    public void saveGoods(Goods goods) {
        Spu spu = goods.getSpu();

        //4.判断当前是保存还是更新
        if(spu.getId()==null){
            //1.设置分布式id
            spu.setId(idWorker.nextId()+"");
            spuMapper.insert(spu);
        }else {
            //1.说明是更新，更新前删除现有SKU列表
            Example example = new Example(Sku.class);
            Example.Criteria criteria = example.createCriteria();
            criteria.andEqualTo("spuId",spu.getId());
            skuMapper.selectByExample(example);
            //2.更新现有SPU列表
            spuMapper.updateByPrimaryKey(spu);
        }

        //2.设置spu的保存
        //2.1.1 获取当前系统时间
        Date date = new Date();
        //2.1.2 获取三级列表对应的name值，需查询template表
        Category category = CategoryMapper.selectByPrimaryKey(spu.getCategory3Id());
        List<Sku> skuList = goods.getSkuList();
        for (Sku sku : skuList) {
            //5.判断当前SKU列表中是否存在id
            if (sku.getId()==null){
                //2.1 设置sku的Id&创建时间
                sku.setId(idWorker.nextId()+"");
                sku.setCreateTime(date);

            }

            //6.如果不存在规格的情况下，需做处理
            if (sku.getSpec()==null || sku.getSpec().equals("")){
                //置为{}空字符串，保证json转换时不报错
                sku.setSpec("{}");
            }


            //2.2 设置spu的外键
            sku.setSpuId(spu.getId());
            //2.3 设置sku的name:规则：根据spu的名称+规格值列表
            String name = spu.getName();
            //2.4 进行Json字符串转json对象， {"颜色":"红","机身内存":"64G"}
            Map<String,String> map = JSON.parseObject(sku.getSpec(), Map.class);
            for (String value : map.values()) {
                name += " "+value;
            }
            sku.setName(name);
            //2.5 设置创建时间与更新时间
            sku.setUpdateTime(date);
            //2.6 设置三级列表的id值
            sku.setCategoryId(spu.getCategory3Id());
            //2.7 设置三级列表的name值，需查询template表
            sku.setCategoryName(category.getName());
            //2.8 设置销量与评论数量
            sku.setSaleNum(0);
            sku.setCommentNum(0);
            skuMapper.insert(sku);
        }

        //3.在保存时，创建品牌与分类的关联
        CategoryBrand categoryBrand = new CategoryBrand();
        categoryBrand.setBrandId(spu.getBrandId());
        categoryBrand.setCategoryId(spu.getCategory3Id());
        //3.1 根据保存的品牌ID与三级分类ID，判断是否已存在中间表中
        int selectCount = categoryBrandMapper.selectCount(categoryBrand);
        //3.2 如果存在说明已经存在关系，不需要再次创建
        if (selectCount==0){
            categoryBrandMapper.insert(categoryBrand);
        }

    }

    /**
     * 根据id查询商品
     * @param id
     * @return
     */
    @Override
    public Goods findGoodsById(String id) {
        //1.查询spu对象
        Spu spu = spuMapper.selectByPrimaryKey(id);

        //2.查询sku对象
        Example  example = new Example(Sku.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("spuId",id);
        List<Sku> skuList = skuMapper.selectByExample(example);

        //3.封装goods对象
        Goods goods = new Goods();
        goods.setSpu(spu);
        goods.setSkuList(skuList);
        return goods;
    }

    /**
     * 商品审核
     * @param id
     * @param status
     * @param message
     */
    @Override
    @Transactional
    public void audit(String id, String status, String message) {
        //1.修改审核状态
        Spu spu = new Spu();
        //1.1 设置id,不采用查询数据库
        spu.setId(id);
        //1.2 更新审核状态
        spu.setStatus(status);
        if ("1".equals(status)){
            //1.3 如何是审核通过，自动上架
            spu.setIsMarketable("1");
        }
        //1.4 更新回数据库中
        spuMapper.updateByPrimaryKeySelective(spu);

        //2.记录审核详情
        Audit audit = new Audit();
        audit.setAuditDate(new Date());
        audit.setAuditStatus(status);
        audit.setAuditMessage(message);
        audit.setAuditName("admin");
        auditMapper.insert(audit);

        //3.记录日志

    }

    /**
     * 下架商品
     * @param id
     */
    @Override
    public void pull(String  id) {
        Spu spu = new Spu();
        spu.setId(id);
        spu.setIsMarketable("0");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 上架商品
     * @param id
     */
    @Override
    public void put(String id) {
        Spu spu = spuMapper.selectByPrimaryKey(id);
        if(!"1".equals(spu.getStatus())){
            throw new RuntimeException("此商品未通过审核！");
        }
        spu.setIsMarketable("1");
        spuMapper.updateByPrimaryKeySelective(spu);
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Spu.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 主键
            if(searchMap.get("id")!=null && !"".equals(searchMap.get("id"))){
                criteria.andLike("id","%"+searchMap.get("id")+"%");
            }
            // 货号
            if(searchMap.get("sn")!=null && !"".equals(searchMap.get("sn"))){
                criteria.andLike("sn","%"+searchMap.get("sn")+"%");
            }
            // SPU名
            if(searchMap.get("name")!=null && !"".equals(searchMap.get("name"))){
                criteria.andLike("name","%"+searchMap.get("name")+"%");
            }
            // 副标题
            if(searchMap.get("caption")!=null && !"".equals(searchMap.get("caption"))){
                criteria.andLike("caption","%"+searchMap.get("caption")+"%");
            }
            // 图片
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
            }
            // 图片列表
            if(searchMap.get("images")!=null && !"".equals(searchMap.get("images"))){
                criteria.andLike("images","%"+searchMap.get("images")+"%");
            }
            // 售后服务
            if(searchMap.get("saleService")!=null && !"".equals(searchMap.get("saleService"))){
                criteria.andLike("saleService","%"+searchMap.get("saleService")+"%");
            }
            // 介绍
            if(searchMap.get("introduction")!=null && !"".equals(searchMap.get("introduction"))){
                criteria.andLike("introduction","%"+searchMap.get("introduction")+"%");
            }
            // 规格列表
            if(searchMap.get("specItems")!=null && !"".equals(searchMap.get("specItems"))){
                criteria.andLike("specItems","%"+searchMap.get("specItems")+"%");
            }
            // 参数列表
            if(searchMap.get("paraItems")!=null && !"".equals(searchMap.get("paraItems"))){
                criteria.andLike("paraItems","%"+searchMap.get("paraItems")+"%");
            }
            // 是否上架
            if(searchMap.get("isMarketable")!=null && !"".equals(searchMap.get("isMarketable"))){
                criteria.andLike("isMarketable","%"+searchMap.get("isMarketable")+"%");
            }
            // 是否启用规格
            if(searchMap.get("isEnableSpec")!=null && !"".equals(searchMap.get("isEnableSpec"))){
                criteria.andLike("isEnableSpec","%"+searchMap.get("isEnableSpec")+"%");
            }
            // 是否删除
            if(searchMap.get("isDelete")!=null && !"".equals(searchMap.get("isDelete"))){
                criteria.andLike("isDelete","%"+searchMap.get("isDelete")+"%");
            }
            // 审核状态
            if(searchMap.get("status")!=null && !"".equals(searchMap.get("status"))){
                criteria.andLike("status","%"+searchMap.get("status")+"%");
            }

            // 品牌ID
            if(searchMap.get("brandId")!=null ){
                criteria.andEqualTo("brandId",searchMap.get("brandId"));
            }
            // 一级分类
            if(searchMap.get("category1Id")!=null ){
                criteria.andEqualTo("category1Id",searchMap.get("category1Id"));
            }
            // 二级分类
            if(searchMap.get("category2Id")!=null ){
                criteria.andEqualTo("category2Id",searchMap.get("category2Id"));
            }
            // 三级分类
            if(searchMap.get("category3Id")!=null ){
                criteria.andEqualTo("category3Id",searchMap.get("category3Id"));
            }
            // 模板ID
            if(searchMap.get("templateId")!=null ){
                criteria.andEqualTo("templateId",searchMap.get("templateId"));
            }
            // 运费模板id
            if(searchMap.get("freightId")!=null ){
                criteria.andEqualTo("freightId",searchMap.get("freightId"));
            }
            // 销量
            if(searchMap.get("saleNum")!=null ){
                criteria.andEqualTo("saleNum",searchMap.get("saleNum"));
            }
            // 评论数
            if(searchMap.get("commentNum")!=null ){
                criteria.andEqualTo("commentNum",searchMap.get("commentNum"));
            }

        }
        return example;
    }

}
