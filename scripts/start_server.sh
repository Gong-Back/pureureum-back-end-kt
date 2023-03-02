#!/bin/bash
PROJECT_NAME="pureureum"
BUILD_LIBS_PATH="/home/ec2-user/$PROJECT_NAME/build/libs/"
JAR_PATH="$BUILD_LIBS_PATH*-SNAPSHOT.jar"
DEPLOY_PATH=/home/ec2-user/$PROJECT_NAME/
DEPLOY_LOG_PATH="/home/ec2-user/$PROJECT_NAME/logs/deploy.log" # Deploy Log 파일
DEPLOY_ERR_LOG_PATH="/home/ec2-user/$PROJECT_NAME/logs/deploy_err.log" # Deploy Error Log 파일
BUILD_JAR=$(ls $JAR_PATH)
JAR_NAME=$(basename $BUILD_JAR)

cd $DEPLOY_PATH

echo "===== 배포 시작 : $(date +%c) =====" >> $DEPLOY_LOG_PATH

echo "> build 파일명: $JAR_NAME" >> $DEPLOY_LOG_PATH

echo "> 현재 동작 중인 애플리케이션 pid 체크" >> $DEPLOY_LOG_PATH
CURRENT_PID=$(pgrep -f $JAR_NAME)

# shellcheck disable=SC2157
if [ -z CURRENT_PID ]
then
  echo "> 현재 동작 중인 애플리케이션이 존재하지 않는다." >> $DEPLOY_LOG_PATH
else
  echo "> 현재 동작 중인 애플리케이션이 존재한다." >> $DEPLOY_LOG_PATH
  echo "> 현재 동작 중인 애플리케이션 강제 종료 진행" >> $DEPLOY_LOG_PATH
  echo "> kill -9 $CURRENT_PID" >> $DEPLOY_LOG_PATH
  kill -9 $CURRENT_PID
fi

DEPLOY_JAR=$DEPLOY_PATH/build/libs/$JAR_NAME
nohup java -jar -Dspring.profiles.active=prod $DEPLOY_JAR --server.port=8080 >> /dev/null 2> $DEPLOY_ERR_LOG_PATH &

sleep 3

echo "> 배포 종료 : $(date +%c)" >> $DEPLOY_LOG_PATH
