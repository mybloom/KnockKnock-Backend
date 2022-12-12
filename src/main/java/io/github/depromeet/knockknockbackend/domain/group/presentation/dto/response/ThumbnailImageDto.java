package io.github.depromeet.knockknockbackend.domain.group.presentation.dto.response;

import io.github.depromeet.knockknockbackend.domain.group.domain.Thumbnail;
import lombok.Getter;

@Getter
public class ThumbnailImageDto {


    private Long thumbnailId;
    private String url;

    public ThumbnailImageDto(Thumbnail thumbnail) {
        this.thumbnailId = thumbnail.getId();
        this.url = thumbnail.getThumbnailImageUrl();
    }
}
