awslocal \
  --region=us-east-1 \
  sqs create-queue \
  --queue-name product_queue_dead_letter

awslocal \
  --region=us-east-1 \
  sqs create-queue \
  --queue-name product_queue \
  --attributes '{
                 "RedrivePolicy": "{\"deadLetterTargetArn\":\"arn:aws:sqs:us-east-1:000000000000:product_queue_dead_letter\",\"maxReceiveCount\":\"1000\"}"
             }'
