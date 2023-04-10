package support

const val ACCESS_TOKEN: String = "valid-access-token"
const val NOT_VALID_ACCESS_TOKEN: String = "not-valid-access-token"
const val REFRESH_TOKEN: String = "valid-refresh-token"
const val NOT_VALID_REFRESH_TOKEN: String = "not-valid-refresh-token"
const val REFRESH_COOKIE_NAME: String = "RefreshToken"

fun createAccessToken(): String = ACCESS_TOKEN

fun createNotValidAccessToken(): String = NOT_VALID_ACCESS_TOKEN

fun createRefreshToken(): String = REFRESH_TOKEN

fun createNotValidRefreshToken(): String = NOT_VALID_REFRESH_TOKEN
