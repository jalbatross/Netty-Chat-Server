//LoginController.js


angular.module("chatApp").controller("LoginController", function($scope,$state, $http, websockets) {
    var aurl = "http://localhost:8080";

    $scope.login = function() {

        var data = {
            username: $scope.username,
            password: $scope.password
        };

        var config = {'Content-Type': 'application/x-www-form-urlencoded'};

        $http({
            method: 'post',
            url: aurl,
            data: data,
            headers: config
        }).then(function (response) {
            console.log(response.data);
            websockets.setTicket(response.data);
            websockets.connect();
            $state.go('chat');

            var socket;
            setTimeout(function() {
                socket = websockets.getSocket();
                socket.onmessage = function(event) {
                    console.log("socket received something");
                    console.log(event.data);
                }

            },1000);

        }, function (response) {
            console.log(response.data);
        });
    }
});