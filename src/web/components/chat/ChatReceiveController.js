angular.module("chatApp").controller("ChatReceiveController", function($scope, websockets) {
    if (!websockets.isConnected()) {
        return;
    }

    var socket = websockets.getSocket();

    //array of strings
    $scope.messages = [];

    function errorMessage(time) {
        this.time = time.toLocaleString();
        this.author = "";
        this.message = "ERROR!";
    }

    /**
     * Converts a time, author, and message into a chat string.
     * 
     * @param  {Date}   date      Date object
     * @param  {string} author    author
     * @param  {string} message   message contents
     * 
     * @return {string} ret       concatenation of above strings, date is in local format
     */
    function chatMessageString(date, author, message) {
        return date.toLocaleString() + " " + author +  ": " + message;
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

    socket.addEventListener("message", function(event) {
        console.log("[ChatReceiveController] Received msg");
        var bytes = new Uint8Array(event.data);

        var buf = new flatbuffers.ByteBuffer(bytes);
        var msg = Schema.Message.getRootAsMessage(buf);

        var dataType = msg.dataType();

        if (dataType == Schema.Data.Chat) {
            var time = msg.data(new Schema.Chat()).timestamp().toFloat64();
            var author = msg.data(new Schema.Chat()).author();
            var contents = msg.data(new Schema.Chat()).message();

            var date = new Date(time);

            ($scope.messages).push(chatMessageString(date, author, contents));


        } 
        else if (dataType == Schema.Data.List){
            console.log("got List");
            
            ($scope.messages).push(lobbyListString(msg));
            
        }
        else {
            console.log("dataType ", dataType);
            var currentTime = new Date(Date.now());
            ($scope.messages).push(new errorMessage(currentTime));
        }

        $scope.$apply();
        /*
        var obj = JSON.parse(event.data);
        var time = new Date(obj.time);
        obj.time = time.toLocaleString();

        ($scope.messages).push(obj);

        $scope.$apply();*/

    });
});