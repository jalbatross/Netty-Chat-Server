/**
 * GameLobbyController.js
 *
 * Responsible for displaying game lobby info to user.
 *
 * by @jalbatross Aug 03 2017
 */

angular.module("chatApp").controller("GameLobbyController", function($scope, websockets, $rootScope, game, $interval) {

    console.log("[GameLobbyController] started");

    $scope.gameLobby = game.currentLobby();
    console.log("[GLC] gamelobby: ", $scope.gameLobby.name, " type: ", $scope.gameLobby.type, " capacity: " , $scope.gameLobby.capacity);
    $scope.gameLobbyUsers = game.lobbyUserList();
    $scope.selectedCapacity = $scope.gameLobby.capacity;
    $scope.generatedCapacities = generateCapacities($scope.gameLobby.capacity, $scope.gameLobby.type);


    var updateGameListener = $rootScope.$on('updateGame', function(){
        $scope.selectedCapacity = $scope.gameLobby.capacity;
        $scope.generatedCapacities = generateCapacities($scope.gameLobby.capacity, $scope.gameLobby.type);
        $scope.$apply();

    });

    var kickedListener = $rootScope.$on('quitLobby', function() {
        console.log('[GameLobbyController] Kicked from lobby');
        $scope.showGameLobbiesDialog();
        $scope.$apply();
    })

    $scope.kickUser = function(username) {
        alert('kicked ' + username);
    }

    $scope.startGame= function() {
        alert('started game');
    }

    /**
     * Generates valid capacities for game
     * @param  {Number} capacity Game capacity
     * @param  {String} gameType Game type
     * @return {Number[]}             Array of numbers from 2-> max game cap
     */
    function generateCapacities(capacity, gameType) {
        var ret = [];

        if (gameType ==="rps") {
            ret = [2];
        }
        else if (gameType ==="coup") {
            ret= [2,3,4];
        }

        return ret;
    }

    $scope.$on('$destroy', function() {
        //remove listener
        updateGameListener();
        kickedListener();
    });

    $scope.$on('$viewContentLoaded', function() {
        alert('started glc');
    })

});