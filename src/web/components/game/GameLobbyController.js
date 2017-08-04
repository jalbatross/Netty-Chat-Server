/**
 * GameLobbyController.js
 *
 * Responsible for displaying game lobby info to user.
 *
 * by @jalbatross Aug 03 2017
 */

angular.module("chatApp").controller("GameLobbyController", function($scope, websockets, $rootScope, game) {

    console.log("[GameLobbyController] started");
    $scope.gameLobby = game.currentLobby();

    $rootScope.$on('updateGame', function(){
        $scope.gameLobby = game.currentLobby();
        $scope.$apply();
    });

});