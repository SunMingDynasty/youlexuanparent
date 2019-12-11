app.controller('cartController',function ($scope,cartService) {

    $scope.order = {paymentType:1};

    $scope.addOrder = function(){

        $scope.order.receiverAreaName = $scope.address.address;
        $scope.order.receiverMobile = $scope.address.mobile;
        $scope.order.receiver = $scope.address.contact;

        cartService.addOrder($scope.order).success(
            function (response) {
                if(response.success){
                    //1 沙箱支付 2 货到付款 3 微信支付
                    if($scope.order.paymentType == "1"){
                        location.href = "pay.html";
                    }else{
                        location.href = "paysuccess.html";
                    }
                }else{
                    alert(response.message);
                }
            }
        );
    }

    $scope.selectPaymentMethod = function(payMethod){
        $scope.order.payment = payMethod;
    }

    $scope.selectAddress = function(address){
        $scope.address = address;

    }

    $scope.isAddressSelected = function(address){
        if($scope.address == address){
            return true;
        }else{
            return false;
        }
    }

    $scope.findAddressByUserId = function(){
        cartService.findAddressByUserId().success(
            function (response) {
                $scope.addressList = response;

                for(var i=0;i<$scope.addressList.length;i++){
                    if($scope.addressList[i].isDefault == '1'){
                        $scope.address = $scope.addressList[i];
                        break;
                    }
                }

            }
        );
    }


    $scope.findCartList = function () {
        cartService.findCartList().success(
            function (response) {
                $scope.list = response;
                $scope.totalValue=cartService.sum($scope.list);//求合计数
            }
        );
    }
    
    
    $scope.addGoodsToCart = function (itemId,num) {
        cartService.addGoodsToCart(itemId,num).success(
            function (response) {
                if(response.success){
                    $scope.findCartList();
                }else{
                    alert(response.message);
                }
            }
        );
    }


})