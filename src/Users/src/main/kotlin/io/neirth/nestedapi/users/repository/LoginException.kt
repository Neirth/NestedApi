package io.neirth.nestedapi.users.repository

import java.lang.RuntimeException

class LoginException(message: String?) : RuntimeException(message)