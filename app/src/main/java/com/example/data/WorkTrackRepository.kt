package com.example.data

import com.example.model.CategoryItem
import com.example.model.ClientItem
import com.example.model.InvoiceItem
import com.example.model.ProjectItem
import com.example.model.UserSettings
import kotlinx.coroutines.flow.Flow

class WorkTrackRepository(
    private val projectDao: ProjectDao,
    private val categoryDao: CategoryDao,
    private val clientDao: ClientDao,
    private val invoiceDao: InvoiceDao,
    private val settingsDao: SettingsDao
) {
    val allProjects: Flow<List<ProjectItem>> = projectDao.getAllProjects()
    val allCategories: Flow<List<CategoryItem>> = categoryDao.getAllCategories()
    val allClients: Flow<List<ClientItem>> = clientDao.getAllClients()
    val allInvoices: Flow<List<InvoiceItem>> = invoiceDao.getAllInvoices()
    val userSettings: Flow<UserSettings?> = settingsDao.getSettings()

    fun getProjectsByMonth(yearMonthPrefix: String): Flow<List<ProjectItem>> {
        return projectDao.getProjectsByMonth(yearMonthPrefix)
    }

    fun getProjectById(id: Long): Flow<ProjectItem?> {
        return projectDao.getProjectById(id)
    }

    suspend fun insertProject(project: ProjectItem): Long {
        return projectDao.insert(project)
    }

    suspend fun insertProjects(projects: List<ProjectItem>): List<Long> {
        return projectDao.insertAll(projects)
    }

    suspend fun updateProject(project: ProjectItem) {
        projectDao.update(project)
    }

    suspend fun deleteProject(project: ProjectItem) {
        projectDao.delete(project)
    }

    suspend fun deleteProjectById(id: Long) {
        projectDao.deleteById(id)
    }

    suspend fun deleteAllProjects() {
        projectDao.deleteAll()
    }

    // Categories
    suspend fun insertCategory(category: CategoryItem): Long {
        return categoryDao.insert(category)
    }

    suspend fun deleteCategory(category: CategoryItem) {
        categoryDao.delete(category)
    }

    suspend fun renameCategory(oldCategory: CategoryItem, newName: String) {
        val trimmed = newName.trim()
        if (trimmed.isNotBlank() && trimmed != oldCategory.name) {
            categoryDao.update(oldCategory.copy(name = trimmed))
            projectDao.updateCategoryName(oldCategory.name, trimmed)
        }
    }

    // Clients
    suspend fun insertClient(client: ClientItem): Long {
        return clientDao.insert(client)
    }

    suspend fun updateClient(client: ClientItem) {
        clientDao.update(client)
    }

    suspend fun deleteClient(client: ClientItem) {
        clientDao.delete(client)
    }

    suspend fun deleteClientById(id: Long) {
        clientDao.deleteById(id)
    }

    // Invoices
    suspend fun insertInvoice(invoice: InvoiceItem): Long {
        return invoiceDao.insert(invoice)
    }

    suspend fun updateInvoice(invoice: InvoiceItem) {
        invoiceDao.update(invoice)
    }

    suspend fun updateInvoicePaymentStatus(id: Long, newStatus: String) {
        invoiceDao.updatePaymentStatus(id, newStatus)
    }

    suspend fun deleteInvoice(invoice: InvoiceItem) {
        invoiceDao.delete(invoice)
    }

    suspend fun deleteInvoiceById(id: Long) {
        invoiceDao.deleteById(id)
    }

    // Settings
    suspend fun updateSettings(settings: UserSettings) {
        settingsDao.insertOrUpdate(settings)
    }

    suspend fun clearAllData() {
        projectDao.deleteAll()
        categoryDao.getAllCategories()
        clientDao.deleteAll()
        invoiceDao.deleteAll()
    }

    suspend fun seedSampleProjects() {
        val sampleClients = listOf(
            ClientItem(name = "Client A", companyOrChannel = "@ClientAVideos", notes = "Regular client - tech & documentary"),
            ClientItem(name = "Client B", companyOrChannel = "@SportsReels", notes = "Football reels and highlights")
        )
        for (c in sampleClients) {
            clientDao.insert(c)
        }

        val sampleProjects = listOf(
            ProjectItem(
                date = "2026-09-30",
                name = "Grock 5",
                category = "Documentary",
                minutes = 5.0,
                itemsCount = 1,
                ratePerMinute = 60.0,
                payment = 300.0,
                startTime = "09:00",
                endTime = "10:30",
                workingHours = 1.5,
                clientName = "Client A",
                channel = "@ClientAVideos",
                notes = "Fast-paced editing + sound design",
                status = ProjectItem.STATUS_COMPLETED,
                paymentStatus = ProjectItem.PAYMENT_PAID
            ),
            ProjectItem(
                date = "2026-09-30",
                name = "Football 5",
                category = "Football",
                minutes = 5.0,
                itemsCount = 1,
                ratePerMinute = 60.0,
                payment = 300.0,
                startTime = "11:00",
                endTime = "12:00",
                workingHours = 1.0,
                clientName = "Client B",
                channel = "@SportsReels",
                notes = "4K 60fps reel with speed ramps",
                status = ProjectItem.STATUS_COMPLETED,
                paymentStatus = ProjectItem.PAYMENT_UNPAID
            ),
            ProjectItem(
                date = "2026-09-28",
                name = "Casino 10",
                category = "Casino",
                minutes = 10.0,
                itemsCount = 1,
                ratePerMinute = 50.0,
                payment = 500.0,
                startTime = "14:00",
                endTime = "16:30",
                workingHours = 2.5,
                clientName = "Client A",
                channel = "@ClientAVideos",
                notes = "Engaging sound FX and motion graphics",
                status = ProjectItem.STATUS_COMPLETED,
                paymentStatus = ProjectItem.PAYMENT_PARTIALLY_PAID
            ),
            ProjectItem(
                date = "2026-09-25",
                name = "Shorts 2",
                category = "Shorts",
                minutes = 2.5,
                itemsCount = 1,
                ratePerMinute = 60.0,
                payment = 150.0,
                startTime = "17:00",
                endTime = "18:00",
                workingHours = 1.0,
                clientName = "Client B",
                channel = "@SportsReels",
                notes = "Vertical 9:16 cut with captions",
                status = ProjectItem.STATUS_COMPLETED,
                paymentStatus = ProjectItem.PAYMENT_PAID
            )
        )
        projectDao.insertAll(sampleProjects)
    }

    suspend fun ensureInitialData() {
        // App starts with no predefined categories - user defines their own.
        // We only ensure base settings record exists.
        settingsDao.insertOrUpdate(UserSettings())
    }
}
