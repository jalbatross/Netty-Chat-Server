//statusController.js

/**
 * Controller that indicates the status of a WebSockets connection to the UI.
 * 
 * Also handles the connection, disconnection, reconnection, and heartbeat
 * via ping/pong with a WebSockets server. 
 *
 * by @jalbatross (Joey Albano)
 */

angular.module("pingApp").controller("statusController", function($scope, $interval) {
    
    $scope.action = "Connect";

    var uri = "ws://localhost:8080/websocket";

    var connected = false;
    var reconnecting = false;

    //websockets variable
    var ws;

    //promises for functions that will be called at intervals
    var rcPromise;
    var checkerPromise;
    var pingerPromise;

    //a long corresponding to Unix time in ms
    var lastPing;

    //constants for connection times and interval times
    var GOOD_CONNECTION_MS = 5000;
    var TIMEOUT_THRESHOLD_MS = 10000;
    var PINGER_INTERVAL_MS = 3000;
    var CHECKER_INTERVAL_MS = 1000;

    /**
     * pingCheck is a function that checks the status of the connection based
     * on the time of the last received message from the server compared to the
     * time that pingCheck is called.
     *
     * pingCheck should be called regularly throughout the application life cycle
     * when the user is connected to make sure that the WebSockets connection is
     * being maintained.
     *
     * If the time between the last ping received and the current time that the
     * function is called is within the GOOD_CONNECTION_MS threshold, nothing
     * happens. If it is between the GOOD_CONNECTION_MS and TIMEOUT_THRESHOLD_MS
     * times, then the UI is changed to reflect that the server connection may be
     * lossy and that the client is attempting to reconnect by spamming pongs
     * to the network.
     *
     * Passing the threshold results in a server disconnect by the client which is also
     * displayed in the View.
     * 
     * @param  {WebSockets} socket    a WebSockets object
     * @return void
     */
    var pingCheck = function(socket) {

        if (Date.now() - lastPing <= GOOD_CONNECTION_MS) {
            //console.log('maintain');

            if (reconnecting) {
                $interval.cancel(rcPromise);
                reconnecting = false;
            }

            $scope.action = 'Connected';
        }

        //bad connection - spam pongs to network
        else if (Date.now() - lastPing <= TIMEOUT_THRESHOLD_MS) {
            //console.log('Bad connection or maybe lost connection');
            if (!reconnecting) {
                rcPromise = $interval(pinger, 1000);
                $scope.action = 'Reconnecting';
                reconnecting = true;
            }
        }

        //lost connection, DC
        else {
            closeWs();
            //console.log('lost connxn');

            if (reconnecting) {
                $interval.cancel(rcPromise);
                reconnecting = false;
            }

            $scope.action = 'Disconnected';
        }
    }

    /**
     * A pinger that sends a pong to a WebSockets server.
     *
     * Intended to be used with $interval so that it fires
     * regularly.
     * 
     * @param  {WebSockets} socket  a WebSockets object
     * @return void
     */
    var pinger = function(socket) {
        socket.send('pong');
    }

    /**
     * Updates the view to show that the client is disconnected,
     * then performs actions to close the WebSockets connection
     * and cleanup interval functions that were firing during connection.
     * 
     * @return void
     */
    var closeWs = function() {
        $scope.action = 'Disconnected';
        connected = false;
        ws.close();
        $interval.cancel(checkerPromise);
        $interval.cancel(pingerPromise);
    }

    /**
     * On-click function that connects to a WebSockets server
     *
     * Assigns pinger and checkerPromise at predefined intervals
     *
     * Updates the view when connected but not when disconnected,
     * wsClose() handles that.
     * 
     * @return void
     */
    $scope.connect = function() {
        if (!connected) {
            ws = new WebSocket(uri);

            ws.onopen = function(event) {
                console.log('successfully connected');
                lastPing = Date.now();
                pingerPromise = $interval(function() {
                    pinger(ws)
                }, PINGER_INTERVAL_MS);
                checkerPromise = $interval(function() {
                    pingCheck(ws)
                }, CHECKER_INTERVAL_MS);
                
                connected = true;

                $scope.action = 'Connected';

            }

            ws.onmessage = function(event) {
                console.log('received msg from server');
                lastPing = Date.now();
            }

            ws.onclose = function(event) {
                console.log('ws socket closed');
            }
        } 
        else {
            closeWs();
            console.log('closed ws connection');
        }
    }
});