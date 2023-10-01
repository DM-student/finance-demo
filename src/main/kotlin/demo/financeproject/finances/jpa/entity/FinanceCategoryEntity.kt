package demo.financeproject.finances.jpa.entity

import demo.financeproject.users.jpa.entity.UserEntity
import jakarta.persistence.*


@Entity
@Table(name = "finance_categories")
class FinanceCategoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @ManyToOne
    @JoinColumn(
        name = "owner_id",
        nullable = false
    )
    var owner: UserEntity? = null

    @Column(
        name = "title",
        nullable = false
    )
    var title: String? = null

    @Transient
    fun isValid(): Boolean {
        if (owner == null) {
            return false
        }
        if (title == null || title!!.length > 64) {
            return false
        }
        return true
    }
}

