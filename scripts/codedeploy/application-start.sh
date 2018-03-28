#!/bin/bash

AWS_REGION=
CONFIG_BUCKET=
ECR_REPOSITORY_URI=
GIT_COMMIT=

INSTANCE=$(curl -s http://instance-data/latest/meta-data/instance-id)
INSTANCE_NUMBER=$(aws --region $AWS_REGION ec2 describe-tags --filters "Name=resource-id,Values=$INSTANCE" "Name=key,Values=Name" --output text | awk '{print $6}')
CONFIG=$(aws --region $AWS_REGION ec2 describe-tags --filters "Name=resource-id,Values=$INSTANCE" "Name=key,Values=Configuration" --output text | awk '{print $5}')

if [[ $DEPLOYMENT_GROUP_NAME =~ [a-z]+-publishing ]]; then
  CONFIG_DIRECTORY=publishing
else
  CONFIG_DIRECTORY=web
fi

(aws s3 cp s3://$CONFIG_BUCKET/babbage/$CONFIG_DIRECTORY/$CONFIG.asc . && gpg --decrypt $CONFIG.asc > $CONFIG) || exit $?

source $CONFIG

if [[ $DEPLOYMENT_GROUP_NAME =~ [a-z]+-web ]]; then
  if [[ $INSTANCE_NUMBER == 1 ]]; then
    ELASTICSEARCH_HOST=$ELASTICSEARCH_1
    CONTENT_SERVICE_URL=$CONTENT_SERVICE_URL_1
  else
    ELASTICSEARCH_HOST=$ELASTICSEARCH_2
    CONTENT_SERVICE_URL=$CONTENT_SERVICE_URL_2
  fi
fi

docker run -d                                                          \
  --env=CONTENT_SERVICE_MAX_CONNECTION=$CONTENT_SERVICE_MAX_CONNECTION \
  --env=CONTENT_SERVICE_URL=$CONTENT_SERVICE_URL                       \
  --env=ELASTIC_SEARCH_CLUSTER=cluster                                 \
  --env=ELASTIC_SEARCH_SERVER=$ELASTICSEARCH_HOST                      \
  --env=ENABLE_CACHE=$ENABLE_CACHE                                     \
  --env=GHOSTSCRIPT_PATH=$GHOSTSCRIPT_PATH                             \
  --env=GLOBAL_CACHE_SIZE=$GLOBAL_CACHE_SIZE                           \
  --env=HIGHCHARTS_EXPORT_SERVER=$HIGHCHARTS_EXPORT_SERVER             \
  --env=IS_PUBLISHING=$IS_PUBLISHING                                   \
  --env=PHANTOMJS_PATH=$PHANTOMJS_PATH                                 \
  --env=TABLE_RENDERER_HOST=$TABLE_RENDERER_HOST                       \
  --env=MAP_RENDERER_HOST=$MAP_RENDERER_HOST                           \
  --env=REDIRECT_SECRET=$REDIRECT_SECRET                               \
  --name=babbage                                                       \
  --net=$DOCKER_NETWORK                                                \
  --restart=always                                                     \
  $ECR_REPOSITORY_URI/babbage:$GIT_COMMIT
