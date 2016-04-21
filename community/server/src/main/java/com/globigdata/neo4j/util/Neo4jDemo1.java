package com.globigdata.neo4j.util;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;


public class Neo4jDemo1 {
	public static void main(String[] args) {
		String neo4jUrlStr = "http://localhost:7474/db/data/cypher";
		//String matchTestStr = "{\"query\":\"MATCH (p:Pesron {name: {name}}) RETURN p\", \"params\":{\"name\":\"villcore\"}}";
        String matchTestStr = "{\"query\" : \"CREATE (n:DISTRICT1:INSTANCE1:batch_insert_test_0 { props } ) RETURN n\",\"params\": {\"props\" : {\"prop1\":\"value1\",\"prop2\":\"value2\",\"id\":\"1\"} }}";

		//String matchTestStr = "{\"query\":\"CREATE (p:Pesron {name: {name}}) RETURN p\", \"params\":{\"name\":\"villcore\"}}";

		try {
			URL neo4jUrl = new URL(neo4jUrlStr);
			URLConnection neo4jConn = neo4jUrl.openConnection();
			
			HttpURLConnection neo4jHttpConn = (HttpURLConnection) neo4jConn;
			
			neo4jHttpConn.setDoOutput(true);
			neo4jHttpConn.setRequestMethod("POST");
			
			String authorization = new String(Base64.getEncoder().encode("neo4j:123456".getBytes("utf-8")), "utf-8");
			System.out.println("authorization : " + authorization);
			//ÔÝÊ±×¢ÊÍ
			neo4jHttpConn.setRequestProperty("Content-Type", "application/json; charset=utf-8");
			neo4jHttpConn.setRequestProperty("Accept","application/json; charset=utf-8");
			neo4jHttpConn.setRequestProperty("Authorization","Basic " + authorization);

			try(OutputStream os = neo4jHttpConn.getOutputStream()) {
				os.write(matchTestStr.getBytes("utf-8"));
			}
			
			System.out.println(neo4jHttpConn.getResponseCode());
			try {
				InputStream is = neo4jHttpConn.getInputStream();
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				byte[] bytes = new byte[1024];
				int pos = -1;
				while((pos = is.read(bytes)) > 0) {
					bos.write(bytes, 0, pos);
				}
				
				System.out.println(bos.toString("utf-8"));
				
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
