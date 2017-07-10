//statusController.js

angular.module("pingApp").controller("statusController", function ($scope, $interval) {
    $scope.connected = false;
    $scope.action = "Connect";

    
    $scope.pingStatus = "off";


    var sendPings = false;
    var promise;
    var lastPing;
    $scope.pinger = function() {
        sendPings = !sendPings;

        if (sendPings) {
            promise = $interval(function() {
                console.log('sending ping at time ', Date.now());
                lastPing = Date.now();
            }, 2000);
        }
        else {
            $interval.cancel(promise);
            console.log('pinger off');
        }
        $scope.pingStatus = sendPings ? "on" : "off";

    }

    var checkerPromise;
    var started = false;

    var pingCheck = function() {
        if (Date.now() - lastPing < 5000) {
            console.log('maintain');
            return 'connected';
        }
        else if (Date.now() - lastPing < 10000){
            console.log('Bad connection or maybe lost connection');
            $scope.action = 'Reconnecting';
        }
        else {
            $scope.toggle = false;
            console.log('lost connxn');
            $interval.cancel(checkerPromise);
            $scope.connected = false;
            $scope.action = 'Disconnected';
            return 'disconnected';

        }
    }

    $scope.connect = function () {
        $scope.connected = !$scope.connected;
        $scope.action = $scope.connected ? 'Connected' : 'Disconnected';

        if($scope.connected) {
            checkerPromise = $interval(pingCheck, 1000);
            console.log('applied checker');

        }
        else {
            $interval.cancel(checkerPromise);
            console.log('stopped checking');
        }
    }
});