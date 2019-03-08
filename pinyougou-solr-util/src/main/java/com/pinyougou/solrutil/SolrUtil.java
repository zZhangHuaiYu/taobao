package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

import static javafx.scene.input.KeyCode.V;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    public void importItemData() {

        TbItemExample example = new TbItemExample();

        TbItemExample.Criteria criteria = example.createCriteria();

        System.out.println("经过了这里");
        criteria.andStatusEqualTo("1");//已审核

        List<TbItem> itemList = itemMapper.selectByExample(example);

        for (TbItem tbItem : itemList) {
            System.out.println(tbItem.getTitle());
            Map map = JSON.parseObject(tbItem.getSpec(), Map.class);
            tbItem.setSpecMap(map);
        }

        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();

    }


}
