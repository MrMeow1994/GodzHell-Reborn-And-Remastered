package com.Ghreborn.jagcached.security;

import com.Ghreborn.jagcached.util.DeepTracer;
import com.Ghreborn.jagcached.util.HyperTraceEngine;

import java.io.*;
import java.net.InetSocketAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class VPNBacktraceLogger {

    private static final File LOG_ROOT = new File("./data/logs/ip");

    /**
     * Attempts to trace origin IP and log proxy/vpn flags.
     * No bans, no traps — just full logging.
     */
    public static void trace(String ip, Map<String, String> headers) {
        try {
            String deep = DeepTracer.analyze(ip);
            String hyper = HyperTraceEngine.traceIP(ip);

            StringBuilder report = new StringBuilder();
            report.append("🛰 Trace for ").append(ip).append("\n");

            report.append("📡 Headers:\n");
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                report.append("  ").append(entry.getKey()).append(": ").append(entry.getValue()).append("\n");
            }

            String[] proxyLeakHeaders = {
                    "X-Real-IP", "X-Forwarded-For", "X-Client-IP", "True-Client-IP",
                    "Forwarded", "CF-Connecting-IP", "Via"
            };
            for (String header : proxyLeakHeaders) {
                if (headers.containsKey(header)) {
                    report.append("\n🔍 Proxy Leak Header: ").append(header)
                            .append(" = ").append(headers.get(header)).append("\n");
                }
            }


            report.append("\n🔎 DeepTrace:\n").append(deep).append("\n");
            report.append("\n🚨 HyperTrace:\n").append(hyper).append("\n");

            log(ip, "vpnbacktrace.log", report.toString());

        } catch (Exception e) {
            System.err.println("⚠ VPNBacktraceLogger failed for " + ip + ": " + e.getMessage());
        }
    }

    private static void log(String ip, String fileName, String content) {
        try {
            File dir = new File(LOG_ROOT, ip);
            if (!dir.exists()) dir.mkdirs();

            File logFile = new File(dir, fileName);
            String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(logFile, true))) {
                writer.write("[" + timestamp + "]\n" + content + "\n");
            }
        } catch (IOException e) {
            System.err.println("⚠ Failed to write log for " + ip + ": " + e.getMessage());
        }
    }
}
