#!/usr/bin/env bash

if [[ -z "${PROJECT_ROOT}" ]]; then
    echo "$PROJECT_ROOT must be set!"
fi

export $(grep -v '^#' "${PROJECT_ROOT}/.env" | xargs)

@JAVA@/bin/java \
    -Dlogging.level.root=info \
    -Dtelegram.bot.token=$telegram_token \
    -Dtelegram.bot.name=$telegram_name \
    -Dspring.datasource.url="jdbc:oracle:thin:@${db_tns_name}?TNS_ADMIN=${ROOT_DIR}/wallet" \
    -Dspring.datasource.username=$db_user \
    -Dspring.datasource.password=$dbpassword \
    -Dspring.datasource.driver-class-name=$driver_class_name \
    -jar @JAR@

