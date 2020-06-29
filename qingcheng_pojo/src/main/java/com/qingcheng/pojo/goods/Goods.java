package com.qingcheng.pojo.goods;

import java.io.Serializable;
import java.util.List;

/**
 * 商品组合类
 */
public class Goods implements Serializable {

    //SPU的数据
    private Spu spu;
    //SKU的数据
    private List<Sku> skuList;

    public Spu getSpu() {
        return spu;
    }

    public void setSpu(Spu spu) {
        this.spu = spu;
    }

    public List<Sku> getSkuList() {
        return skuList;
    }

    public void setSkuList(List<Sku> skuList) {
        this.skuList = skuList;
    }
}
