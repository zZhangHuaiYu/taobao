app.service("contentService", function ($http) {


    //根据分类的id查询广告

    this.findByCategoryId = function (categoryId) {

        return $http.get("content/findByCategoryId.do?categoryId=" + categoryId);

    }

})