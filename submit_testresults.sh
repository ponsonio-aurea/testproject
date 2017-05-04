#!/usr/bin/env bash

set -euo -pipefail

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
mclient --quiet cp build/test-results/test/*.xml s3/${S3_BUCKET}/${TESTRUN_ID}/testresults/

PREFIX="s3://${S3_BUCKET}/${TESTRUN_ID}/testresults/"

PAYLOAD=$(ls build/test-results/test/*.xml | sed 's#build/test-results/test/##g' | \
  sed 's/\(.*\)/\"\1\"/g' | tr '\n' ' ' | sed 's/ \(\S\)/,\1/g' | sed 's/.*/[&]/g' | \
  jq -cM '["'${PREFIX}'"+.[]] | {xunitXmlUris: .}')

RES=$(curl --fail --silent --show-error -X POST --header 'Content-Type: application/json' \
  --header 'Accept: application/json' -d "${PAYLOAD}" \
  "${PROXY_ENDPOINT}/testruns/${TESTRUN_ID}/testresults/xunit")

echo $RES | jq ".artifactSubmissionId"
