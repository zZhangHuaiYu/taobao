app.controller("brandController", function ($scope, $http, brandService, $controller) {


    /*继承*/
    $controller('baseController', {$scope: $scope});


    $scope.findAll = function () {
        brandService.findAll().success(
            function (response) {
                $scope.list = response;
            }
        );
    }


    $scope.findPage = function (page, size) {
        brandService.findPage(page, size).success(
            function (response) {
                $scope.list = response.rows;//显示当前页的数据
                $scope.paginationConf.totalItems = response.total;//修改总记录数，根据总记录数获取前段的分页
            })
    }


    $scope.save = function () {
        var Object = null;
        if ($scope.entry.id != null) {//如果有 ID

            Object = brandService.update($scope.entry);//则执行修改方法
        } else {

            Object = brandService.add($scope.entry);
        }

        Object.success(
            function (response) {
                if (response.success) {
                    $scope.readLoad();
                } else {
                    alert(response.message)
                }

            }
        )
    }

    $scope.findOne = function (id) {
        brandService.findOne(id).success(
            function (response) {
                $scope.entry = response;
            }
        )

    }


    $scope.dele = function () {
        brandService.dele($scope.selectIds).success(
            function (response) {
                if (response.success) {
                    $scope.readLoad();
                } else {
                    alert($scope.message)
                }

            }
        )
    }

    $scope.searchEntry = {};
    $scope.search = function (page, size) {
        $http.post('../brand/search.do?page=' + page + '&size=' + size, $scope.searchEntry).success(
            function (response) {
                $scope.list = response.rows;
                $scope.paginationConf.totalItems = response.total;//修改总记录数，根据总记录数获取前段的分页


            }
        )
    }


})
