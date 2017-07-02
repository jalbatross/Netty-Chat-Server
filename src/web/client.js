var socket;
var connected = false;
var connectionFailed = true;

var fs = require('fs');
var flatbuffers = require('../flatbuffers').flatbuffers;
var MyGame = require('./schema_generated').Message;

var buf = new flatbuffers.ByteBuffer(data);


window.onload = function() {
    
    //Functions for connection button
    document.getElementById("connectBtn").addEventListener("click", function() {
        if (!window.WebSocket) {
            console.log("WebSocket not supported");
            return;
        } 
        
        else if (!connected) {
            //socket = new WebSocket("ws://34.212.146.20:8080/websocket");
            socket= new WebSocket("ws://localhost:8080/websocket");
            socket.onopen = function wsInit() {
                socket.binaryType = "arraybuffer";
                connected = true;
                document.getElementById("connectBtn").firstChild.data = 
                    "Disconnect from WS";
                document.getElementById("chatHistory").value += "Connected to server!\n" +
                		"Please send a username from 1-12 characters in length.\n";

                socket.onmessage = function wsMessageHandler(event) {
                                    
                    if (event.data instanceof ArrayBuffer) {
                        var view = new DataView(event.data,0,12);
                        var sender = view.getInt32(0);
                        var receiver = view.getInt32(4);
                        var txVal = view.getInt32(8);

                        var bufMsg = [sender, receiver, txVal];
                        document.getElementById("chatHistory").value += bufMsg + "\n";
                    }
                    // parse text data
                    else {
                        parseText(event.data);
                    }
                }
            };
            
            socket.onerror = function wsErrorHandler() {
                var errMsg = "ERROR: Couldn't connect to server.";
                document.getElementById("chatHistory").value += errMsg + "\n";
            };
            
        }

        else {
            socket.send("bye");
            socket.close();
            document.getElementById("connectBtn").firstChild.data = "Connect to WS";
            connected = false;
            authorized = false;
        }
    });
    
    document.getElementById("sendBtn").addEventListener("click", function(){
        if(connected) {
            //get the message
            var msg = new Object();
            msg = document.getElementById("msgBox").value;

            //send msg to server
            socket.send(msg);
            document.getElementById("msgBox").value = "";
        }


    });
    
}

function parseText(wsData) {
    var chatMsg = "";
    var hourOffset = new Date().getTimezoneOffset() / 60;
    
    console.log("time offset from GMT: " + hourOffset );

    //parse if it is json
    try{
        var jsonMsg = JSON.parse(wsData);
        var date = new Date(parseInt(jsonMsg['time']));
        var dateString = date.toLocaleString();
        dateString = dateString.replace(',', '');
        
        chatMsg = dateString + " " + jsonMsg['author'] + ": " 
                  + jsonMsg['message']+"\n";
    } 
    catch(err) {
        chatMsg = wsData +"\n";
    }

    document.getElementById("chatHistory").value += chatMsg;
}


    

    

