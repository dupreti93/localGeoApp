# SSO start url
aws configure sso
url: https://d-9a676284b3.awsapps.com/start
region: us-east-2

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

### Cloudwatch agent
sudo systemctl start amazon-cloudwatch-agent
sudo systemctl status amazon-cloudwatch-agent








