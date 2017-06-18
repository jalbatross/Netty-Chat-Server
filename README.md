# Netty-Chat-Server
Simple netty chat server &amp; client

# Development Log

# Version 0.10 
### May 19, 2017
 - Right now just messing around with trying to get a simple echo server working on localhost.
 - Managed to host the server on EC2 after toying around with Linux commands.
 - Realized that we need to probably make this a REST API type of service for it to be cross platform.

### May 23, 2017
 - Made server *theoretically* capable of accepting WSS connections. Trying to test it but running into troubles with using a self-signed  SSL certificate for the server.
 - Current issue is that System.setProperty is not properly using the SSL certificate. System.getProperty returns null for "ssl" after setting. Might be issue with keystore, cert, password, or all three.

### June 18, 2017
 - Things are humming along nicely. Been awhile since I've developed due to business from work - got plenty of time now that summer is here!
 - WSS connections work now. Made a hacky solution using my own SSL certificate.
 - Server now takes messages from client then encapsulates it in a JSON object with the current time, placeholder for author of the message, and the message itself. Used the GSON library for this.
 - Coming up next we need to make it so that each user can submit their username so that the server knows how to delegate the author of each outgoing message. 
