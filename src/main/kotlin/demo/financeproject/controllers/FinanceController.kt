package demo.financeproject.controllers

import demo.financeproject.finances.*
import demo.financeproject.users.UserAuthorization
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.web.bind.annotation.*
import java.time.ZonedDateTime


@RestController
class FinanceController(
    val financeService: FinanceService,
    val userAuthorization: UserAuthorization
) {
    @PostMapping("/finance/operation")
    fun postOperation(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestBody requestBody: RequestOperationDto
    ): OperationDto {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
        responseHttpServlet.status = 201
        return financeService.postOperation(user, requestBody)
    }

    @PatchMapping("/finance/operation")
    fun updateOperation(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestBody requestBody: RequestOperationDto
    ): OperationDto {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
        return financeService.updateOperation(user, requestBody)
    }

    @DeleteMapping("/finance/operation/{id}")
    fun deleteOperation(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @PathVariable id: Int
    ) {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
        financeService.deleteOperation(user, id)
    }

    @GetMapping("/finance/operation/{id}")
    fun getOperation(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @PathVariable id: Int
    ): OperationDto {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
        return financeService.getOperation(user, id)
    }

    @GetMapping("/finance/operation")
    fun getOperations(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestParam page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "DATE") sort: OperationSort
    ): Page<OperationDto> {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
        val pageable = PageRequest.of(page, pageSize)
        return financeService.getOperations(user, pageable, sort)
    }

    @GetMapping("/finance/operation/category")
    fun getOperationsForCategory(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestParam categoryId: Int,
        @RequestParam page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "DATE") sort: OperationSort
    ): Page<OperationDto> {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
        val pageable = PageRequest.of(page, pageSize)
        return financeService.getOperationsForCategory(user, pageable, categoryId, sort)
    }

    @GetMapping("/finance/operation/search")
    fun getOperationsSearch(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestParam query: String,
        @RequestParam page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int,
        @RequestParam(defaultValue = "DATE") sort: OperationSort
    ): Page<OperationDto> {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
        val pageable = PageRequest.of(page, pageSize)
        return financeService.getOperationsForSearchQuery(user, pageable, query, sort)
    }

    @PostMapping("/finance/category")
    fun postCategory(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestBody requestBody: RequestCategoryDto
    ): CategoryDto {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
        responseHttpServlet.status = 201
        return financeService.postCategory(user, requestBody)
    }

    @PatchMapping("/finance/category")
    fun updateCategory(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestBody requestBody: RequestCategoryDto
    ): CategoryDto {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
        return financeService.updateCategory(user, requestBody)
    }

    @DeleteMapping("/finance/category/{id}")
    fun deleteCategory(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @PathVariable id: Int
    ) {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
        financeService.deleteCategory(user, id)
    }

    @GetMapping("/finance/category/{id}")
    fun getCategory(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @PathVariable id: Int
    ): CategoryDto {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
        return financeService.getCategory(user, id)
    }

    @GetMapping("/finance/category")
    fun getCategories(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestParam page: Int,
        @RequestParam(defaultValue = "10") pageSize: Int
    ): Page<CategoryDto> {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)
        val pageable = PageRequest.of(page, pageSize)
        return financeService.getCategories(user, pageable)
    }

    @GetMapping("/finance/summary")
    fun getSummary(
        requestHttpServlet: HttpServletRequest,
        responseHttpServlet: HttpServletResponse,
        @RequestParam rangeStart: String?,
        @RequestParam rangeEnd: String?,
        @RequestParam categoryId: Int?
    ): FinanceSummaryDto {
        val user = userAuthorization.getAuthorizedUser(requestHttpServlet.cookies)

        var start: ZonedDateTime? = null
        var end: ZonedDateTime? = null

        if (rangeStart != null) {
            start = ZonedDateTime.parse(rangeStart)
        }
        if (rangeEnd != null) {
            end = ZonedDateTime.parse(rangeStart)
        }

        if (categoryId == null) {
            return financeService.getSummary(user, start, end)
        }
        return financeService.getSummaryForCategory(user, start, end, categoryId)
    }

}