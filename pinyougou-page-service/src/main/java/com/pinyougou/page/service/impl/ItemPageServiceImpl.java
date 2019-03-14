package com.pinyougou.page.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    @Value("${pagedir}")
    private String pagedir;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper tbGoodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;

    @Override
    public boolean genItemHtml(Long goodsId) {

        //创建配置对象
        Configuration configuration = freeMarkerConfig.getConfiguration();
        //创建模板对象
        try {
            configuration.setDefaultEncoding("utf-8");
            Template template = configuration.getTemplate("item.ftl");
            Map map = new HashMap();
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);//加载商品表信息
            map.put("goods", goods);
            TbGoodsDesc goodsDesc = tbGoodsDescMapper.selectByPrimaryKey(goodsId);//加载商品规格信息
            map.put("goodsDesc", goodsDesc);
            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            map.put("itemCat1", itemCat1);
            map.put("itemCat2", itemCat2);
            map.put("itemCat3", itemCat3);
            TbItemExample example = new TbItemExample();//读取SKU列表
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");
            criteria.andGoodsIdEqualTo(goodsId);
            example.setOrderByClause("is_default desc");
            List<TbItem> itemsList = itemMapper.selectByExample(example);
            map.put("itemsList", itemsList);
            Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(pagedir + goodsId + ".html")), "utf-8"));
            template.process(map, out);//输出
            out.close();//关闭流
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }


    }
}
