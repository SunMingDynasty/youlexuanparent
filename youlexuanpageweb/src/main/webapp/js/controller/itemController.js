app.controller('itemController',function($scope,$http){
	
	$scope.addNum = function(x){
		$scope.num = $scope.num + x; 
		
		if($scope.num < 1 ){
			$scope.num = 1;
		}
		
	}

	$scope.specificationItems = {};
	
	$scope.selectSpec = function(name,value){
		$scope.specificationItems[name] = value;
		searchSku();
		
	}
	
	$scope.isSelected = function(name,value){
		if($scope.specificationItems[name] == value){
			return true;
		}else{
			return false;
		}
		
	}

	
	//加载默认SKU
	$scope.loadSku=function(){
		$scope.sku=specList[0];		
		$scope.specificationItems= JSON.parse(JSON.stringify($scope.sku.spec)) ;
	}
	// 选择sku
	matchObject=function(map1,map2){	
		for(var k in map1){
			if(map1[k]!=map2[k]){
				return false;
			}			
		}
		for(var k in map2){
			if(map2[k]!=map1[k]){
				return false;
			}			
		}
		return true;		
	}
	
	//查询SKU
	searchSku=function(){
		for(var i=0;i<specList.length;i++ ){
			if( matchObject(specList[i].spec ,$scope.specificationItems ) ){
				$scope.sku=specList[i];
				return ;
			}			
		}	
		$scope.sku={id:0,title:'--------',price:0};//如果没有匹配的		
	}
	// 选择sku
	
	$scope.addCart = function(){
		$http.get('http://localhost:9107/cart/addGoodsToCart.do?itemId='+$scope.sku.id+"&num="+$scope.num,{'withCredentials':true}).success(
			function (response) {
				if(response.success){
					location.href='http://localhost:9107/cart.html';
				}else{
					alert(response.message);
				}
			}
		);
	}

	
});