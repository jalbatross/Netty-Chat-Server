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
            url: '/chat/',
            templateUrl: 'components/chat/chat.html'
        })


});
