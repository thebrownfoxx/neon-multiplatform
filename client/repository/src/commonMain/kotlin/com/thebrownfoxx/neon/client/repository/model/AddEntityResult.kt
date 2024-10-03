package com.thebrownfoxx.neon.client.repository.model

import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.UnitResult

typealias AddEntityResult = UnitResult<AddEntityError>

sealed interface AddEntityError {
    data class Unauthorized(val loggedInMemberId: MemberId?) : AddEntityError
    data object DuplicateId : AddEntityError
    data object ConnectionError : AddEntityError
}