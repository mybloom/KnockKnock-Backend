package io.github.depromeet.knockknockbackend.domain.asset.presentation;

import io.github.depromeet.knockknockbackend.domain.asset.presentation.dto.response.ProfileImagesResponse;
import io.github.depromeet.knockknockbackend.domain.asset.service.AssetService;
import io.github.depromeet.knockknockbackend.domain.asset.presentation.dto.response.BackgroundsResponse;
import io.github.depromeet.knockknockbackend.domain.asset.presentation.dto.response.ThumbnailsResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController("/asset")
public class AssetController {

    private final AssetService assetService;

    @Operation(summary = "백그라운드 이미지")
    @GetMapping("/backgrounds")
    public BackgroundsResponse getBackgroundImageUrls(){
        return assetService.getAllBackgroundImage();
    }

    @Operation(summary = "썸네일 이미지")
    @GetMapping("/thumbnails")
    public ThumbnailsResponse getThumbnailImageUrls(){
        return assetService.getAllThumbnailImage();
    }

    @Operation(summary = "프로필 이미지")
    @GetMapping("/profiles")
    public ProfileImagesResponse getAllProfileImages(){
        return assetService.getAllProfileImages();
    }


}
