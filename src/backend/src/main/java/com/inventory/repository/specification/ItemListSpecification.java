package com.inventory.repository.specification;

import com.inventory.model.ItemList;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.springframework.util.StringUtils.hasText;

public class ItemListSpecification {

    private static final String LIKE_ESCAPE_CHAR = "\\";

    private ItemListSpecification() {
        // Utility class
    }

    @NonNull
    public static Specification<ItemList> withCriteria(@Nullable String search, @Nullable String category, @Nullable UUID ownerId) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (hasText(search)) {
                @SuppressWarnings("null")
                String escaped = escapeLikePattern(search.toLowerCase());
                String pattern = "%" + escaped + "%";
                predicates.add(cb.like(cb.lower(root.get("name")), pattern, LIKE_ESCAPE_CHAR.charAt(0)));
            }

            if (hasText(category)) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            if (ownerId != null) {
                predicates.add(cb.equal(root.get("user").get("id"), ownerId));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }

    private static String escapeLikePattern(String input) {
        return input
                .replace(LIKE_ESCAPE_CHAR, LIKE_ESCAPE_CHAR + LIKE_ESCAPE_CHAR)
                .replace("%", LIKE_ESCAPE_CHAR + "%")
                .replace("_", LIKE_ESCAPE_CHAR + "_");
    }
}
