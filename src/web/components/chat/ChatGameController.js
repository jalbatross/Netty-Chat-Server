angular.module("chatApp").controller("ChatGameController", function($scope, websockets, $http, $timeout) {
    console.log("hello");

    $scope.gameLobbies = [];
    $scope.gameModalTemplate = "/components/chat/modalBody.html";
    var socket = websockets.getSocket();

    /**
     * Game lobby object
     * @param {String} name     Game lobby name
     * @param {String} type     Type of game
     * @param {String} capacity numUsersInLobby/MaxUsersAllowed
     */
    function GameLobby(name, type, capacity) {
        this.name = name;
        this.type = type;
        this.capacity = capacity;

        return this;
    }

    /**
     * Converts a list of gamelobbies to a String
     * @param  {FlatBuffers Message} msg A FlatBuffers Message object
     * @return {GameLobby[]}             An array of GameLobby objects
     */
    function gameLobbyListString(msg) {

        if (msg == null || msg.dataType() != Schema.Data.List){
            throw 'Invalid list';
        }

        var len = msg.data(new Schema.List()).contentsLength();
        var listType = msg.data(new Schema.List()).type();

        if (listType != "games") {
            throw 'Invalid list type: expected games';
        }

        var ret = [];
        var temp = ""; 

        for (var i = 0; i < len; i++) {
            temp = msg.data(new Schema.List()).contents(i);
            temp = temp.split(",");

            ret[i] = new GameLobby(temp[0], temp[1],temp[2]);
        }

        return ret;
    }

    socket.addEventListener("message", function(event) {
        console.log("[GameController] read message");
        var bytes = new Uint8Array(event.data);

        var buf = new flatbuffers.ByteBuffer(bytes);
        var msg = Schema.Message.getRootAsMessage(buf);

        var dataType = msg.dataType();

        if (dataType == Schema.Data.List){
            var listType = msg.data(new Schema.List()).type();

            if (listType != "games") {
                console.log("[GameController] List was not games");
                return;
            }
            var ret = gameLobbyListString(msg);
            
            $scope.gameLobbies = ret;
        }
        $scope.$apply();
    });

    $scope.joinGame = function(lobbyName) {
        alert("tried to join game with name: " + lobbyName);
    }
    $scope.showGameLobbyDialog = function() {
        $scope.gameModalTemplate = "/components/chat/gameLobbyTemplate.html";
    }
    $scope.showGameLobbiesDialog = function() {
        $scope.gameModalTemplate = "/components/chat/modalBody.html";
    }

    $scope.createGameLobby = function() {
        if ($scope.gameLobbyForm.$invalid) {
            alert("invalid data");
            return;
        }

        var ret = "---Game Lobby Info: ---\n";
        ret += "Name: " + $scope.gameName +"\n";
        ret += "Type: " + $scope.gameType +"\n";
        ret += "Capacity: " +$scope.gameCapacity +"\n";
        if ($scope.gamePassword == null) {
            $scope.gamePassword = "";
        }
        ret += "Password:" + $scope.gamePassword + "\n";

        var builder = new flatbuffers.Builder(1024);

        var nameData = builder.createString($scope.gameName);
        var typeData = builder.createString($scope.gameType);
        var capacityData = builder.createString($scope.gameCapacity);
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

        socket.send(reqBytes);
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
});
