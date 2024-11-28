package com.thebrownfoxx.neon.server.repository

import com.thebrownfoxx.neon.common.data.ConnectionError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.neon.common.type.Outcome

interface ConfigurationRepository {
    suspend fun getInitialized(): Outcome<Boolean, ConnectionError>
    suspend fun setInitialized(initialized: Boolean): ReversibleUnitOutcome<ConnectionError>
}