package com.darro_tech.revengproject.models;

import java.time.Instant;

import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Mapping for DB view email_logs_view
 */
@Entity
@Immutable
@Table(name = "email_logs_view")
public class EmailLogView {

    @Id
    @Column(name = "id", nullable = false, length = 36)
    private String id;

    @Column(name = "mail_from", nullable = false)
    private String mailFrom;

    @Column(name = "mail_to", nullable = false)
    private String mailTo;

    @Column(name = "subject")
    private String subject;

    @Column(name = "attached_files")
    @JdbcTypeCode(SqlTypes.JSON)
    private String attachedFiles;

    @Column(name = "mail_type")
    private String mailType;

    @Column(name = "company_id")
    private String companyId;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "status", nullable = false)
    private String status; // 'pending', 'sent', 'failed'

    @Column(name = "message_id")
    private String messageId;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "sent_at")
    private Instant sentAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @Column(name = "company_name")
    private String companyName;

    @Column(name = "company_display_name")
    private String companyDisplayName;

    @Column(name = "username")
    private String username;

    @Column(name = "firstName")
    private String firstName;

    @Column(name = "lastName")
    private String lastName;

    // Getters
    public String getId() {
        return id;
    }

    public String getMailFrom() {
        return mailFrom;
    }

    public String getMailTo() {
        return mailTo;
    }

    public String getSubject() {
        return subject;
    }

    public String getAttachedFiles() {
        return attachedFiles;
    }

    public String getMailType() {
        return mailType;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getUserId() {
        return userId;
    }

    public String getStatus() {
        return status;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getSentAt() {
        return sentAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public String getCompanyName() {
        return companyName;
    }

    public String getCompanyDisplayName() {
        return companyDisplayName;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    protected EmailLogView() {
    }
}



