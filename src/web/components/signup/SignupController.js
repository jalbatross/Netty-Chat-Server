angular.module("chatApp").controller("SignupController", function($scope, $http, $state) {
    $scope.response = false;
    $scope.error = false;
    var aurl = "http://localhost:8080";

    $scope.signup = function() {
        var builder = new flatbuffers.Builder(1024);

        var nameData = builder.createString($scope.username);
        var pwData = builder.createString($scope.password);
        $scope.username = "";
        $scope.password = "";


        Schema.Credentials.startCredentials(builder);
        Schema.Credentials.addUsername(builder, nameData);
        Schema.Credentials.addPassword(builder, pwData);
        Schema.Credentials.addSignup(builder, true);

        var cred = Schema.Credentials.endCredentials(builder);

        Schema.Message.startMessage(builder);
        Schema.Message.addDataType(builder, Schema.Data.Credentials);
        Schema.Message.addData(builder, cred);

        var data = Schema.Message.endMessage(builder);
        builder.finish(data);

        var credBytes = builder.asUint8Array();

        var config = {'Content-Type': 'application/x-www-form-urlencoded'};

        $http({
            method: 'post',
            url: aurl,
            data: credBytes,
            headers: config,
            responseType: "arraybuffer",
            transformRequest: []
        }).then(function (response) {
            $scope.response = true;
            $scope.error = false;

            $scope.responseInfo = "Successfully registered!";

        }, function (error) {
            $scope.response = true;
            $scope.error = true;
            switch(error.status) {
                case -1:
                  console.log("Couldn't establish connection to the server!");
                  $scope.responseInfo = "Couldn't establish connection to the server!";
                  break;
                case 422:
                  $scope.responseInfo = "Invalid username/password combination!";
                  break;
                default:
                  console.log("Unk error");
                  console.log("Errorcode: " + error.status);
                  $scope.responseInfo = "ERROR";
                  break;
            }



        });
    }
})