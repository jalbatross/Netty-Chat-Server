angular.module("chatApp").controller("ChatSendController", function ($scope, $state, $http, websockets) {
    if (!websockets.isConnected()) {
        $state.go('/');
        return;
    }
    else {
        console.log('connected');
    }

    var socket = websockets.getSocket();

    $scope.sendMessage = function() {
        console.log("sending message");
        socket.send($scope.message);
        $scope.message = "";
    }
    socket.onclose = function() {
        $state.go('/');
    }

})

angular.module("chatApp").controller("ChatReceiveController", function($scope, websockets) {
    var socket = websockets.getSocket();
    $scope.messages = [];
    function errorMessage(time) {
        this.time = time.toLocaleString();
        this.author = "";
        this.message = "ERROR!";
    }


    socket.onmessage = function(event) {
        var bytes = new Uint8Array(event.data);

        var buf = new flatbuffers.ByteBuffer(bytes);
        var msg = Schema.Message.getRootAsMessage(buf);

        var dataType = msg.dataType();

        if (dataType == Schema.Data.Chat) {
            var time = msg.data(new Schema.Chat()).timestamp().toFloat64();
            var author = msg.data(new Schema.Chat()).author();
            var contents = msg.data(new Schema.Chat()).message();

            var converted = new Date(time);

            var obj = {time: converted.toLocaleString(), author: author, message: contents};
            ($scope.messages).push(obj);


        } else if (dataType == Schema.Data.List){
            console.log("got List");
            var len = msg.data(new Schema.List()).contentsLength();
            var listType = msg.data(new Schema.List()).type();
            console.log("type of list: ", listType);
            for (var i = 0; i < len; i++) {
                console.log(i, " : " , msg.data(new Schema.List()).contents(i));
            }

            
            
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

    }
})