    angular.module("chatApp")
    .service('game', function (websockets,$rootScope) {

        var _inLobby = false;
        var _inGame = false;
        var _socket = websockets.getSocket();

        var _currentGameLobby = undefined;
        _socket.addEventListener("message", function(event) {
            console.log('[GameService] Updating lobby');
            var bytes = new Uint8Array(event.data);

            var buf = new flatbuffers.ByteBuffer(bytes);
            var msg = Schema.Message.getRootAsMessage(buf);

            var dataType = msg.dataType();

            if (dataType !== Schema.Data.GameCreationRequest) {
                return;
            }
            _currentGameLobby = msg.data(new Schema.GameCreationRequest());
            _inLobby = true;
            $rootScope.$emit('updateGame');
        })

        this.inLobby = function() {
            return _inLobby;
        }


        this.currentLobby = function() {
            return _currentGameLobby;
        }

        this.inGame = function() {
            return _inGame;
        }

        this.leaveGame = function() {
            //send request to leave all lobbies
            _socket.send('/leave');

            resetFields();

        }

        resetFields = function() {
            _inLobby = false;
            _inGame = false;
            _currentGameLobby = undefined;
        }

    });