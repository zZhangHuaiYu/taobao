package com.pinyougou.sellergoods.service.impl;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojoGroups.Goods;
import com.pinyougou.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import entity.PageResult;

/**
 * 服务实现层
 *
 * @author Administrator
 */
@Service
public class GoodsServiceImpl implements GoodsService {

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbBrandMapper brandMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbSellerMapper sellerMapper;

    @Autowired
    private TbItemMapper itemMapper;

    /**
     * 查询全部
     */
    @Override
    public List<TbGoods> findAll() {
        return goodsMapper.selectByExample(null);
    }

    /**
     * 按分页查询
     */
    @Override
    public PageResult findPage(int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);
        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(null);
        return new PageResult(page.getTotal(), page.getResult());
    }

    /**
     * 增加
     */
    @Override
    public void add(Goods goods) {

        goods.getGoods().setAuditStatus("0");
        goodsMapper.insert(goods.getGoods()); //插入商品表
        goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
        goodsDescMapper.insert(goods.getGoodsDesc());//插入商品扩展数据
        saveItemList(goods);
    }

    private void setItemValus(Goods goods, TbItem item) {
        item.setGoodsId(goods.getGoods().getId());//商品 SPU 编号
        item.setSellerId(goods.getGoods().getSellerId());//商家编号
        item.setCategoryid(goods.getGoods().getCategory3Id());//商品分类编号（3 级）
        item.setCreateTime(new Date());//创建日期
        item.setUpdateTime(new Date());//修改日期
//品牌名称
        TbBrand brand =
                brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
        item.setBrand(brand.getName());
//分类名称
        TbItemCat itemCat =
                itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
        item.setCategory(itemCat.getName());
//商家名称
        TbSeller seller =
                sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
        item.setSeller(seller.getNickName());
//图片地址（取 spu 的第一个图片）
        List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(),
                Map.class);
        if (imageList.size() > 0) {
            item.setImage((String) imageList.get(0).get("url"));
        }
    }


    private void saveItemList(Goods goods) {

        if ("1".equals(goods.getGoods().getIsEnableSpec())) {
            for (TbItem item : goods.getItemList()) {
//标题
                String title = goods.getGoods().getGoodsName();
                Map<String, Object> specMap = JSON.parseObject(item.getSpec());
                for (String key : specMap.keySet()) {
                    title += " " + specMap.get(key);
                }
                item.setTitle(title);
                setItemValus(goods, item);
                itemMapper.insert(item);
            }
        } else {
            TbItem item = new TbItem();
            item.setTitle(goods.getGoods().getGoodsName());//商品 KPU+规格描述串作为SKU 名称
            item.setPrice(goods.getGoods().getPrice());//价格
            item.setStatus("1");//状态
            item.setIsDefault("1");//是否默认
            item.setNum(99999);//库存数量
            item.setSpec("{}");
            setItemValus(goods, item);
            itemMapper.insert(item);
        }

    }

    /**
     * 修改
     */
    @Override
    public void update(Goods goods) {

        //更新基本表数据
        goodsMapper.updateByPrimaryKey(goods.getGoods());

        //获取扩展表数据
        goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

        //删除原有SKU表中的数据
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(goods.getGoods().getId());
        itemMapper.deleteByExample(example);

        //插入新的SKU方法
        saveItemList(goods);

    }

    /**
     * 根据ID获取实体
     *
     * @param id
     * @return
     */
    @Override
    public Goods findOne(Long id) {
        Goods goods = new Goods();

        //根据Id查询出Goods表的信息，并设置到Goods类中
        TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
        goods.setGoods(tbGoods);

        //根据Id查询出GoodsDesc表中的信息并将表中数据设置到goods中
        TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
        goods.setGoodsDesc(tbGoodsDesc);

        //查询SKU商品列表

        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        criteria.andGoodsIdEqualTo(id);
        List<TbItem> items = itemMapper.selectByExample(example);
        goods.setItemList(items);

        return goods;
    }

    /**
     * 批量删除
     */
    @Override
    public void delete(Long[] ids) {
        for (Long id : ids) {
            //查询出数据库中的数据
            TbGoods goods = goodsMapper.selectByPrimaryKey(id);

            //将商品id设置为1
            goods.setIsDelete("1");

            //更新商品的信息
            goodsMapper.updateByPrimaryKey(goods);
        }
    }


    @Override
    public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
        PageHelper.startPage(pageNum, pageSize);

        TbGoodsExample example = new TbGoodsExample();
        TbGoodsExample.Criteria criteria = example.createCriteria();
        //指定查询条件为逻辑删除
        criteria.andIsDeleteIsNull();
        if (goods != null) {

            if (goods.getSellerId() != null && goods.getSellerId().length() > 0) {

                //修改条件查询的语句，将模糊查询更改为精确查询，目的是为了防止
                // criteria.andSellerIdLike("%" + goods.getSellerId() + "%");
                criteria.andSellerIdEqualTo(goods.getSellerId());
            }
            if (goods.getGoodsName() != null && goods.getGoodsName().length() > 0) {
                criteria.andGoodsNameLike("%" + goods.getGoodsName() + "%");
            }
            if (goods.getAuditStatus() != null && goods.getAuditStatus().length() > 0) {
                criteria.andAuditStatusLike("%" + goods.getAuditStatus() + "%");
            }
            if (goods.getIsMarketable() != null && goods.getIsMarketable().length() > 0) {
                criteria.andIsMarketableLike("%" + goods.getIsMarketable() + "%");
            }
            if (goods.getCaption() != null && goods.getCaption().length() > 0) {
                criteria.andCaptionLike("%" + goods.getCaption() + "%");
            }
            if (goods.getSmallPic() != null && goods.getSmallPic().length() > 0) {
                criteria.andSmallPicLike("%" + goods.getSmallPic() + "%");
            }
            if (goods.getIsEnableSpec() != null && goods.getIsEnableSpec().length() > 0) {
                criteria.andIsEnableSpecLike("%" + goods.getIsEnableSpec() + "%");
            }
            if (goods.getIsDelete() != null && goods.getIsDelete().length() > 0) {
                criteria.andIsDeleteLike("%" + goods.getIsDelete() + "%");
            }

        }

        Page<TbGoods> page = (Page<TbGoods>) goodsMapper.selectByExample(example);
        return new PageResult(page.getTotal(), page.getResult());
    }

    //批量修改状态
    @Override
    public void updateStatus(Long[] ids, String status) {
        for (Long id : ids) {

            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setAuditStatus(status);
            goodsMapper.updateByPrimaryKey(tbGoods);


        }

    }

    @Override
    public void updateMarketable(Long[] ids, String status) {
        for (Long id : ids) {
            TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
            tbGoods.setIsMarketable(status);
            goodsMapper.updateByPrimaryKey(tbGoods);
        }

    }

    //根据商品ID和状态查询Item表信息
    @Override
    public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
        TbItemExample example = new TbItemExample();
        com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
        System.out.println(goodsIds);
        criteria.andGoodsIdIn(Arrays.asList(goodsIds));//指定条件,SPUid集合
        criteria.andStatusEqualTo(status);
        List<TbItem> tbItems = itemMapper.selectByExample(example);
        System.out.println(tbItems);
        return tbItems;
    }
}
