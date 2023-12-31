package demo.financeproject.users

import demo.financeproject.controllers.BaseError
import demo.financeproject.users.jpa.entity.UserEntity
import demo.financeproject.users.jpa.entity.UserEntityStatus
import demo.financeproject.users.jpa.repository.UsersRepository
import org.apache.commons.validator.routines.EmailValidator
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import kotlin.jvm.optionals.getOrNull


@Service
class UserService(val usersRepository: UsersRepository) {
    val hashCrypt = BCryptPasswordEncoder()
    val emailValidator: EmailValidator = EmailValidator.getInstance()
    fun validatePassword(password: String) {
        // Представим, что пароль может использовать любые символы, но не может содержать только пробелы.
        if (password.isBlank()) {
            throw UserError("Пароль не соответствует требуемому формату.", HttpStatus.BAD_REQUEST)
        }
        if (password.length < 5) {
            throw UserError("Пароль не соответствует требуемому формату.", HttpStatus.BAD_REQUEST)
        }
        if (password.length > 64) {
            throw UserError("Пароль не соответствует требуемому формату.", HttpStatus.BAD_REQUEST)
        }
    }

    fun validateLogin(login: String) {
        if (!emailValidator.isValid(login)) {
            throw UserError("Электронная почта не соответствует требуемому формату.", HttpStatus.BAD_REQUEST)
        }
        if (usersRepository.findUserByLogin(login).isPresent) {
            throw UserError("Электронная почта уже занята.", HttpStatus.CONFLICT)
        }
    }

    // Заготовил пару методов на всякий случай.

    fun getUser(userId: Int): UserEntity {
        return usersRepository.findById(userId)
            .orElseThrow { UserError("Пользователь №$userId не найден.") }
    }

    fun getUserOrNull(userId: Int): UserEntity? {
        return usersRepository.findById(userId).getOrNull()
    }

    /**
     * Создаёт пользователя принимая логин и пароль.
     * @param rawLogin электронная почта.
     * @param rawPassword пароль.
     * @throws UserError если не вышло создать пользователя.
     */
    fun createUser(rawLogin: String, rawPassword: String) {
        val login = rawLogin.lowercase().trim()

        val newUser = UserEntity()

        validateLogin(login)
        validatePassword(rawPassword)

        newUser.apply {
            this.login = login
            password = hashCrypt.encode(rawPassword)
            token = generateLoginToken(newUser.login!!, newUser.password!!)
            status = UserEntityStatus.AWAITS_ACTIVATION
            statusReason = "Ожидает подтверждение."
        }


        usersRepository.save(newUser)
    }

    /**
     * Метод активирует аккаунт пользователя если тот был деактивирован или заблокирован.
     *
     * Метод должен вызываться только из системных эндпоинтов и/или авторизированных источников!
     *
     * @param userId айди пользователя которого требуется активировать.
     * @throws UserError если пользователя не вышло активировать.
     */
    fun activateUser(userId: Int) {
        val user: UserEntity = usersRepository.findById(userId)
            .orElseThrow { UserError("Пользователь №$userId не найден.") }
        if (user.status != UserEntityStatus.AWAITS_ACTIVATION) {
            throw UserError("Пользователь №$userId не нуждается в активации.", HttpStatus.CONFLICT)
        }
        user.apply {
            status = UserEntityStatus.ACTIVE
            statusReason = null
        }
        usersRepository.save(user)
    }

    fun blockUser(userId: Int, reason: String?) {
        val user: UserEntity = usersRepository.findById(userId)
            .orElseThrow { UserError("Пользователь №$userId не найден.") }
        if (user.status == UserEntityStatus.BLOCKED) {
            throw UserError("Пользователь №$userId уже заблокирован.", HttpStatus.CONFLICT)
        }
        user.apply {
            status = UserEntityStatus.BLOCKED
            statusReason = reason
        }
        usersRepository.save(user)
    }

    fun unblockUser(userId: Int, reason: String?) {
        val user: UserEntity = usersRepository.findById(userId)
            .orElseThrow { UserError("Пользователь №$userId не найден.") }
        if (user.status != UserEntityStatus.BLOCKED) {
            throw UserError("Пользователь №$userId не заблокирован.", HttpStatus.CONFLICT)
        }
        user.apply {
            status = UserEntityStatus.ACTIVE
            statusReason = reason
        }
        usersRepository.save(user)
    }

    /**
     * Принимает логин и пароль, возвращая токен для входа.
     * @param rawLogin логин.
     * @param password пароль.
     * @return персональный токен для входа.
     * @throws UserLoginError если вход не удался.
     */
    fun getUserAuthToken(rawLogin: String, password: String): String {
        val login = rawLogin.lowercase().trim()

        val user = usersRepository.findUserByLogin(login)
            .orElseThrow { UserLoginError("Пользователя с данным логином не найдено.", HttpStatus.BAD_REQUEST) }
        if (user.status != UserEntityStatus.ACTIVE) { // Токен юзер получает только после активации.
            throw UserLoginError(
                "Отказано в доступе. Статус пользователя: ${user.status!!.status_text}",
                HttpStatus.FORBIDDEN
            )
        }
        if (!hashCrypt.matches(password, user.password)) {
            throw UserLoginError("Введён неверный пароль.")
        } else { // Просто на всякий случай помещу сюда else
            return user.token!!
        }
    }


    /**
     * Метод для смены пароля пользователю. Если указан старый пароль - он проверит его подлинность.
     * @param userId айди пользователя которому нужно изменить пароль.
     * @param newPassword новый пароль для пользователя.
     * @param oldPassword (ОПЦОИНАЛЬНО) старый пароль пользователя. Если эта переменная не null,
     * то перед сменой пароля будет проведена проверка на соответствие с текущим паролем перед его сменой.
     * @throws UserError если попытка сменить пароль была неудачной.
     */
    fun changeUserPassword(userId: Int, newPassword: String, oldPassword: String?) {
        val user: UserEntity = usersRepository.findById(userId)
            .orElseThrow { UserError("Пользователь №$userId не найден") }

        if (oldPassword != null && !hashCrypt.matches(oldPassword, user.password)) {
            throw UserError("Старый пароль не совпадает с предоставленным.", HttpStatus.BAD_REQUEST)
        }

        validatePassword(newPassword)

        user.password = hashCrypt.encode(newPassword)
        usersRepository.save(user)
    }

    fun changeUserLogin(userId: Int, newRawLogin: String, password: String) {
        val user: UserEntity = usersRepository.findById(userId)
            .orElseThrow { UserError("Пользователь №$userId не найден") }
        val newLogin = newRawLogin.lowercase().trim()

        validateLogin(newLogin)

        user.apply {
            login = newLogin
            status = UserEntityStatus.AWAITS_ACTIVATION
        }
        usersRepository.save(user)
    }

    /**
     * Метод для генерации токена.
     */
    private fun generateLoginToken(rawLogin: String, hashedPassword: String): String {
        val login = rawLogin.lowercase()

        val hashedLogin = hashCrypt.encode(login)
        return hashCrypt.encode(hashedLogin + hashedPassword)
    }

    // Это для админской части, но я не уверен, будет ли она у меня вообще.
    fun getUsersForStatus(status: UserEntityStatus, pageNum: Int, pageSize: Int): List<UserEntity> {
        val page = PageRequest.of(pageNum, pageSize)
        return usersRepository.pageUsersForStatusWithPagination(status, page).toList()
    }
}

class UserLoginError(message: String, status: HttpStatus? = null) : BaseError(message, status)
class UserError(message: String, status: HttpStatus? = null) : BaseError(message, status)