package com.voom.messagingservice.infractructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Value("${SPRING_DATA_MONGODB_DATABASE:messaging_db}")
    private String database;

    @Value("${SPRING_DATA_MONGODB_HOST:mongodb}")
    private String host;

    @Value("${SPRING_DATA_MONGODB_PORT:27017}")
    private String port;

    @Override
    protected String getDatabaseName() {
        return database;
    }

    @Override
    protected void configureClientSettings(com.mongodb.MongoClientSettings.Builder builder) {
        builder.applyConnectionString(new com.mongodb.ConnectionString("mongodb://" + host + ":" + port + "/" + database));
    }
}