angular.module("chatApp").controller("ChatGameController", function($scope, websockets, $http, $timeout) {
    console.log("hello");

    var socket = websockets.getSocket();


    socket.addEventListener("message", function(event) {
        $('#gameModal').modal('show');
    })
});
