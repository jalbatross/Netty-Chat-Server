# Netty-Chat-Server
Simple netty chat server &amp; client

# Requirements

To run the server, you will need the following:
- A Postgres database with which to store username/password credentials. In src/com/test/chatserver/LoginAuthorizer.java, replace the fields dbUrl, user, and password with your Postgres database credentials.
- Your Postgres database needs a table with two columns. One column stores the usernames (each username should be unique) and the other stores the passwords.
- Make sure that credentialsTableName, usernameColumn, and passwordHashColumn are set to the correct
values for the names of each.

- Once this is setup, you should be able to run the server by running ChatServer.java! 