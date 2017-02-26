# instatweet
A social networking application that uses JMS.

## TODO
- add glassfish download to `init.sh` and merge some scripts


## Software needed
Download a version of GlassFish server

Set the env adding `asadmin` and `appclient` from GlassFish

## How to build

run `./init.sh`

run `./build.sh`

## How to run (manual)
Start GlassFish server and add resources
    
    asadmin start-domain
 
If you want to stop the domain use
    
    asadmin stop-domain
     
Launch RMI registry in a different shell from instatweet folder

    ./runrmi.sh

In the previous shell again from instatweet folder run the server

    ./runServer.sh
    
And then, the client:
   
    ./runClient.sh
    


