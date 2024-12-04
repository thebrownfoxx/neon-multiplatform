package com.thebrownfoxx.neon.client.repository.offlinefirst

import com.thebrownfoxx.neon.client.converter.toLocalMember
import com.thebrownfoxx.neon.client.model.LocalMember
import com.thebrownfoxx.neon.client.repository.MemberRepository
import com.thebrownfoxx.neon.client.repository.local.LocalMemberDataSource
import com.thebrownfoxx.neon.client.repository.remote.GetMemberError
import com.thebrownfoxx.neon.client.repository.remote.RemoteMemberDataSource
import com.thebrownfoxx.neon.common.data.GetError
import com.thebrownfoxx.neon.common.outcome.Failure
import com.thebrownfoxx.neon.common.outcome.Outcome
import com.thebrownfoxx.neon.common.outcome.Success
import com.thebrownfoxx.neon.common.outcome.getOrElse
import com.thebrownfoxx.neon.common.type.id.MemberId
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
            localDataSource.getAsFlow(id).collect { localMemberOutcome ->
                val localMember = localMemberOutcome.getOrElse { error ->
                    when (error) {
                        GetError.NotFound -> {}
                        GetError.ConnectionError ->
                            sharedFlow.emit(Failure(GetError.ConnectionError))
                    }
                    return@collect
                }
                sharedFlow.emit(Success(localMember))
            }
        }

        coroutineScope.launch {
            remoteDataSource.getAsFlow(id).collect { remoteMemberOutcome ->
                val remoteMember = remoteMemberOutcome.getOrElse { error ->
                    val mappedError = when (error) {
                        GetMemberError.NotFound -> GetError.NotFound
                        GetMemberError.ServerError -> GetError.ConnectionError // TODO: Fix errors T-T
                    }
                    sharedFlow.emit(Failure(mappedError))
                    return@collect
                }
                localDataSource.upsert(remoteMember.toLocalMember())
            }
        }

        return sharedFlow.asSharedFlow()
    }
}