app.controller('searchController',function ($scope,$location,searchService) {

    $scope.searchMap = {keywords:'',category:'',brand:'',spec:{},price:''
        ,pageNo:1,pageSize:20,sort:'',sortField:''};


    $scope.loadKeywords = function(){
        //获取9103 传递过来的keywords
        $scope.searchMap.keywords = $location.search()['keywords'];
        $scope.search();
    }

    $scope.keywordsIsBrand = function(){
        for (var i=0;i<$scope.resultMap.brandList.length;i++){
            var num = $scope.searchMap.keywords.indexOf($scope.resultMap.brandList[i].text);
            if(num >= 0){
                return true;
            }
        }
        return false;
    }

    $scope.sortSearch = function(sort,sortField){
        $scope.searchMap.sort = sort;
        $scope.searchMap.sortField = sortField;

        $scope.search();
    }

    $scope.isTopPage = function(){
        if($scope.searchMap.pageNo == 1){
            return true;
        }else{
            return false;
        }
    }

    $scope.isLastPage = function(){
        if($scope.searchMap.pageNo == $scope.resultMap.totalPages){
            return true;
        }else{
            return false;
        }
    }

    //点击分页的查询
    $scope.queryByPage = function(pageNo){
        if(pageNo<1 || pageNo >$scope.resultMap.totalPages){
            return ;
        }
        $scope.searchMap.pageNo = pageNo;
        $scope.search();
    }

    //没有放入在scope中
    buildPageLabel = function(){
        //用于装每页的页码数据
        $scope.pageLable = [];
        //取当前查询共多少页 也就是当前所有数据的最后一页
        var totalPages = $scope.resultMap.totalPages;
        //页码编辑
        //定义页码起始页
        var firstNo = 1;
        //定义页码最后一页
        var lastNo = totalPages;

        //定义省略样式开关
        $scope.firstDot = false;
        $scope.lastDot = false;

        //逻辑运算
        if(totalPages>5){
            if($scope.searchMap.pageNo <= 3){
                lastNo = 5;
                $scope.lastDot = true;
            }else if($scope.searchMap.pageNo >= lastNo - 2 ){
                firstNo = $scope.searchMap.pageNo - 2;
                $scope.firstDot = true;
            }else{
                firstNo = $scope.searchMap.pageNo - 2;
                lastNo = $scope.searchMap.pageNo + 2;

                $scope.firstDot = true;
                $scope.lastDot = true;
            }
        }

        //封装页码栏
        for(var i=firstNo;i<=lastNo;i++){
            $scope.pageLable.push(i);
        }

    }


    $scope.addSearchMap = function(key,value){
        if('category'==key || 'brand'== key || 'price'==key){
            $scope.searchMap[key] = value;
        }else{
            $scope.searchMap.spec[key] = value;
        }
        $scope.search();
    }

    $scope.removeSearchMap = function(key){
        if('category'==key || 'brand'==key || 'price'==key){
            $scope.searchMap[key] = "";
        }else{
            delete $scope.searchMap.spec[key];
        }
        $scope.search();
    }

    $scope.search = function () {
        $scope.searchMap.pageNo = parseInt($scope.searchMap.pageNo);
        searchService.search($scope.searchMap).success(
            function (response) {
                $scope.resultMap = response;
                $scope.list = response.rows
                //封装分页数据
                buildPageLabel();
            }
        );
    }


})