    angular.module("chatApp")
    .service('websockets', function () {
        var _socket = undefined;
        var _ticket = undefined;

        var baseUri = "ws://localhost:8080/websocket?ticket=";
        var uri = "ws://localhost:8080/websocket?ticket=";

        var connected = false;

        this.getSocket = function() {
            if (connected) {
                return _socket;
            }
            else {
                return null;
            }
        }

        this.setTicket = function(ticketId) {
            console.log("updated ticket");
            _ticket = ticketId;
            uri = baseUri + ticketId;
            console.log("uri: " + uri);
            console.log("ticket: " + _ticket);
        }

        this.connect = function() {
            console.log("ws service trying to connect");
            if (_ticket == null) {
                console.log("can't connect without ticket");
                return;
            }
            
            _socket = new WebSocket(uri);

            _socket.onopen = function() {
                connected = true;
            }
            _socket.onclose = function() {
                connected = false;
            }
            _socket.onerror = function() {
                connected = false;
            }
        }

        this.disconnect = function() {
            if (_socket == null) {
                return;
            }

            connected = false;
            _socket.close();

            console.log("closed ws");
            _socket = undefined;
        }

        this.isConnected = function() {
            return connected;
        }


    });