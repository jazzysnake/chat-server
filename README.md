# Ktor Chat-Server

_This server is made for the BME Kotlin course, and serves as a backend for a compose-multiplatfrom application_

## Deployment

If you have docker installed, from the root folder of the project run:
```
docker-compose up
```
This will build a jar from the source, start a mongodb container, and then start the server with the produced jar.
The client's source code can be found [here](https://github.com/tuku13/chat-app).

## Features

- Personal chats
- Group chats
- Account creation
- Messages can be accessed from multiple devices

## Architecture

The server is made with ktor, and serves content on REST endpoints.
User data is saved in a mongodb database, apart from the cookies, which are only stored on
the server for faster access. User credentials are hashed with Bcrypt for safe storage.
Websockets are used for the instant messages, and all other endpoints can be accessed with http requests.
The mongodb database is queried with the help of Kmongo, for typesafe queries.
Dependency injection is used everywhere it makes sense, so that components can be swapped on later iterations.
