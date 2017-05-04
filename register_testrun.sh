#!/usr/bin/env bash

if [ -z $1 ]; then
  (>&2 echo "format: $0 <build_id> [url_endpoint]")
  exit 1
fi

BUILD_ID=$1
SCM_REVISION=$2

URL=${3:-"http://testserver-api:8080/ts/v2"}

RES=$(curl --fail --silent --show-error -X POST --header 'Content-Type: application/json' --header 'Accept: application/json' \
  -d '{
     "productVersionId": 234,
     "scmRepoUrl": "http://gitserver:3001/testproject.git?branch=master",
     "scmRepoId": 0,
     "scmRevision": "'${SCM_REVISION}'",
     "buildId": '${BUILD_ID}',
     "executionStartUtc": "2017-04-16T15:51:03.993Z",
     "executionEndUtc": "2017-04-16T15:51:03.993Z",
     "originDetails": "string"
  }' \
  "${URL}/testruns/getorcreate"
)

echo $RES | jq ".testrunId"
