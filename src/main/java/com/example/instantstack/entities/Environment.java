package com.example.instantstack.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = "project")
@Builder
@Entity
public class Environment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int port;

    @Enumerated(EnumType.STRING) // שומר את הטקסט של ה-Enum ב-DB
    private Status status;

    @ManyToOne
    @JoinColumn(name = "project_id") // הגדרה מפורשת של עמודת הקשר
    @JsonIgnore
    private Project project;
    private Long workerId;

    @Builder.Default
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();//זמן יצירת הפרויקט

    public enum Status {
        PENDING,   // בהמתנה/יצירה
        STARTING,  // בתהליך עלייה בדוקר
        RUNNING,   // עובד ותקין
        STOPPED,   // כבוי
        ERROR     // תקלה בהפעלה
    }
}