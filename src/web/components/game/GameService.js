    angular.module("chatApp")
        .service('game', function(websockets, $rootScope) {

            var _inLobby = false;
            var _inGame = false;
            var _socket = websockets.getSocket();

            var _lobbyUsers = [];
            
            var _currentGameLobby = new GameLobby(undefined, undefined, undefined);
            var _currentGame = new Game(undefined, undefined, undefined, undefined, undefined);
            var _gameUpdateBytes = undefined;
            var _dataReady = false;

            _socket.addEventListener("message", function(event) {
                console.log('[GameService] Got message');
                _dataReady = false;
                let bytes = new Uint8Array(event.data);

                let buf = new flatbuffers.ByteBuffer(bytes);
                let msg = Schema.Message.getRootAsMessage(buf);

                let dataType = msg.dataType();

                if (dataType === Schema.Data.GameCreationRequest) {
                    console.log('[GameService] Updating lobby');
                    let temp = msg.data(new Schema.GameCreationRequest());

                    _currentGameLobby.name = temp.name();
                    _currentGameLobby.type = temp.type();
                    _currentGameLobby.capacity = temp.capacity();

                    _inLobby = true;

                    $rootScope.$emit('updateGameLobby');
                    console.log('[GameService] finished updating lobby with name ', _currentGameLobby.name);
                } 
                else if (dataType === Schema.Data.List && msg.data(new Schema.List()).type() === 'gameLobbyUsers') {
                    console.log('[GameService] Updating lobby users');

                    console.log('[GameService] Got correct list type for conversion');

                    //reset lobby list, then update
                    _lobbyUsers.length = 0;
                    Array.prototype.push.apply(_lobbyUsers,gameLobbyUsersArr(msg));

                    if (_lobbyUsers.length === 0) {
                        console.log('[GameService] Got empty lobby, kicking');
                        $rootScope.$emit('quitLobby');
                        resetFields();
                        return;
                    }

                    $rootScope.$emit('updateGameLobby');
                    console.log('[GameService] finished updating lobby USERS with name ', _currentGameLobby.name);
                }
                else if (dataType == Schema.Data.Game) {
                    console.log('[GameService] Retrieved Game from server');

                    let temp = msg.data(new Schema.Game());

                    _currentGame.type = temp.type();
                    _currentGame.data = temp.gameDataArray();
                    _currentGame.players = [];
                    for (let i = 0; i < temp.playersLength(); i++) {
                        _currentGame.players[i] = temp.players(i);
                    }
                    _currentGame.bestOf = temp.bestOf();
                    _currentGame.completed = temp.completed();

                    console.log('[GameService] Got Game from server with data: \n',
                        'type: ', _currentGame.type, '\n',
                        'data: ', _currentGame.data, '\n',
                        'players ', _currentGame.players, '\n',
                        'bestOf ', _currentGame.bestOf, '\n',
                        'completed ', _currentGame.completed, '\n');

                    $rootScope.$emit('initGame');
                }
                else if (dataType == Schema.Data.GameUpdate) {
                    console.log('[GameService] Retrieved update byets from server');

                    let temp = msg.data(new Schema.GameUpdate());
                    gameUpdateBytes = temp.updateArray();

                    $rootScope.$emit('updateGame');
                }


            })

            this.inLobby = function() {
                return _inLobby;
            }


            this.currentLobby = function() {
                return _currentGameLobby;
            }

            this.currentGame = function() {
                return _currentGame;
            }

            this.gameUpdate = function() {
                return _gameUpdateBytes;
            }

            this.lobbyUserList = function() {
                return _lobbyUsers;
            }

            this.username = function() {
                return _username;
            }

            this.inGame = function() {
                return _inGame;
            }

            this.leaveGame = function() {
                _socket.send('/leave');
                resetFields();

            }

            this.dataReady = function() {
                return _dataReady;
            }

            /**
             * Sets the parameters for the currently joined game lobby
             * @param {string} name     A lobby name
             * @param {string} type     Lobby type
             * @param {number} capacity Max capacity of lobby
             */
            this.setLobbyInfo = function(name, type, capacity) {
                _currentGameLobby.name = name;
                _currentGameLobby.type = type;
                _currentGameLobby.capacity = capacity;
                _inLobby = true;
            }
            
            /**
             * Sets username
             * @param {string} name 
             */
            this.setUsername = function(name) {
                _username = name;
            }

            function resetFields() {
                _inLobby = false;
                _inGame = false;

                _currentGameLobby.name = undefined;
                _currentGameLobby.type = undefined;
                _currentGameLobby.capacity = undefined;

                _currentGame.name = undefined;
                _currentGame.type = undefined;
                _currentGame.capacity = undefined;
                
                _gameUpdateBytes = undefined;
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

            function Game(type, data, players, bestOf, completed) {
                this.type = type;
                this.data = data;
                this.players = players;
                this.bestOf = bestOf;
                this.completed = completed;
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