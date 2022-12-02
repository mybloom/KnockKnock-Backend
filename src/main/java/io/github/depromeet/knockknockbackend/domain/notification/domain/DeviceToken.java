package io.github.depromeet.knockknockbackend.domain.notification.domain;

import io.github.depromeet.knockknockbackend.global.database.BaseTimeEntity;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "tbl_device_token")
@Entity
public class DeviceToken extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;

    private String deviceId;

    private String token;


    public static DeviceToken of(Long userId, String deviceId, String deviceToken) {
        return DeviceToken.builder()
            .userId(userId)
            .deviceId(deviceId)
            .token(deviceToken)
            .build();
    }

    public DeviceToken changeToken(String token) {
        this.token = token;
        return this;
    }
}
