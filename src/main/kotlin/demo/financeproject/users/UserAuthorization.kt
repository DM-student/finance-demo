package demo.financeproject.users

import demo.financeproject.controllers.BaseError
import jakarta.servlet.http.Cookie

import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

/**
 * Авторизация пользователя была вынесена мною в отдельный сервис.
 */
@Service
class UserAuthorization(val usersRepository: UsersRepository) {
    /**
     * Принимает куки клиента и извлекает из них данные для попытки автоматической авторизации.
     * @param cookies куки из которых надо извлечь данные пользователя.
     * @return объект пользователя.
     * @throws UserAuthError в случае если авторизация прошла неуспешно.
     */
    fun getAuthorizedUser(cookies: Array<Cookie>): UserEntity {
        val cookie = cookies.firstOrNull { it.name!! == "userAuthToken" } ?: throw UserAuthError()

        val user = usersRepository.findUserByToken(cookie.value)
            .orElseThrow { throw UserAuthError() }
        if (user.status != UserEntityStatus.ACTIVE) {
            throw UserAuthError("Authorization has failed. Account status is: ${user.status!!.status_text}")
        }
        return user
    }

    /**
     * Принимает куки клиента и извлекает из них данные для попытки автоматической авторизации.
     * В случае провала НЕ выкидывает исключение, а лишь возвращает null.
     * @param cookies куки из которых надо извлечь данные пользователя.
     * @return объект пользователя или null.
     */
    fun getAuthorizedUserOrNull(cookies: Array<Cookie>): UserEntity? {
        return try {
            getAuthorizedUser(cookies)
        } catch (e: UserAuthError) {
            null
        }
    }
}

class UserAuthError(message: String = "Authorization has failed.") : BaseError(message, HttpStatus.FORBIDDEN)