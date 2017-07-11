//statusController.js

angular.module("pingApp").controller("statusController", function ($scope, $interval) {
    $scope.connected = false;
    $scope.action = "Connect";

    
    $scope.pingStatus = "off";


    var sendPings = false;
    var pingerPromise;
    var lastPing;

    $scope.pinger = function() {
        promise = $interval(function() {
            console.log('sending ping at time ', Date.now());
            lastPing = Date.now();
            //socket.send('pong');
            }, 2000);
        }
    

    var checkerPromise;
    var rcPromise;
    var started = false;

    var pingCheck = function() {
        //good connection
        if (Date.now() - lastPing < 5000) {
            console.log('maintain');
            $scope.action = 'Connected';
            return 'connected';
        }
        //bad connection - spam pongs to network
        else if (Date.now() - lastPing < 10000){
            console.log('Bad connection or maybe lost connection');
            rcPromise = $interval(pinger,1000);
            $scope.action = 'Reconnecting';
        }
        //lost connection, DC
        else {
            console.log('lost connxn');
            $interval.cancel(checkerPromise);
            $scope.action = 'Disconnected';
            return 'disconnected';

        }
    }

    var pinger2 = function(socket) {
        socket.send('pong');
    }

    $scope.connect = function () {
        var ws = new WebSocket("ws://localhost:8080/websocket");

        ws.onopen = function (event) {
            console.log('successfully connected');
            lastPing = Date.now();
            pingerPromise = $interval(pinger2(ws),3000);
            checkerPromise = $interval(pingCheck, 1000);

            $scope.action = 'Connected';

        }

        ws.onmessage = function(event) {
            console.log('received msg from server');
            lastPing = Date.now();
        }

        ws.onclose = function(event) {
            console.log('disconnected from server');
            $interval.cancel(checkerPromise);
            $interval.cancel(pingerPromise);
            $scope.action = 'Disconnected';
        }
    }
});