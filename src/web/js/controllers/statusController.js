//statusController.js

angular.module("pingApp").controller("statusController", function ($scope, $interval) {
    $scope.toggle = false;
    $scope.action = "Connect";

    
    $scope.pingStatus = "off";

    var sendPings = false;
    var promise;
    $scope.pinger = function() {
        sendPings = !sendPings;

        if (sendPings) {
            promise = $interval(function() {
                console.log('sending ping at time ', Date.now());
            }, 2000);
        }
        else {
            $interval.cancel(promise);
            console.log('pinger off');
        }
        $scope.pingStatus = sendPings ? "on" : "off";

    }
});