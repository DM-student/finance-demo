package demo.financeproject.controllers

import demo.financeproject.finances.FinanceService
import demo.financeproject.finances.OperationDto
import demo.financeproject.finances.RequestOperationDto
import demo.financeproject.users.UserAuthorization
import demo.financeproject.users.UserService
import jakarta.servlet.http.Cookie
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.web.bind.annotation.*


@RestController
class FinanceController(val financeService: FinanceService, val userAuthorization: UserAuthorization) {
    @PostMapping("/finance")
    fun postOperation(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestBody requestBody: RequestOperationDto
    ): OperationDto {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
        responseHttpServlet.status = 201
        return financeService.postOperation(user, requestBody)
    }

    @PatchMapping("/finance")
    fun updateOperation(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestBody requestBody: RequestOperationDto
    ): OperationDto {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
        responseHttpServlet.status = 200
        return financeService.updateOperation(user, requestBody)
    }
}