angular.module("chatApp").controller("ChatGameController", function($scope, websockets, game) {

    $scope.gameModalTemplate = "/components/game/game-lobbies-modal.html";

    $scope.showCreateGameDialog = function() {
        $scope.gameModalTemplate = "/components/game/game-create-modal.html";
    }
    $scope.showGameLobbiesDialog = function() {
        if (game.inLobby()) {
            game.leaveGame();
        }
        $scope.gameModalTemplate = "/components/game/game-lobbies-modal.html";
    }

    $scope.showGameLobbyDialog = function() {
        $scope.gameModalTemplate = "/components/game/game-lobby-modal.html";
    }

});