angular.module("chatApp").controller("ChatLobbiesController", function($scope, $state, websockets, $uibModalInstance) {

    var $ctrl = this;

    $ctrl.close = function() {
        $uibModalInstance.close();
    };

    $ctrl.cancel = function() {
        $uibModalInstance.dismiss('cancel');
    };

    if (!websockets.isConnected()) {
        console.log("[LobbiesController] Not connected, exit");
        $state.go('/');
        return;
    } else {
        console.log('[LobbiesController] connected');
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

    $scope.lobbies = [];
    var socket = websockets.getSocket();

    var lobbiesRequestBytes = makeFlatbuffersRequest(Schema.RequestType.CHAT_LOBBIES);
    socket.send(lobbiesRequestBytes);
    $scope.data = "";

    function Lobby(name, capacity) {
        this.name = name;
        this.capacity = capacity;

        return this;
    }

    /**
     * Converts a list of lobbies to a String
     * @param  {FlatBuffers Message} msg A Flatbuffers Message
     * @return {Lobby[]}                 Array of Lobby objects 
     */
    function lobbyListString(msg) {
        console.log("[LobbiesController] data type: " + msg.dataType());

        if (msg == null || msg.dataType() != Schema.Data.List) {
            throw 'Invalid list';
        }

        var len = msg.data(new Schema.List()).contentsLength();
        var listType = msg.data(new Schema.List()).type();

        if (listType != "lobbies") {
            throw 'Invalid list type: expected lobbies';
        }

        var ret = [];
        var temp = "";

        for (var i = 0; i < len; i++) {
            temp = msg.data(new Schema.List()).contents(i);
            temp = temp.split(",");
            console.log("temp[0]: " + temp[0]);
            console.log("temp[1]: " + temp[1]);
            ret[i] = new Lobby(temp[0], temp[1]);
        }

        return ret;
    }

    socket.addEventListener("message", function(event) {
        console.log("[LobbiesController] read message");
        var bytes = new Uint8Array(event.data);

        var buf = new flatbuffers.ByteBuffer(bytes);
        var msg = Schema.Message.getRootAsMessage(buf);

        var dataType = msg.dataType();
        console.log("[LobbiesController DataType: ", dataType);

        if (dataType == Schema.Data.List) {
            console.log("[LobbiesController] got List");
            var listType = msg.data(new Schema.List()).type();

            if (listType != "lobbies") {
                console.log("[LobbiesController] List was not lobbies");
                return;
            }
            var ret = lobbyListString(msg);

            $scope.lobbies = ret;
            console.log("[LobbiesController] data: ", $scope.lobbies);

        }
        $scope.$apply();
    });

    $scope.changeLobby = function(lobbyName) {
        console.log("[LobbiesController] changeLobby called");
        console.log("[LobbiesController] Param: ", lobbyName);
        socket.send("/connect " + lobbyName);
    }


});