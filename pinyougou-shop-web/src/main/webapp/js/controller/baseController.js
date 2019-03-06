app.controller('baseController', function ($scope) {


    //分页空间
    $scope.paginationConf = {
        //当前页码
        currentPage: 1,
        //总记录数
        totalItems: 10,
        //每页记录数
        itemsPerPage: 10,
        //分页选项
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function () {
            $scope.readLoad();
        }
    };


    //分页表单改变时执行的方法
    $scope.readLoad = function () {
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }


    //多选删除
    $scope.selectIds = [];
    $scope.updateSelection = function ($event, id) {

        if ($event.target.checked) {
            $scope.selectIds.push(id)

        } else {
            var idx = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(idx, 1);//删除
        }

    }


    //
    $scope.searchObjectByKey = function (list, key, keyValue) {
        debugger;
        for (var i = 0; i < list.length; i++) {

            if (list[i][key] == keyValue) {

                return list[i];
            }
        }

        return null;
    }


})