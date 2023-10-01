package demo.financeproject.finances.dto

import demo.financeproject.finances.jpa.entity.FinanceOperationEntity
import java.time.ZonedDateTime

data class ResponseOperationDto(
    var id: Int?,
    var ownerId: Int?,
    var category: ResponseCategoryDto? = null,
    var timestamp: ZonedDateTime?,
    var amount: Double?,
    var title: String?,
    var description: String?
)

fun FinanceOperationEntity.mapToDto(): ResponseOperationDto {
    val dto = ResponseOperationDto(
        id,
        owner!!.id,
        null,
        operationTimestamp,
        amount, title,
        description
    )
    if (category != null) {
        dto.category = category?.mapToDto()
    }
    return dto
}