app.controller("searchController", function ($scope, searchService, $location) {


    //定义搜索对象的结构，category：商品分类，这里是传给后台的条件
    $scope.searchMap = {
        'keywords': '',
        'category': '',
        'brand': '',
        'spec': {},
        'price': '',
        'pageNo': 1,
        'pageSize': 40,
        'sort': '',
        'sortField': ''

    }
    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;//搜索返回的结果
                buildPageLabel();


            }
        )

    };

    //构建分页栏
    buildPageLabel = function () {
        $scope.pageLabel = [];
        var firstPage = 1;//开始的页码
        var lastPage = $scope.resultMap.totalPages;//截止页码
        $scope.firstDot = true //前面有点
        $scope.lastDot = true //后面有点

        //分页逻辑，前二后二，总共一下显示五页
        //首先判断总页数是不是大于五，如果不是大于5就直接全部显示,大于5才接着判断
        if ($scope.resultMap.totalPages > 5) {
            //如果当前页码小于3，就显示前五页
            if ($scope.searchMap.pageNo <= 3) {
                lastPage = 5;
                $scope.firstDot = false //前面没点
            } else if ($scope.searchMap.pageNo >= $scope.resultMap.totalPages - 2) {//如果当前页码大于最后的页码减2，则显示最后五页
                firstPage = $scope.resultMap.totalPages - 4;
                $scope.lastDot = false // 后面没点
            } else {
                firstPage = $scope.searchMap.pageNo - 2;
                lastPage = $scope.searchMap.pageNo + 2;
            }
        } else {
            //如果总页数小于5，都不显示
            $scope.firstDot = false;
            $scope.lastDot = false;
        }


        for (var i = firstPage; i <= lastPage; i++) {
            $scope.pageLabel.push(i);
        }
    }


    $scope.isTopPage = function () {//判断当前页是不是第一页
        if ($scope.searchMap.pageNo == 1) {
            return true;
        } else {
            return false;
        }

    }

    $scope.isEndPage = function () {//判断当前页是不会死最后一页
        if ($scope.searchMap.pageNo == $scope.resultMap.totalPages) {

            return true;
        } else {
            return false;
        }

    }

    //根据页码查询
    $scope.queryByPage = function (pageNo) {
        if (pageNo < 1 || pageNo > $scope.resultMap.totalPages) {
            return
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }


    $scope.addSearchItem = function (key, value) {
        if (key == 'category' || key == 'brand' || key == 'price') {//如果点击的是商品分类或者是品牌
            $scope.searchMap[key] = value;
        } else {//如果点的是规格
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();
    }

    $scope.removeSearchItem = function (key) {

        if (key == 'category' || key == 'brand' || key == 'price') {//如果点击的是商品分类或者是品牌
            $scope.searchMap[key] = "";
        } else {
            delete $scope.searchMap.spec[key];//清除规格选项
        }
        $scope.search()
    }


    //设置排序规格
    $scope.sortSearch = function (sortField, sort) {
        $scope.searchMap.sort = sort;
        $scope.searchMap.sortField = sortField;
        $scope.search();
    }

    //判断关键字是不是品牌
    $scope.keywordsIsBrand = function () {
        for (var i = 0; i < $scope.resultMap.brandList.length; i++) {
            if ($scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text) >= 0) {//如果包含
                return true;
            }
        }
        return false;
    }

    //接收首页传过来的关键字
    $scope.loadkeywords = function () {
        $scope.searchMap.keywords=  $location.search()['keywords'];
        $scope.search();//调用查询方法

    }
})