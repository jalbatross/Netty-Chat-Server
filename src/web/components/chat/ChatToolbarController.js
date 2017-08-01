angular.module("chatApp").controller("ChatToolbarController", function ($scope, $state, websockets) {

    var socket = websockets.getSocket();
    $scope.updateLobbies = function() {
        socket.send('/lobbies');
    }

    $scope.updateGames = function() {
        socket.send('/games');
    }
});