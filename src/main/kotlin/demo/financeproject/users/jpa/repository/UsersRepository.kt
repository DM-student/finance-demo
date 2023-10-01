package demo.financeproject.users.jpa.repository

import demo.financeproject.users.jpa.entity.UserEntity
import demo.financeproject.users.jpa.entity.UserEntityStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface UsersRepository : JpaRepository<UserEntity, Int> {
    @Query(value = "SELECT u FROM UserEntity u WHERE u.status = ?1 ORDER BY u.id DESC")
    fun pageUsersForStatusWithPagination(status: UserEntityStatus, pageable: Pageable): Page<UserEntity>

    @Query(value = "SELECT u FROM UserEntity u WHERE u.login = ?1")
    fun findUserByLogin(login: String): Optional<UserEntity>

    @Query(value = "SELECT u FROM UserEntity u WHERE u.token = ?1")
    fun findUserByToken(login: String): Optional<UserEntity>
}