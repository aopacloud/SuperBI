#!/bin/sh
APP_NAME="super-bi-push"
PROJECT_NAME="dm-BDP-UI-App-SuperBI-push"
DEPLOY_PATH="/home/dws/service/helicarrier/front/new/dist/micro/${APP_NAME}"
SOURCE_PATH="/home/dws/service/helicarrier/front/deploy/${PROJECT_NAME}/dist"
# 前端项目限定在g1机器上发布

host=${DEPLOY_HOST:=g2}

ENV=$1

zip -qq -r super-bi.push.zip dist-${ENV}.push/*
scp super-bi.push.zip dws@$host:/home/dws/service/helicarrier/front/new/dist/micro
ssh -t dws@$host "cd /home/dws/service/helicarrier/front/new/dist/micro;mv bi bi_$(date '+%Y-%m-%d_%H-%M-%S');unzip -qq super-bi.push.zip && mv dist-${ENV}.push bi"