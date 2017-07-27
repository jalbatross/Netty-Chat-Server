angular.module("chatApp").controller("ChatUsersController", function ($scope, websockets) {
    if (!websockets.isConnected()) {
        console.log("[UsersController] Not connected, exit");
        $state.go('/');
        return;
    }
    else {
        console.log('[UsersController] connected');
    }

    $scope.usernames = "";
    $scope.lobbyName = "loading . . .";
    var socket = websockets.getSocket();

     $scope.rightClickUser = [
          ['Favorite Color', function ($itemScope, $event, color) {
                alert(color);
          }],
          ['Get User Info', function($itemScope) {
            alert($itemScope.user);
          }
          ]
      ];
    
    function userListString(msg) {
        console.log("[UsersController] data type: " + msg.dataType());

        if (msg == null || msg.dataType() != Schema.Data.List){
            throw 'Invalid list';
        }

        var len = msg.data(new Schema.List()).contentsLength();
        var listType = msg.data(new Schema.List()).type();

        if (listType != "users") {
            throw 'Invalid list type: expected users';
        }

        var ret = [];

        $scope.lobbyName = msg.data(new Schema.List()).contents(0);
        for (var i = 1; i < len; i++) {
            ret [i] = msg.data(new Schema.List()).contents(i);
        }

        return ret;
    }

    socket.addEventListener("message", function(event) {
        console.log("[UsersController] read message");
        var bytes = new Uint8Array(event.data);

        var buf = new flatbuffers.ByteBuffer(bytes);
        var msg = Schema.Message.getRootAsMessage(buf);

        var dataType = msg.dataType();
        console.log("[UsersController DataType: ", dataType);

        if (dataType == Schema.Data.List){
            console.log("[UsersController] got List");
            var listType = msg.data(new Schema.List()).type();

            if (listType != "users") {
                console.log("[UsersController] List was not users");
                return;
            }
            var ret = userListString(msg);
            
            $scope.usernames = ret;
            console.log("[UsersController] data: ", $scope.usernames);
            
        }
        $scope.$apply();
    });

});