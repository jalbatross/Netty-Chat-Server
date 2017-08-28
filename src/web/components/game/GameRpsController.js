angular.module("chatApp").controller("GameRpsController", function($scope, websockets, game, $rootScope) {
    var $rps = this;

    $rps.game = game.currentGame();

    $rps.bestOf = $rps.game.bestOf;
    $rps.gameOver = $rps.game.completed;

    $rps.p1Wins = new Array(($rps.bestOf + 1) / 2).fill(false);
    $rps.p2Wins = new Array(($rps.bestOf + 1) / 2).fill(false);

    $rps.player1 = {};
    $rps.player2 = {};
    $rps.playerId = -1;
    $rps.opponentId = -1;

    $rps.p1Choice = -1;
    $rps.p2Choice = -1;

    $rps.won = false;


    $rps.sentChoice = false;

    /**
     * Listens for RPS Game Update byte[]
     * Contains [player1choice,player2choice,winner]
     * 
     * CHOICES:
     * Rock: 0
     * Paper: 1
     * Scissors: 2
     *
     * Winner:
     * Player 1: 0
     * Player 2: 1
     * Draw: 2
     * 
     */
    //Get player name
    if (websockets.getUsername() === $rps.game.players[0]) {
        $rps.player1.name = $rps.game.players[0];
        $rps.player2.name = $rps.game.players[1];
        $rps.playerId = 0;
        $rps.opponentId = 1;
    } else {
        $rps.player1.name = $rps.game.players[1];
        $rps.player2.name = $rps.game.players[0];
        $rps.playerId = 1;
        $rps.opponentId = 0;
    }

    /**
     * Listens for update from server
     * @param  {[type]} ) {                   
     * @return {[type]}   [description]
     */
    $rps.updateListener = $rootScope.$on('updateGame', function() {
        let updateArr = game.gameUpdate();

        $rps.p1Choice = updateArr[$rps.playerId];
        $rps.p2Choice = updateArr[$rps.opponentId];

        console.log('[GameRpsController] Winner byte was: ', updateArr[2]);
        if (updateArr[2] === $rps.playerId) {
            $rps.win();
        } else if (updateArr[2] === $rps.opponentId) {
            $rps.lose();
        } else {
            $rps.win();
            $rps.lose();
        }

        $scope.$apply();
    });

    /** Sends RPS command 
     *
     * @param {string} command  Rps command, rock paper or scissors
     */
    $rps.sendCommand = function(command) {
        if ($rps.gameOver) {
            return;
        }
        var byte = -1;

        switch (command) {
            case 'rock':
                byte = 0;
                $rps.p1Choice = 0;
                break;
            case 'paper':
                byte = 1;
                $rps.p1Choice = 1;
                break;
            case 'scissors':
                byte = 2;
                $rps.p1Choice = 2;
                break;
            default:
                byte = 0;
                $rps.p1Choice = 0;
                console.log('[GameRpsController] error with command, set sendbyte to rock');
        }
        var byteArr = [];
        byteArr.push(byte);

        let data = makeGameUpdate(byteArr);
        websockets.getSocket().send(data);
        console.log('[GameRpsController] sent command with byte: ', byte);

        $rps.sentChoice = true;
    }

    $rps.countdownFinished = function() {
        //Choose random choice if user has not sent in a choice yet
        if (!$rps.sentChoice) {
            var randomNumber = Math.floor(Math.random() * 1000) % 3;
            switch (randomNumber) {
                case 0:
                    $rps.choice = 'rock';
                    break;
                case 1:
                    $rps.choice = 'paper';
                    break;
                case 2:
                    $rps.choice = 'scissors'
                    break;
                default:
                    alert('error');
            }

            $rps.sendCommand($rps.choice);
        }

        $rps.sentChoice = false;


        if (!$rps.gameOver) {
            $scope.$broadcast('timer-set-countdown', 4);
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
                    $rps.won = true;
                    console.log('[RpsController] Game over, we won!');
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
                    console.log('[RpsController] Game over, we lost');
                    $rps.gameOver = true;
                }
                return;
            }
        }
    }

    /**
     * Makes Schema.GameUpdate object from bytes
     * @param  {byte[]} bytes 
     * @return {Uint8Array}       Flatbuffers message object with Schema.GameUpdate 
     *                            bytes enclosed
     */
    function makeGameUpdate(bytes) {
        var builder = new flatbuffers.Builder(1024);

        var updateOffset = Schema.GameUpdate.createUpdateVector(builder, bytes);
        Schema.GameUpdate.startGameUpdate(builder);
        Schema.GameUpdate.addUpdate(builder, updateOffset);
        let update = Schema.GameUpdate.endGameUpdate(builder);

        Schema.Message.startMessage(builder);
        Schema.Message.addDataType(builder, Schema.Data.GameUpdate);
        Schema.Message.addData(builder, update);

        let data = Schema.Message.endMessage(builder);

        builder.finish(data);

        return builder.asUint8Array();
    }

    $scope.$on('$destroy', function() {
        alert('destroyed rps');
        $rps = {};
    })

})