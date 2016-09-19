#!/bin/bash

AWS_REGION=
CONFIG_BUCKET=
ECR_REPOSITORY_URI=
GIT_COMMIT=

if [[ $DEPLOYMENT_GROUP_NAME =~ [a-z]+-publishing ]]; then
  docker run -d                                           \
    --env=CONTENT_SERVICE_URL=http://zebedee:8080         \
    --env=ELASTIC_SEARCH_CLUSTER=cluster                  \
    --env=ELASTIC_SEARCH_SERVER=elasticsearch             \
    --env=GHOSTSCRIPT_PATH=/usr/bin/gs                    \
    --env=HIGHCHARTS_EXPORT_SERVER=http://highcharts:8080 \
    --env=IS_PUBLISHING=Y                                 \
    --name=babbage                                        \
    --net=publishing                                      \
    --restart=always                                      \
    $ECR_REPOSITORY_URI/babbage:$GIT_COMMIT

    exit $?
fi

INSTANCE=$(curl -s http://instance-data/latest/meta-data/instance-id)
CONFIG=$(aws --region $AWS_REGION ec2 describe-tags --filters "Name=resource-id,Values=$INSTANCE" "Name=key,Values=Configuration" --output text | awk '{print $5}')

(aws s3 cp s3://$CONFIG_BUCKET/babbage/$CONFIG.asc . && gpg --decrypt $CONFIG.asc > $CONFIG) || exit $?

source $CONFIG && docker run -d                                                  \
  --env=CONTENT_SERVICE_MAX_CONNECTION=1000                                      \
  --env=CONTENT_SERVICE_URL=http://zebedee-reader:8080                           \
  --env=ELASTIC_SEARCH_CLUSTER=cluster                                           \
  --env=ELASTIC_SEARCH_SERVER=elasticsearch                                      \
  --env=ENABLE_CACHE=Y                                                           \
  --env=FEEDBACK_FOLDER=$FEEDBACK_FOLDER_PATH                                    \
  --env=GHOSTSCRIPT_PATH=/usr/bin/gs                                             \
  --env=GLOBAL_CACHE_SIZE=5000                                                   \
  --env=HIGHCHARTS_EXPORT_SERVER=http://highcharts:8080                          \
  --env=MAX_NOTIFICATION_TOKENS=$MAX_SLACK_NOTIFICATION_TOKENS                   \
  --env=MS_UNTIL_NEW_NOTIFICATION_TOKEN=$SLACK_NOTIFICATION_TOKEN_BACKOFF_PERIOD \
  --env=PHANTOMJS_PATH=/usr/local/bin/phantomjs                                  \
  --env=PUBLIC_KEY=$PUBLIC_KEY_PATH                                              \
  --env=SLACK_FEEDBACK_CHANNEL_URL=$SLACK_FEEDBACK_CHANNEL_URL                   \
  --env=SLACK_FEEDBACK_NOTIFICATION_HOST=$SLACK_FEEDBACK_NOTIFICATION_HOST       \
  --env=SLACK_THROTTLE_ENABLED=$SLACK_THROTTLE_ENABLED                           \
  --name=babbage                                                                 \
  --net=website                                                                  \
  --restart=always                                                               \
  $ECR_REPOSITORY_URI/babbage:$GIT_COMMIT
