app.service('brandService', function ($http) {
    //查询所有
    this.findAll = function () {
        return $http.get("../brand/findAll.do")
    };

    /* //分页查询
     this.findPage = function (page, size) {

         $http.get('../brand/findPage.do?page=' + page + '&size=' + size)
     };*/
    //数据回显
    this.findOne = function (id) {
        return  $http.get('../brand/findOne.do?id=' + id)

    };
    //添加数据
    this.add = function (entry) {

        return $http.post('../brand/add.do', entry)
    };
    //修改数据
    this.update = function (entry) {

        return $http.post('../brand/add.do', entry)
    };

    //批量删除
    this.dele = function (ids) {
        return $http.get('../brand/delete.do?ids=' + ids)

    };

    //模糊查询
    this.sea= function (page, size, searchEntry) {

        $http.post('../brand/search.do?page=' + page + '&size=' + size, searchEntry)
    }

    this.selectOptionList=function() {
        return $http.get('../brand/selectOptionList.do');
    }
});