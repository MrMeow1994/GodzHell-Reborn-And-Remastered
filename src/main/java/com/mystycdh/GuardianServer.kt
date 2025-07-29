package com.mystycdh

import com.Ghreborn.jagcached.util.DeepTracer
import com.Ghreborn.jagcached.util.HyperTraceEngine
import com.github.benmanes.caffeine.cache.Caffeine
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.net.*
import java.sql.DriverManager.println
import java.time.Duration
import java.util.*
import java.util.concurrent.*
import java.util.concurrent.atomic.AtomicInteger

object GuardianServer {

    // Tunable via system props / env vars
    private val PORT: Int = System.getProperty("guardian.port", "29432").toInt()
    private val MAX_REQUESTS_PER_SECOND: Int = System.getProperty("guardian.maxRps", "10").toInt()
    private val MAX_CONNECTIONS_PER_MINUTE: Int = System.getProperty("guardian.maxCpm", "60").toInt()
    private val BLACKLIST_DURATION_MS: Long = Duration.ofMinutes(1).toMillis()
    private const val BAN_FILE = "banned_ips.txt"
    private const val MAX_LOG_ENTRIES = 100
    private val portToRealIp = ConcurrentHashMap<Int, String>()

    // Sliding‑window counters via Caffeine
    private val requestCounts = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofSeconds(1))
        .build<String, AtomicInteger>()

    private val connectionCounts = Caffeine.newBuilder()
        .expireAfterWrite(Duration.ofMinutes(1))
        .build<String, AtomicInteger>()

    // Ban info + pending flush set
    private val bannedClients = ConcurrentHashMap<String, BanInfo>()
    private val pendingBans = ConcurrentSkipListSet<String>()

    // Active connection tracking
    val activeConnections = ConcurrentHashMap<String, ConnectionInfo>()
    val liveSockets = ConcurrentHashMap<String, Socket>()

    // Thread pools
    private val clientHandlerPool: ExecutorService = ThreadPoolExecutor(
        10, 100, 60L, TimeUnit.SECONDS, LinkedBlockingQueue(500)
    )
    private val ioPool: ExecutorService = Executors.newCachedThreadPool()
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1)

    @JvmStatic
    fun main(args: Array<String>) {
        Runtime.getRuntime().addShutdownHook(Thread { shutdown() })
        loadBannedIps()

        // Batch‑flush bans every 5 seconds
        scheduler.scheduleWithFixedDelay({ flushPendingBans() }, 5, 5, TimeUnit.SECONDS)
        startUdpListener(PORT)
        // Launch dashboard
        javax.swing.SwingUtilities.invokeLater {
            com.mystycdh.gui.GuardianDashboard().isVisible = true
        }
        ServerSocket(PORT).use { serverSocket ->
            //logger.info("Guardian server is running on port {}", PORT)
            while (true) {

                val clientSocket = serverSocket.accept()
                val ip = clientSocket.inetAddress.hostAddress
                if (isBanned(ip)) {
                    //logger.warn("Rejected banned IP {}", ip)
                    clientSocket.close()
                    continue
                }
                clientSocket.soTimeout = 20000
                clientHandlerPool.submit { handleClient(clientSocket) }
            }
        }
    }
    private fun isUntrusted(trace: String, hyperTrace: String): Boolean {
        return trace.contains("Proxy: true") || trace.contains("Hosting: true") || hyperTrace.contains("Data Center")
    }
    private fun handleClient(clientSocket: Socket) {
        val ip = clientSocket.inetAddress.hostAddress

        //logger.debug("Incoming connection from {}", ip)
        liveSockets[ip] = clientSocket
        try {
            val traceReport = DeepTracer.analyze(ip)
            val hyperTrace = HyperTraceEngine.traceIP(ip)

            if (isUntrusted(traceReport, hyperTrace)) {
                kotlin.io.println("[BLOCKED] $ip | Reason: Untrusted | Trace: $traceReport | Hyper: $hyperTrace")
                //returnCode = 26;
                // savefile = false;
                // disconnected = true;
                return
            }
            if (rateLimit(ip) && throttleConnections(ip)) {
                forwardToGameServer(clientSocket)
            } else {
                forwardToFalseServer(clientSocket)
            }
        } catch (e: IOException) {
            //logger.error("I/O error for {}: {}", ip, e.message)
        } finally {
            liveSockets.remove(ip)
            try { clientSocket.close() } catch (_: IOException) { }
        }
    }

    private fun isBanned(ip: String): Boolean {
        val banInfo = bannedClients[ip]
        return banInfo != null && System.currentTimeMillis() < banInfo.banEndTime
    }
    @JvmStatic
    fun getRealIpForLoginSocket(socket: Socket): String {
        return portToRealIp[socket.port] ?: socket.inetAddress.hostAddress
    }
    fun ban(ip: String) {
        val banEnd = System.currentTimeMillis() + BLACKLIST_DURATION_MS
        bannedClients[ip] = BanInfo(ip, banEnd)
        pendingBans += ip
        //logger.info("Banned IP {} until {}", ip, Date(banEnd))

        // Kick any live connection
        liveSockets.remove(ip)?.let { socket ->
            try {
                socket.close()
                //logger.debug("Kicked active socket for {}", ip)
            } catch (e: IOException) {
                //logger.warn("Failed to kick {}: {}", ip, e.message)
            }
        }
    }

    private fun rateLimit(ip: String): Boolean {
        val counter = requestCounts.get(ip) { AtomicInteger(0) }
        if (counter.incrementAndGet() > MAX_REQUESTS_PER_SECOND) {
            ban(ip)
            return false
        }
        return true
    }

    private fun throttleConnections(ip: String): Boolean {
        val counter = connectionCounts.get(ip) { AtomicInteger(0) }
        if (counter.incrementAndGet() > MAX_CONNECTIONS_PER_MINUTE) {
            ban(ip)
            return false
        }
        return true
    }
    private fun startUdpListener(port: Int = PORT) {
        val udpSocket = DatagramSocket(port)
        val buffer = ByteArray(8192)

        ioPool.submit {
            println("Guardian UDP listener active on port $port")

            while (!udpSocket.isClosed) {
                try {
                    val packet = DatagramPacket(buffer, buffer.size)
                    udpSocket.receive(packet)

                    val clientIp = packet.address.hostAddress
                    if (isBanned(clientIp)) continue

                    if (!rateLimit(clientIp) || !throttleConnections(clientIp)) {
                        continue
                    }

                    // Forward UDP packet to local RakNet listener (e.g., Geyser)
                   val geyserAddress = InetSocketAddress("127.0.0.1",19132)
                    val target = DatagramPacket(
                        packet.data,
                        packet.length,
                        geyserAddress.address,
                        geyserAddress.port // If Geyser also listens on the same port (25588)
                    )
                    udpSocket.send(target)

                    // Log
                    val info = activeConnections.computeIfAbsent(clientIp) {
                        ConnectionInfo(clientIp, MAX_LOG_ENTRIES)
                    }
                    info.incrementSent(packet.length)
                    info.logSent(packet.data.copyOf(packet.length))

                } catch (e: IOException) {
                    println("UDP error: ${e.message}")
                }
            }
        }
    }

    private fun forwardToGameServer(clientSocket: Socket) {
        val ip = clientSocket.inetAddress.hostAddress
        Socket("127.0.0.1", 19562).use {

            gameServerSocket ->
            // Store the *real* IP associated with Guardian’s outbound port
            portToRealIp[clientSocket.localPort] = ip
            // Store the mapping: Guardian's local port used to connect RSPS → real client IP
            val connectionInfo = activeConnections.computeIfAbsent(ip) {
                ConnectionInfo(ip, MAX_LOG_ENTRIES)
            }

            val latch = CountDownLatch(2)

            ioPool.submit {
                proxyStream(
                    clientSocket.getInputStream(),
                    gameServerSocket.getOutputStream(),
                    connectionInfo::incrementReceived,
                    connectionInfo::logReceived
                )
                latch.countDown()
            }

            ioPool.submit {
                proxyStream(
                    gameServerSocket.getInputStream(),
                    clientSocket.getOutputStream(),
                    connectionInfo::incrementSent,
                    connectionInfo::logSent
                )
                latch.countDown()
            }

            latch.await()
        }
    }


    private fun proxyStream(
        inStream: java.io.InputStream,
        outStream: java.io.OutputStream,
        byteCounter: (Int) -> Unit,
        loggerFn: (ByteArray) -> Unit
    ) {
        val buffer = ByteArray(8 * 1024)
        try {
            var read: Int
            while (inStream.read(buffer).also { read = it } != -1) {
                outStream.write(buffer, 0, read)
                byteCounter(read)
                loggerFn(buffer.copyOf(read))
            }
        } catch (_: IOException) {
        }
    }

    private fun forwardToFalseServer(clientSocket: Socket) {
        Thread.sleep(10_000)
    }

    private fun flushPendingBans() {
        if (pendingBans.isEmpty()) return
        synchronized(BAN_FILE) {
            try {
                BufferedWriter(FileWriter(BAN_FILE, true)).use { writer ->
                    pendingBans.forEach { writer.appendLine(it) }
                }
                pendingBans.clear()
                //logger.debug("Flushed bans to file")
            } catch (e: IOException) {
                //logger.error("Failed to flush bans: {}", e.message)
            }
        }
    }

    private fun loadBannedIps() {
        val file = File(BAN_FILE)
        if (!file.exists()) return
        try {
            file.readLines().forEach {
                val ip = it.trim()
                if (ip.isNotEmpty()) {
                    bannedClients[ip] = BanInfo(ip, Long.MAX_VALUE)
                    //logger.debug("Loaded banned IP {}", ip)
                }
            }
        } catch (e: IOException) {
            //logger.error("Failed to load banned IPs: {}", e.message)
        }
    }

    private fun shutdown() {
        //logger.info("Shutting down GuardianServer…")
        try { clientHandlerPool.shutdownNow() } catch (_: Throwable) {}
        try { ioPool.shutdownNow() } catch (_: Throwable) {}
        try { scheduler.shutdownNow() } catch (_: Throwable) {}
    }

    private data class BanInfo(val ip: String, val banEndTime: Long)

    data class ConnectionInfo(
        val ip: String,
        private val maxEntries: Int,
        @Volatile var bytesReceived: Long = 0,
        @Volatile var bytesSent: Long = 0,
        @Volatile var lastUpdated: Long = System.currentTimeMillis()
    ) {
        private val receivedLog: Deque<String> = ArrayDeque(maxEntries)
        private val sentLog: Deque<String> = ArrayDeque(maxEntries)

        fun incrementReceived(n: Int) {
            bytesReceived += n
            lastUpdated = System.currentTimeMillis()
        }

        fun incrementSent(n: Int) {
            bytesSent += n
            lastUpdated = System.currentTimeMillis()
        }

        fun logReceived(data: ByteArray) = synchronized(receivedLog) {
            addToLog(receivedLog, data)
        }

        fun logSent(data: ByteArray) = synchronized(sentLog) {
            addToLog(sentLog, data)
        }

        private fun addToLog(log: Deque<String>, data: ByteArray) {
            val entry = data.toString(Charsets.UTF_8).take(200)
            if (log.size == maxEntries) log.removeFirst()
            log.addLast(entry)
        }

        // **New**: expose snapshots for the GUI
        fun getRecentReceivedData(): List<String> = synchronized(receivedLog) {
            receivedLog.toList()
        }

        fun getRecentSentData(): List<String> = synchronized(sentLog) {
            sentLog.toList()
        }
    }
}
