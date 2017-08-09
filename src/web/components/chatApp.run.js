chatApp.run(['$rootScope', '$uibModalStack', function ($rootScope, $uibModalStack) {
   $rootScope.$on('$locationChangeSuccess', () => $uibModalStack.dismissAll());
   $rootScope.$on('$routeChangeSuccess', () => $uibModalStack.dismissAll());
}]);