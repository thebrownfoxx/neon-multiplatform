package com.thebrownfoxx.neon.client.application

import androidx.lifecycle.viewmodel.CreationExtras
import com.thebrownfoxx.neon.client.application.dummy.DummyDependencies
import com.thebrownfoxx.neon.client.service.Dependencies

object DependencyProvider {
    val dependencies = run {
//        val localAppData = System.getenv("LOCALAPPDATA")
//        val directory = File("$localAppData/Foxx/Neon").apply { mkdirs() }
//        val database = Database.connect(
//            url = "jdbc:sqlite:/${directory.path}/neon.db",
//            driver = "org.sqlite.JDBC",
//        )
//        DefaultDependencies(HttpClient(), database)

        DummyDependencies()
    }
}

actual val CreationExtras.dependencies: Dependencies
    get() = DependencyProvider.dependencies