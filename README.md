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
 
### June 20, 2017
 - After many trials and tribulations, we now have a **working multi-person chat implementation!** It's all very barebones, but anyone with the client who connects and sends a message will broadcast it to all other people who are connected!
 - I struggled for a long time with trying to get the ChannelGroup working today. Everything added to ChatServerHandler is responsible for the multi-person implementation - the key step was making the ChannelGroup channels a **static final** variable as opposed to just final. 
 - This may have consequences down the road or might be completely not optimal, but it works for now and I'm sticking with it!
 - With all that being said, I think that this update brings on the next version of this project...**version 0.20!!**
 
 # Version 0.20

### June 22, 2017
 - Lots of stuff to mention. First and foremost, we've got a working browser client that can send and handle chat messages! 
 - Browser and Java client both convert timestamps to local machine time in whatever format the machine uses.
 - Server timestamps UTC time in milliseconds so that clients are responsible for how to display the time.
 - Not shown on the commits, but a lot of work has been done on the EC2 side of things, namely:
 1. **Elastic IP Addresses**: The EC2 instance now has its own IP address, so clients don't have to keep being updated to point to new domains for connections.
 2. **Elastic File Systems & Apache Servers**: Had to learn how to host an Apache server on my local machine and on EC2 using Amazon EFS in order to do proper testing of the browser client. Browser client is now hosted on an Apache server.
 - Last but not least, made it so that the EC2 instance auto mounts the EFS and runs the httpd service (Apache) on boot.
 - Coming up next, I want to make it so that users authenticate themselves for login. They should also receive the last 10 sent chat messages in their chat box when they log in.
 
 ### June 24, 2017: Version 0.21
 - Redid a lot of the code for server and both clients to accommodate new feature: user authentication!
 - Users now submit their username to the server after the WS handshake is complete. Once they submit a valid username (one that is not already in the list of connected users and with correct characteristics), the channel pipeline is replaced with one that enables the expected chat server functionality.
 - With the implementation of connected users/verification, the server and client side code for sending/receiving messages is a lot more simplified. JSON messages are only sent from the server and can be parsed by the clients specifically for the purpose of chat transmission, which relieves a lot of the worry that I had before of having to parse JSONs of different formats in different ways.
 - Updates to browser interface because we no longer send a username with every message request. This also reduces the payload of chat transmission between server and client!
 - Up next, I want to implement a username/password system server side. After that's working, let's try using a database!
