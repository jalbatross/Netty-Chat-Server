    angular.module("chatApp")
    .service('game', function (websockets) {

        var _inLobby = false;
        var _inGame = false;
        var _socket = undefined;

        var gameName = undefined;
        var gameType = undefined;

        this.isInLobby = function() {
            return _inLobby;
        }

        this.isInGame = function() {
            return _inGame;
        }

        this.inLobby = function(boolean) {
            _inLobby = boolean;
        }

        this.inGame = function(boolean) {
            _inGame = boolean;
        }
    });