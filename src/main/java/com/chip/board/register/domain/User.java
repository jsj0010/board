package com.chip.board.register.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "`user`",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_user_boj_id", columnNames = {"boj_id", "is_deleted"}),
                @UniqueConstraint(name = "uk_user_username", columnNames = {"username", "is_deleted"}),
                @UniqueConstraint(name = "uk_user_student_id", columnNames = {"student_id", "is_deleted"}),
                @UniqueConstraint(name = "uk_user_phone_number", columnNames = {"phone_number", "is_deleted"})
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE user SET is_deleted = true WHERE user_id = ?")
@SQLRestriction("is_deleted = false")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String department;

    @Column(name = "student_id", nullable = false, length = 20)
    private String studentId;

    @Column(nullable = false)
    private int grade;

    @Column(length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private Role role;

    @Column(name = "boj_id", nullable = false, length = 64)
    private String bojId;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "is_deleted")
    private boolean deleted = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Builder
    private User(
            String username,
            String password,
            String name,
            String department,
            String studentId,
            int grade,
            Role role,
            String bojId,
            String phoneNumber

    ) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.department = department;
        this.studentId = studentId;
        this.grade = grade;
        this.role = role;
        this.bojId = bojId;
        this.phoneNumber = phoneNumber;
    }

    public void issuePassword(String password) {
        this.password = password;
    }

    public void onLoginSuccess() {
        this.lastLoginAt = LocalDateTime.now();
    }
}