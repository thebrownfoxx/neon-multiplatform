package com.thebrownfoxx.neon.client.application

import android.app.Application
import com.thebrownfoxx.neon.client.application.http.HttpClient
import com.thebrownfoxx.neon.client.service.Dependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.exposed.sql.Database

class NeonApplication : Application() {
    private val serviceScope = CoroutineScope(SupervisorJob())

    private lateinit var _dependencies: Dependencies
    val dependencies get() = _dependencies

    override fun onCreate() {
        super.onCreate()
        _dependencies = createDependencies()
    }

    private fun createDependencies(): Dependencies {
        val database = Database.connect(
            url = "jdbc:sqlite:/${filesDir.path}/neon.db",
            driver = "org.sqlite.JDBC",
        )
        return AppDependencies(HttpClient(), database, serviceScope)
    }
}