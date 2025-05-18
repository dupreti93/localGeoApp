# API calls

[//]: # (curl "http://18.224.30.8:8080/api/feed/restaurants?city=City+of+New+York")
[//]: # (curl "http://18.224.30.8:8080/api/feed/events?city=City+of+New+York")


### Pins: 

curl -X POST "http://18.224.30.8:8080/api/posts/pin?shared=true&type=food"   -H "Content-Type: application/j
son"   -H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNzQ0NDk4NTI2LCJleHAiOjE3NDQ1ODQ5MjZ9.sGlvRR-KIgnilaidtElAx_WGwPuBN3MgqcdU5V1rDybQ91YKYMDE9AmMFsRMyjBtbF9pDCnd68CKk3iQzmD22g"   -d '{"content":"Great cafe","latitude":40.74,"longitude":-73.98}'
{"postId":"3f0de868-ec55-43b5-93b3-1bfa6121d4fa","userId":"user123","content":"Great cafe","latitude":40.74,"longitude":-73.98,"city":"City of New York","category":"pin","timestamp":"2025-04-13T00:40:56.419517308Z","geoHash":"dr5ru8","shared":true,"type":"food"}

curl -X GET "http://18.224.30.8:8080/api/posts/my-pins" \
-H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNzQ0MzQxOTMyLCJleHAiOjE3NDQ0MjgzMzJ9.XU2UYm3klmVhXZLroFPS4B_qI1VOj-XwSYxNZM-lpNTR-UuL40PD13TTG-h_IzIRDoQqPch-PwDkJdceQ-jocA"

curl -X DELETE "http://18.224.30.8:8080/api/posts/pin/243801ef-b450-446e-8169-0c8c92ad547f" \
-H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNzQ0MzQxOTMyLCJleHAiOjE3NDQ0MjgzMzJ9.XU2UYm3klmVhXZLroFPS4B_qI1VOj-XwSYxNZM-lpNTR-UuL40PD13TTG-h_IzIRDoQqPch-PwDkJdceQ-jocA"

curl -X PUT "http://18.224.30.8:8080/api/posts/pin/7e5dcbd4-cebd-4251-b109-a615771005cf" \
-H "Content-Type: application/json" \
-H "Authorization: Bearer eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNzQ0MzQxOTMyLCJleHAiOjE3NDQ0MjgzMzJ9.XU2UYm3klmVhXZLroFPS4B_qI1VOj-XwSYxNZM-lpNTR-UuL40PD13TTG-h_IzIRDoQqPch-PwDkJdceQ-jocA" \
-d '{"content":"Updated spot","latitude":40.74,"longitude":-73.98,"shared":true}'

curl -X GET "http://18.224.30.8:8080/api/feed/shared?lat=40.7128&lon=-74.0060&radiusMiles=5.0"

Get Pins:

curl -X GET "http://18.224.30.8:8080/api/posts/my-pins?userId=user123"

Get Feed:

curl -X GET "http://18.224.30.8:8080/api/feed/shared?lat=40.7128&lon=-74.0060&radiusMiles=5.0"


### User:

Register:
curl -X POST "http://18.224.30.8:8080/api/auth/register" \
-H "Content-Type: application/json" \
-d '{"username":"user123","password":"pass123"}'

Login:
curl -X POST "http://18.224.30.8:8080/api/auth/login" \
-H "Content-Type: application/json" \
-d '{"username":"timessquarefan","password":"pass123"}'

Token:
eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyMTIzIiwiaWF0IjoxNzQ0NDk4NTI2LCJleHAiOjE3NDQ1ODQ5MjZ9.sGlvRR-KIgnilaidtElAx_WGwPuBN3MgqcdU5V1rDybQ91YKYMDE9AmMFsRMyjBtbF9pDCnd68CKk3iQzmD22g

eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyNDU2IiwiaWF0IjoxNzQ0NTA1Nzc3LCJleHAiOjE3NDQ1OTIxNzd9.aZLMRqBpj_5TUDu4sogoPjcJRYgV2Rti75eg32pEE2HYjCCm2UkukPVQxZe-1FBuYqc_vcv4T1yUqhyHjk89Rw

# SSO start url
url: https://d-9a676284b3.awsapps.com/start
region: us-east-2

## Ticketmaster
Consumer key: iAcSKeK7GtYqoRgu13dl6ofGOplBl4xT
Consumer secret: fLbEz9HWC2wGDEna

## FourSquares
fsq3LjORgWnhW9YgtxhFjnW5K2LLgg0ab3892chwbpAhbFw=


## MapBox
pk.eyJ1IjoiZHVwcmV0aSIsImEiOiJjbTlxaG41Y20wOWVqMmpvZHN5OTR2dnBkIn0.GNnnW-_aqXYZsV-9H7uBnQ

# MacOs:

### Rebuild: 
mvn clean install

### Sync code changes:  
rsync -avz -e "ssh -i 'Nearby App Key Pair.pem'" /Users/divyansh_upreti/IdeaProjects/localGeoApp ec2-user@18.224.30.8:/home/ec2-user/

# EC-2 instance:
### Ssh to instance
ssh -i "Nearby App Key Pair.pem" ec2-user@18.224.30.8

### Rebuild and repackage: 
mvn clean package spring-boot:repackage

### Run server
java -jar /home/ec2-user/localGeoApp/target/localGeoApp-1.0-SNAPSHOT.jar

### Instance id:
aws ec2 describe-instances --filters "Name=ip-address,Values=18.224.30.8" --query "Reservations[].Instances[].InstanceId" --region us-east-2 --profile Admin --output text
i-07bc8c181d485fa23

### Cloudwatch agent
sudo systemctl start amazon-cloudwatch-agent
sudo systemctl status amazon-cloudwatch-agent








