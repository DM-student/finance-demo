package demo.financeproject.controllers

import demo.financeproject.users.UserAuthorization
import demo.financeproject.users.UserService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*

@RestController
class UserController(val userService: UserService, val userAuthorization: UserAuthorization) {
    data class UserLoginDto(var login:String, var password:String)

    @PostMapping("/register")
    fun register (requestHttpServlet: HttpServletRequest,
                  responseHttpServlet:HttpServletResponse,
                  @RequestBody requestBody: UserLoginDto) {
        userService.createUser(requestBody.login, requestBody.password)
    }

    @PostMapping("/login")
    fun login (requestHttpServlet: HttpServletRequest,
               responseHttpServlet: HttpServletResponse,
               @RequestBody requestBody: UserLoginDto) {
        val token = userService.getUserAuthToken(requestBody.login, requestBody.password)
        responseHttpServlet.addCookie(Cookie( "userAuthToken", token))
    }

    /**
     * Очень условный эндпоинт для активации юзера после регистрации.
     * На-пример ссылку с этим эндпоинтом и переменными запроса можно отослать на почту.
     */
    @PostMapping("/system/user/activate")
    fun activateUser (requestHttpServlet: HttpServletRequest,
                      responseHttpServlet:HttpServletResponse,
                      @RequestParam userId: Int) {
        userService.activateUser(userId)
    }

    @GetMapping("/usertest")
    fun test (requestHttpServlet: HttpServletRequest,
               responseHttpServlet: HttpServletResponse) {
        userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
    }
}