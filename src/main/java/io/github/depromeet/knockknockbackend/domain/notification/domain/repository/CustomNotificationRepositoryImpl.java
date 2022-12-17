package io.github.depromeet.knockknockbackend.domain.notification.domain.repository;

import static io.github.depromeet.knockknockbackend.domain.notification.domain.QNotification.notification;
import static io.github.depromeet.knockknockbackend.domain.storage.domain.QStorage.storage;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import io.github.depromeet.knockknockbackend.domain.notification.domain.Notification;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class CustomNotificationRepositoryImpl implements CustomNotificationRepository {

    private static final long NEXT_SLICE_CHECK = 1;
    private final JPAQueryFactory queryFactory;

    private boolean hasNext(List<Notification> notifications, Pageable pageable) {
        boolean hasNext = false;
        if (notifications.size() > pageable.getPageSize()) {
            notifications.remove(pageable.getPageSize());
            hasNext = true;
        }
        return hasNext;
    }

    @Override
    public Slice<Notification> findSliceFromStorage(Long userId, Long groupId, Integer periodOfMonth, Pageable pageable) {
        List<Notification> notifications = queryFactory
            .select(notification)
            .from(storage)
            .innerJoin(storage.notification, notification)
            .where(storage.user.id.eq(userId),
                eqGroupId(groupId),
                greaterEqualPeriodOfMonth(periodOfMonth)
            )
            .orderBy(sort(pageable))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize() + NEXT_SLICE_CHECK)
            .fetch();

        return new SliceImpl<>(notifications, pageable, hasNext(notifications, pageable));
    }

    private BooleanExpression eqGroupId(Long groupId) {
        if (groupId == null) {
            return null;
        }
        return notification.group.id.eq(groupId);
    }

    private BooleanExpression greaterEqualPeriodOfMonth(Integer periodOfMonth) {
        if (periodOfMonth == null) {
            return null;
        }
        LocalDate dateCondition = LocalDate.now().minusMonths(periodOfMonth);
        return storage.createdDate.goe(dateCondition.atStartOfDay());
    }

    private OrderSpecifier<?> sort(Pageable pageable) {
        OrderSpecifier<?> orderSpecifier = null;

        Sort sort = pageable.getSort();
        if (sort.isEmpty()) {
            return null;
        }

        for (Order order : sort) {
            com.querydsl.core.types.Order direction =
                order.getDirection().isAscending() ?
                    com.querydsl.core.types.Order.ASC : com.querydsl.core.types.Order.DESC;
            switch (order.getProperty()) {
                case "createdDate":
                    orderSpecifier = new OrderSpecifier<>(direction, storage.createdDate);
                    break;
                default:
                    break;
            }
        }

        return orderSpecifier;
    }

}