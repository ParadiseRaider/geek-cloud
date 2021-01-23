package com.geekbrains.geek.cloud.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class MainServer {
    public static void main(String[] args) {
        try {
            ServerSocket serverSocket = new ServerSocket(8189);
            System.out.println("Сервер запущен. Ожидаем подключение клиента");
            Socket socket = serverSocket.accept();
            System.out.println("Клиент подключился");
            DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            while (true) {
                int n = in.read();
                if (n == 15) {
                    readFile(in);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void readFile(DataInputStream in) throws IOException {
        int fileNameLength = in.readInt();
        byte[] fileNameBytes = new byte[fileNameLength];
        in.read(fileNameBytes);
        String filename = new String(fileNameBytes);
        long fileSize = in.readLong();
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream("server_repository/" + filename))) {
            for (long i = 0; i < fileSize; i++) {
                out.write(in.read());
            }
        }
        System.out.println(String.format("Сервер: фаил %s успешно получен", filename));
    }
}
