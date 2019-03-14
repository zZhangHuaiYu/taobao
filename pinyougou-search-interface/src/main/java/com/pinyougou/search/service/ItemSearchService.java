package com.pinyougou.search.service;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {

    public Map<String,Object> search(Map searchMap);

    //导入sku的数据
    public void importList(List list);

    public void deleteByGoodsIds(List goodsIdList);
}
