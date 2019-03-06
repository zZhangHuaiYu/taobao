//控制层
app.controller('goodsController', function ($scope, $controller, goodsService, uploadService, itemCatService, typeTemplateService, $location) {

    $controller('baseController', {$scope: $scope});//继承

    //读取列表数据绑定到表单中  
    $scope.findAll = function () {
        goodsService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }

    //分页
    $scope.findPage = function (page, rows) {
        goodsService.findPage(page, rows).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //修改时，获取回显值
    $scope.findOne = function () {
        //获取传过来的Id值
        var id = $location.search()['id'];

        if (id == null) {
            return;
        }

        goodsService.findOne(id).success(
            function (response) {
                $scope.entity = response;

                //向富文本编辑器添加商品介绍
                editor.html($scope.entity.goodsDesc.introduction);

                //显示图片信息,将图片信息字符串转换为json字符串
                $scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
                //将扩展信息字符串转换为json字符串
                $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);

                //将规格信息字符串转换为json字符串
                $scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);

                //SKU列表规格的转换
                for (var i = 0; i < $scope.entity.itemList.length; i++) {
                    $scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
                }
            }
        );
    }

    //保存
    $scope.save = function () {
        //提取文本编辑器的值
        $scope.entity.goodsDesc.introduction = editor.html();
        var serviceObject;//服务层对象
        if ($scope.entity.goods.id != null) {//如果有ID
            serviceObject = goodsService.update($scope.entity); //修改
        } else {
            serviceObject = goodsService.add($scope.entity);//增加
        }
        serviceObject.success(
            function (response) {
                if (response.success) {
                    alert('保存成功');
                    location.href="goods.html"
                } else {
                    alert(response.message);
                }
            }
        );
    }

    //增加商品
    $scope.add = function (entity) {

        $scope.entity.goodsDesc.introduction = editor.html();

        goodsService.add($scope.entity).success(
            function (response) {
                if (response.success) {

                    alert("增加成功")
                    //清空表单内容
                    $scope.entity = {};
                    //清空文本编辑器
                    editor.html('');

                } else {
                    alert(response.message);
                }
            }
        );
    }


    //批量删除
    $scope.dele = function () {
        //获取选中的复选框
        goodsService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.readLoad();//刷新列表
                    $scope.selectIds = [];
                }
            }
        );
    }

    $scope.searchEntity = {};//定义搜索对象

    //搜索
    $scope.search = function (page, rows) {
        goodsService.search(page, rows, $scope.searchEntity).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//更新总记录数
            }
        );
    }

    //上传图片
    $scope.uploadFile = function () {
        uploadService.uploadFile().success(
            function (response) {
                //如果上传成功，去除url
                if (response.success) {
                    alert(response.message)
                    $scope.image_entity.url = response.message;//设置文
                } else {
                    alert(response.message);
                }


            }
        )
    }

    $scope.entity = {goods: {}, goodsDesc: {itemImages: [], specificationItems: []}};

    $scope.add_image_entity = function () {

        $scope.entity.goodsDesc.itemImages.push($scope.image_entity);

    }

    $scope.update_image_entity = function (index) {
        $scope.entity.goodsDesc.itemImages.splice(index)


    }

    $scope.todoSomething = function ($event) {
        if ($event.keyCode == 13) {//回车
            location.href = "/false.html"
        }
    }

    //实现一级下拉列表

    $scope.selectItemCat1List = function () {
        debugger;
        itemCatService.findByParentId(0).success(
            function (response) {
                $scope.itemCat1List = response;
            }
        );
    }

    //实现二级分类和三级分类，监控下拉列表的变化，根据这个下拉列表的变化，获取他的parentId值
    $scope.$watch('entity.goods.category1Id', function (newValue, oldValue) {

        itemCatService.findByParentId(newValue).success(
            function (response) {

                $scope.itemCat2List = response;

            }
        )


    })

    $scope.$watch('entity.goods.category2Id', function (newValue, oldValue) {


        itemCatService.findByParentId(newValue).success(
            function (response) {

                $scope.itemCat3List = response;

            }
        )

    })

    //返回模板id
    $scope.$watch('entity.goods.category3Id', function (newValue, oldValue) {

        itemCatService.findOne(newValue).success(
            function (response) {

                $scope.entity.goods.typeTemplateId = response.typeId; //更新模板 ID

            }
        )

    })

    //监控模板id的变化，查询模板列表
    $scope.$watch('entity.goods.typeTemplateId', function (newValue, oldValue) {
        typeTemplateService.findOne(newValue).success(
            function (response) {
                $scope.typeTemplate = response;//获取类型
                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);//品牌列表

                //扩展属性列表,如果id为空就执行该方法
                if ($location.search()['id'] == null) {
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);

                }
            }
        )
        //显示规格信息
        typeTemplateService.findSpecList(newValue).success(
            function (response) {
                $scope.specList = response;

            }
        )
    })


    $scope.updateSpecAttribute = function ($event, name, value) {
        var object = $scope.searchObjectByKey(
            $scope.entity.goodsDesc.specificationItems, 'attributeName', name);
        if (object != null) {
            if ($event.target.checked) {
                object.attributeValue.push(value);
            } else {//取消勾选
                object.attributeValue.splice(object.attributeValue.indexOf(value), 1);//移除选项
                //如果选项都取消了，将此条记录移除
                if (object.attributeValue.length == 0) {
                    $scope.entity.goodsDesc.specificationItems.splice(
                        $scope.entity.goodsDesc.specificationItems.indexOf(object), 1);
                }
            }
        } else {
            $scope.entity.goodsDesc.specificationItems.push(
                {"attributeName": name, "attributeValue": [value]});
        }
    }


    //创建 SKU 列表
    $scope.createItemList = function () {
        $scope.entity.itemList = [{spec: {}, price: 0, num: 99999, status: '0', isDefault: '0'}]
        ;//初始
        var items = $scope.entity.goodsDesc.specificationItems;
        for (var i = 0; i < items.length; i++) {
            $scope.entity.itemList =
                addColumn($scope.entity.itemList, items[i].attributeName, items[i].attributeValue);
        }
    }
//添加列值
    addColumn = function (list, columnName, conlumnValues) {
        var newList = [];//新的集合
        for (var i = 0; i < list.length; i++) {
            var oldRow = list[i];
            for (var j = 0; j < conlumnValues.length; j++) {
                var newRow = JSON.parse(JSON.stringify(oldRow));//深克隆
                newRow.spec[columnName] = conlumnValues[j];
                newList.push(newRow);
            }
        }
        return newList;
    }


    //项目第七天代码
    //商品的状态
    $scope.status = ['未审核', '申请中', '审核通过', '已驳回'];


    //显示分类
    $scope.itemCatList = [];
    debugger;
    $scope.findItemCatList = function () {

        itemCatService.findAll().success(
            function (response) {
                for (var i = 0; i < response.length; i++) {

                    //需要根据id获取分类的名称。在以数组返回到页面上
                    $scope.itemCatList[response[i].id] = response[i].name;
                }

            }
        )

    }

    $scope.checkAttributeValue = function (specName, optionName) {
        debugger;
        //获取规格的集合
        var items = $scope.entity.goodsDesc.specificationItems;
        var object = $scope.searchObjectByKey(items, 'attributeName', specName);
        if (object != null) {
            if (object.attributeValue.indexOf(optionName) >= 0) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }

    }


});
