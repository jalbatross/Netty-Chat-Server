angular.module("chatApp").controller("GameRpsController", function($scope, websockets, game) {
    var $rps = this;

    $rps.p1Wins = [true, true, false];
    $rps.p2Wins = [false, false, true];

    $rps.player1 = {};
    $rps.player2 = {};

    $rps.player1.name = "Alice";
    $rps.player2.name = "Bob";

    $scope.$on('$destroy', function() {
        alert('destroyed rps');
        $rps = {};
    })
})