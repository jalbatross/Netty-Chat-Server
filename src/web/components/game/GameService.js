    angular.module("chatApp")
        .service('game', function(websockets, $rootScope) {

            var _inLobby = false;
            var _inGame = false;
            var _socket = websockets.getSocket();
            var _lobbyUsers = [];

            var _currentGameLobby = new GameLobby(undefined, undefined, undefined);
            var _dataReady = false;

            _socket.addEventListener("message", function(event) {
                console.log('[GameService] Got message');
                _dataReady = false;
                var bytes = new Uint8Array(event.data);

                var buf = new flatbuffers.ByteBuffer(bytes);
                var msg = Schema.Message.getRootAsMessage(buf);

                var dataType = msg.dataType();

                if (dataType === Schema.Data.GameCreationRequest) {
                    console.log('[GameService] Updating lobby');
                    var temp = msg.data(new Schema.GameCreationRequest());

                    _currentGameLobby.name = temp.name();
                    _currentGameLobby.type = temp.type();
                    _currentGameLobby.capacity = temp.capacity();

                    _inLobby = true;

                    $rootScope.$emit('updateGame');
                    console.log('[GameService] finished updating lobby with name ', _currentGameLobby.name);
                } 
                else if (dataType === Schema.Data.List && msg.data(new Schema.List()).type() === 'gameLobbyUsers') {
                    console.log('[GameService] Updating lobby users');

                    console.log('[GameService] Got correct list type for conversion');

                    Array.prototype.push.apply(_lobbyUsers,gameLobbyUsersArr(msg));

                    $rootScope.$emit('updateGame');
                    console.log('[GameService] finished updating lobby USERS with name ', _currentGameLobby.name);
                }


            })

            this.inLobby = function() {
                return _inLobby;
            }


            this.currentLobby = function() {
                return _currentGameLobby;
            }

            this.lobbyUserList = function() {
                return _lobbyUsers;
            }

            this.inGame = function() {
                return _inGame;
            }

            this.leaveGame = function() {
                //send request to leave all lobbies
                _socket.send('/leave');

                resetFields();

            }

            this.dataReady = function() {
                return _dataReady;
            }

            resetFields = function() {
                _inLobby = false;
                _inGame = false;

                _currentGameLobby.name = undefined;
                _currentGameLobby.type = undefined;
                _currentGameLobby.capacity = undefined;

                _lobbyUsers.length = 0;
            }

            function GameLobbyUser(name, isHost) {
                this.name = name;
                this.isHost = isHost;
            }

            function GameLobby(name, type, capacity) {
                this.name = name;
                this.type = type;
                this.capacity = capacity;
            }

            /**
             * Converts a list of lobbies to a GameLobbyUser[]
             * @param  {FlatBuffers Message} msg A Flatbuffers Message
             * @return {GameLobbyUser[]}                 Array of GameLobbyUser objects 
             */
            function gameLobbyUsersArr(msg) {
                console.log("[GameService] data type: " + msg.dataType());

                if (msg == null || msg.dataType() != Schema.Data.List) {
                    throw 'Invalid list';
                }

                var len = msg.data(new Schema.List()).contentsLength();
                var listType = msg.data(new Schema.List()).type();

                if (listType != "gameLobbyUsers") {
                    throw 'Invalid list type: expected lobbies';
                }

                var ret = [];
                var temp = "";
                var isHost = false;

                for (var i = 0; i < len; i++) {
                    temp = msg.data(new Schema.List()).contents(i);
                    isHost = temp.includes(',');

                    if (isHost) {
                        temp = temp.split(',');
                        temp = temp[0];
                    }

                    ret[i] = new GameLobbyUser(temp, isHost);
                }

                return ret;
            }


        });