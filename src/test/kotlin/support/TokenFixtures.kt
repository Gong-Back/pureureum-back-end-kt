package support

const val TOKEN_TYPE: String = "Bearer "
const val ACCESS_TOKEN: String = "valid-access-token"
const val NOT_VALID_ACCESS_TOKEN: String = "not-valid-access-token"
const val REFRESH_TOKEN: String = "valid-refresh-token"

fun createAccessToken(): String = ACCESS_TOKEN

fun createRefreshToken(): String = REFRESH_TOKEN
