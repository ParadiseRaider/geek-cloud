package com.geekbrains.geek.cloud.client;

import java.io.*;
import java.net.Socket;

public class MainClient {
    public static void main(String[] args) {
        try {
            Socket socket = new Socket("localhost",8189);
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            sendFile("winter.jpg", out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void sendFile(String filename, DataOutputStream out) throws IOException {
        out.write(15);
        int filenameLength = filename.length();
        out.writeInt(filenameLength);
        out.write(filename.getBytes());
        out.writeLong(new File("client_repository/"+filename).length());
        byte[] buf = new byte[8192];
        try (InputStream in = new FileInputStream("client_repository/"+filename)) {
            int n;
            while ((n=in.read(buf))!=-1) {
                out.write(buf,0,n);
            }
        }
        System.out.println(String.format("Клиент: фаил %s отправлен", filename));
    }
}
