package com.inventory.repository.specification;

import com.inventory.dto.request.ItemSearchCriteria;
import com.inventory.model.Item;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import static org.springframework.util.StringUtils.hasText;

public class ItemSpecification {

    private ItemSpecification() {
        // Utility class
    }

    @NonNull
    public static Specification<Item> withCriteria(ItemSearchCriteria criteria, @Nullable UUID userId) {
        return withCriteria(criteria, userId, null);
    }

    @NonNull
    public static Specification<Item> withCriteria(ItemSearchCriteria criteria, @Nullable UUID userId,
                                                    @Nullable java.util.Collection<UUID> workspaceIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Workspace-scope filter: filter by accessible workspaces
            if (workspaceIds != null && !workspaceIds.isEmpty()) {
                predicates.add(root.get("itemList").get("workspace").get("id").in(workspaceIds));
            } else if (userId != null) {
                // Fallback: user-scope filter for backward compatibility
                predicates.add(cb.equal(root.get("itemList").get("user").get("id"), userId));
            }

            if (criteria != null) {
                if (hasText(criteria.search())) {
                    String escaped = SpecificationUtils.escapeLikePattern(criteria.search().toLowerCase());
                    String pattern = "%" + escaped + "%";
                    char esc = SpecificationUtils.LIKE_ESCAPE_CHAR.charAt(0);
                    List<Predicate> searchPredicates = new ArrayList<>();
                    searchPredicates.add(cb.like(cb.lower(root.get("name")), pattern, esc));
                    searchPredicates.add(cb.like(cb.lower(root.get("barcode")), pattern, esc));
                    predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
                }

                if (criteria.itemListId() != null) {
                    predicates.add(cb.equal(root.get("itemList").get("id"), criteria.itemListId()));
                }

                if (criteria.status() != null) {
                    predicates.add(cb.equal(root.get("status"), criteria.status()));
                }

                if (hasText(criteria.barcode())) {
                    predicates.add(cb.equal(root.get("barcode"), criteria.barcode()));
                }
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
