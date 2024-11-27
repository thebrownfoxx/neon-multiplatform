package com.thebrownfoxx.neon.common.data

enum class GetError {
    NotFound,
    ConnectionError,
}

enum class AddError {
    Duplicate,
    ConnectionError,
}

enum class UpdateError {
    NotFound,
    ConnectionError,
}

enum class DeleteError {
    NotFound,
    ConnectionError,
}

data object ConnectionError