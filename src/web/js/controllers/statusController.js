//statusController.js

angular.module("pingApp").controller("statusController", function($scope, $interval) {
    $scope.action = "Connect";

    var connected = false;
    var reconnecting = false;

    var ws;

    var rcPromise;
    var checkerPromise;
    var pingerPromise;

    //a long corresponding to Unix time in ms
    var lastPing;

    var pingCheck = function(socket) {
        //good connection
        if (Date.now() - lastPing < 5000) {
            console.log('maintain');

            if (reconnecting) {
                $interval.cancel(rcPromise);
                reconnecting = false;
            }

            $scope.action = 'Connected';
        }

        //bad connection - spam pongs to network
        else if (Date.now() - lastPing < 10000) {
            console.log('Bad connection or maybe lost connection');
            if (!reconnecting) {
                rcPromise = $interval(pinger, 1000);
                $scope.action = 'Reconnecting';
                reconnecting = true;
            }
        }

        //lost connection, DC
        else {
            closeWs();
            console.log('lost connxn');

            if (reconnecting) {
                $interval.cancel(rcPromise);
                reconnecting = false;
            }

            $scope.action = 'Disconnected';
        }
    }

    var pinger = function(socket) {
        socket.send('pong');
    }

    var closeWs = function() {
        $scope.action = 'Disconnected';
        connected = false;
        ws.close();
        $interval.cancel(checkerPromise);
        $interval.cancel(pingerPromise);
    }

    $scope.connect = function() {
        if (!connected) {
            ws = new WebSocket("ws://localhost:8080/websocket");

            ws.onopen = function(event) {
                console.log('successfully connected');
                lastPing = Date.now();
                pingerPromise = $interval(function() {
                    pinger(ws)
                }, 3000);
                checkerPromise = $interval(function() {
                    pingCheck(ws)
                }, 1000);
                connected = true;

                $scope.action = 'Connected';

            }

            ws.onmessage = function(event) {
                console.log('received msg from server');
                lastPing = Date.now();
            }

            ws.onclose = function(event) {
                console.log('ws socket closed');
            }
        } 
        else {
            closeWs();
            console.log('closed ws connection');
        }
    }
});