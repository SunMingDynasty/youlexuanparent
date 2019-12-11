app.controller('brandController',function($controller,$scope,$http,brandService){

    //父控制器的继承
    $controller('baseController',{$scope:$scope});//继承

    $scope.save = function () {

        var id = $scope.entity.id;

        var methodName = "add";

        if(id != null){
            methodName = "update";
        }

        brandService.save(methodName,$scope.entity).success(
            function (response) {
                if(response.success){
                    $scope.reloadList();
                }else{
                    alert(response.message);
                }

            }
        );
    }

    $scope.findOne = function(id){
        brandService.findOne(id).success(
            function (response) {
                $scope.entity = response;
            }
        );
    }

    $scope.delete = function () {
        brandService.delete($scope.selectIds).success(
            function(response){
                if(response.success){
                    $scope.reloadList();
                }else{
                    alert($scope.message);
                }
            }
        );
    }

    $scope.searchEntity = {};

    $scope.search = function (page,rows) {
        brandService.search(page,rows,$scope.searchEntity).success(
            function (response) {
                $scope.paginationConf.totalItems = response.total;//总记录数
                $scope.list = response.rows;//给列表变量赋值
            }
        );
    }

});