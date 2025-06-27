package com.mystycdh.gui

import com.formdev.flatlaf.FlatDarkLaf
import com.mystycdh.GuardianServer
import java.awt.*
import java.awt.datatransfer.StringSelection
import java.util.*
import javax.swing.*
import javax.swing.Timer
import javax.swing.table.DefaultTableModel
import javax.swing.table.TableCellRenderer
import kotlin.math.abs

class GuardianDashboard : JFrame("Guardian Server Monitor") {

    /** Any connection whose `lastUpdated` is older than this is considered “inactive”. */
    private val STALE_AFTER_MS = 60_000L   // 60 s - adjust as you like

    private val tableModel = DefaultTableModel(
        arrayOf("IP", "Recv (KB)", "Sent (KB)", "Last Updated", "Ban", "Inspect"),
        0
    )
    private val table = JTable(tableModel)

    init {
        try { UIManager.setLookAndFeel(FlatDarkLaf()) } catch (_: Exception) {}

        defaultCloseOperation = EXIT_ON_CLOSE
        size = Dimension(900, 500)
        layout = BorderLayout()

        table.rowHeight = 30
        table.columnModel.getColumn(4).cellRenderer = ButtonRenderer()
        table.columnModel.getColumn(4).cellEditor  = ButtonEditor(JCheckBox())
        table.columnModel.getColumn(5).cellRenderer = ButtonRenderer()
        table.columnModel.getColumn(5).cellEditor  = ButtonEditor(JCheckBox())

        add(JScrollPane(table), BorderLayout.CENTER)

        /* Refresh (and prune) every second */
        Timer(1_000) { pruneAndRefresh() }.start()
    }

    /** Remove stale entries, then repaint the table. */
    private fun pruneAndRefresh() {
        val now = System.currentTimeMillis()

        /* --- purge old entries from the backing map --- */
        GuardianServer.activeConnections.entries.removeIf { (_, info) ->
            abs(now - info.lastUpdated) > STALE_AFTER_MS
        }

        /* --- rebuild the table --- */
        SwingUtilities.invokeLater {
            tableModel.setRowCount(0)
            for ((ip, info) in GuardianServer.activeConnections) {
                tableModel.addRow(
                    arrayOf(
                        ip,
                        "%.2f".format(info.bytesReceived / 1024.0),
                        "%.2f".format(info.bytesSent     / 1024.0),
                        Date(info.lastUpdated).toString(),
                        "Ban",
                        "Inspect"
                    )
                )
            }
        }
    }

    /* ---------- helpers ---------- */

    private class ButtonRenderer : JButton(), TableCellRenderer {
        override fun getTableCellRendererComponent(
            table: JTable, value: Any?, isSelected: Boolean,
            hasFocus: Boolean, row: Int, column: Int
        ): Component = apply { text = value?.toString() ?: "" }
    }

    private class ButtonEditor(cb: JCheckBox) : DefaultCellEditor(cb) {
        private val btn  = JButton()
        private var ip   = ""
        private var hit  = false

        init { btn.addActionListener { fireEditingStopped() } }

        override fun getTableCellEditorComponent(
            table: JTable, value: Any?, isSelected: Boolean, row: Int, column: Int
        ): Component {
            if (row in 0 until table.rowCount) ip = table.getValueAt(row, 0).toString()
            btn.text = value?.toString() ?: ""
            hit = true
            return btn
        }

        override fun getCellEditorValue(): Any {
            if (hit) when (btn.text) {
                "Ban"     -> GuardianServer.ban(ip).also {
                    JOptionPane.showMessageDialog(btn, "Banned $ip")
                }
                "Inspect" -> (SwingUtilities.getWindowAncestor(btn) as? GuardianDashboard)
                    ?.showInspector(ip)
            }
            hit = false
            return btn.text
        }

        override fun stopCellEditing(): Boolean { hit = false; return super.stopCellEditing() }
    }

    private fun showInspector(ip: String) {
        val info = GuardianServer.activeConnections[ip] ?: return

        val receivedArea = JTextArea().apply { isEditable = false; font = mono }
        val sentArea     = JTextArea().apply { isEditable = false; font = mono }

        val copyRecv = JButton("Copy All").apply {
            addActionListener {
                Toolkit.getDefaultToolkit().systemClipboard.setContents(
                    StringSelection(info.getRecentReceivedData().joinToString("\n")), null
                )
            }
        }
        val copySent = JButton("Copy All").apply {
            addActionListener {
                Toolkit.getDefaultToolkit().systemClipboard.setContents(
                    StringSelection(info.getRecentSentData().joinToString("\n")), null
                )
            }
        }

        val tabs = JTabbedPane().apply {
            add("Received", buildPanel(receivedArea, copyRecv))
            add("Sent",     buildPanel(sentArea,     copySent))
        }

        val dlg = JDialog(this, "Inspect $ip", true).apply {
            contentPane = tabs
            setSize(600, 400)
            setLocationRelativeTo(this@GuardianDashboard)
        }

        val updater = Timer(1_000) {
            receivedArea.text = info.getRecentReceivedData().joinToString("\n")
            sentArea.text     = info.getRecentSentData().joinToString("\n")
        }
        updater.start()
        dlg.addWindowListener(object : java.awt.event.WindowAdapter() {
            override fun windowClosing(e: java.awt.event.WindowEvent?) = updater.stop()
        })

        dlg.isVisible = true
    }

    /* ---------- small utility builders ---------- */

    private fun buildPanel(area: JTextArea, btn: JButton) =
        JPanel(BorderLayout()).apply {
            add(JScrollPane(area), BorderLayout.CENTER)
            add(btn,               BorderLayout.SOUTH)
        }

    private val mono = Font(Font.MONOSPACED, Font.PLAIN, 12)
}
