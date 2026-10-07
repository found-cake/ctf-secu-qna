package kr.foundcake.secu_qna.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_accounts", uniqueConstraints = @UniqueConstraint(columnNames = "username"))
public class UserAccount {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 20)
    private String username;
    @Column(nullable = false, name = "password_hash", length = 60)
    private String passwordHash;
    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();
    public UserAccount(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
    }
}
