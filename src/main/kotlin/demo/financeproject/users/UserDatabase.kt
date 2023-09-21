package demo.financeproject.users

import jakarta.persistence.*
import lombok.Data
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.Optional


@Data
@Entity
@Table(name = "users")
class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Int? = null

    @Column(name = "login",
            nullable = false)
    var login: String? = null

    @Column(name = "password",
            nullable = false)
    var password: String? = null

    @Column(name = "login_token",
            nullable = true)
    var token: String? = null

    @Column(name = "status",
        nullable = false)
    @Enumerated(EnumType.STRING)
    var status: UserEntityStatus? = null

    @Column(name = "status_reason",
        nullable = true)
    var statusReason: String? = null
}

enum class UserEntityStatus(val status: String) {
    ACTIVE ("ACTIVE"),
    NOT_ACTIVE("NOT ACTIVE"),
    BLOCKED("BLOCKED")
}

interface UserRepository : JpaRepository<UserEntity, Int> {
    @Query (value = "SELECT u FROM UserEntity u WHERE u.status = ?1 ORDER BY id")
    fun findAllUsersForStatusWithPagination(status: UserEntityStatus, pageable: Pageable): Page<UserEntity>

    @Query (value = "SELECT u FROM UserEntity u WHERE u.login = ?1 ORDER BY u.id")
    fun findUserByLogin(login: String): Optional<UserEntity>
}