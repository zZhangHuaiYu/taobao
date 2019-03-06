app.controller('indexController', function ($scope, loginService, $controller) {

    //读取登录人
    $scope.showName = function () {

        loginService.loginName().success(
            function (response) {

                $scope.loginName = response.loginName;

            })
    }


})