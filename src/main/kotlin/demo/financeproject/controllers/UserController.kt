package demo.financeproject.controllers

import demo.financeproject.users.UserAuthorization
import demo.financeproject.users.UserService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
class UserController(val userService: UserService, val userAuthorization: UserAuthorization) {
    @PostMapping("/register")
    fun register(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestBody requestBody: UserLoginDto
    ) {
        userService.createUser(requestBody.login, requestBody.password)
        responseHttpServlet.status = 201
    }

    @PatchMapping("/user/password")
    fun changePassword(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestBody requestBody: UserPasswordChangeDto
    ) {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
        userService.changeUserPassword(user.id!!, requestBody.newPassword, requestBody.oldPassword)
    }

    @PatchMapping("/user/login")
    fun changeLogin(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestBody requestBody: UserLoginDto
    ) {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
        userService.changeUserLogin(user.id!!, requestBody.login, requestBody.password)
    }

    @PostMapping("/login")
    fun login(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestBody requestBody: UserLoginDto
    ) {
        val token = userService.getUserAuthToken(requestBody.login, requestBody.password)
        responseHttpServlet.addCookie(Cookie("userAuthToken", token))
    }

    /**
     * Максимально условный эндпоинт для активации юзера после регистрации.
     * По-хорошему по этому эндпоинту должны приходить системные запросы на активацию юзера,
     * за сим оно и проверяет условный системный "пароль". Реализовал я это просто как абстрактный пример.
     */
    @PostMapping("/system/user/activate")
    fun activateUser(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestParam userId: Int
    ) {
        val password: String? = requestHttpServlet.getHeader("system-password")
        if (password != "admin") {
            throw SystemPasswordError()
        }
        userService.activateUser(userId)
    }

    /**
     * Аналогично методу выше.
     */
    @PostMapping("/system/user/block")
    fun blockUser(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestParam userId: Int,
        @RequestParam reason: String?
    ) {
        val password: String? = requestHttpServlet.getHeader("system-password")
        if (password != "admin") {
            throw SystemPasswordError()
        }
        userService.blockUser(userId, reason)
    }

    @PostMapping("/system/user/unblock")
    fun unblockUser(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestParam userId: Int,
        @RequestParam reason: String?
    ) {
        val password: String? = requestHttpServlet.getHeader("system-password")
        if (password != "admin") {
            throw SystemPasswordError()
        }
        userService.unblockUser(userId, reason)
    }

    @GetMapping("/usertest")
    fun test(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse
    ) {
        userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
    }
}

class SystemPasswordError(message: String = "System password is wrong!") : BaseError(message, HttpStatus.FORBIDDEN)
data class UserLoginDto(var login: String, var password: String)
data class UserPasswordChangeDto(var newPassword: String, var oldPassword: String)