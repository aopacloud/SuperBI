# base image
FROM --platform=$TARGETPLATFORM openjdk:8-jdk-alpine

RUN apk add fontconfig && apk add --update ttf-dejavu  \
    && apk add tzdata && cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime \
    && echo "Asia/Shanghai" > /etc/timezone && apk del tzdata \
    && apk add nginx && mkdir -p /run/nginx \
    && fc-cache --force && rm -rf /var/cache/apk/*

WORKDIR /app

COPY super-bi.jar ./
COPY super-bi ./super-bi
COPY nginx.conf /etc/nginx/nginx.conf
COPY start.sh ./

EXPOSE 80

ENTRYPOINT ["sh","/app/start.sh"]