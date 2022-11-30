package io.github.depromeet.knockknockbackend.domain.group.presentation;

import io.github.depromeet.knockknockbackend.domain.group.presentation.dto.request.CreateCategoryRequest;
import io.github.depromeet.knockknockbackend.domain.group.presentation.dto.request.CreateFriendGroupRequest;
import io.github.depromeet.knockknockbackend.domain.group.presentation.dto.request.CreateOpenGroupRequest;
import io.github.depromeet.knockknockbackend.domain.group.presentation.dto.request.GroupInTypeRequest;
import io.github.depromeet.knockknockbackend.domain.group.presentation.dto.request.UpdateGroupRequest;
import io.github.depromeet.knockknockbackend.domain.group.presentation.dto.response.CategoryDto;
import io.github.depromeet.knockknockbackend.domain.group.presentation.dto.response.CategoryListResponse;
import io.github.depromeet.knockknockbackend.domain.group.presentation.dto.response.CreateGroupResponse;
import io.github.depromeet.knockknockbackend.domain.group.presentation.dto.response.GroupBriefInfoListResponse;
import io.github.depromeet.knockknockbackend.domain.group.presentation.dto.response.GroupResponse;
import io.github.depromeet.knockknockbackend.domain.group.service.CategoryService;
import io.github.depromeet.knockknockbackend.domain.group.service.GroupService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
@Tag(name = "그룹 관련 컨트롤러", description = "")
@SecurityRequirement(name = "access-token")
public class GroupController {


    private final GroupService groupService;

    private final CategoryService categoryService;

    @Operation(summary = "공개 그룹을 만듭니다")
    @PostMapping("/open")
    public CreateGroupResponse createOpenGroup(@Valid @RequestBody CreateOpenGroupRequest createOpenGroupRequest){
        return this.groupService.createOpenGroup(createOpenGroupRequest);
    }

    @Operation(summary = "친구 그룹을 만듭니다")
    @PostMapping("/friend")
    public CreateGroupResponse createFriendGroup(@Valid @RequestBody CreateFriendGroupRequest createFriendGroupRequest){
        return this.groupService.createFriendGroup(createFriendGroupRequest);
    }

    @Operation(summary = "방장 권한 그룹 설정")
    @PutMapping("/{id}")
    public GroupResponse putGroup(@PathVariable("id") Long groupId ,@Valid @RequestBody UpdateGroupRequest updateGroupRequest){

        return groupService.updateGroup(groupId,updateGroupRequest);
    }

    @Operation(summary = "방장 권한 그룹 제거")
    @DeleteMapping("/{id}")
    public void deleteGroup(@PathVariable("id") Long groupId){
        groupService.deleteGroup(groupId);
    }


    @GetMapping("/{id}")
    public GroupResponse getGroupDetail(@PathVariable("id") Long groupId){
        return groupService.getGroupDetailById(groupId);
    }


    @Parameter(name = "type", description = "type", schema = @Schema(implementation = GroupInTypeRequest.class)
        , in = ParameterIn.QUERY)
    @Operation(summary = "참여중인 그룹 목록 전체 홀로외침 친구들 방 필터링")
    @GetMapping("/joined")
    public GroupBriefInfoListResponse getParticipatingGroups(
        @RequestParam("type") GroupInTypeRequest groupInTypeRequest){
        if(groupInTypeRequest == GroupInTypeRequest.ALL){
            return groupService.findAllJoinedGroups();
        }
        return groupService.findJoinedGroupByType(groupInTypeRequest);
    }


    @Parameter(name = "category", description = "category", schema = @Schema(implementation = Long.class)
        , in = ParameterIn.QUERY)
    @Operation(summary = "방 찾기")
    @GetMapping("/open")
    public GroupBriefInfoListResponse getAllOpenGroups(@RequestParam(value = "category" ,required = false  ) Long categoryId) {

        return groupService.findOpenGroupByCategory(categoryId);
    }

    @GetMapping("/categories")
    public CategoryListResponse getCategory(){
        return categoryService.findAllCategory();
    }

    //TODO : 관리자권한 필요
    @PostMapping("/categories")
    public CategoryDto createCategory(@RequestBody @Valid CreateCategoryRequest createCategoryRequest){
        return categoryService.saveCategory(createCategoryRequest);
    }

    @Parameter(name = "searchString", description = "searchString", schema = @Schema(implementation = String.class)
        , in = ParameterIn.QUERY)
    @Operation(summary = "방 검색하기")
    @GetMapping("/search/{searchString}")
    public GroupBriefInfoListResponse searchGroups(@PathVariable(value = "searchString") String searchString){
        return groupService.searchGroups(searchString);
    }

}
