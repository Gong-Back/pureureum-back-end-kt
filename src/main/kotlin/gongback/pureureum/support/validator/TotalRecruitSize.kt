package gongback.pureureum.support.validator

import jakarta.validation.Constraint
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.Payload
import kotlin.reflect.KClass

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [TotalRecruitSizeValidator::class])
annotation class TotalRecruitSize(
    val message: String = "올바른 제한 인원 값을 입력해야 합니다",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

private const val UNLIMITED_RECRUITS = -1
private const val MIN_RECRUITS = 1

class TotalRecruitSizeValidator : ConstraintValidator<TotalRecruitSize, Int> {
    override fun isValid(value: Int?, context: ConstraintValidatorContext?): Boolean {
        if (value == null) {
            return false
        }
        return value == UNLIMITED_RECRUITS || value >= MIN_RECRUITS
    }
}
