app.service('cartService',function ($http) {

    this.addOrder = function (order) {
       return $http.post('../order/add.do',order);
    }

    this.findAddressByUserId = function () {
        return $http.get('../address/findAddressByUserId.do');
    }

    this.findCartList = function () {
        return $http.get('../cart/findCookieCartList.do');
    }

    this.addGoodsToCart = function (itemId,num) {
        return $http.get('../cart/addGoodsToCart.do?itemId='+itemId+'&num='+num);
    }

    //求合计
    this.sum=function(cartList){
        var totalValue={totalNum:0, totalMoney:0.00 };//合计实体
        for(var i=0;i<cartList.length;i++){
            var cart=cartList[i];
            for(var j=0;j<cart.orderItemList.length;j++){
                var orderItem=cart.orderItemList[j];//购物车明细
                totalValue.totalNum+=orderItem.num;
                totalValue.totalMoney+= orderItem.totalFee;
            }
        }
        return totalValue;
    }






})