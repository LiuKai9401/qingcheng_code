package com.qingcheng.service.impl;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qingcheng.dao.AlbumMapper;
import com.qingcheng.entity.PageResult;
import com.qingcheng.pojo.goods.Album;
import com.qingcheng.service.goods.AlbumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service(interfaceClass = AlbumService.class)
public class AlbumServiceImpl implements AlbumService {

    @Autowired
    private AlbumMapper albumMapper;

    /**
     * 返回全部记录
     * @return
     */
    public List<Album> findAll() {
        return albumMapper.selectAll();
    }

    /**
     * 分页查询
     * @param page 页码
     * @param size 每页记录数
     * @return 分页结果
     */
    public PageResult<Album> findPage(int page, int size) {
        PageHelper.startPage(page,size);
        Page<Album> albums = (Page<Album>) albumMapper.selectAll();
        return new PageResult<Album>(albums.getTotal(),albums.getResult());
    }

    /**
     * 条件查询
     * @param searchMap 查询条件
     * @return
     */
    public List<Album> findList(Map<String, Object> searchMap) {
        Example example = createExample(searchMap);
        return albumMapper.selectByExample(example);
    }

    /**
     * 分页+条件查询
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    public PageResult<Album> findPage(Map<String, Object> searchMap, int page, int size) {
        PageHelper.startPage(page,size);
        Example example = createExample(searchMap);
        Page<Album> albums = (Page<Album>) albumMapper.selectByExample(example);
        return new PageResult<Album>(albums.getTotal(),albums.getResult());
    }

    /**
     * 根据Id查询
     * @param id
     * @return
     */
    public Album findById(Long id) {
        return albumMapper.selectByPrimaryKey(id);
    }

    /**
     * 新增
     * @param album
     */
    public void add(Album album) {
        albumMapper.insert(album);
    }

    /**
     * 修改
     * @param album
     */
    public void update(Album album) {
        albumMapper.updateByPrimaryKeySelective(album);
    }

    /**
     * 更新图片列表
     * @param album
     */
    public void updateImage(Album album) {
        //1.新增加的图片地址：
        String imageUrl = album.getImageItems();
        //2.查询之前的详细数据
        Album album1 = albumMapper.selectByPrimaryKey(album);
        //3.将字符串转换为json对象
        List<Map> list = JSON.parseArray(album1.getImageItems(), Map.class);
        //4.创建一个与数据库一直的map对象，后续改为对象存储
        Map<String,Object> map = new ConcurrentHashMap<String, Object>();
        map.put("uid", Timestamp.valueOf(LocalDateTime.now()).getTime());
        map.put("status","success");
        map.put("url",imageUrl);
        list.add(map);
        //5.将json对象转换为字符串，再存入数据库中
        album1.setImageItems(JSON.toJSONString(list));
        albumMapper.updateByPrimaryKey(album1);
    }

    /**
     * 删除图片子列表
     * @param id
     * @param imageItemsUid
     */
    @Transactional
    public void deleteImages(String id, Long imageItemsUid) {
        Album album = albumMapper.selectByPrimaryKey(id);
        List<Map> list = JSON.parseArray(album.getImageItems(), Map.class);
        for (Map<String,Object> map : list) {
            Long uid = (Long) map.get("uid");
            if(uid.longValue()==imageItemsUid.longValue()){
               list.remove(map);
                break;
            }
        }
       album.setImageItems(JSON.toJSONString(list));
        albumMapper.updateByPrimaryKey(album);
    }

    /**
     *  删除
     * @param id
     */
    public void delete(Long id) {
        albumMapper.deleteByPrimaryKey(id);
    }

    /**
     * 构建查询条件
     * @param searchMap
     * @return
     */
    private Example createExample(Map<String, Object> searchMap){
        Example example=new Example(Album.class);
        Example.Criteria criteria = example.createCriteria();
        if(searchMap!=null){
            // 相册名称
            if(searchMap.get("title")!=null && !"".equals(searchMap.get("title"))){
                criteria.andLike("title","%"+searchMap.get("title")+"%");
            }
            // 相册封面
            if(searchMap.get("image")!=null && !"".equals(searchMap.get("image"))){
                criteria.andLike("image","%"+searchMap.get("image")+"%");
            }
            // 图片列表
            if(searchMap.get("imageItems")!=null && !"".equals(searchMap.get("imageItems"))){
                criteria.andLike("imageItems","%"+searchMap.get("imageItems")+"%");
            }


        }
        return example;
    }

}
