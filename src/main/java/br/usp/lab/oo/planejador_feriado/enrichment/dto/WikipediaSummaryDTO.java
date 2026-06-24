package br.usp.lab.oo.planejador_feriado.enrichment.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record WikipediaSummaryDTO(
        String description,
        String extract,
        ImageDTO thumbnail,
        ImageDTO originalimage,
        @JsonProperty("content_urls") ContentUrlsDTO contentUrls
) {
    public record ImageDTO(String source) {}
    public record ContentUrlsDTO(DesktopDTO desktop) {}
    public record DesktopDTO(String page) {}
}
