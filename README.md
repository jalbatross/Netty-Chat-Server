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
