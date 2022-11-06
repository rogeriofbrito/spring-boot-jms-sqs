# Run tests

1. Start localstack: `docker-compose up -d`
2. Run tests: `mvn test`

# Run application

1. Start localstack: `docker-compose up -d`
2. Run application: `mvn spring-boot:run`
3. Send a message to SQS queue: `aws --endpoint-url=http://localhost:4566 --region=us-east-1 sqs send-message --queue-url=http://localhost:4566/000000000000/product_queue --message-body='{"id":1,"title":"Nike Court","brand":"Nike"}'`

# References
https://github.com/awslabs/amazon-sqs-java-messaging-lib/issues/75
