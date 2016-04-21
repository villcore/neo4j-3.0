package com.globigdata.neo4j.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.UUID;

import com.globigdata.neo4j.util.OperateType;
import com.globigdata.neo4j.util.TcpMoudleType;
import com.globigdata.neo4j.util.TcpRequest;
import com.globigdata.neo4j.util.TcpResponse;

public class TcpSocketClient {
	public static void main(String[] args) {
		long time = -System.currentTimeMillis();
		int insertCount = 2;

		String queryStr = "{\"query\" : \"CREATE (n:DISTRICT1:INSTANCE1:batch_insert_test_0 { props } ) RETURN n\",\"params\": {\"props\" : {\"prop1\":\"value1\",\"prop2\":\"value2\",\"id\":\"1\"} }}";
		try {
			Socket socket = new Socket("127.0.0.1", 30000);
				OutputStream os = socket.getOutputStream();
				ObjectOutputStream oos = new ObjectOutputStream(os);
				InputStream is = socket.getInputStream();
				ByteArrayOutputStream bos = new ByteArrayOutputStream() ;

			for(int i = 0; i < insertCount; i++) {
				TcpRequest request = new TcpRequest("local", UUID.randomUUID(), System.currentTimeMillis(), TcpMoudleType.CYPHER.getType(), OperateType.EXECUTE.getType(), queryStr);
				oos.writeObject(request);
				oos.flush();

				try (ObjectInputStream ois = new ObjectInputStream(is)) {
					Object obj = ois.readObject();
					if (obj instanceof TcpResponse) {
						TcpResponse response = (TcpResponse) obj;
						System.out.println(response.toString());
					}
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}

				try {
					Thread.sleep(10000);
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		time += System.currentTimeMillis();
		System.out.printf("insert %d records use time is %d\n", insertCount, time);
	}
}
