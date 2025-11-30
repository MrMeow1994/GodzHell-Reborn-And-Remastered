package com.Ghreborn.jagcached.util;

import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeepTracer {

    private static final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<>();
    private static final int TIMEOUT_MS = 1500;
    private static final int[] PROXY_PORTS = {1080, 3128, 8080, 8443};
    private static final String LOG_ROOT = "./data/logs/ip";

    public static String analyze(String ip) {
        if (cache.containsKey(ip)) return cache.get(ip);

        StringBuilder trace = new StringBuilder();
        trace.append("\uD83D\uDD0D Deep Trace: ").append(ip).append("\n");

        try {
            URL url = new URL("http://ip-api.com/json/" + ip + "?fields=status,message,country,regionName,city,zip,lat,lon,org,as,proxy,hosting,mobile,query");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIMEOUT_MS);
            conn.setReadTimeout(TIMEOUT_MS);
            conn.setRequestMethod("GET");

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                StringBuilder json = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) json.append(line);

                String data = json.toString();
                if (!data.contains("\"status\":\"success\"")) {
                    trace.append("  ↳ Trace failed: Unsuccessful lookup\n");
                    log(ip, "deeptrace.log", trace.toString());
                    return trace.toString();
                }

                Map<String, String> info = extractJson(data);
                trace.append("  ↳ Country: ").append(info.get("country")).append("\n")
                        .append("  ↳ Region: ").append(info.get("regionName")).append("\n")
                        .append("  ↳ City: ").append(info.get("city")).append("\n")
                        .append("  ↳ ASN: ").append(info.get("as")).append("\n")
                        .append("  ↳ Org: ").append(info.get("org")).append("\n")
                        .append("  ↳ Proxy: ").append(info.get("proxy")).append("\n")
                        .append("  ↳ Hosting: ").append(info.get("hosting")).append("\n")
                        .append("  ↳ Mobile: ").append(info.get("mobile")).append("\n");

                try {
                    InetAddress addr = InetAddress.getByName(ip);
                    String host = addr.getCanonicalHostName();
                    trace.append("  ↳ Reverse DNS: ").append(host).append("\n");
                } catch (Exception e) {
                    trace.append("  ↳ Reverse DNS: Failed\n");
                }

                for (int port : PROXY_PORTS) {
                    try (Socket socket = new Socket()) {
                        socket.connect(new InetSocketAddress(ip, port), 500);
                        trace.append("  ↳ Port ").append(port).append(": Open (Proxy suspected)\n");
                    } catch (Exception ignored) {
                        // Closed port; do nothing
                    }
                }

                trace.append("  ↳ Verdict: ");
                if ("true".equals(info.get("proxy")) || "true".equals(info.get("hosting"))) {
                    trace.append("Suspicious Proxy/Host\n");
                } else {
                    trace.append("Likely Residential or Legitimate\n");
                }

            }

        } catch (Exception e) {
            trace.append("  ↳ Trace Exception: ").append(e.getMessage()).append("\n");
        }

        String result = trace.toString();
        cache.put(ip, result);
        log(ip, "deeptrace.log", result);
        return result;
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
            writer.write("[" + timestamp + "]\n" + message + "\n");
        } catch (IOException e) {
            System.err.println("⚠ Failed to write deep trace for " + ip + ": " + e.getMessage());
        }
    }

    private static Map<String, String> extractJson(String json) {
        Map<String, String> map = new HashMap<>();
        String[] fields = {"country", "regionName", "city", "org", "as", "proxy", "hosting", "mobile"};
        for (String field : fields) {
            int idx = json.indexOf("\"" + field + "\":");
            if (idx != -1) {
                int start = json.charAt(idx + field.length() + 3) == '"'
                        ? idx + field.length() + 4
                        : idx + field.length() + 3;
                int end = json.indexOf(json.charAt(start - 1) == '"' ? '"' : ',', start);
                map.put(field, json.substring(start, end).replace("\"", "").trim());
            } else {
                map.put(field, "?");
            }
        }
        return map;
    }
}
