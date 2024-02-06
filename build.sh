#!/usr/bin/env bash

source /etc/profile

BASE_DIR=$(cd "$(dirname "$0")"; pwd)

# check maven environment
mvn_path="$(which mvn)"
if [ -z "${mvn_path}" ]; then
  echo "maven not found, please install maven first"
  exit 1
fi
# check npm environment
npm_path="$(which npm)"
if [ -z "${npm_path}" ]; then
  echo "npm not found, please install npm first"
  exit 1
fi

## build backend project
cd ${BASE_DIR}/backend
mvn clean package -Dmaven.test.skip=true

## build frontend project
cd ${BASE_DIR}/frontend
npm install --force && npm run build

if [ $# -gt 0 ] && [ "docker" = "$1" ]; then
  # build docker image
  rm ${BASE_DIR}/docker/super-bi.jar
  rm -r ${BASE_DIR}/docker/super-bi
  cp ${BASE_DIR}/backend/super-bi/target/super-bi.jar ${BASE_DIR}/docker/super-bi.jar
  cp -r ${BASE_DIR}/frontend/dist-production ${BASE_DIR}/docker/super-bi
  cd ${BASE_DIR}/docker
  docker build -t superbi:1.0.0 .
fi

echo "build success `date +%Y-%m-%d\ %H:%M:%S`"
