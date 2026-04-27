#!/bin/bash
set -euo pipefail

awslocal sqs create-queue --queue-name facturas-envio
awslocal sqs create-queue --queue-name facturas-respuesta

cd /opt/lambda

if [ ! -d "node_modules" ]; then
  echo "ERROR: node_modules ausente en /opt/lambda. Ejecutar 'npm install --production' en proyecto/docker/lambda/ antes de docker-compose up."
  exit 1
fi

# Limpiar artefactos previos (zips temporales o function.zip de arranques anteriores)
find . -maxdepth 1 -type f \( -name 'function.zip' -o -name 'zi*' \) -delete

# Empaquetar en /tmp para evitar problemas de I/O sobre el bind-mount, luego mover
ZIP_TMP="/tmp/function.zip"
rm -f "$ZIP_TMP"
zip -qr "$ZIP_TMP" index.mjs node_modules package.json
mv "$ZIP_TMP" /opt/lambda/function.zip

awslocal lambda create-function \
  --function-name lambda-contabilizadora \
  --runtime nodejs20.x \
  --handler index.handler \
  --zip-file fileb:///opt/lambda/function.zip \
  --role arn:aws:iam::000000000000:role/lambda-role \
  --environment "Variables={DB_HOST=centralizador-mysql,DB_PORT=3306,DB_USER=root,DB_PASSWORD=root123,DB_NAME=db_procesos_masivos,SQS_ENDPOINT_URL=http://centralizador-localstack:4566,SQS_RESPONSE_URL=http://centralizador-localstack:4566/000000000000/facturas-respuesta}"

awslocal lambda wait function-active --function-name lambda-contabilizadora

awslocal lambda create-event-source-mapping \
  --function-name lambda-contabilizadora \
  --event-source-arn arn:aws:sqs:us-east-1:000000000000:facturas-envio \
  --batch-size 1
