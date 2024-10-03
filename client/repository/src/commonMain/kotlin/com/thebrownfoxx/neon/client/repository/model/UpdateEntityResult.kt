package com.thebrownfoxx.neon.client.repository.model

import com.thebrownfoxx.neon.common.model.MemberId
import com.thebrownfoxx.neon.common.model.UnitResult

typealias UpdateEntityResult = UnitResult<UpdateEntityError>

sealed interface UpdateEntityError {
    data class Unauthorized(val loggedInMemberId: MemberId?) : UpdateEntityError
    data object NotFound : UpdateEntityError
    data object ConnectionError : UpdateEntityError
}