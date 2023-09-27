package demo.financeproject.users

import jakarta.persistence.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*


@Entity
@Table(name = "users")
class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(
        name = "login",
        nullable = false
    )
    var login: String? = null

    @Column(
        name = "password",
        nullable = false
    )
    var password: String? = null

    @Column(
        name = "login_token",
        nullable = true
    )
    var token: String? = null

    @Column(
        name = "status",
        nullable = false
    )
    @Enumerated(EnumType.STRING)
    var status: UserEntityStatus? = null

    @Column(
        name = "status_reason",
        nullable = true
    )
    var statusReason: String? = null
}

enum class UserEntityStatus(val status_text: String) {
    ACTIVE("ACTIVE"),
    AWAITS_ACTIVATION("AWAITS ACTIVATION"),
    BLOCKED("BLOCKED")
}

interface UsersRepository : JpaRepository<UserEntity, Int> {
    @Query(value = "SELECT u FROM UserEntity u WHERE u.status = ?1 ORDER BY u.id DESC")
    fun pageUsersForStatusWithPagination(status: UserEntityStatus, pageable: Pageable): Page<UserEntity>

    @Query(value = "SELECT u FROM UserEntity u WHERE u.login = ?1")
    fun findUserByLogin(login: String): Optional<UserEntity>

    @Query(value = "SELECT u FROM UserEntity u WHERE u.token = ?1")
    fun findUserByToken(login: String): Optional<UserEntity>
}