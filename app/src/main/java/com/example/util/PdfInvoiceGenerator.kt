package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.example.model.ProjectItem
import com.example.model.UserSettings
import java.io.File
import java.io.FileOutputStream
import java.util.Locale

object PdfInvoiceGenerator {

    data class InvoiceData(
        val invoiceNumber: String,
        val invoiceDate: String,
        val billingPeriod: String,
        val invoiceTitle: String,
        val clientName: String,
        val clientEmail: String = "",
        val clientAddress: String = "",
        val projects: List<ProjectItem>,
        val currencySymbol: String = "৳",
        val customNotes: String = ""
    )

    fun generateInvoicePdf(
        context: Context,
        settings: UserSettings,
        invoiceData: InvoiceData
    ): File {
        val pdfDoc = PdfDocument()

        val pageWidth = 595 // Standard A4 width in points
        val pageHeight = 842 // Standard A4 height in points

        // Paints
        val paintTitle = Paint().apply {
            color = Color.rgb(15, 23, 42) // Dark Slate
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintSubtitle = Paint().apply {
            color = Color.rgb(99, 102, 241) // Indigo
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintHeader = Paint().apply {
            color = Color.rgb(71, 85, 105)
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintBody = Paint().apply {
            color = Color.rgb(30, 41, 59)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintBodyBold = Paint().apply {
            color = Color.rgb(15, 23, 42)
            textSize = 9.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val paintMuted = Paint().apply {
            color = Color.rgb(100, 116, 139)
            textSize = 8.5f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

        val paintTotalBold = Paint().apply {
            color = Color.rgb(16, 185, 129) // Emerald
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            isAntiAlias = true
        }

        val linePaint = Paint().apply {
            color = Color.rgb(226, 232, 240)
            strokeWidth = 1f
            style = Paint.Style.STROKE
        }

        val bgPaint = Paint().apply {
            color = Color.rgb(248, 250, 252)
            style = Paint.Style.FILL
        }

        val primaryBgPaint = Paint().apply {
            color = Color.rgb(241, 245, 249)
            style = Paint.Style.FILL
        }

        val accentCardPaint = Paint().apply {
            color = Color.rgb(238, 242, 255)
            style = Paint.Style.FILL
        }

        // Pagination setup
        val itemsPerPage = 14
        val totalPages = Math.max(1, Math.ceil(invoiceData.projects.size.toDouble() / itemsPerPage).toInt())

        var projectIndex = 0

        for (pageNumber in 1..totalPages) {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdfDoc.startPage(pageInfo)
            val canvas: Canvas = page.canvas

            var y = 40f

            // Top Header Banner
            canvas.drawText(invoiceData.invoiceTitle.uppercase(Locale.US), 40f, y, paintTitle)
            canvas.drawText("WORKTRACK INVOICE", pageWidth - 180f, y - 5f, paintSubtitle)
            y += 20f

            canvas.drawText("Invoice #: ${invoiceData.invoiceNumber}", pageWidth - 180f, y, paintBodyBold)
            y += 14f
            canvas.drawText("Date: ${DateUtils.formatDisplayDate(invoiceData.invoiceDate)}", pageWidth - 180f, y, paintBody)
            y += 14f
            canvas.drawText("Period: ${invoiceData.billingPeriod}", pageWidth - 180f, y, paintBody)
            y -= 48f

            // From / To Information Boxes
            val boxY = y + 20f
            // From Box
            canvas.drawRoundRect(RectF(40f, boxY, 280f, boxY + 80f), 6f, 6f, primaryBgPaint)
            canvas.drawText("FROM:", 50f, boxY + 16f, paintHeader)
            canvas.drawText(settings.editorName, 50f, boxY + 30f, paintBodyBold)
            var fromLineY = boxY + 44f
            if (settings.editorEmail.isNotBlank()) {
                canvas.drawText(settings.editorEmail, 50f, fromLineY, paintMuted)
                fromLineY += 12f
            }
            if (settings.editorPhone.isNotBlank()) {
                canvas.drawText(settings.editorPhone, 50f, fromLineY, paintMuted)
                fromLineY += 12f
            }
            if (settings.editorAddress.isNotBlank()) {
                canvas.drawText(settings.editorAddress, 50f, fromLineY, paintMuted)
            }

            // Billed To Box
            canvas.drawRoundRect(RectF(300f, boxY, pageWidth - 40f, boxY + 80f), 6f, 6f, primaryBgPaint)
            canvas.drawText("BILLED TO:", 310f, boxY + 16f, paintHeader)
            canvas.drawText(invoiceData.clientName.ifBlank { settings.defaultClientName }, 310f, boxY + 30f, paintBodyBold)
            var toLineY = boxY + 44f
            if (invoiceData.clientEmail.isNotBlank() || settings.defaultClientEmail.isNotBlank()) {
                val email = invoiceData.clientEmail.ifBlank { settings.defaultClientEmail }
                canvas.drawText(email, 310f, toLineY, paintMuted)
                toLineY += 12f
            }
            if (invoiceData.clientAddress.isNotBlank() || settings.defaultClientAddress.isNotBlank()) {
                val addr = invoiceData.clientAddress.ifBlank { settings.defaultClientAddress }
                canvas.drawText(addr, 310f, toLineY, paintMuted)
            }

            y = boxY + 95f

            // Table Header
            val tableTop = y
            canvas.drawRect(40f, tableTop, pageWidth - 40f, tableTop + 24f, accentCardPaint)
            canvas.drawLine(40f, tableTop, pageWidth - 40f, tableTop, linePaint)
            canvas.drawLine(40f, tableTop + 24f, pageWidth - 40f, tableTop + 24f, linePaint)

            canvas.drawText("#", 50f, tableTop + 16f, paintHeader)
            canvas.drawText("DATE", 75f, tableTop + 16f, paintHeader)
            canvas.drawText("PROJECT / DESCRIPTION", 140f, tableTop + 16f, paintHeader)
            canvas.drawText("CATEGORY", 290f, tableTop + 16f, paintHeader)
            canvas.drawText("MINUTES", 370f, tableTop + 16f, paintHeader)
            canvas.drawText("RATE", 435f, tableTop + 16f, paintHeader)
            canvas.drawText("AMOUNT", pageWidth - 100f, tableTop + 16f, paintHeader)

            y = tableTop + 24f

            // Table Rows
            val endIdx = Math.min(projectIndex + itemsPerPage, invoiceData.projects.size)
            for (i in projectIndex until endIdx) {
                val p = invoiceData.projects[i]
                val rowHeight = 22f

                if ((i % 2) == 1) {
                    canvas.drawRect(40f, y, pageWidth - 40f, y + rowHeight, bgPaint)
                }

                canvas.drawText("${i + 1}", 50f, y + 15f, paintMuted)
                canvas.drawText(DateUtils.formatDisplayDate(p.date), 75f, y + 15f, paintBody)
                val safeName = if (p.name.length > 24) p.name.take(22) + ".." else p.name
                canvas.drawText(safeName, 140f, y + 15f, paintBodyBold)
                canvas.drawText(p.category, 290f, y + 15f, paintBody)
                canvas.drawText("${p.minutes}m", 370f, y + 15f, paintBody)
                canvas.drawText("${invoiceData.currencySymbol}${p.ratePerMinute}/m", 435f, y + 15f, paintBody)
                canvas.drawText("${invoiceData.currencySymbol}${String.format(Locale.US, "%,.2f", p.payment)}", pageWidth - 100f, y + 15f, paintBodyBold)

                canvas.drawLine(40f, y + rowHeight, pageWidth - 40f, y + rowHeight, linePaint)
                y += rowHeight
            }
            projectIndex = endIdx

            // If last page, draw Summary, Payment Info & Notes
            if (pageNumber == totalPages) {
                val totalMins = invoiceData.projects.sumOf { it.minutes }
                val totalPayment = invoiceData.projects.sumOf { it.payment }
                val totalHours = invoiceData.projects.sumOf { it.workingHours }

                y += 15f

                // Summary Card on right
                val summaryLeft = pageWidth - 230f
                canvas.drawRoundRect(RectF(summaryLeft, y, pageWidth - 40f, y + 75f), 6f, 6f, accentCardPaint)
                canvas.drawText("Total Projects:", summaryLeft + 15f, y + 20f, paintBody)
                canvas.drawText("${invoiceData.projects.size}", pageWidth - 55f, y + 20f, paintBodyBold)

                canvas.drawText("Total Video Minutes:", summaryLeft + 15f, y + 36f, paintBody)
                canvas.drawText("${totalMins}m", pageWidth - 55f, y + 36f, paintBodyBold)

                if (totalHours > 0) {
                    canvas.drawText("Total Work Hours:", summaryLeft + 15f, y + 50f, paintMuted)
                    canvas.drawText(DateUtils.formatHours(totalHours), pageWidth - 55f, y + 50f, paintMuted)
                }

                canvas.drawLine(summaryLeft + 10f, y + 54f, pageWidth - 50f, y + 54f, linePaint)
                canvas.drawText("GRAND TOTAL:", summaryLeft + 15f, y + 68f, paintBodyBold)
                canvas.drawText("${invoiceData.currencySymbol}${String.format(Locale.US, "%,.2f", totalPayment)}", pageWidth - 120f, y + 68f, paintTotalBold)

                // Payment Info & Notes on left
                val notesLeft = 40f
                val notesWidth = summaryLeft - 60f
                val infoY = y
                canvas.drawRoundRect(RectF(notesLeft, infoY, notesLeft + notesWidth, infoY + 75f), 6f, 6f, primaryBgPaint)
                canvas.drawText("PAYMENT INFORMATION & NOTES:", notesLeft + 12f, infoY + 16f, paintHeader)

                val payInfoLines = settings.paymentInfo.lines()
                var pLineY = infoY + 30f
                for (line in payInfoLines.take(3)) {
                    val safeLine = if (line.length > 40) line.take(38) + ".." else line
                    canvas.drawText(safeLine, notesLeft + 12f, pLineY, paintMuted)
                    pLineY += 12f
                }

                val notesText = invoiceData.customNotes.ifBlank { settings.invoiceNotes }
                if (notesText.isNotBlank()) {
                    val safeNote = if (notesText.length > 45) notesText.take(43) + ".." else notesText
                    canvas.drawText(safeNote, notesLeft + 12f, pLineY + 2f, paintMuted)
                }
            }

            // Footer
            canvas.drawText("Thank you for your business!", 40f, pageHeight - 35f, paintMuted)
            canvas.drawText("Page $pageNumber of $totalPages  •  Generated with WorkTrack", pageWidth - 220f, pageHeight - 35f, paintMuted)

            pdfDoc.finishPage(page)
        }

        // Save PDF to cache directory
        val cleanNumber = invoiceData.invoiceNumber.replace("[^a-zA-Z0-9-]".toRegex(), "_")
        val file = File(context.cacheDir, "Invoice_${cleanNumber}.pdf")
        val fos = FileOutputStream(file)
        pdfDoc.writeTo(fos)
        fos.close()
        pdfDoc.close()

        return file
    }

    fun shareOrPrintPdf(context: Context, pdfFile: File, title: String = "Share Invoice") {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, pdfFile.nameWithoutExtension)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        context.startActivity(Intent.createChooser(intent, title))
    }

    fun openPdfViewer(context: Context, pdfFile: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            pdfFile
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            context.startActivity(intent)
        } catch (_: Exception) {
            // Fallback to share
            shareOrPrintPdf(context, pdfFile, "Open Invoice")
        }
    }
}
