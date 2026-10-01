package com.example.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.model.CategoryItem
import com.example.model.ProjectItem
import com.example.model.UserSettings
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object CsvBackupHelper {

    fun exportProjectsToCsv(projects: List<ProjectItem>): String {
        val sb = StringBuilder()
        // Header
        sb.append("ID,Date,Project Name,Category,Minutes,Items Count,Rate Per Min,Payment,Start Time,End Time,Working Hours,Client Name,Status,Notes\n")
        for (p in projects) {
            sb.append(escapeCsv(p.id.toString())).append(",")
            sb.append(escapeCsv(p.date)).append(",")
            sb.append(escapeCsv(p.name)).append(",")
            sb.append(escapeCsv(p.category)).append(",")
            sb.append(p.minutes).append(",")
            sb.append(p.itemsCount).append(",")
            sb.append(p.ratePerMinute).append(",")
            sb.append(p.payment).append(",")
            sb.append(escapeCsv(p.startTime)).append(",")
            sb.append(escapeCsv(p.endTime)).append(",")
            sb.append(p.workingHours).append(",")
            sb.append(escapeCsv(p.clientName)).append(",")
            sb.append(escapeCsv(p.status)).append(",")
            sb.append(escapeCsv(p.notes))
            sb.append("\n")
        }
        return sb.toString()
    }

    private fun escapeCsv(value: String): String {
        var str = value.replace("\"", "\"\"")
        if (str.contains(",") || str.contains("\n") || str.contains("\"")) {
            str = "\"$str\""
        }
        return str
    }

    fun parseProjectsFromCsv(csvText: String, defaultRate: Double): List<ProjectItem> {
        val lines = csvText.lines().filter { it.isNotBlank() }
        if (lines.size <= 1) return emptyList()

        val results = mutableListOf<ProjectItem>()
        // Skip header line
        for (i in 1 until lines.size) {
            val line = lines[i]
            val cols = parseCsvLine(line)
            if (cols.isEmpty()) continue

            // Expecting: ID(0), Date(1), Name(2), Category(3), Minutes(4), Items(5), Rate(6), Payment(7), Start(8), End(9), Hours(10), Client(11), Status(12), Notes(13)
            // Or simpler user pasted CSV format: Date, Project, Category, Minutes
            if (cols.size >= 4) {
                var date = cols.getOrNull(1)?.trim() ?: DateUtils.todayIso()
                var name = cols.getOrNull(2)?.trim() ?: "Project"
                var category = cols.getOrNull(3)?.trim() ?: "Other"
                var minutes = cols.getOrNull(4)?.toDoubleOrNull()

                // Check if it's the simpler format: Date | Project | Category | Minutes
                if (minutes == null && cols[0].contains("-") || cols[0].contains("/")) {
                    date = cols[0].trim()
                    name = cols.getOrElse(1) { "Project" }.trim()
                    category = cols.getOrElse(2) { "Other" }.trim()
                    minutes = cols.getOrNull(3)?.toDoubleOrNull() ?: 0.0
                }

                val mins = minutes ?: 0.0
                val rate = cols.getOrNull(6)?.toDoubleOrNull() ?: defaultRate
                val payment = cols.getOrNull(7)?.toDoubleOrNull() ?: (mins * rate)
                val start = cols.getOrNull(8)?.trim() ?: ""
                val end = cols.getOrNull(9)?.trim() ?: ""
                val hours = cols.getOrNull(10)?.toDoubleOrNull() ?: DateUtils.calculateWorkingHours(start, end)
                val client = cols.getOrNull(11)?.trim() ?: ""
                val status = cols.getOrNull(12)?.trim() ?: ProjectItem.STATUS_COMPLETED
                val notes = cols.getOrNull(13)?.trim() ?: ""

                results.add(
                    ProjectItem(
                        date = date,
                        name = name,
                        category = category,
                        minutes = mins,
                        itemsCount = 1,
                        ratePerMinute = rate,
                        payment = payment,
                        startTime = start,
                        endTime = end,
                        workingHours = hours,
                        clientName = client,
                        notes = notes,
                        status = if (status.isNotBlank()) status else ProjectItem.STATUS_COMPLETED
                    )
                )
            }
        }
        return results
    }

    private fun parseCsvLine(line: String): List<String> {
        val result = mutableListOf<String>()
        val sb = java.lang.StringBuilder()
        var inQuotes = false
        var i = 0
        while (i < line.length) {
            val c = line[i]
            if (c == '\"') {
                if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
                    sb.append('\"')
                    i++
                } else {
                    inQuotes = !inQuotes
                }
            } else if (c == ',' && !inQuotes) {
                result.add(sb.toString().trim())
                sb.clear()
            } else {
                sb.append(c)
            }
            i++
        }
        result.add(sb.toString().trim())
        return result
    }

    fun exportBackupJson(
        projects: List<ProjectItem>,
        categories: List<CategoryItem>,
        settings: UserSettings?
    ): String {
        val root = JSONObject()

        val projArray = JSONArray()
        for (p in projects) {
            val pObj = JSONObject()
            pObj.put("id", p.id)
            pObj.put("date", p.date)
            pObj.put("name", p.name)
            pObj.put("category", p.category)
            pObj.put("minutes", p.minutes)
            pObj.put("itemsCount", p.itemsCount)
            pObj.put("ratePerMinute", p.ratePerMinute)
            pObj.put("payment", p.payment)
            pObj.put("isPaymentOverridden", p.isPaymentOverridden)
            pObj.put("startTime", p.startTime)
            pObj.put("endTime", p.endTime)
            pObj.put("workingHours", p.workingHours)
            pObj.put("clientName", p.clientName)
            pObj.put("notes", p.notes)
            pObj.put("status", p.status)
            pObj.put("customFieldsJson", p.customFieldsJson)
            projArray.put(pObj)
        }
        root.put("projects", projArray)

        val catArray = JSONArray()
        for (c in categories) {
            val cObj = JSONObject()
            cObj.put("name", c.name)
            cObj.put("colorHex", c.colorHex)
            catArray.put(cObj)
        }
        root.put("categories", catArray)

        if (settings != null) {
            val sObj = JSONObject()
            sObj.put("editorName", settings.editorName)
            sObj.put("editorEmail", settings.editorEmail)
            sObj.put("editorPhone", settings.editorPhone)
            sObj.put("editorAddress", settings.editorAddress)
            sObj.put("defaultRatePerMinute", settings.defaultRatePerMinute)
            sObj.put("currencySymbol", settings.currencySymbol)
            sObj.put("currencyCode", settings.currencyCode)
            sObj.put("paymentInfo", settings.paymentInfo)
            sObj.put("invoiceNotes", settings.invoiceNotes)
            root.put("settings", sObj)
        }

        return root.toString(2)
    }

    fun parseBackupJson(jsonString: String): Triple<List<ProjectItem>, List<CategoryItem>, UserSettings?> {
        val root = JSONObject(jsonString)
        val projects = mutableListOf<ProjectItem>()
        if (root.has("projects")) {
            val arr = root.getJSONArray("projects")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                projects.add(
                    ProjectItem(
                        date = o.optString("date", DateUtils.todayIso()),
                        name = o.optString("name", "Project"),
                        category = o.optString("category", "Other"),
                        minutes = o.optDouble("minutes", 0.0),
                        itemsCount = o.optInt("itemsCount", 1),
                        ratePerMinute = o.optDouble("ratePerMinute", 60.0),
                        payment = o.optDouble("payment", 0.0),
                        isPaymentOverridden = o.optBoolean("isPaymentOverridden", false),
                        startTime = o.optString("startTime", ""),
                        endTime = o.optString("endTime", ""),
                        workingHours = o.optDouble("workingHours", 0.0),
                        clientName = o.optString("clientName", ""),
                        notes = o.optString("notes", ""),
                        status = o.optString("status", ProjectItem.STATUS_COMPLETED),
                        customFieldsJson = o.optString("customFieldsJson", "{}")
                    )
                )
            }
        }

        val categories = mutableListOf<CategoryItem>()
        if (root.has("categories")) {
            val arr = root.getJSONArray("categories")
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                categories.add(
                    CategoryItem(
                        name = o.optString("name", "Category"),
                        colorHex = o.optString("colorHex", "#6366F1")
                    )
                )
            }
        }

        var settings: UserSettings? = null
        if (root.has("settings")) {
            val o = root.getJSONObject("settings")
            settings = UserSettings(
                editorName = o.optString("editorName", "Freelance Video Editor"),
                editorEmail = o.optString("editorEmail", ""),
                editorPhone = o.optString("editorPhone", ""),
                editorAddress = o.optString("editorAddress", ""),
                defaultRatePerMinute = o.optDouble("defaultRatePerMinute", 60.0),
                currencySymbol = o.optString("currencySymbol", "৳"),
                currencyCode = o.optString("currencyCode", "BDT"),
                paymentInfo = o.optString("paymentInfo", ""),
                invoiceNotes = o.optString("invoiceNotes", "")
            )
        }

        return Triple(projects, categories, settings)
    }

    fun shareTextFile(context: Context, fileName: String, content: String, mimeType: String, chooserTitle: String) {
        val file = File(context.cacheDir, fileName)
        file.writeText(content)

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }
}
