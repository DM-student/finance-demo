package demo.financeproject.finances

import demo.financeproject.controllers.BaseError
import demo.financeproject.finances.dto.*
import demo.financeproject.finances.jpa.repository.FinanceCategoriesRepository
import demo.financeproject.finances.jpa.repository.FinanceOperationsRepository
import demo.financeproject.finances.jpa.entity.FinanceCategoryEntity
import demo.financeproject.finances.jpa.entity.FinanceOperationEntity
import demo.financeproject.users.jpa.entity.UserEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.time.ZonedDateTime

@Service
class FinanceService(
    val operationsRepository: FinanceOperationsRepository,
    val categoriesRepository: FinanceCategoriesRepository
) {
    fun postOperation(
        user: UserEntity,
        operationDto: OperationDto
    ): ResponseOperationDto {
        val error = BadRequestError("Финансовая операция не соответствует формату и/или не содержит нужные переменные.")
        val newOperation = FinanceOperationEntity().apply {
            owner = user
            if (operationDto.categoryId != null) {
                category = getCategoryRaw(user, operationDto.categoryId!!)
            }
            operationTimestamp = ZonedDateTime.now()
            amount = operationDto.amount
            title = operationDto.title
            description = operationDto.description
        }


        if (!newOperation.isValid(categoriesRepository)) {
            throw error
        }

        return operationsRepository.save(newOperation).mapToDto()
    }

    fun updateOperation(
        user: UserEntity,
        operationDto: OperationDto
    ): ResponseOperationDto {
        val error = BadRequestError("Финансовая операция не соответствует формату и/или не содержит нужные переменные.")
        if (operationDto.id == null) {
            throw error
        }
        val operation = getOperationRaw(user, operationDto.id!!).apply {
            if (operationDto.categoryId != null) {
                category = getCategoryRaw(user, operationDto.categoryId!!)
            }
            amount = operationDto.amount
            title = operationDto.title
            description = operationDto.description
        }

        if (!operation.isValid(categoriesRepository)) {
            throw error
        }

        return operationsRepository.save(operation).mapToDto()
    }

    fun deleteOperation(
        user: UserEntity,
        operationId: Int
    ) {
        getOperationRaw(user, operationId)
        operationsRepository.deleteById(operationId)
    }

    fun getOperationRaw(
        user: UserEntity,
        operationId: Int
    ): FinanceOperationEntity {
        val error = BadRequestError("Финансовая операция №${operationId} не найдена среди операций пользователя.")
        val operation = operationsRepository.findById(operationId)
            .orElseThrow { error }

        if (operation.owner!!.id!! != user.id!!) {
            throw error
        }

        return operation
    }

    fun getOperation(
        user: UserEntity,
        operationId: Int
    ): ResponseOperationDto {
        return getOperationRaw(user, operationId).mapToDto()
    }

    fun getOperations(
        user: UserEntity,
        pageable: Pageable,
        sort: OperationSort
    ): Page<ResponseOperationDto> {
        val page = when (sort) {
            OperationSort.DATE -> {
                operationsRepository.getOperationsPageForUserSortByDate(user.id!!, pageable)
            }

            OperationSort.AMOUNT -> {
                operationsRepository.getOperationsPageForUserSortByAmount(user.id!!, pageable)
            }
        }
        return page.map { it.mapToDto() }
    }

    fun getOperationsForCategory(
        user: UserEntity,
        pageable: Pageable,
        categoryId: Int,
        sort: OperationSort
    ): Page<ResponseOperationDto> {
        val category = categoriesRepository.findById(categoryId)
            .orElseThrow { BadRequestError("Категория №$categoryId не найдена в категориях пользователя.") }

        if (category.owner!!.id!! != user.id) {
            throw BadRequestError("Категория №$categoryId не найдена в категориях пользователя.")
        }

        val page = when (sort) {
            OperationSort.DATE -> {
                operationsRepository.getOperationsPageForCategorySortByDate(categoryId, pageable)
            }

            OperationSort.AMOUNT -> {
                operationsRepository.getOperationsPageForCategorySortByAmount(categoryId, pageable)
            }
        }
        return page.map { it.mapToDto() }
    }

    fun getOperationsForSearchQuery(
        user: UserEntity,
        pageable: Pageable,
        searchQueryRaw: String,
        sort: OperationSort
    ): Page<ResponseOperationDto> {
        val searchQuery = "%${searchQueryRaw.lowercase().trim()}%"

        val page = when (sort) {
            OperationSort.DATE -> {
                operationsRepository.getOperationsPageForSearchSortByDate(searchQuery, user.id!!, pageable)
            }

            OperationSort.AMOUNT -> {
                operationsRepository.getOperationsPageForSearchSortByAmount(searchQuery, user.id!!, pageable)
            }
        }
        return page.map { it.mapToDto() }
    }

    fun postCategory(
        user: UserEntity,
        categoryDto: CategoryDto
    ): ResponseCategoryDto {
        val error =
            BadRequestError("Финансовая категория не соответствует формату и/или не содержит нужные переменные.")
        val category = FinanceCategoryEntity().apply {
            owner = user
            title = categoryDto.title
        }
        if (!category.isValid()) {
            throw error
        }
        return categoriesRepository.save(category).mapToDto()
    }

    fun updateCategory(
        user: UserEntity,
        categoryDto: CategoryDto
    ): ResponseCategoryDto {
        val error =
            BadRequestError("Финансовая категория не соответствует формату и/или не содержит нужные переменные.")
        if (categoryDto.id == null) {
            throw error
        }
        val category = getCategoryRaw(user, categoryDto.id!!).apply {
            owner = user
            title = categoryDto.title
        }
        if (!category.isValid()) {
            throw error
        }
        return categoriesRepository.save(category).mapToDto()
    }

    fun deleteCategory(
        user: UserEntity,
        categoryId: Int
    ) {
        getCategoryRaw(user, categoryId)
        categoriesRepository.deleteById(categoryId)
    }

    fun getCategoryRaw(
        user: UserEntity,
        categoryId: Int
    ): FinanceCategoryEntity {
        val error = BadRequestError("Финансовая категория №${categoryId} не найдена среди категорий пользователя.")
        val category = categoriesRepository.findById(categoryId)
            .orElseThrow { error }

        if (category.owner!!.id!! != user.id!!) {
            throw error
        }

        return category
    }

    fun getCategory(
        user: UserEntity,
        categoryId: Int
    ): ResponseCategoryDto {
        return getCategoryRaw(user, categoryId).mapToDto()
    }

    fun getCategories(user: UserEntity, pageable: Pageable): Page<ResponseCategoryDto> {
        val page = categoriesRepository.getCategoriesPageForUser(user.id!!, pageable)
        return page.map { it.mapToDto() }
    }

    fun getSummary(user: UserEntity, rangeStart: ZonedDateTime?, rangeEnd: ZonedDateTime?): FinanceSummaryDto {
        val start = rangeStart ?: ZonedDateTime.now().minusYears(100) // условно скинем 100 лет.
        val end = rangeEnd ?: ZonedDateTime.now().plusYears(100) // условно докинем 100 лет.

        // У меня были большие трудности с иными методами получения агрегатных результатов.
        val resultList = operationsRepository.getFinanceSummary(start, end, user.id!!).firstOrNull()
            ?: return FinanceSummaryDto(0.0, 0)
        return FinanceSummaryDto(resultList.getOrNull(0), resultList.getOrNull(1))
    }

    fun getSummaryForCategory(
        user: UserEntity,
        rangeStart: ZonedDateTime?,
        rangeEnd: ZonedDateTime?,
        categoryId: Int
    ): FinanceSummaryDto {
        getCategoryRaw(user, categoryId) // проверка на существование категории.

        var start = rangeStart
        var end = rangeEnd
        if (start == null) {
            start = ZonedDateTime.now().minusYears(100) // условно скинем 100 лет.
        }
        if (end == null) {
            end = ZonedDateTime.now().plusYears(100) // условно докинем 100 лет.
        }
        // У меня были большие трудности с иными методами получения агрегатных результатов.
        val resultList = operationsRepository.getFinanceSummaryForCategory(start!!, end!!, categoryId).firstOrNull()
            ?: return FinanceSummaryDto(0.0, 0)
        return FinanceSummaryDto(resultList.getOrNull(0), resultList.getOrNull(1))
    }
}

class BadRequestError(message: String, status: HttpStatus? = HttpStatus.BAD_REQUEST) : BaseError(message, status)

enum class OperationSort {
    DATE,
    AMOUNT
}

