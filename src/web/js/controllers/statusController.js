//statusController.js

angular.module("pingApp").controller("statusController", function ($scope, $interval) {
    $scope.action = "Connect";
    
    var connected = false;

    var ws;

    var rcPromise;
    var checkerPromise;
    var pingerPromise;

    var reconnecting = false;

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
        else if (Date.now() - lastPing < 10000){
            console.log('Bad connection or maybe lost connection');
            rcPromise = $interval(pinger,1000);
            $scope.action = 'Reconnecting';
            reconnecting = true;
        }

        //lost connection, DC
        else {
            socket.close();
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

    $scope.connect = function () {
        if (!connected) {
          ws = new WebSocket("ws://localhost:8080/websocket");

          ws.onopen = function (event) {
              console.log('successfully connected');
              lastPing = Date.now();
              pingerPromise = $interval(function() {pinger(ws)},3000);
              checkerPromise = $interval(function() {pingCheck(ws)}, 1000);
              connected = true;

              $scope.action = 'Connected';

          }

          ws.onmessage = function(event) {
              console.log('received msg from server');
              lastPing = Date.now();
          }

          ws.onclose = function(event) {
              console.log('disconnected from server');
              if ($interval.cancel(checkerPromise)) {
                console.log('turned off checker');
              }
              if ($interval.cancel(pingerPromise)) {
                console.log('turned off pinger');
              }
              checkerPromise = undefined;
              pingerPromise = undefined;
              connected = false;

              $scope.action = 'Disconnected';
              $scope.$apply();

              return;
          }
        }

        else {
            $interval.cancel(checkerPromise);
            $interval.cancel(pingerPromise);
            ws.close();
            console.log('closed ws connection');
        }
    }
});