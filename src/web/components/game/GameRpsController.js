angular.module("chatApp").controller("GameRpsController", function($scope, websockets, game) {
    var $rps = this;

    $rps.game = game.currentGame();

    $rps.bestOf = $rps.game.bestOf;
    $rps.gameOver = $rps.game.completed;

    $rps.p1Wins = new Array(($rps.bestOf + 1) / 2).fill(false);
    $rps.p2Wins = new Array(($rps.bestOf + 1) / 2).fill(false);

    $rps.player1 = {};
    $rps.player2 = {};

    //Get player name
    if (websockets.getUsername() === $rps.game.players[0]) {
        $rps.player1.name = $rps.game.players[0];
        $rps.player2.name = $rps.game.players[1];
    }
    else {
        $rps.player1.name = $rps.game.players[1];
        $rps.player2.name = $rps.game.players[0];
    }

    //TODO: implement send choice
    $rps.countdownFinished = function() {
        //Choose random choice if user has not sent in a choice yet
        if (!$rps.sentChoice) {
            var randomNumber = Math.floor(Math.random() * 1000) % 3;
            switch (randomNumber) {
                case 0:
                  //rock
                  break;
                case 1:
                  //paper
                  break;
                case 2:
                  //scissors
                  break;
                default:
                  alert('error');
            }

            sendChoice($rps.choice);
            $rps.sentChoice = true;
        }


        if (!$rps.gameOver) {
            $scope.$broadcast('timer-set-countdown', 10);
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