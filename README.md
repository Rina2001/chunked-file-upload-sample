# Sample application for chunked file upload
[![Build Status](https://travis-ci.org/edsoncunha/chunked-file-upload-sample.svg?branch=master)](https://travis-ci.org/edsoncunha/chunked-file-upload-sample)
[![License Apache2](https://img.shields.io/hexpm/l/plug.svg)](http://www.apache.org/licenses/LICENSE-2.0)

## Client

Requirements:
- [Node.js](https://nodejs.org/en/download/package-manager/)

To install dependencies and run:

    cd client
    npm install
    node app.js

Then browse to [localhost:3000](http://localhost:3000).


## Server

To install and run:

    cd server
    ./mvnw package jetty:run
    
Then browse to [localhost:8002](http://localhost:8002).

...


Or simply cross your fingers and run both at once with:

    ./run_all.sh


### Suggestion for test

Generate large files

    dd if=/dev/urandom of=file.tmp bs=1M count=1024 #creates a 1GB file

Calculate SHA1 

    sha1sum file.tmp

Upload file.tmp, download it from server and check whether the checksums match


# Acknowledgements

This sample takes [Resumable.js](https://github.com/23/resumable.js) and [Swagger code samples](https://github.com/swagger-api/swagger-samples) as a frame of reference.
