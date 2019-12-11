 //控制层 
app.controller('goodsController' ,function($scope,$controller,$location,goodsService,uploadService,itemCatService,typeTemplateService){
	
	$controller('baseController',{$scope:$scope});//继承

	$scope.status=['未审核','已审核','审核未通过','关闭'];//商品状态

	$scope.itemCatList2 = [];

	$scope.findItemCatList = function(){
		itemCatService.findAll().success(
			function (response) {
				for(var i=0;i<response.length;i++){
					$scope.itemCatList2[response[i].id] = response[i].name;
				}
			}
		);
	}

	//定义 js中的 商品组对象
	$scope.entity = {
		goods:{},
		goodsDesc:{
			itemImages:[],
			specificationItems:[]
		}
	};

	$scope.findByParentId = function(parentId){
		itemCatService.findByParentId(parentId).success(
			function (response) {
				$scope.itemCat1List = response;
			}
		);
	}

	$scope.$watch('entity.goods.category1Id',function (newValue) {
		itemCatService.findByParentId(newValue).success(
			function (response) {
				$scope.itemCat2List = response;
			}
		);
	})

	$scope.$watch('entity.goods.category2Id',function (newValue) {
		itemCatService.findByParentId(newValue).success(
			function (response) {
				$scope.itemCat3List = response;
			}
		);
	})

	$scope.$watch('entity.goods.category3Id',function (newValue) {
		itemCatService.findOne(newValue).success(
			function (response) {
				$scope.entity.goods.typeTemplateId = response.typeId;
			}
		);
	})

	$scope.$watch('entity.goods.typeTemplateId',function (newValue) {
		typeTemplateService.findOne(newValue).success(
            function (response) {
                $scope.typeTemplate = response;
                $scope.typeTemplate.brandIds = JSON.parse($scope.typeTemplate.brandIds);
                if($location.search()['id'] == null){
                    $scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.typeTemplate.customAttributeItems);
                    $scope.typeTemplate.specIds = JSON.parse($scope.typeTemplate.specIds);
                }

            }
        );

        typeTemplateService.findSpecByTypeId(newValue).success(
            function(response){
				$scope.specList = response;
            }
        );
		$scope.entity.goodsDesc.specificationItems = [];

	})

	// 从集合中按照key查询对象
	$scope.searchObjectByKey = function(list, key, keyValue) {
		for (var i = 0; i < list.length; i++) {
			if (list[i][key] == keyValue) {
				return list[i];
			}
		}
		return null;
	}

	$scope.updateSpecAttribute = function($event, name, value) {

		var object = $scope.searchObjectByKey($scope.entity.goodsDesc.specificationItems, 'attributeName',name);

		if (object != null) {
			if ($event.target.checked) {
				object.attributeValue.push(value);
			} else {
				// 取消勾选
				object.attributeValue.splice(object.attributeValue.indexOf(value) ,1);//移除选项
				// 如果选项都取消了，将此条记录移除
				if (object.attributeValue.length == 0) {
					$scope.entity.goodsDesc.specificationItems.splice(
						$scope.entity.goodsDesc.specificationItems
							.indexOf(object), 1);
				}
			}
		} else {
			$scope.entity.goodsDesc.specificationItems.push({
				"attributeName" : name,
				"attributeValue" : [ value ]
			});
		}
	}

	//创建SKU列表
	$scope.createItemList=function(){
		//定义 数组
		$scope.entity.itemList=[{spec:{},price:0,num:99999,status:'0',isDefault:'0'}];//初始
		//勾选SKU
		//[{"attributeName":"机身内存","attributeValue":["64G","32G"]},{"attributeName":"网络","attributeValue":["移动3G","移动4G"]}]
		var items = $scope.entity.goodsDesc.specificationItems;

		for(var i=0;i< items.length;i++){
			//{spec:{{机身内存：64G}},price:0,num:99999,status:'0',isDefault:'0'},{spec:{{机身内存：32G}},price:0,num:99999,status:'0',isDefault:'0'}
			$scope.entity.itemList = addColumn($scope.entity.itemList,items[i].attributeName,items[i].attributeValue );
		}
	}
	//添加列值
	addColumn=function(list,columnName,conlumnValues){
		var newList=[];//新的集合
		//第一次进来 长度是1  {spec:{},price:0,num:99999,status:'0',isDefault:'0'}
		//第二次进来 长度是2 {spec:{{机身内存：64G}},price:0,num:99999,status:'0',isDefault:'0'},{spec:{{机身内存：32G}},price:0,num:99999,status:'0',isDefault:'0'}
		for(var i=0;i<list.length;i++){
			//{spec:{},price:0,num:99999,status:'0',isDefault:'0' }
			var oldRow= list[i];
			//["移动3G","移动4G"]
			for(var j=0;j<conlumnValues.length;j++){
				// {spec:{{机身内存：64G}},price:0,num:99999,status:'0',isDefault:'0'}
				var newRow= JSON.parse(JSON.stringify(oldRow));//深克隆
				newRow.spec[columnName]=conlumnValues[j];
				// {spec:{{机身内存：64G，网络：移动3G}},price:0,num:99999,status:'0',isDefault:'0'};spec:{{机身内存：64G，网络：移动4G}},price:0,num:99999,status:'0',isDefault:'0'};
				// {spec:{{机身内存：32G，网络：移动3G}},price:0,num:99999,status:'0',isDefault:'0'}，{spec:{{机身内存：32G}，网络：移动4G},price:0,num:99999,status:'0',isDefault:'0'}
				newList.push(newRow);
			}
		}
		return newList;
	}

	//将图片存入到 js中的商品组对象中的goodsDesc.itemImages数组
	$scope.add_images = function(){
		$scope.entity.goodsDesc.itemImages.push($scope.image_entity);
	}

	$scope.remove_images = function(index){
		$scope.entity.goodsDesc.itemImages.splice(index,1);
	}

    $scope.upload = function(){
        uploadService.uploadFile().success(
            function (response) {
                if(response.success){
                    $scope.image_entity.url = response.message;
                }else{
                    alert(response.message);
                }
            }
        );
    }

	$scope.add = function(){

		$scope.entity.goodsDesc.introduction = editor.html();

		var obj = '';

		if($scope.entity.goods.id){
			obj = goodsService.update($scope.entity);
		}else{
			obj = goodsService.add($scope.entity);
		}

		obj.success(
			function (response) {
				if(response.success){
					location.href = "goods.html";
				}else{
					alert(response.message);
				}
			}
		);
	}

    //读取列表数据绑定到表单中  
	$scope.findAll=function(){
		goodsService.findAll().success(
			function(response){
				$scope.list=response;
			}			
		);
	}    
	
	//分页
	$scope.findPage=function(page,rows){			
		goodsService.findPage(page,rows).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}

	var specItems = '';

	//查询实体
	$scope.findOne=function(){
		var id= $location.search()['id'];//获取参数值
		if(id == null){
			return ;
		}
		goodsService.findOne(id).success(
			function(response){
				$scope.entity= response;
				//向富文本编辑器中加入带有样式的值
				editor.html($scope.entity.goodsDesc.introduction);
				//向页面中加入图片
				$scope.entity.goodsDesc.itemImages = JSON.parse($scope.entity.goodsDesc.itemImages);
				//向页面中加入扩展属性
				$scope.entity.goodsDesc.customAttributeItems = JSON.parse($scope.entity.goodsDesc.customAttributeItems);
				//向页面中存入规格
				$scope.entity.goodsDesc.specificationItems = JSON.parse($scope.entity.goodsDesc.specificationItems);

				specItems = $scope.entity.goodsDesc.specificationItems;

				//取规格
				for(var i=0;i<$scope.entity.itemList.length;i++){
					$scope.entity.itemList[i].spec = JSON.parse($scope.entity.itemList[i].spec);
				}

			}
		);
	}
	//根据规格名称和选项名称返回是否被勾选
	$scope.checkAttributeValue=function(specName,optionName){
		var object= $scope.searchObjectByKey(specItems,'attributeName',specName);
		if(object==null){
			return false;
		}else{
			if(object.attributeValue.indexOf(optionName)>=0){
				return true;
			}else{
				return false;
			}
		}
	}
	
	//保存 
	$scope.save=function(){				
		var serviceObject;//服务层对象  				
		if($scope.entity.id!=null){//如果有ID
			serviceObject=goodsService.update( $scope.entity ); //修改  
		}else{
			serviceObject=goodsService.add( $scope.entity  );//增加 
		}				
		serviceObject.success(
			function(response){
				if(response.success){
					//重新查询 
		        	$scope.reloadList();//重新加载
				}else{
					alert(response.message);
				}
			}		
		);				
	}
	
	 
	//批量删除 
	$scope.dele=function(){
		alert($scope.selectIds);
		//获取选中的复选框			
		goodsService.dele($scope.selectIds).success(
			function(response){
				if(response.success){
					$scope.reloadList();//刷新列表
					$scope.selectIds=[];
				}						
			}		
		);				
	}
	
	$scope.searchEntity={};//定义搜索对象 
	
	//搜索
	$scope.search=function(page,rows){			
		goodsService.search(page,rows,$scope.searchEntity).success(
			function(response){
				$scope.list=response.rows;	
				$scope.paginationConf.totalItems=response.total;//更新总记录数
			}			
		);
	}
    
});	