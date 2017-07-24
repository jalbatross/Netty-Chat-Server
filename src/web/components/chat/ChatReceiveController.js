angular.module("chatApp").controller("ChatReceiveController", function($scope, websockets) {
    if (!websockets.isConnected()) {
        console.log('[ReceiveController] Not connected. exit');
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
    console.log('[ChatReceiveController] Connected');
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
            return;
        }
        else {
            console.log("[ChatReceiveController] Received unknown, dataType ", dataType);
            console.log("[ChatReceiveController] Raw data: ", event.data);
            var currentTime = new Date(Date.now());
            ($scope.messages).push(new errorMessage(currentTime));
        }

        $scope.$apply();

    });
});