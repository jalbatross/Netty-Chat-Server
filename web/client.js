var socket;
var connected = false;

document.getElementById("connectBtn").addEventListener("click", function() {
   if (!window.WebSocket) {
      console.log("WebSocket not supported");
      return;
   } else if (!connected) {
      socket = new WebSocket("ws://localhost:8080/websocket");
      socket.binaryType = "arraybuffer";
      connected = true;
      document.getElementById("connectBtn").firstChild.data = "Disconnect 		   from WS";
      socket.onmessage = function(event) {
         if (event.data instanceof ArrayBuffer) {
            	var view = new DataView(event.data,0,12);
            	var sender = view.getInt32(0);
            	var receiver = view.getInt32(4);
            	var txVal = view.getInt32(8);
            
            	var bufMsg = [sender, receiver, txVal];
            	document.getElementById("chatHistory").value += bufMsg +
               "\n";
         }
         //parse text data
         else{
            parseText(event.data);
         }            
         
      };
   } else {
      socket.close();
      document.getElementById("theButton").firstChild.data = "Connect to WS";
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

function parseText(wsData) {
	var chatMsg = "";

   //parse if it is json
   try{
   	var jsonMsg = JSON.parse(wsData);
      var date = "[" + jsonMsg['time'].substring(0,10) + "] ";
      chatMsg = date + jsonMsg['author'] + ": " + jsonMsg['message']+"\n"; 
      
   } 
   catch(err) {
      chatMsg = wsData +"\n";
   }
   
   document.getElementById("chatHistory").value += chatMsg;

}
