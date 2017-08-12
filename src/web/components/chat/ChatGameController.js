/**
 * ChatGameController.js
 *
 * All Game modals and controllers are nested in this controller.
 *
 * Responsible for switching out modals related to Game lobby, Game lobbies,
 * and Game lobby creation.
 *
 * References to $ctrl in modal html files found in /components/game refer to
 * ChatGameController functions and objects.
 */

angular.module("chatApp").controller("ChatGameController", function($rootScope, $scope, $uibModalInstance, websockets, game) {

    //alert('ChatGameController instance started');

    var gameLobbiesUrl = "components/game/game-lobbies-modal.html";
    var createGameUrl = "components/game/game-create-modal.html";
    var gameLobbyUrl = "components/game/game-lobby-modal.html";
    var rpsGameUrl = "components/game/game-rps-modal.html";

    var $ctrl = this;

    $ctrl.gameModalTemplate = gameLobbiesUrl;

    $ctrl.close = function() {
        if (game.inLobby()) {
            game.leaveGame();
        }
        $uibModalInstance.close();
    };

    $ctrl.cancel = function() {
        $uibModalInstance.dismiss('cancel');
    };

    $ctrl.showCreateGameDialog = function() {
        $ctrl.gameModalTemplate = createGameUrl;
    };

    $ctrl.showGameLobbiesDialog = function() {
        if (game.inLobby()) {
            game.leaveGame();
        }
        $ctrl.gameModalTemplate = gameLobbiesUrl;
    };

    $ctrl.showGameLobbyDialog = function() {
        $ctrl.gameModalTemplate = gameLobbyUrl;
    };

    $ctrl.showGameDialog = function() {
        $ctrl.gameModalTemplate = rpsGameUrl;
    }

    var updateGameListener = $rootScope.$on('updateGame', function() {
        $ctrl.showGameLobbyDialog();
        $scope.$apply();

    });

    var kickedListener = $rootScope.$on('quitLobby', function() {
        console.log('[ChatGameController] Kicked from lobby');
        $ctrl.showGameLobbiesDialog();

        $scope.$apply();
    });

    $scope.$on('modal.closing', function() {
        //alert('destroyed ChatGameController');

        //Turn off listeners
        kickedListener();
        updateGameListener();

        //Get rid of scope vars
        $ctrl = {};
    });

});