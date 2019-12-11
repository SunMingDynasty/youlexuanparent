app.controller('baseController',function ($scope) {

    //提取json字符串数据中某个属性，返回拼接字符串 逗号分隔
    $scope.jsonToString=function(jsonString,key){

        var json=JSON.parse(jsonString);//将json字符串转换为json对象

        var value="";

        for(var i=0;i<json.length;i++){

            if(i>0){
                value+=","
            }

            value+=json[i][key];
        }

        return value;
    }


    //重新加载列表 数据
    $scope.reloadList=function(){
        //切换页码
        //$scope.findPage( $scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
        $scope.search($scope.paginationConf.currentPage, $scope.paginationConf.itemsPerPage);
    }

    //分页控件配置
    $scope.paginationConf = {
        currentPage: 1,
        totalItems: 10,
        itemsPerPage: 10,
        perPageOptions: [10, 20, 30, 40, 50],
        onChange: function(){
            $scope.reloadList();//重新加载
        }
    };
    //分页

    //创建要删除的id数组
    $scope.selectIds = [];

    $scope.updateSelection = function($event,id){
        //alert("event:"+$event.target.checked+"----id:"+id);
        if($event.target.checked == true){
            $scope.selectIds.push(id);
        }else{
            var index = $scope.selectIds.indexOf(id);
            $scope.selectIds.splice(index,1);
        }
    }

});