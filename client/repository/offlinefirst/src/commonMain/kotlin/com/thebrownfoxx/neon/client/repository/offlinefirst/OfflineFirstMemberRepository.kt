package com.thebrownfoxx.neon.client.repository.offlinefirst

import com.thebrownfoxx.neon.client.converter.toLocalMember
import com.thebrownfoxx.neon.client.model.LocalMember
import com.thebrownfoxx.neon.client.repository.MemberRepository
import com.thebrownfoxx.neon.client.repository.local.LocalMemberDataSource
import com.thebrownfoxx.neon.client.repository.remote.RemoteMemberDataSource
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.type.id.MemberId
import com.thebrownfoxx.outcome.Outcome
import com.thebrownfoxx.outcome.Success
import com.thebrownfoxx.outcome.getOrElse
import com.thebrownfoxx.outcome.mapError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class OfflineFirstMemberRepository(
    private val localDataSource: LocalMemberDataSource,
    private val remoteDataSource: RemoteMemberDataSource,
) : MemberRepository {
    private val coroutineScope = CoroutineScope(Dispatchers.Default) + SupervisorJob()

    override fun get(id: MemberId): Flow<Outcome<LocalMember, GetError>> {
        val sharedFlow = MutableSharedFlow<Outcome<LocalMember, GetError>>(replay = 1)

        coroutineScope.launch {
            localDataSource.getAsFlow(id).collect { localGroupOutcome ->
                val localMember = localGroupOutcome.getOrElse {
                    when (error) {
                        GetError.NotFound -> {}
                        GetError.ConnectionError, GetError.UnexpectedError ->
                            sharedFlow.emit(mapError(error))
                    }
                    return@collect
                }
                sharedFlow.emit(Success(localMember))
            }
        }

        coroutineScope.launch {
            remoteDataSource.getAsFlow(id).collect { remoteGroupOutcome ->
                val remoteGroup = remoteGroupOutcome.getOrElse {
                    sharedFlow.emit(mapError(error))
                    return@collect
                }
                localDataSource.upsert(remoteGroup.toLocalMember())
            }
        }

        return sharedFlow.asSharedFlow()
    }
}