package com.inventory.repository.specification;

import com.inventory.model.ItemList;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

public class ItemListSpecification {

    private ItemListSpecification() {
        // Utility class
    }

    @NonNull
    public static Specification<ItemList> withCriteria(@Nullable String search, @Nullable String category, @Nullable UUID ownerId) {
        return withCriteria(search, category, ownerId, null);
    }

    @NonNull
    public static Specification<ItemList> withCriteria(@Nullable String search, @Nullable String category,
                                                       @Nullable UUID ownerId, @Nullable Collection<UUID> workspaceIds) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Workspace-scope filter
            if (workspaceIds != null && !workspaceIds.isEmpty()) {
                predicates.add(root.get("workspace").get("id").in(workspaceIds));
            } else if (ownerId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), ownerId));
            }

            if (hasText(search)) {
                @SuppressWarnings("null")
                String escaped = SpecificationUtils.escapeLikePattern(search.toLowerCase());
                String pattern = "%" + escaped + "%";
                char esc = SpecificationUtils.LIKE_ESCAPE_CHAR.charAt(0);
                List<Predicate> searchPredicates = new ArrayList<>();
                searchPredicates.add(cb.like(cb.lower(root.get("name")), pattern, esc));
                searchPredicates.add(cb.like(cb.lower(root.get("description")), pattern, esc));
                searchPredicates.add(cb.like(cb.lower(root.get("category")), pattern, esc));
                predicates.add(cb.or(searchPredicates.toArray(new Predicate[0])));
            }

            if (hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
