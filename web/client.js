var socket;
var connected = false;
var connectionFailed = true;

window.onload = function() {
    
    //Functions for connection button
    document.getElementById("connectBtn").addEventListener("click", function() {
        if (!window.WebSocket) {
            console.log("WebSocket not supported");
            return;
        } 
        
        else if (!connected) {
            socket = new WebSocket("ws://ec2-54-67-84-244.us-west-1.compute.amazonaws.com:8080/websocket");
            
            socket.onopen = function wsInit() {
                socket.binaryType = "arraybuffer";
                connected = true;
                document.getElementById("connectBtn").firstChild.data = 
                    "Disconnect from WS";

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
        }
    });
    
    document.getElementById("sendBtn").addEventListener("click", function(){
        if(connected) {
            //get the message
            var msg = new Object();
            msg.author = document.getElementById("nameBox").value;
            msg.message = document.getElementById("msgBox").value;

            //send msg to server
            var jsonMsg = JSON.stringify(msg);
            socket.send(jsonMsg);
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
        var day = "[" + jsonMsg['time'].substring(0,10) + "] ";
        var fixedHour = parseInt(jsonMsg['time'].substring(11,13)) - hourOffset;
       
        //pad hour with zeros if needed
        var fixedHourStr = fixedHour.toString();
        if (fixedHourStr.length < 2) {
            fixedHourStr = "0" + fixedHourStr;
        }
        
        var time = fixedHourStr + jsonMsg['time'].substring(13,16) + " ";
        chatMsg = day + time + jsonMsg['author'] + ": " + jsonMsg['message']+"\n"; 

    } 
    catch(err) {
        chatMsg = wsData +"\n";
    }

    document.getElementById("chatHistory").value += chatMsg;
}


    

    

