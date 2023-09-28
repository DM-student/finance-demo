package demo.financeproject.finances

import demo.financeproject.controllers.BaseError
import demo.financeproject.users.UserEntity
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
        requestOperationDto: RequestOperationDto
    ): OperationDto {
        val error = BadRequestError("Финансовая операция не соответствует формату и/или не содержит нужные переменные.")
        val newOperation = FinanceOperationEntity()
        newOperation.owner = user

        if (requestOperationDto.categoryId != null) {
            newOperation.category = getCategoryRaw(user, requestOperationDto.categoryId!!)
        }
        newOperation.operationTimestamp = ZonedDateTime.now()
        newOperation.amount = requestOperationDto.amount
        newOperation.title = requestOperationDto.title
        newOperation.description = requestOperationDto.description


        if (!newOperation.isValid(categoriesRepository)) {
            throw error
        }

        return OperationDto.mapToDto(operationsRepository.save(newOperation))
    }

    fun updateOperation(
        user: UserEntity,
        requestOperationDto: RequestOperationDto
    ): OperationDto {
        val error = BadRequestError("Финансовая операция не соответствует формату и/или не содержит нужные переменные.")
        if (requestOperationDto.id == null) {
            throw error
        }
        val operation = getOperationRaw(user, requestOperationDto.id!!)

        if (requestOperationDto.categoryId != null) {
            operation.category = getCategoryRaw(user, requestOperationDto.categoryId!!)
        }
        operation.amount = requestOperationDto.amount
        operation.title = requestOperationDto.title
        operation.description = requestOperationDto.description

        if (!operation.isValid(categoriesRepository)) {
            throw error
        }

        return OperationDto.mapToDto(operationsRepository.save(operation))
    }

    fun deleteOperation(
        user: UserEntity,
        operationId: Int
    ) {
        OperationDto.mapToDto(getOperationRaw(user, operationId))
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
    ): OperationDto {
        return OperationDto.mapToDto(getOperationRaw(user, operationId))
    }

    fun getOperations(
        user: UserEntity,
        pageable: Pageable,
        sort: OperationSort
    ): Page<OperationDto> {
        val page = when (sort) {
            OperationSort.DATE -> {
                operationsRepository.getOperationsPageForUserSortByDate(user.id!!, pageable)
            }

            OperationSort.AMOUNT -> {
                operationsRepository.getOperationsPageForUserSortByAmount(user.id!!, pageable)
            }
        }
        return page.map { OperationDto.mapToDto(it) }
    }

    fun getOperationsForCategory(
        user: UserEntity,
        pageable: Pageable,
        categoryId: Int,
        sort: OperationSort
    ): Page<OperationDto> {
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
        return page.map { OperationDto.mapToDto(it) }
    }

    fun getOperationsForSearchQuery(
        user: UserEntity,
        pageable: Pageable,
        searchQueryRaw: String,
        sort: OperationSort
    ): Page<OperationDto> {
        val searchQuery = "%${searchQueryRaw.lowercase().trim()}%"

        val page = when (sort) {
            OperationSort.DATE -> {
                operationsRepository.getOperationsPageForSearchSortByDate(searchQuery, user.id!!, pageable)
            }

            OperationSort.AMOUNT -> {
                operationsRepository.getOperationsPageForSearchSortByAmount(searchQuery, user.id!!, pageable)
            }
        }
        return page.map { OperationDto.mapToDto(it) }
    }

    fun postCategory(
        user: UserEntity,
        requestCategoryDto: RequestCategoryDto
    ): CategoryDto {
        val error =
            BadRequestError("Финансовая категория не соответствует формату и/или не содержит нужные переменные.")
        val category = FinanceCategoryEntity()
        category.owner = user
        category.title = requestCategoryDto.title
        if (!category.isValid()) {
            throw error
        }
        return CategoryDto.mapToDto(categoriesRepository.save(category))
    }

    fun updateCategory(
        user: UserEntity,
        requestCategoryDto: RequestCategoryDto
    ): CategoryDto {
        val error =
            BadRequestError("Финансовая категория не соответствует формату и/или не содержит нужные переменные.")
        if (requestCategoryDto.id == null) {
            throw error
        }
        val category = getCategoryRaw(user, requestCategoryDto.id!!)
        category.owner = user
        category.title = requestCategoryDto.title
        if (!category.isValid()) {
            throw error
        }
        return CategoryDto.mapToDto(categoriesRepository.save(category))
    }

    fun deleteCategory(
        user: UserEntity,
        categoryId: Int
    ) {
        CategoryDto.mapToDto(getCategoryRaw(user, categoryId))
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
    ): CategoryDto {
        return CategoryDto.mapToDto(getCategoryRaw(user, categoryId))
    }

    fun getCategories(user: UserEntity, pageable: Pageable): Page<CategoryDto> {
        val page = categoriesRepository.getCategoriesPageForUser(user.id!!, pageable)
        return page.map { CategoryDto.mapToDto(it) }
    }

    fun getSummary(user: UserEntity, rangeStart: ZonedDateTime?, rangeEnd: ZonedDateTime?): FinanceSummaryDto {
        var start = rangeStart
        var end = rangeEnd
        if (start == null) {
            start = ZonedDateTime.now().minusYears(100) // условно скинем 100 лет.
        }
        if (end == null) {
            end = ZonedDateTime.now().plusYears(100) // условно докинем 100 лет.
        }
        // У меня были большие трудности с иными методами получения агрегатных результатов.
        val resultList = operationsRepository.getFinanceSummary(start!!, end!!, user.id!!).firstOrNull()
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

data class OperationDto(
    var id: Int,
    var ownerId: Int,
    var category: CategoryDto? = null,
    var timestamp: ZonedDateTime,
    var amount: Double,
    var title: String,
    var description: String?
) {
    companion object {
        fun mapToDto(operationEntity: FinanceOperationEntity): OperationDto {
            val dto = OperationDto(
                operationEntity.id!!,
                operationEntity.owner!!.id!!,
                null,
                operationEntity.operationTimestamp!!,
                operationEntity.amount!!, operationEntity.title!!,
                operationEntity.description
            )
            if (operationEntity.category != null) {
                dto.category = CategoryDto.mapToDto(operationEntity.category!!)
            }
            return dto
        }
    }
}

data class CategoryDto(
    var id: Int,
    var ownerId: Int,
    var title: String
) {
    companion object {
        fun mapToDto(categoryEntity: FinanceCategoryEntity): CategoryDto {
            return CategoryDto(categoryEntity.id!!, categoryEntity.owner!!.id!!, categoryEntity.title!!)
        }
    }
}

data class FinanceSummaryDto(var totalSum: Any?, var operationsCount: Any?)

data class RequestCategoryDto(
    var id: Int? = null,
    var ownerId: Int? = null,
    var title: String? = null
)

data class RequestOperationDto(
    var id: Int? = null,
    var categoryId: Int? = null,
    var amount: Double? = null,
    var title: String? = null,
    var description: String? = null
)