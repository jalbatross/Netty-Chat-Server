angular.module("chatApp").controller("ChatLobbiesController", function ($scope, $state, websockets) {
    if (!websockets.isConnected()) {
        console.log("[LobbiesController] Not connected, exit");
        $state.go('/');
        return;
    }
    else {
        console.log('[LobbiesController] connected');
    }

    /**
     * Converts a Message Flatbuffers object containing a List to string
     * @param  {[type]} msg [description]
     * @return {[type]}     [description]
     */
    function lobbyListString(msg) {
        if (msg == null || msg.dataType() != Schema.Data.List){
            throw 'Invalid lobby list';
        }

        var len = msg.data(new Schema.List()).contentsLength();
        var listType = msg.data(new Schema.List()).type();


        var ret = listType + ":\n";

        for (var i = 0; i < len; i++) {
            ret += msg.data(new Schema.List()).contents(i);
            if (i != len - 1) {
                ret += "\n";
            }
        }

        return ret;
    }

    var socket = websockets.getSocket();

    $scope.data = [];
    
    socket.addEventListener("message", function(event) {
        console.log("[LobbiesController] read message");
        var bytes = new Uint8Array(event.data);

        var buf = new flatbuffers.ByteBuffer(bytes);
        var msg = Schema.Message.getRootAsMessage(buf);

        var dataType = msg.dataType();

        if (dataType == Schema.Data.List){
            console.log("[LobbiesController] got List");
            console.log("[LobbiesController] going to push: ", lobbyListString(msg));
            ($scope.data).push(lobbyListString(msg));
            console.log("[LobbiesController] data: ", $scope.data);
            
        }
        $scope.$apply();
    });
});