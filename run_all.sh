#!/bin/bash

cd client
npm install
node app.js &
CLIENT_PID=$!

cd -
cd server
./mvnw clean package jetty:run

# this line is reached after a ^C, so there is no need to kill the server process here
kill $CLIENT_PID
