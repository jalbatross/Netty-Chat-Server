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

    var $ctrl = this;

    $ctrl.close = function() {
        $uibModalInstance.close();
    };

    $ctrl.cancel = function() {
        $uibModalInstance.dismiss('cancel');
    };

    $ctrl.gameModalTemplate = "/components/game/game-lobbies-modal.html";

    $ctrl.showCreateGameDialog = function() {
        $ctrl.gameModalTemplate = "/components/game/game-create-modal.html";
    }
    $ctrl.showGameLobbiesDialog = function() {
        if (game.inLobby()) {
            game.leaveGame();
        }
        $ctrl.gameModalTemplate = "/components/game/game-lobbies-modal.html";
    }

    $ctrl.showGameLobbyDialog = function() {
        $ctrl.gameModalTemplate = "/components/game/game-lobby-modal.html";
    }

    var updateGameListener = $rootScope.$on('updateGame', function() {
        $ctrl.gameModalTemplate = "/components/game/game-lobby-modal.html";
        $scope.$apply();

    });

    var kickedListener = $rootScope.$on('quitLobby', function() {
        console.log('[ChatGameController] Kicked from lobby');
        $ctrl.showGameLobbiesDialog();
        
        $scope.$apply();
    })

    $scope.$on('destroy', function() {
        updateGameListener();
        $ctrl = {};
    })

});