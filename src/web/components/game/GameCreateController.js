angular.module("chatApp").controller("GameCreateController", function($scope, websockets) {
    var socket = websockets.getSocket();

    $scope.capacityOptions = [2, 3];
    $scope.gameCapacity = 2;

    /**
     * Shortens game types into shorter abbreviations for
     * server
     * 
     * @param  {string} type Game type
     * @return {string}      shortened version of type if type was valid
     */
    function formatGameType(type) {
        if (type == null || !(typeof type === "string")) {
            return;
        }

        switch(type) {
            case 'Rock Paper Scissors':
              return 'rps';
            case 'Coup':
              return 'coup';
            default:
              return '';
        }

    }

    var _selected;

    $scope.gameType = undefined;
    $scope.gameTypes = ['Rock Paper Scissors', 'Coup'];

    $scope.ngModelOptionsSelected = function(value) {
        if (arguments.length) {
            _selected = value;
        }
        else {
            return _selected;
        }
    };

    $scope.selectedType = function() {

        switch($scope.gameType) {
            case 'Rock Paper Scissors':
              $scope.capacityOptions = [2];
              break;
            case 'Coup':
              $scope.capacityOptions = [2];
              break;
            default:
              $scope.capacityOptions = [];
              break;
        }
    }

    $scope.createGameLobby = function() {
        if ($scope.gamePassword == null) {
            $scope.gamePassword = "";
        }

        var typeFormatted = formatGameType($scope.gameType);
        
        var builder = new flatbuffers.Builder(1024);

        var nameData = builder.createString($scope.gameName);
        var typeData = builder.createString(typeFormatted);
        var capacityData = Number($scope.gameCapacity);
        var pwData = builder.createString($scope.gamePassword);

        Schema.GameCreationRequest.startGameCreationRequest(builder);
        Schema.GameCreationRequest.addName(builder, nameData);
        Schema.GameCreationRequest.addType(builder, typeData);
        Schema.GameCreationRequest.addCapacity(builder, capacityData);
        Schema.GameCreationRequest.addPassword(builder, pwData);

        var req = Schema.GameCreationRequest.endGameCreationRequest(builder);

        Schema.Message.startMessage(builder);
        Schema.Message.addDataType(builder, Schema.Data.GameCreationRequest);
        Schema.Message.addData(builder, req);

        var data = Schema.Message.endMessage(builder);
        builder.finish(data);

        var reqBytes = builder.asUint8Array();

        var buf = new flatbuffers.ByteBuffer(reqBytes);

        var data = Schema.Message.getRootAsMessage(buf);
        var bufType = data.dataType();

        socket.send(reqBytes);
    }



});