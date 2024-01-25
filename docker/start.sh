#!/bin/bash

nohup nginx -g "daemon off;" &

java -jar /app/super-bi.jar --spring.profiles.active=prod
