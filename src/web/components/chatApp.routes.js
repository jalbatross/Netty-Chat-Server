//chatApp.routes.js

var chatApp = angular.module('chatApp', ['ui.router']);

chatApp.config(function($stateProvider, $urlRouterProvider) {

    $urlRouterProvider.otherwise('/');

    $stateProvider

        // HOME STATES AND NESTED VIEWS ========================================
        .state('login', {
            url: '/components/login',
            templateUrl: 'components/login/login.html'
        })

        // ABOUT PAGE AND MULTIPLE NAMED VIEWS =================================
        .state('authorized', {
            url: 'components/chat',
            templateUrl: 'components/chat/chat.html'
            // we'll get to this in a bit       
        });

});
