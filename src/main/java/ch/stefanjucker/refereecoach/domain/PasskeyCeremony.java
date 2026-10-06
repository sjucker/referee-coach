package ch.stefanjucker.refereecoach.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

/**
 * Pending WebAuthn registration or login, i.e., the challenge sent to the browser that must be verified on finish.
 */
@Entity
@Table(name = "passkey_ceremony")
@Getter
@Setter
@ToString(of = {"id", "userId", "createdAt"})
@AllArgsConstructor
@NoArgsConstructor
public class PasskeyCeremony {

    @Id
    private String id;

    @Column(name = "request_json", nullable = false)
    private String requestJson;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
