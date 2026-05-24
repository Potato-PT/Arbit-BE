package com.arbit.app.preference.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

@Schema(description = "Choose-preferences request")
public record CreatePreferenceRequest(
        @NotNull
        @NotEmpty
        @Valid
        @Schema(description = "Selected categories and subcategories for choose preferences")
        List<Keyword1Item> keyword1,

        @NotNull
        @Schema(description = "Selected mood keywords", example = "[\"가족친화\", \"학술/사색적\"]")
        List<@NotBlank @Size(max = 50) String> keyword2,

        @NotNull
        @Schema(description = "Selected age keywords", example = "[\"전 연령\", \"태그 없음\"]")
        List<@NotBlank @Size(max = 50) String> keyword3,

        @NotNull
        @Schema(description = "Free-text preference keywords", example = "[\"블라블라\"]")
        List<@NotBlank @Size(max = 50) String> keyword4
) {

    @Schema(description = "Choose-preferences category selection item")
    public record Keyword1Item(
            @NotBlank
            @Size(max = 50)
            @Schema(description = "Top-level category", example = "전시/미술")
            String category,

            @NotNull
            @Schema(description = "Selected subcategories", example = "[\"개인전/초대전\", \"기획/테마 전시\"]")
            List<@NotBlank @Size(max = 50) String> subcategories
    ) {
    }
}
