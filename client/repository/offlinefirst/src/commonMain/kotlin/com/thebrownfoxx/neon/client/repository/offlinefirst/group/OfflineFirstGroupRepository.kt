package com.thebrownfoxx.neon.client.repository.offlinefirst.group

import com.thebrownfoxx.neon.client.converter.toLocalGroup
import com.thebrownfoxx.neon.client.model.LocalGroup
import com.thebrownfoxx.neon.client.repository.group.GroupRepository
import com.thebrownfoxx.neon.client.repository.group.model.RepositoryGetGroupError
import com.thebrownfoxx.neon.client.repository.local.group.LocalGroupDataSource
import com.thebrownfoxx.neon.client.repository.local.group.model.LocalGetGroupError
import com.thebrownfoxx.neon.client.repository.remote.group.RemoteGroupDataSource
import com.thebrownfoxx.neon.client.repository.remote.group.model.RemoteGetGroupError
import com.thebrownfoxx.neon.common.type.Failure
import com.thebrownfoxx.neon.common.type.Outcome
import com.thebrownfoxx.neon.common.type.Success
import com.thebrownfoxx.neon.common.type.getOrElse
import com.thebrownfoxx.neon.common.type.id.GroupId
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
    private val coroutineScope = CoroutineScope(Dispatchers.Default) + SupervisorJob()

    override fun get(id: GroupId): Flow<Outcome<LocalGroup, RepositoryGetGroupError>> {
        val sharedFlow = MutableSharedFlow<Outcome<LocalGroup, RepositoryGetGroupError>>()

        coroutineScope.launch {
            localDataSource.get(id).collect { localGroupOutcome ->
                val localGroup = localGroupOutcome.getOrElse { error ->
                    when (error) {
                        LocalGetGroupError.NotFound -> {}
                        LocalGetGroupError.ConnectionError ->
                            sharedFlow.emit(Failure(RepositoryGetGroupError.ConnectionError))
                    }
                    return@collect
                }
                sharedFlow.emit(Success(localGroup))
            }
        }

        coroutineScope.launch {
            remoteDataSource.get(id).collect { remoteGroupOutcome ->
                val remoteGroup = remoteGroupOutcome.getOrElse { error ->
                    when (error) {
                        RemoteGetGroupError.NotFound ->
                            sharedFlow.emit(Failure(RepositoryGetGroupError.NotFound))

                        RemoteGetGroupError.ConnectionError ->
                            sharedFlow.emit(Failure(RepositoryGetGroupError.ConnectionError))
                    }
                    return@collect
                }

                localDataSource.upsert(remoteGroup.toLocalGroup())
            }
        }

        return sharedFlow.asSharedFlow()
    }
}