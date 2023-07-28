awslocal \
  --region=us-east-1 \
  sqs create-queue \
  --queue-name product_queue \
  --attributes VisibilityTimeout=5
