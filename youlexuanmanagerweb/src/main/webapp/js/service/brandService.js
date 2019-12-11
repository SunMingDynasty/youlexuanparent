app.service('brandService',function ($http) {

    this.search = function (page,rows,searchEntity) {
        return $http.post('../brand/search.do?page='+page+'&rows='+rows,searchEntity);
    }

    this.delete = function (selectIds) {
        return $http.get('../brand/delete.do?ids='+selectIds);
    }

    this.findOne = function (id) {
        return $http.get('../brand/findOne.do?id='+id);
    }

    this.save = function (methodName,entity) {
        return $http.post('../brand/'+methodName+'.do',entity);
    }
    
    
    this.selectOptionList = function () {
        return $http.get('../brand/selectOptionList.do');
    }

});