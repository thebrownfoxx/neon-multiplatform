package com.thebrownfoxx.neon.client.repository.offlinefirst

import com.thebrownfoxx.neon.client.repository.GroupMemberRepository
import com.thebrownfoxx.neon.client.repository.local.LocalGroupMemberDataSource
import com.thebrownfoxx.neon.client.repository.local.LocalGroupMemberDataSource.LocalGroupMember
import com.thebrownfoxx.neon.client.repository.remote.RemoteGroupMemberDataSource
import com.thebrownfoxx.neon.common.data.DataOperationError
import com.thebrownfoxx.neon.common.type.id.GroupId
import com.thebrownfoxx.neon.common.type.id.MemberId
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

class OfflineFirstGroupMemberRepository(
    private val localDataSource: LocalGroupMemberDataSource,
    private val remoteDataSource: RemoteGroupMemberDataSource,
) : GroupMemberRepository {
    private val coroutineScope = CoroutineScope(Dispatchers.IO) + SupervisorJob()

    override fun getMembersAsFlow(
        groupId: GroupId,
    ): Flow<Outcome<Set<MemberId>, DataOperationError>> {
        val sharedFlow = MutableSharedFlow<Outcome<Set<MemberId>, DataOperationError>>(replay = 1)

        coroutineScope.launch {
            localDataSource.getMembersAsFlow(groupId).collect { localMembersOutcome ->
                val localMembers = localMembersOutcome.getOrElse { error ->
                    sharedFlow.emit(Failure(error))
                    return@collect
                }
                sharedFlow.emit(Success(localMembers))
            }
        }

        coroutineScope.launch {
            remoteDataSource.getMembersAsFlow(groupId).collect { remoteMembersOutcome ->
                val remoteMembers = remoteMembersOutcome.getOrElse {
                    sharedFlow.emit(Failure(DataOperationError.UnexpectedError))
                    return@collect
                }.map {
                    LocalGroupMember(
                        groupId = groupId,
                        memberId = it,
                        isAdmin = false,
                    )
                }
                localDataSource.batchUpsert(remoteMembers)
            }
        }

        return sharedFlow.asSharedFlow()
    }
}