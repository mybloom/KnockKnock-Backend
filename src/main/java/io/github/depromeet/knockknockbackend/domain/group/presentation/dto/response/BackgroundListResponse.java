package io.github.depromeet.knockknockbackend.domain.group.presentation.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class BackgroundListResponse {
    List<BackgroundImageDto> backgroundList ;

}