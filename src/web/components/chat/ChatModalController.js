angular.module("chatApp").controller("ChatModalController", function($uibModal, $log, $document, game) {
    var $ctrl = this;

    $ctrl.items = ['item1', 'item2', 'item3'];

    var chatLobbiesUrl = '/components/chat/chat-lobbies-modal.html';

    $ctrl.openChatLobbies = function() {
        var modalInstance = $uibModal.open({
            animation: $ctrl.animationsEnabled,
            ariaLabelledBy: 'modal-title',
            ariaDescribedBy: 'modal-body',
            templateUrl: chatLobbiesUrl,
            controller: 'ChatLobbiesController',
            controllerAs: '$ctrl',
            size: 'sm'
        });

        modalInstance.result.then(function () {
            $log.info('ChatLobbies modal dismissed at: ' + new Date());
        });
    }

    $ctrl.openGames = function() {
        if (game.inLobby() /*|| game.inGame()*/) {
            //no-op
        }
        var modalInstance = $uibModal.open({
            animation: $ctrl.animationsEnabled,
            ariaLabelledBy: 'modal-title',
            ariaDescribedBy: 'modal-body',
            templateUrl: '/components/chat/test-modal-template.html',
            controller: 'ChatGameController',
            controllerAs: '$ctrl',
            size: 'md'
        });

        modalInstance.result.then(function () {
            $log.info('Game modal dismissed at: ' + new Date());
        });

    }
});