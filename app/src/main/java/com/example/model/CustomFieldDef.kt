package com.example.model

data class CustomFieldDef(
    val id: String,
    val label: String,
    val isEnabled: Boolean = true,
    val showInTable: Boolean = true,
    val showInInvoice: Boolean = false
) {
    companion object {
        fun defaultList(): List<CustomFieldDef> = listOf(
            CustomFieldDef("youtube_channel", "YouTube Channel", isEnabled = true, showInTable = true, showInInvoice = true),
            CustomFieldDef("video_type", "Video Type", isEnabled = true, showInTable = true, showInInvoice = false),
            CustomFieldDef("priority", "Priority", isEnabled = true, showInTable = false, showInInvoice = false),
            CustomFieldDef("revision_count", "Revision Count", isEnabled = false, showInTable = false, showInInvoice = false),
            CustomFieldDef("editor", "Editor Name", isEnabled = false, showInTable = false, showInInvoice = false),
            CustomFieldDef("deadline", "Deadline", isEnabled = false, showInTable = false, showInInvoice = false),
            CustomFieldDef("custom_notes", "Custom Notes", isEnabled = false, showInTable = false, showInInvoice = false)
        )
    }
}
