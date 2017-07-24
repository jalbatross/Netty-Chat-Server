angular.module("chatApp").controller("ChatSendController", function ($scope, $state, $http, websockets) {
    if (!websockets.isConnected()) {
        console.log('[ChatSendController] Not connected, return');
        $state.go('/');
        return;
    }
    else {
        console.log('[ChatSendController] connected');
    }
    var socket = websockets.getSocket();
    socket.send('');
    console.log('[ChatSendController] Sending init');
    
    $scope.sendMessage = function() {
        console.log("[ChatSendController] sending message");
        socket.send($scope.message);
        $scope.message = "";
    }

    socket.addEventListener("close",function() {
        $state.go('/');
    });

});

