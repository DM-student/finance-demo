package demo.financeproject.finances.dto

data class OperationDto(
    var id: Int? = null,
    var categoryId: Int? = null,
    var amount: Double? = null,
    var title: String? = null,
    var description: String? = null
)