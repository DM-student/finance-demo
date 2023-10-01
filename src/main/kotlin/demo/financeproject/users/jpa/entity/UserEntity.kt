package demo.financeproject.users.jpa.entity

import jakarta.persistence.*

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