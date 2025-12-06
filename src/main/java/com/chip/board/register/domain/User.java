package com.chip.board.register.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "user")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLDelete(sql = "UPDATE user SET is_deleted = true WHERE user_id = ?")
@SQLRestriction("is_deleted = false")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(length = 100)
    private String department;

    @Column(name = "student_id", length = 20)
    private String studentId;

    @Column(nullable = false)
    private int grade;

    @Column(length = 50)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private Role role;

    @Column(name = "phone_number", length = 20)
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
            String phoneNumber

    ) {
        this.username = username;
        this.password = password;
        this.name = name;
        this.department = department;
        this.studentId = studentId;
        this.grade = grade;
        this.role = role;
        this.phoneNumber = phoneNumber;
    }

    public void issuePassword(String password) {
        this.password = password;
    }

    public void onLoginSuccess() {
        this.lastLoginAt = LocalDateTime.now();
    }
}