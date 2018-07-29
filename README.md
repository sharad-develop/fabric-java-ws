# FabCar Service

This project demonstrates the integration of spring/java/webservices with the hyperledger java sdk.
It primarily illustrates client sdk exposed as webservice for the fabcar sample n/w which is
available at https://github.com/hyperledger/fabric-samples.

## Pre-req
Download fabcar at https://github.com/hyperledger/fabric-samples and run it on docker container.

## App details
Before invoking any chaincode requests, crypto material needs to be generated for the admin user.
The api right now only demonstrates generation of crypto material for Preregistered admin only.
That admin is admin/adminpw.
Once admin is enrolled all the crypto material will be written to hfc_key_store folder and accessed
for any subsequent requests.

## Environment
I have used Intellij and java 1.8 to build the app.

## Run
Run the CarApplication class which starts the embedded tomcat server at localhost:8080

## Usage

All available application endpoints are documented using [Swagger](http://swagger.io/).

You can view the Swagger UI at http://localhost:8080/swagger-ui.html. From here you
can perform all interactions with hyperledger through the api.

Example requests:
### Enroll admin
/enroll/admin
{"name":"admin", "secret":"adminpw"}

### Add car to fabcar n/w
/create
{
  "colour": "black",
  "key": "CAR99",
  "make": "TESLA",
  "model": "X",
  "owner": "ELON MUSK"
}


### Fetch all the cars in the n/w
/query/all


### Fetch a car in the n/w
/query/CAR99

For more detailed info java sdk interaction with hyperledger refer to EndtoEndIT.java at
https://github.com/hyperledger/fabric-sdk-java