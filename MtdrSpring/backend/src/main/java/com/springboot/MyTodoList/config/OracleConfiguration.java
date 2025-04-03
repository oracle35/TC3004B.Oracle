package com.springboot.MyTodoList.config;


import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import oracle.jdbc.pool.OracleDataSource;
///*
//    This class grabs the appropriate values for OracleDataSource,
//    The method that uses env, grabs it from the environment variables set
//    in the docker container. The method that uses dbSettings is for local testing
//    @author: peter.song@oracle.com
// */
//
//
@Configuration
public class OracleConfiguration {
    Logger logger = LoggerFactory.getLogger(DbSettings.class);
    @Autowired
    private DbSettings dbSettings;
    @Autowired
    private Environment env;
    @Bean
    public DataSource dataSource() throws SQLException{
        OracleDataSource ds = new OracleDataSource();
        if (env.getProperty("IS_CONTAINER") == "1") {
            logger.info("Container detected, using environment variables");
            ds.setDriverType(env.getProperty("driver_class_name"));
            ds.setURL(env.getProperty("db_url"));
            ds.setUser(env.getProperty("db_user"));
            ds.setPassword(env.getProperty("dbpassword"));
        } else {
            logger.info("Using oracle driver settings defined in application.properties");
            ds.setDriverType(dbSettings.getDriver_class_name());
            ds.setURL(dbSettings.getUrl());
            ds.setUser(dbSettings.getUsername());
            ds.setPassword(dbSettings.getPassword());
        }
        
        logger.info("Using Driver " + ds.getDriverType());
        logger.info("Using URL: " + ds.getURL());
        logger.info("Using Username " + ds.getUser());

        return ds;
    }
}
