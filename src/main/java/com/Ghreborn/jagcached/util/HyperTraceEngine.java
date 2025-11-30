package com.Ghreborn.jagcached.util;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class HyperTraceEngine {

    private static final String ABUSEIPDB_API_KEY = "fff7f41d7e20e51ac52bb63b265858ce670c1f395c457d49aaa4e7df1c970483abdcd69bfedb7998";
    private static final String IPQS_API_KEY = "YOUR_IPQS_API_KEY";
    private static final String SHODAN_API_KEY = "YOUR_SHODAN_API_KEY";

    private static final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    private static final String LOG_ROOT = "./data/logs/ip";

    public static String traceIP(String ip) {
        if (cache.containsKey(ip)) return cache.get(ip);

        StringBuilder report = new StringBuilder();
        report.append("📡 HyperTrace Report for ").append(ip).append("\n");
        report.append("--------------------------------------------------\n");

        try {
            report.append(getIPAPIInfo(ip)).append("\n");
            report.append(getAbuseIPDBInfo(ip)).append("\n");
            //report.append(getIPQSInfo(ip)).append("\n");
            //report.append(getShodanInfo(ip)).append("\n");
            //report.append(getReverseDNS(ip)).append("\n");

        } catch (Exception e) {
            report.append("Trace failed: ").append(e.getMessage()).append("\n");
        }

        String result = report.toString();
        cache.put(ip, result);
        log(ip, "hypertrace.log", result);
        return result;
    }

    private static String getIPAPIInfo(String ip) throws Exception {
        StringBuilder sb = new StringBuilder("[ip-api.com]\n");
        URL url = new URL("http://ip-api.com/json/" + ip);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(2000);
        conn.setReadTimeout(2000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append("  ").append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private static String getAbuseIPDBInfo(String ip) throws Exception {
        StringBuilder sb = new StringBuilder("[AbuseIPDB]\n");
        URL url = new URL("https://api.abuseipdb.com/api/v2/check?ipAddress=" + ip + "&maxAgeInDays=90");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("Key", ABUSEIPDB_API_KEY);
        conn.setRequestProperty("Accept", "application/json");
        conn.setConnectTimeout(3000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append("  ").append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private static String getIPQSInfo(String ip) throws Exception {
        StringBuilder sb = new StringBuilder("[IPQS]\n");
        URL url = new URL("https://ipqualityscore.com/api/json/ip/" + IPQS_API_KEY + "/" + ip);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(3000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append("  ").append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private static String getShodanInfo(String ip) throws Exception {
        StringBuilder sb = new StringBuilder("[Shodan]\n");
        URL url = new URL("https://api.shodan.io/shodan/host/" + ip + "?key=" + SHODAN_API_KEY);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(3000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append("  ").append(line).append("\n");
            }
        }
        return sb.toString();
    }

    private static String getReverseDNS(String ip) {
        try {
            InetAddress addr = InetAddress.getByName(ip);
            return "[Reverse DNS] " + addr.getCanonicalHostName() + "\n";
        } catch (Exception e) {
            return "[Reverse DNS] Lookup failed\n";
        }
    }

    private static void log(String ip, String fileName, String message) {
        File dir = new File(LOG_ROOT, ip);
        if (!dir.exists() && !dir.mkdirs()) {
            System.err.println("⚠ Failed to create directory for IP: " + ip);
            return;
        }

        File logFile = new File(dir, fileName);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write("[" + timestamp + "]\n" + message + "\n");
        } catch (IOException e) {
            System.err.println("⚠ Failed to write hypertrace for " + ip + ": " + e.getMessage());
        }
    }

}
