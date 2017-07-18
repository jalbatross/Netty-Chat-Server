angular.module("chatApp").controller("ChatSendController", function ($scope, $state, $http, websockets) {
    if (!websockets.isConnected()) {
        $state.go('/');
        return;
    }
    else {
        console.log('connected');
    }

    var socket = websockets.getSocket();

    $scope.sendMessage = function() {
        console.log("sending message");
        socket.send($scope.message);
    }

})

angular.module("chatApp").controller("ChatReceiveController", function($scope, websockets) {
    var socket = websockets.getSocket();
    $scope.messages = [];

    socket.onmessage = function(event) {
        console.log('server said: ', event.data);
        var obj = JSON.parse(event.data);
        var time = new Date(obj.time);
        obj.time = time.toLocaleString();

        ($scope.messages).push(obj);

        $scope.$apply();

    }
})