#!/bin/bash
awslocal sqs create-queue --queue-name facturas-envio
awslocal sqs create-queue --queue-name facturas-respuesta

# Desplegar Lambda
cd /opt/lambda
zip -r function.zip index.mjs
awslocal lambda create-function \
  --function-name lambda-contabilizadora \
  --runtime nodejs20.x \
  --handler index.handler \
  --zip-file fileb://function.zip \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --environment "Variables={DB_HOST=centralizador-mysql,DB_PORT=3306,DB_USER=root,DB_PASSWORD=root123,DB_NAME=db_procesos_masivos,SQS_RESPONSE_URL=http://sqs.us-east-1.localhost.localstack.cloud:4566/000000000000/facturas-respuesta}"

# Mapear cola de envío como trigger de la Lambda
awslocal lambda create-event-source-mapping \
  --function-name lambda-contabilizadora \
  --event-source-arn arn:aws:sqs:us-east-1:000000000000:facturas-envio \
  --batch-size 1
