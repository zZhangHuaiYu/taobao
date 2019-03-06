package com.pinyougou.sellergoods.service;

import java.util.List;
import java.util.Map;

import com.pinyougou.pojo.TbBrand;
import entity.PageResult;

/**
 * 品牌接口
 *
 * @author Administrator
 */
public interface BrandService {

    //查询所有
    public List<TbBrand> findAll();

    //分页
   // PageResult findPage(int pageNum, int pageSize);

    //添加品牌
    void add(TbBrand tbBrand);

    //数据辉县
    public TbBrand findOne(Long id);

    //修改
    void update(TbBrand tbBrand);

    //删除数据
    void delete(Long[] ids);


    //进行模糊查询
    PageResult findPage(TbBrand tbBrand,int pageNum, int pageSize);

    public List<Map> selectOptionList();

}
