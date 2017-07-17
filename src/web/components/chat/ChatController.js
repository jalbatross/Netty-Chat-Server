angular.module("chatApp").controller("ChatSendController", function ($scope, $state, $http, websockets) {
    if (!websockets.isConnected()) {
        $state.go('/');
        return;
    }
    else {
        console.log('connected');
    }

    var socket = websockets.getSocket();
    socket.onmessage = function(event) {
        console.log('server said: ', event.data);
    }

    $scope.sendMessage = function() {
        console.log("sending message");
        socket.send($scope.message);
    }

})