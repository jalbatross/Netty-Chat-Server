//chatApp.routes.js

chatApp.config(function($stateProvider, $urlRouterProvider) {

    $urlRouterProvider.otherwise('/');

    $stateProvider

        // HOME STATES AND NESTED VIEWS ========================================
        .state('/', {
            url: '/',
            templateUrl: '/components/login/login.html'
        })

        .state('chat', {
            url: '/chat',
            templateUrl: 'components/chat/chat.html'
        })

        // ABOUT PAGE AND MULTIPLE NAMED VIEWS =================================
        .state('authorized', {
            url: 'components/chat',
            templateUrl: 'components/chat/chat.html'
            // we'll get to this in a bit       
        });

});
