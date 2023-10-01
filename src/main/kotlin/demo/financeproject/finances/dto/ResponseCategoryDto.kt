package demo.financeproject.finances.dto

import demo.financeproject.finances.jpa.entity.FinanceCategoryEntity

data class ResponseCategoryDto(
    var id: Int?,
    var ownerId: Int?,
    var title: String?
)

fun FinanceCategoryEntity.mapToDto(): ResponseCategoryDto {
    return ResponseCategoryDto(id, owner?.id, title)
}