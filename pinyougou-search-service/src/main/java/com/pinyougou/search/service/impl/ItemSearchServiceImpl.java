package com.pinyougou.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service(timeout = 5000)
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;


    @Override
    public Map<String, Object> search(Map searchMap) {

        /*Map<String, Object> map = new HashMap<>();

        Query query = new SimpleQuery("*:*");

        //按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));

        query.addCriteria(criteria);

        ScoredPage<TbItem> page = solrTemplate.queryForPage(query, TbItem.class);

        map.put("rows", page.getContent());*/

        String keywords = (String) searchMap.get("keywords");
        String replace = keywords.replace(" ", "");//将用户输入的空格置换为字符串
        searchMap.put("keywords", replace);
        if (searchMap.get("keywords").equals("")) {
            return null;
        }


        Map<String, Object> map = new HashMap();

        //查询列表
        map.putAll(searchList(searchMap));

        //分组查询,查询商品分类列表
        List<String> categoryList = searchCategoryList(searchMap);
        map.put("categoryList", categoryList);

        //从缓存中查询出品牌和规格
        String category = (String) searchMap.get("category");
        if (!category.equals("")) {
            map.putAll(searchBrandAndSpecList(category));
        } else {
            if (categoryList.size() > 0) {
                map.putAll(searchBrandAndSpecList(categoryList.get(0)));
            }
        }

        return map;
    }

    //将sku中的数据导入索引库中
    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }

    @Override
    public void deleteByGoodsIds(List goodsIdList) {//删除索引库的数据

        Query query = new SimpleQuery("*:*");
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();

    }


    //查询列表
    private Map searchList(Map searchMap) {

        Map map = new HashMap();
        //实现高亮显示
        HighlightQuery query = new SimpleHighlightQuery();

        //设置高亮选项的域
        HighlightOptions highlightOptions = new HighlightOptions().addField("item_title");

        //设置高亮的前缀后后缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        highlightOptions.setSimplePostfix("</em>");
        //设置高亮选项，将高亮选项的格式加入到查询对象中
        query.setHighlightOptions(highlightOptions);
        //创建查询的条件,按照关键字查询
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //将条件添加到高亮的查询对象里面
        query.addCriteria(criteria);


        //商品分类过滤
        if (!"".equals(searchMap.get("category"))) {//如果用户选择了商品分类
            System.out.println("商品分类过滤了");
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filtercriteria = new Criteria("item_category").is(searchMap.get("category"));
            filterQuery.addCriteria(filtercriteria);
            query.addFilterQuery(filterQuery);
        }

        //品牌列表过滤
        if (!"".equals(searchMap.get("brand"))) {
            System.out.println("品牌过滤啦");
            FilterQuery filterQuery = new SimpleFilterQuery();
            Criteria filtercriteria = new Criteria("item_brand").is(searchMap.get("brand"));
            filterQuery.addCriteria(filtercriteria);
            query.addFilterQuery(filterQuery);
        }

        //按规格过滤
        if (searchMap.get("spec") != null) {
            Map<String, String> specMap = (Map) searchMap.get("spec");//获取品牌的map集合
            if (specMap.size() > 0) {
                System.out.println("规格过滤啦");
                for (String key : specMap.keySet()) {//遍历集合
                    FilterQuery filterQuery = new SimpleFilterQuery();
                    Criteria filtercriteria = new Criteria("item_spec_" + key).is(searchMap.get(key));
                    filterQuery.addCriteria(filtercriteria);
                    query.addFilterQuery(filterQuery);
                }

            }


        }

        //价格过滤
        if (!"".equals(searchMap.get("price"))) {
            String str = (String) searchMap.get("price");
            String[] price = str.split("-");

            if (!price[0].equals("0")) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

            if (!price[1].equals("*")) {
                FilterQuery filterQuery = new SimpleFilterQuery();
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);
            }

        }
        //分页

        Integer pageNo = (Integer) searchMap.get("pageNo");//获取当前页数

        if (pageNo == null) {
            pageNo = 1;//如果前台传过来的页面为空，就默认唯一
        }

        Integer pageSize = (Integer) searchMap.get("pageSize");
        if (pageSize == null) {
            pageSize = 10;//如果传过来的当前记录数为空，就自定义为10
        }

        query.setOffset((pageNo - 1) * pageSize);//设置每页开始的索引
        query.setRows(pageSize);//每页显示多少记录数

        //按价格进行排序


        String sortValue = (String) searchMap.get("sort");//获取升序还是降序
        String sortField = (String) searchMap.get("sortField");//获取排序的域
        if (sortValue != null && !sortValue.equals("")) {
            if (sortValue.equals("ASC")) {
                Sort sort = new Sort(Sort.Direction.ASC, "item_" + sortField);
                query.addSort(sort);
            }

            if (sortValue.equals("DESC")) {
                Sort sort = new Sort(Sort.Direction.DESC, "item_" + sortField);
                query.addSort(sort);
            }
        }

        //返回一个包含高亮标签的集合
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //从包含高亮标签的集合中获取高亮标签和所在域的集合
        List<HighlightEntry<TbItem>> entryList = page.getHighlighted();
        for (HighlightEntry<TbItem> entry : entryList) {

            //遍历集合，获取高亮标签的集合
            List<HighlightEntry.Highlight> highlightList = entry.getHighlights();
          /*  for (HighlightEntry.Highlight h : highlightList) {
                //遍历高亮集合，获取高亮标签
                List<String> sns = h.getSnipplets();
                TbItem tbItem = entry.getEntity();
                tbItem.setTitle(sns.get(0));
            }*/
            if (highlightList.size() > 0 && highlightList.get(0).getSnipplets().size() > 0) {
                TbItem item = entry.getEntity();
                item.setTitle(highlightList.get(0).getSnipplets().get(0));
            }
        }

        map.put("rows", page.getContent());
        map.put("totalPages", page.getTotalPages());//返回总页数
        map.put("total", page.getTotalElements());//返回总记录数
        return map;
    }


    //分组查询,查询商品分类列表
    private List<String> searchCategoryList(Map searchMap) {

        List<String> list = new ArrayList<>();
        Query query = new SimpleQuery("*:*");

        //关键字Where
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);
        //设置分页选项,可以获取多个分页,只需要在后面增加域名称就可以
        GroupOptions groupOptions = new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        //获取分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获取对应的域的分组结果对象
        GroupResult<TbItem> result = page.getGroupResult("item_category");
        //获取分页的入口页
        Page<GroupEntry<TbItem>> entries = result.getGroupEntries();
        //获取分页入口集合
        List<GroupEntry<TbItem>> content = entries.getContent();
        for (GroupEntry<TbItem> entry : content) {
            list.add(entry.getGroupValue());
        }

        return list;

    }

    //查询品牌和规格列表，category分类的名称
    private Map searchBrandAndSpecList(String category) {
        Map map = new HashMap();
        //从缓存中获取模板id
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);
        if (typeId != null) {
            //根据查询出的模板id，从缓存中查询出品牌和规格
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(typeId);
            map.put("brandList", brandList);

            List specList = (List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList", specList);

        }
        return map;

    }


}
