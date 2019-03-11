app.controller("searchController", function ($scope, searchService) {


    //定义搜索对象的结构，category：商品分类，这里是传给后台的条件
    $scope.searchMap = {'keywords': '', 'category': '', 'brand': '', 'spec': {}};//搜索对象

    $scope.search = function () {
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;//搜索返回的结果
            }
        )

    }


    $scope.addSearchItem = function (key, value) {

        if (key == 'category' || key == 'brand') {//如果点击的是商品分类或者是品牌
            $scope.searchMap[key] = value;
        } else {//如果点的是规格
            $scope.searchMap.spec[key] = value;

        }
        $scope.search();

    }

    $scope.removeSearchItem = function (key) {

        if (key == 'category' || key == 'brand') {//如果点击的是商品分类或者是品牌
            $scope.searchMap[key] = "";
        } else {
            delete $scope.searchMap.spec[key];//清除规格选项
        }

        $scope.search()
    }

})