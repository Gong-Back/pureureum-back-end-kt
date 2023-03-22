package support

const val ACCESS_TOKEN: String = "valid-access-token"
const val REFRESH_TOKEN: String = "valid-refresh-token"
const val REFRESH_HEADER_NAME: String = "RefreshToken"

fun createAccessToken(): String = ACCESS_TOKEN
fun createRefreshToken(): String = REFRESH_TOKEN
