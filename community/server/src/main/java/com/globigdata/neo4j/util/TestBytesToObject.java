package com.globigdata.neo4j.util;

import org.neo4j.shell.impl.SystemOutput;

import java.io.*;

/**
 * Created by Administrator on 2016/4/21.
 */
public class TestBytesToObject {
    public static void main(String[] args) {
        User user = new User("villcore", 11);

        byte[] userBytes = null;

        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(bos)) {
            oos.writeObject(user);
            oos.flush();
            userBytes = bos.toByteArray();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        if(userBytes == null) {
            return;
        }

        try (ByteArrayInputStream bis = new ByteArrayInputStream(userBytes);
                ObjectInputStream ois = new ObjectInputStream(bis)) {
            User user2 = (User) ois.readObject();
            System.out.println(user2);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(userBytes.length);

    }
}

class User implements Serializable {
    private String name;
    private int age;

    public User(String name, int age) {
        this.name = name;
        this.age = age;
    }

    @Override
    public String toString() {
        return "User{" +
                "name='" + name + '\'' +
                ", age=" + age +
                '}';
    }
}
