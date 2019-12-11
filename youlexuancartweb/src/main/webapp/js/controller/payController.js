app.controller('payController',function ($scope,$location,payService) {

    $scope.getMoney = function(){
        return $location.search()['money'];
    }

    queryPayStatus = function(out_trade_no){
        payService.queryPayStatus(out_trade_no).success(
            function (response) {
                if(response.success){
                    location.href = "paysuccess.html#?money="+$scope.total_fee;
                }else{
                    if('二维码超时' == response.message){
                        document.getElementById("timeout").innerHTML = "二维码已过期，刷新页面重新获取二维码。";
                        $scope.createNative();
                    }else{
                        location.href = "payfail.html";
                    }
                }
            }
        );
    }

    $scope.createNative = function () {
        payService.createNative().success(
            function (response) {
                $scope.total_fee = response.total_fee;
                $scope.out_trade_no = response.out_trade_no;

                var qr = new QRious({
                    element:document.getElementById('qrious'),
                    size:250,
                    level:'H',
                    value:response.qrcode
                });

                //查询支付状态的方法
                queryPayStatus($scope.out_trade_no);
            }
        );
    }

});