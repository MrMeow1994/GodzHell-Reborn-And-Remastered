package com.Ghreborn.jagcached.util;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

public class IPBacktraceUtil {

    private static final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    private static final String LOG_ROOT = "./data/logs/ip";

    public static String traceIP(String ip) {
        if (cache.containsKey(ip)) return cache.get(ip);

        try {
            URL url = new URL("http://ip-api.com/json/" + ip + "?fields=status,message,country,regionName,city,zip,lat,lon,org,as,mobile,proxy,hosting,query");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(2000);
            conn.setReadTimeout(2000);
            conn.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    json.append(line);
                }

                String data = json.toString();
                if (!data.contains("\"status\":\"success\"")) {
                    log(ip, "error.log", "Failed: " + data);
                    return "(unknown)";
                }

                // Extract fields
                String country = extract(data, "country");
                String region = extract(data, "regionName");
                String city = extract(data, "city");
                String org = extract(data, "org");
                String asn = extract(data, "as");
                String proxy = extract(data, "proxy");
                String hosting = extract(data, "hosting");
                String mobile = extract(data, "mobile");

                // Format trace strings
                String geo = String.format("%s, %s (%s)", country, region, city);
                String organization = String.format("Org: %s | ASN: %s", org, asn);
                String metadata = String.format("Proxy: %s | Hosting: %s | Mobile: %s", proxy, hosting, mobile);
                String full = geo + " | " + organization + " | " + metadata;

                // Log entries
                log(ip, "geo.log", geo);
                log(ip, "org.log", organization);
                log(ip, "meta.log", metadata);
                log(ip, "full.log", full);

                cache.put(ip, full);
                return full;
            }

        } catch (Exception e) {
            log(ip, "error.log", "Exception: " + e.getMessage());
            return "(trace failed)";
        }
    }

    private static void log(String ip, String fileName, String message) {
        File dir = new File(LOG_ROOT, ip);
        if (!dir.exists()) {
            boolean created = dir.mkdirs();
            if (!created) {
                System.err.println("⚠ Failed to create directory for IP: " + ip);
                return;
            }
        }

        File logFile = new File(dir, fileName);
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
            writer.write("[" + timestamp + "] " + message);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("⚠ Failed to write log for IP " + ip + ": " + e.getMessage());
        }
    }

    private static String extract(String json, String key) {
        int start = json.indexOf("\"" + key + "\":");
        if (start == -1) return "?";
        int valueStart = start + key.length() + 3;
        if (json.charAt(valueStart) == '"') {
            int end = json.indexOf('"', valueStart + 1);
            return json.substring(valueStart + 1, end);
        } else {
            int end = json.indexOf(',', valueStart);
            if (end == -1) end = json.length() - 1;
            return json.substring(valueStart, end).trim();
        }
    }
}
