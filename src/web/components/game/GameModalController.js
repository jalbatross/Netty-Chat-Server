angular.module("chatApp").controller("GameModalController", function($uibModal, $log, $document, game) {
    var $ctrl = this;

    $ctrl.items = ['item1', 'item2', 'item3'];

    var gameWinUrl = '/components/game/game-victory-modal.html';
    var gameLossUrl = '/components/game/game-loss-modal.html';

    $ctrl.showVictory = function() {
        var modalInstance = $uibModal.open({
            animation: $ctrl.animationsEnabled,
            ariaLabelledBy: 'modal-title',
            ariaDescribedBy: 'modal-body',
            templateUrl: gameWinUrl,
            controller: 'GameOverController';
            controllerAs: '$ctrl',
            size: 'sm'
        });

        modalInstance.result.then(function () {
            $log.info('GameWin modal dismissed at: ' + new Date());
        });
    }

    $ctrl.showDefeat = function() {
        var modalInstance = $uibModal.open({
            animation: $ctrl.animationsEnabled,
            ariaLabelledBy: 'modal-title',
            ariaDescribedBy: 'modal-body',
            templateUrl: gameLossUrl;
            controller: 'GameOverController',
            controllerAs: '$ctrl',
            size: 'sm'
        });

        modalInstance.result.then(function () {
            $log.info('GameOver modal dismissed at: ' + new Date());
        });

    }
});