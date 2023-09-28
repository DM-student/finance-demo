package demo.financeproject.finances

import demo.financeproject.users.UserEntity
import jakarta.persistence.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
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

interface FinanceOperationsRepository : JpaRepository<FinanceOperationEntity, Int> {

    // No filter

    @Query(value = "SELECT f FROM FinanceOperationEntity f WHERE f.owner.id = ?1 ORDER BY f.operationTimestamp DESC")
    fun getOperationsPageForUserSortByDate(userId: Int, pageable: Pageable): Page<FinanceOperationEntity>

    @Query(value = "SELECT f FROM FinanceOperationEntity f WHERE f.owner.id = ?1 ORDER BY f.amount DESC")
    fun getOperationsPageForUserSortByAmount(userId: Int, pageable: Pageable): Page<FinanceOperationEntity>

    // category

    @Query(value = "SELECT f FROM FinanceOperationEntity f WHERE f.category.id = ?1 ORDER BY f.operationTimestamp DESC")
    fun getOperationsPageForCategorySortByDate(categoryId: Int, pageable: Pageable): Page<FinanceOperationEntity>

    @Query(value = "SELECT f FROM FinanceOperationEntity f WHERE f.category.id = ?1 ORDER BY f.amount DESC")
    fun getOperationsPageForCategorySortByAmount(categoryId: Int, pageable: Pageable): Page<FinanceOperationEntity>

    // search

    @Query(
        value = "SELECT f " +
                "FROM FinanceOperationEntity f " +
                "WHERE f.owner.id = ?2 " +
                "AND " +
                "( " +
                "LOWER(f.title) LIKE ?1 " +
                "OR " +
                "LOWER(f.description) LIKE ?1" +
                ") " +
                "ORDER BY f.operationTimestamp DESC"
    )
    fun getOperationsPageForSearchSortByDate(
        searchQuery: String,
        userId: Int,
        pageable: Pageable
    ): Page<FinanceOperationEntity>

    @Query(
        value = "SELECT f " +
                "FROM FinanceOperationEntity f " +
                "WHERE f.owner.id = ?2 " +
                "AND " +
                "( " +
                "LOWER(f.title) LIKE ?1 " +
                "OR " +
                "LOWER(f.description) LIKE ?1" +
                ") " +
                "ORDER BY f.amount DESC"
    )
    fun getOperationsPageForSearchSortByAmount(
        searchQuery: String,
        userId: Int,
        pageable: Pageable
    ): Page<FinanceOperationEntity>

    @Query(
        value = "SELECT new list(SUM(f.amount), COUNT(f.id)) " +
                "FROM FinanceOperationEntity f " +
                "WHERE f.owner.id = ?3 " +
                "AND " +
                "f.operationTimestamp > ?1 " +
                "AND " +
                "f.operationTimestamp < ?2 "
    )
    fun getFinanceSummary(
        rangeStart: ZonedDateTime,
        rangeEnd: ZonedDateTime,
        userId: Int
    ): List<List<Any>>

    @Query(
        value = "SELECT new list(SUM(f.amount), COUNT(f.id)) " +
                "FROM FinanceOperationEntity f " +
                "WHERE f.category.id = ?3 " +
                "AND " +
                "f.operationTimestamp > ?1 " +
                "AND " +
                "f.operationTimestamp < ?2 "
    )
    fun getFinanceSummaryForCategory(
        rangeStart: ZonedDateTime,
        rangeEnd: ZonedDateTime,
        categoryId: Int
    ): List<List<Any>>
}

interface FinanceCategoriesRepository : JpaRepository<FinanceCategoryEntity, Int> {
    @Query(value = "SELECT c FROM FinanceCategoryEntity c WHERE c.owner.id = ?1 ORDER BY c.id DESC")
    fun getCategoriesPageForUser(userId: Int, pageable: Pageable): Page<FinanceCategoryEntity>
}