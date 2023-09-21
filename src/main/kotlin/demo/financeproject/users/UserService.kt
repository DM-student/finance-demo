package demo.financeproject.users

import demo.financeproject.controllers.BaseError
import org.apache.commons.validator.routines.EmailValidator
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder


@Service
class UserService(val userRepository: UserRepository) {
    val hashCrypt = BCryptPasswordEncoder()
    val emailValidator: EmailValidator = EmailValidator.getInstance()

    fun createUser(rawLogin: String, rawPassword: String) {
        val login = rawLogin.lowercase()

        val newUser = UserEntity()

        if(!emailValidator.isValid(login)) {
            throw UserError("Электронная почта не соответствует требуемому формату.", HttpStatus.BAD_REQUEST)
        }
        if(userRepository.findUserByLogin(login).isPresent) {
            throw UserError("Электронная почта уже занята.", HttpStatus.CONFLICT)
        }

        // Представим, что пароль может быть любым набором символов.
        if(rawPassword.isBlank()) {
            throw UserError("Пароль не соответствует требуемому формату.", HttpStatus.BAD_REQUEST)
        }

        newUser.login = login
        newUser.password = hashCrypt.encode(rawPassword)
        newUser.status = UserEntityStatus.NOT_ACTIVE
        newUser.statusReason = "Awaits verification."

        userRepository.save(newUser)
    }

    fun activateUser(userId: Int){
        val user: UserEntity = userRepository.findById(userId)
            .orElseThrow{UserError("Пользователь №$userId не найден.")}
        if(user.status == UserEntityStatus.ACTIVE && user.token != null) {
            throw UserError("Пользователь №$userId уже активирован.", HttpStatus.CONFLICT)
        }
        user.token = generateLoginToken(user.login!!, user.password!!)
        user.status = UserEntityStatus.ACTIVE
        user.statusReason = null
        userRepository.save(user)
    }

    fun loginUser(rawLogin: String, password: String) : String {
        val login = rawLogin.lowercase()

        val user = userRepository.findUserByLogin(login)
            .orElseThrow{UserLoginError("Пользователя с данным логином не найдено.", HttpStatus.BAD_REQUEST)}
        if(user.token == null) {
            throw UserLoginError("Пользователь не был активирован или был деактивирован.",
                                 HttpStatus.FORBIDDEN)
        }
        if(hashCrypt.matches(password, user.password)) {
            throw UserLoginError("Введён неверный пароль.")
        }
        return user.token!!
    }
    

    /**
     * Метод для смены пароля пользователю. Если указан старый пароль - он проверит его подлинность.
     */
    fun changeUserPassword(userId: Int, newPassword: String, oldPassword: String?) {
        val user: UserEntity = userRepository.findById(userId)
            .orElseThrow{UserError("Пользователь №$userId не найден")}

        if(oldPassword != null && hashCrypt.matches(oldPassword, user.password)) {
            throw UserError("Старый пароль не совпадает с предоставленным.", HttpStatus.BAD_REQUEST)
        }
        // Представим, что пароль может быть любым набором символов.
        if(newPassword.isBlank()) {
            throw UserError("Новый пароль не соответствует требуемому формату.", HttpStatus.BAD_REQUEST)
        }
        user.password = hashCrypt.encode(newPassword)
        userRepository.save(user)
    }

    fun generateLoginToken(rawLogin: String, hashedPassword: String): String {
        val login = rawLogin.lowercase()

        val hashedLogin = hashCrypt.encode(login)
        return hashCrypt.encode(hashedLogin + hashedPassword)
    }

    fun getUsersForStatus(status:UserEntityStatus, pageNum: Int, pageSize: Int): List<UserEntity> {
        val page = PageRequest.of(pageNum, pageSize)
        return userRepository.findAllUsersForStatusWithPagination(status, page).toList()
    }
}

class UserLoginError(message: String, status: HttpStatus? = null) : BaseError(message, status)
class UserError(message: String, status: HttpStatus? = null) : BaseError(message, status)