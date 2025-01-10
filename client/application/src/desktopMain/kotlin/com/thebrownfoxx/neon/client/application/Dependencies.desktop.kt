package com.thebrownfoxx.neon.client.application

import androidx.lifecycle.viewmodel.CreationExtras
import com.thebrownfoxx.neon.client.application.environment.BuildKonfigEnvironment
import com.thebrownfoxx.neon.client.application.environment.ClientEnvironmentKey.LocalPath
import com.thebrownfoxx.neon.client.application.http.HttpClient
import com.thebrownfoxx.neon.client.service.Dependencies
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import org.jetbrains.exposed.sql.Database
import java.io.File

object DependencyProvider {
    private val serviceScope = CoroutineScope(SupervisorJob())

    val dependencies = run {
        val environment = BuildKonfigEnvironment()
        val localAppData = System.getenv("LOCALAPPDATA")
        val directory = File("$localAppData/${environment[LocalPath]}").apply { mkdirs() }
        val database = Database.connect(
            url = "jdbc:sqlite:/${directory.path}/neon.db",
            driver = "org.sqlite.JDBC",
        )
        AppDependencies(HttpClient(environment), database, serviceScope)
//        DummyDependencies
    }
}

actual val CreationExtras.dependencies: Dependencies
    get() = DependencyProvider.dependencies