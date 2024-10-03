package com.thebrownfoxx.neon.client.repository.model

import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.Result

typealias GetEntityResult<T> = Result<T, GetEntityError>

sealed interface GetEntityError {
    data class Unauthorized(val loggedInMemberId: MemberId?) : GetEntityError
    data object NotFound : GetEntityError
    data object ConnectionError : GetEntityError
}