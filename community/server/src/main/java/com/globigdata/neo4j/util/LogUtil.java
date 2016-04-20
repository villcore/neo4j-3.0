package com.globigdata.neo4j.util;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.neo4j.logging.FormattedLogProvider;
import org.neo4j.logging.Log;
import org.neo4j.logging.LogProvider;

public class LogUtil {
	public static Log getLog(Class<?> clazz) {
		LogProvider userLogProvider = FormattedLogProvider.withDefaultLogLevel(org.neo4j.logging.Level.INFO).toOutputStream( System.out );
		Logger.getLogger( "" ).setLevel( Level.INFO );
		return userLogProvider.getLog(clazz);
	}
	
	public static void main(String[] args) {
		Log log = LogUtil.getLog(LogUtil.class);
		log.info("test...");
	}
}
