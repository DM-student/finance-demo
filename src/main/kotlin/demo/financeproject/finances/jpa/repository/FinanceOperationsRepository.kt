package demo.financeproject.finances.jpa.repository

import demo.financeproject.finances.jpa.entity.FinanceOperationEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.ZonedDateTime

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
        value = """
            SELECT f 
            FROM FinanceOperationEntity f
            WHERE f.owner.id = ?2 
            AND 
            (
            LOWER(f.title) LIKE ?1
            OR 
            LOWER(f.description) LIKE ?1 
            ) 
            ORDER BY f.operationTimestamp DESC
            """
    )
    fun getOperationsPageForSearchSortByDate(
        searchQuery: String,
        userId: Int,
        pageable: Pageable
    ): Page<FinanceOperationEntity>

    @Query(
        value = """
            SELECT f 
            FROM FinanceOperationEntity f 
            WHERE f.owner.id = ?2 
            AND 
            ( 
            LOWER(f.title) LIKE ?1 
            OR 
            LOWER(f.description) LIKE ?1
            ) 
            ORDER BY f.amount DESC
            """
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