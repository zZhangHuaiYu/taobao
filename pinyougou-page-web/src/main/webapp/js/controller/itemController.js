app.controller("itemController", function ($scope) {


	//对数量的加减
	$scope.num = 1;
	$scope.add = function(x){
		$scope.num +=x;
		if($scope.num<1){
			$scope.num = 1;
		}
	}
	
	$scope.specificationItems = {};//记录用户选择的规则
	$scope.selectSpecification = function(key,value){
		$scope.specificationItems[key]= value;
		searchSku();
	}

	$scope.isSelected= function(key,value){
		if($scope.specificationItems[key]== value){
			return true;
		}else{
			return false;
		}
	}
	
	$scope.loadSku = function(){
		$scope.sku=skuList[0];
		$scope.specificationItems= JSON.parse(JSON.stringify($scope.sku.spec)) ;		
	}
	
	
	
	//匹配两个对象
	matchObject = function(map1,map2){
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
	
	//查询sku列表
	searchSku= function(){
		for(var i=0;i<skuList.length;i++){
			if(matchObject(skuList[i].spec,$scope.specificationItems )){
				$scope.sku=skuList[i];
				return;
			}
		}
		$scope.sku={id:0,title:'--------',price:0};//如果没有匹配的	
	}
	
		//添加商品到购物车
	$scope.addToCart=function(){
		alert('skuid:'+$scope.sku.id);				
	}
	
})