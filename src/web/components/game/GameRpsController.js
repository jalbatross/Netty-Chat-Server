angular.module("chatApp").controller("GameRpsController", function($scope, websockets, game) {
    var $rps = this;

    $rps.bestOf = 5;
    $rps.gameOver = false;

    $rps.p1Wins = new Array(($rps.bestOf + 1) / 2).fill(false);
    $rps.p2Wins = new Array(($rps.bestOf + 1) / 2).fill(false);

    $rps.player1 = {};
    $rps.player2 = {};

    $rps.player1.name = "Alice";
    $rps.player2.name = "Bob";

    $rps.countdownFinished = function() {
        var randomNumber = Math.floor(Math.random() * 1000);
        //Choose random winner
        if (randomNumber % 2 === 0) {
            $rps.win();
        } 
        else {
            $rps.lose();
        }
        if (!$rps.gameOver) {
            $scope.$broadcast('timer-set-countdown', 5);
            $scope.$broadcast('timer-start');
        }
    }

    $rps.win = function() {
        for (let i = 0; i < $rps.p1Wins.length; i++) {
            if ($rps.p1Wins[i] === true) {
                continue;
            } 
            else {
                $rps.p1Wins[i] = true;
                $scope.$apply();
                if (i === $rps.p1Wins.length - 1) {
                    $rps.gameOver = true;
                }
                return;
            }
        }
    }

    $rps.lose = function() {
        for (let i = $rps.p2Wins.length - 1; i >= 0; i--) {
            if ($rps.p2Wins[i] === true) {
                continue;
            } 
            else {
                $rps.p2Wins[i] = true;
                $scope.$apply();
                if (i === 0) {
                    $rps.gameOver = true;
                }
                return;
            }
        }
    }



    $scope.$on('$destroy', function() {
        alert('destroyed rps');
        $rps = {};
    })

})