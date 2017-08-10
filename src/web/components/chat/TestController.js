angular.module('chatApp').controller('TestController', function($scope, $state, $uibModalInstance) {
    alert('testcontroller loaded');
    $scope.testModal = "/components/chat/test.html";
})