package com.thebrownfoxx.neon.client.repository.offlinefirst

import com.thebrownfoxx.neon.client.converter.toLocalGroup
import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.repository.GroupRepository
import com.thebrownfoxx.neon.client.repository.local.LocalGroupDataSource
import com.thebrownfoxx.neon.client.repository.remote.RemoteGroupDataSource
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.map.getOrElse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class OfflineFirstGroupRepository(
    private val localDataSource: LocalGroupDataSource,
    private val remoteDataSource: RemoteGroupDataSource,
) : GroupRepository {
    private val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    override fun getAsFlow(id: GroupId): Flow<Outcome<LocalGroup, GetError>> {
        val sharedFlow = MutableSharedFlow<Outcome<LocalGroup, GetError>>(replay = 1)

        // TODO: Fix this. RemoteDataSource must continuously retry.
        //  And this is also a lot of repeated code.
        coroutineScope.launch {
            localDataSource.getAsFlow(id).collect { localGroupOutcome ->
                val localGroup = localGroupOutcome.getOrElse { error ->
                    when (error) {
                        GetError.NotFound -> {}
                        GetError.ConnectionError, GetError.UnexpectedError ->
                            sharedFlow.emit(Failure(error))
                    }
                    return@collect
                }
                sharedFlow.emit(Success(localGroup))
            }
        }

        coroutineScope.launch {
            remoteDataSource.getAsFlow(id).collect { remoteGroupOutcome ->
                val remoteGroup = remoteGroupOutcome.getOrElse { error ->
                    sharedFlow.emit(Failure(error))
                    return@collect
                }
                localDataSource.upsert(remoteGroup.toLocalGroup())
            }
        }

        return sharedFlow.asSharedFlow()
    }
}