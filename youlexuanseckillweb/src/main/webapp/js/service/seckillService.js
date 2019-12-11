app.service('seckillService',function ($http) {
    
    this.findAll = function () {
        return $http.get('../seckillGoods/findAll.do');
    }
    
    this.findGoodsFromRedis = function (id) {
        return $http.get('../seckillGoods/findGoodsFromRedis.do?id='+id)
    }


    this.submitOrder = function (seckillId) {
        return $http.get('../seckillOrder/submitOrder.do?seckillId='+seckillId);
    }

})