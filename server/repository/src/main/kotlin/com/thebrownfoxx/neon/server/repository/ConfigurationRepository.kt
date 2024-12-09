package com.thebrownfoxx.neon.server.repository

import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.data.transaction.ReversibleUnitOutcome
import com.thebrownfoxx.outcome.Outcome

interface ConfigurationRepository {
    suspend fun getInitialized(): Outcome<Boolean, DataOperationError>
    suspend fun setInitialized(initialized: Boolean): ReversibleUnitOutcome<DataOperationError>
}