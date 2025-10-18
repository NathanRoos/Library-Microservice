#!/usr/bin/env bash

spring init \
--boot-version=3.4.3 \
--build=gradle \
--type=gradle-project \
--java-version=21 \
--packaging=jar \
--name=customer-service \
--package-name=com.nathanroos.library.customersubdomain \
--groupId=com.nathanroos.library.customersubdomain \
--dependencies=web,data-jpa,data-jdbc,h2,lombok,validation \
--version=1.0.0-SNAPSHOT \
customer-service

spring init \
--boot-version=3.4.3 \
--build=gradle \
--type=gradle-project \
--java-version=21 \
--packaging=jar \
--name=libraryworker-service \
--package-name=com.nathanroos.library.libraryworkerssubdomain \
--groupId=com.nathanroos.library.libraryworkerssubdomain \
--dependencies=web,data-jpa,data-jdbc,h2,lombok,validation \
--version=1.0.0-SNAPSHOT \
libraryworker-service

spring init \
--boot-version=3.4.3 \
--build=gradle \
--type=gradle-project \
--java-version=21 \
--packaging=jar \
--name=loan-service \
--package-name=com.nathanroos.library.loansubdomain \
--groupId=com.nathanroos.library.loansubdomain \
--dependencies=web,data-jpa,data-jdbc,h2,lombok,validation \
--version=1.0.0-SNAPSHOT \
loan-service

spring init \
--boot-version=3.4.3 \
--build=gradle \
--type=gradle-project \
--java-version=21 \
--packaging=jar \
--name=library-service \
--package-name=com.nathanroos.library.librarysubdomain \
--groupId=com.nathanroos.library.librarysubdomain \
--dependencies=web,data-jpa,data-jdbc,h2,lombok,validation \
--version=1.0.0-SNAPSHOT \
library-service

spring init \
--boot-version=3.4.3 \
--build=gradle \
--type=gradle-project \
--java-version=21 \
--packaging=jar \
--name=api-gateway \
--package-name=com.nathanroos.library.apigateway \
--groupId=com.nathanroos.library.apigateway \
--dependencies=web,webflux,data-jpa,data-jdbc,h2,lombok,validation,hateoas \
--version=1.0.0-SNAPSHOT \
api-gateway
