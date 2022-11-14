package io.github.depromeet.knockknockbackend.domain.group.domain.repository;

import io.github.depromeet.knockknockbackend.domain.group.domain.GroupCategory;
import org.springframework.data.repository.CrudRepository;

public interface GroupRepository extends CrudRepository<GroupCategory, Long> {
}
