package com.thebrownfoxx.neon.common.data

enum class DataOperationError {
    ConnectionError,
    UnexpectedError,
}

enum class GetError {
    NotFound,
    ConnectionError,
    UnexpectedError;

    fun toUpdateError() = when (this) {
        NotFound -> UpdateError.NotFound
        ConnectionError -> UpdateError.ConnectionError
        UnexpectedError -> UpdateError.UnexpectedError
    }
}

enum class AddError {
    Duplicate,
    ConnectionError,
    UnexpectedError,
}

enum class UpdateError {
    NotFound,
    ConnectionError,
    UnexpectedError,
}

enum class DeleteError {
    NotFound,
    ConnectionError,
    UnexpectedError,
}

data object ConnectionError