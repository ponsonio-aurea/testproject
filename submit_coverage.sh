#!/usr/bin/env bash

if [ -z $1 ]; then
  (>&2 echo "format: $0 <testrun_id> [url_endpoint] [s3|minio_endpoint] [access key] [secret key] [bucket]")
  exit 1
fi

TESTRUN_ID=$1
PROXY_ENDPOINT=${2:-"http://testserver-worker:8081/ts/v2"}
S3_ENDPOINT=${3:-"http://minio:9000"}
S3_KEY=${4:-testkey}
S3_SECRET=${5:-testsecret}
S3_BUCKET=${6:-testserver-testing}

mclient config host add s3 ${S3_ENDPOINT} ${S3_KEY} ${S3_SECRET}
mclient mb s3/${S3_BUCKET} || true # might be created already

pushd build/classes/main

zip -r ../../../main.zip *

popd

pushd build/classes/test

zip -r ../../../test.zip *

popd

mclient --quiet cp build/jacoco/test.exec s3/${S3_BUCKET}/${TESTRUN_ID}/jacoco/
mclient --quiet cp main.zip s3/${S3_BUCKET}/${TESTRUN_ID}/jacoco/
mclient --quiet cp test.zip s3/${S3_BUCKET}/${TESTRUN_ID}/jacoco/
mclient --quiet cp mapping.json s3/${S3_BUCKET}/${TESTRUN_ID}/jacoco/

PREFIX="s3://${S3_BUCKET}/${TESTRUN_ID}/jacoco"

PAYLOAD='{
  "jacocoExecFilesUris": [
    "'$PREFIX'/test.exec"
  ],
  "classPathUris": [
    "'$PREFIX'/main.zip","'$PREFIX'/test.zip"
  ],
  "sourceFileToDfScmUrlMappingUri": "'$PREFIX'/mapping.json"
}'


RES=$(curl --fail --silent --show-error -X POST --header 'Content-Type: application/json' \
  --header 'Accept: application/json' -d "${PAYLOAD}" \
  "${PROXY_ENDPOINT}/testruns/${TESTRUN_ID}/coverage/jacoco")

echo $RES | jq ".artifactSubmissionId"
