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
    //console.log("[GLC] gamelobby: ", $scope.gameLobby.name, " type: ", $scope.gameLobby.type, " capacity: " , $scope.gameLobby.capacity);
    $scope.gameLobbyUsers = game.lobbyUserList();
    $scope.selectedCapacity = $scope.gameLobby.capacity;
    $scope.generatedCapacities = generateCapacities($scope.gameLobby.capacity, $scope.gameLobby.type);


    var updateGameListener = $rootScope.$on('updateGameLobby', function(){
        $scope.selectedCapacity = $scope.gameLobby.capacity;
        $scope.generatedCapacities = generateCapacities($scope.gameLobby.capacity, $scope.gameLobby.type);
        $scope.$apply();

    });

    $scope.kickUser = function(username) {
        alert('kicked ' + username);
        websockets.getSocket().send('/kick ' + username);
    }

    $scope.startGame = function() {
        websockets.getSocket().send(makeFlatbuffersRequest(Schema.RequestType.START_GAME));
    }

    /**
     * Returns the bytes of a Flatbuffers serialized Message object
     * containing a Schema.Request object of RequestType type.
     * 
     * @param  {Schema.RequestType} type   Type of request
     * @return {Uint8Array}                bytes of Flatbuffers Message
     *                          
     */
    //TODO: pass in a builder as the builder is expensive
    function makeFlatbuffersRequest(type) {
        let builder = new flatbuffers.Builder(1024);
        Schema.Request.startRequest(builder);
        Schema.Request.addType(builder, type);
        let req = Schema.Request.endRequest(builder);

        Schema.Message.startMessage(builder);
        Schema.Message.addDataType(builder, Schema.Data.Request);
        Schema.Message.addData(builder, req);

        let data = Schema.Message.endMessage(builder);

        builder.finish(data);

        return builder.asUint8Array();
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
    });

    $scope.$on('$viewContentLoaded', function() {
        alert('started glc');
    })

});