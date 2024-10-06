package com.thebrownfoxx.neon.client.repository.model

import com.thebrownfoxx.neon.common.model.UnitResult

typealias UpdateEntityResult = UnitResult<UpdateEntityError>

sealed interface UpdateEntityError {
    data object NotFound : UpdateEntityError
    data object ConnectionError : UpdateEntityError
}