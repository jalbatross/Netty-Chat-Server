angular.module("chatApp").controller("GameLobbiesController", function($scope, websockets, game) {

    $scope.gameLobbies = [];

    var socket = websockets.getSocket();
    socket.send('/games');

    /**
     * Refreshes game lobies list
     */
    $scope.refreshLobbies = function() {
        console.log('*** CLICKED REF LOBBIES ***');
        socket.send('/games');
    }

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
        //console.log("[GameLobbyController] read message");
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

    $scope.joinGame = function(name, type, capacityString) {
        if (isFull(capacityString)) {
            return;
        }

        //Sanitize capacity string
        var numPlayers = parseInt(capacityString.split('/')[0]) + 1;
        console.log('numPlayers: ' + numPlayers);

        game.setLobbyInfo(name, type,numPlayers);
        socket.send('/join ' + name);
    }

    /**
     * Parses capacityString which is in format "numPlayers/maxPlayers".
     * Returns true if numPlayers < maxPlayers.
     * 
     * @param  {string}  capacityString Capacity string
     * @return {Boolean}                True if the lobby is full, 
     *                                  false otherwise
     */
    var isFull = function(capacityString) {

        var temp = capacityString.split('/');

        var currentNumPlayers = parseInt(temp[0]);
        var maxPlayers = parseInt(temp[1]);

        return currentNumPlayers >= maxPlayers;
    }

});