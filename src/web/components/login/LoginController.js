//LoginController.js

var app = angular.module("chatApp", []);

angular.module("chatApp").controller("LoginController", function($scope, $http) {
    var aurl = "http://localhost:8080";

    $scope.login = function() {

        var data = {
            username: $scope.username,
            password: $scope.password
        };

        var config = {'Content-Type': 'text/plain'};

        $http({
            method: 'post',
            url: aurl,
            data: data,
            headers: config
        }).then(function (response) {
            console.log(response.data);
        }, function (response) {
            console.log(response.data);
        });
    }
});