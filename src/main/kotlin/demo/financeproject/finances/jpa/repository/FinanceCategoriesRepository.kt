package demo.financeproject.finances.jpa.repository

import demo.financeproject.finances.jpa.entity.FinanceCategoryEntity
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface FinanceCategoriesRepository : JpaRepository<FinanceCategoryEntity, Int> {
    @Query(value = "SELECT c FROM FinanceCategoryEntity c WHERE c.owner.id = ?1 ORDER BY c.id DESC")
    fun getCategoriesPageForUser(userId: Int, pageable: Pageable): Page<FinanceCategoryEntity>
}