package demo.financeproject.finances.jpa.entity

import demo.financeproject.finances.jpa.repository.FinanceCategoriesRepository
import demo.financeproject.users.jpa.entity.UserEntity
import jakarta.persistence.*
import jakarta.persistence.Transient
import java.time.ZonedDateTime

@Entity
@Table(name = "finance_operations")
class FinanceOperationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @ManyToOne
    @JoinColumn(
        name = "owner_id",
        nullable = false
    )
    var owner: UserEntity? = null

    @ManyToOne
    @JoinColumn(
        name = "category_id",
        nullable = true
    )
    var category: FinanceCategoryEntity? = null

    @Column(
        name = "operation_timestamp",
        nullable = false
    )
    var operationTimestamp: ZonedDateTime? = null

    @Column(
        name = "amount",
        nullable = false
    )
    var amount: Double? = null

    @Column(
        name = "title",
        nullable = false
    )
    var title: String? = null

    @Column(
        name = "description",
        nullable = true
    )
    var description: String? = null

    @Transient
    fun isValid(categoriesRepository: FinanceCategoriesRepository): Boolean {
        if (owner == null) {
            return false
        }
        if (category != null && category!!.owner!!.id != owner!!.id) {
            return false
        }
        if (operationTimestamp == null) {
            return false
        }
        if (amount == null) {
            return false
        }
        if (title == null || title!!.length > 64) {
            return false
        }
        if (description != null && description!!.isBlank()) {
            return false
        }
        return true
    }
}