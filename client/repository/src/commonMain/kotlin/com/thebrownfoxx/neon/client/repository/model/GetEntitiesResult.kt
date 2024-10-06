package com.thebrownfoxx.neon.client.repository.model

import com.thebrownfoxx.neon.common.model.Result

typealias GetEntitiesResult<T> = Result<List<T>, GetEntitiesError>

sealed interface GetEntitiesError {
    data object ConnectionError : GetEntityError
}