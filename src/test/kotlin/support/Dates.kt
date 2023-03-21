package support

import java.time.LocalDate

fun createLocalDate(
    year: Int,
    month: Int = 1,
    dayOfMonth: Int = 1
): LocalDate {
    return LocalDate.of(year, month, dayOfMonth)
}
