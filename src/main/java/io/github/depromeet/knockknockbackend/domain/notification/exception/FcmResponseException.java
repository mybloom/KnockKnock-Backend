package io.github.depromeet.knockknockbackend.domain.notification.exception;

import io.github.depromeet.knockknockbackend.global.error.exception.ErrorCode;
import io.github.depromeet.knockknockbackend.global.error.exception.KnockException;

public class FcmResponseException extends KnockException {
    public static final KnockException EXCEPTION = new FcmResponseException();

    private FcmResponseException() {
        super(ErrorCode.NOT_GROUP_MEMBER);
    }
}
