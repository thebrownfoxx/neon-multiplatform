package com.thebrownfoxx.neon.client.repository.model

import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.UnitResult

typealias DeleteEntityResult = UnitResult<DeleteEntityError>

sealed interface DeleteEntityError {
    data class Unauthorized(val loggedInMemberId: MemberId?) : DeleteEntityError
    data object NotFound : DeleteEntityError
    data object ConnectionError : DeleteEntityError
}