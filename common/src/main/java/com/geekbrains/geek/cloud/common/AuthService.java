package com.geekbrains.geek.cloud.common;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AuthService {
    private static Connection connection;
    private static Statement stmt;
    private static PreparedStatement prstmt;

    public static void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:DB/clients.db");
            stmt = connection.createStatement();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getNickByLoginAndPass(String login, String pass) throws SQLException {
        login = login.toLowerCase();
        String sql = String.format("SELECT nickname FROM clients where login = '%s' and password = '%s'", login, pass);
        ResultSet rs = stmt.executeQuery(sql);

        if(rs.next()) {
            return rs.getString(1);
        }

        return null;
    }

    public static String getNickByLogin(String login) throws SQLException {
        login = login.toLowerCase();
        String sql = String.format("SELECT nickname FROM clients where login = '%s'", login);
        ResultSet rs = stmt.executeQuery(sql);

        if(rs.next()) {
            return rs.getString(1);
        }

        return null;
    }

    public static void registerPerson(String login, String pass, String nick) throws SQLException {
        prstmt = connection.prepareStatement("INSERT INTO clients (login,password,nickname) VALUES (?,?,?)");
        prstmt.setString(1,login);
        prstmt.setString(2,pass);
        prstmt.setString(3,nick);
        prstmt.execute();
    }

    public static List<String> LoadBlackList(String name) {
        List<String> blacklist = new ArrayList<>();
        try {
            String sql = String.format("SELECT c1.nickname FROM clients as c1 Inner Join BlackList as b on c1.id=b.id_block_user Inner Join clients as c2 on c2.id=b.id_user WHERE c2.nickname='%s'",name);
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                blacklist.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return blacklist;
    }

    public static boolean addBlackList(String nickThis, String nickBlack) {
        boolean res = false;
        if (!nickThis.equalsIgnoreCase(nickBlack)) {
            try {
                String id_user;
                String id_block_user;
                String sql = String.format("SELECT clients.id FROM clients WHERE clients.nickname='%s'",nickBlack);
                ResultSet rs = stmt.executeQuery(sql);
                if (rs.next()) {
                    sql = String.format("SELECT clients.id FROM clients WHERE clients.nickname='%s'", nickThis);
                    rs = stmt.executeQuery(sql);
                    id_user = rs.getString(1);
                    sql = String.format("SELECT clients.id FROM clients WHERE clients.nickname='%s'", nickBlack);
                    rs = stmt.executeQuery(sql);
                    id_block_user = rs.getString(1);
                    sql = String.format("INSERT INTO BlackList (id_user,id_block_user) VALUES ('%s','%s')", id_user, id_block_user);
                    stmt.execute(sql);
                    res = true;
                } else {
                    res = false;
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else {
            res = false;
        }
        return res;
    }

    public static void historyMsg(Long time, String msg) {
        try {
            prstmt = connection.prepareStatement("INSERT INTO history (Date,Message) VALUES (?,?)");
            prstmt.setLong(1,time);
            prstmt.setString(2,msg);
            prstmt.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String loadHistoryMsg() {
        String res="";
        try {
            StringBuilder sb = new StringBuilder();
            ResultSet rs = stmt.executeQuery("SELECT * FROM history order by Date desc limit 50;");
            while (rs.next()) {
                sb.insert(0,rs.getString("Message")+"\n");
            }
            res = sb.toString();
            res = res.substring(0,res.length()-1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return res;
    }

    public static void disconnect() {
        try {
            connection.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
