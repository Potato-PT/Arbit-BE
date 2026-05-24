package com.arbit.app.preference.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Preference category selection response")
public record PreferenceCategoriesResponse(
        @Schema(description = "Primary category groups")
        List<PreferenceCategoryGroup> keyword1,

        @Schema(description = "Mood keywords")
        List<String> keyword2,

        @Schema(description = "Audience keywords")
        List<String> keyword3
) {

    @Schema(description = "Primary category group")
    public record PreferenceCategoryGroup(
            @Schema(description = "Category name", example = "전시/미술")
            String category,

            @Schema(description = "Subcategory names")
            List<String> subcategories
    ) {
    }
}
