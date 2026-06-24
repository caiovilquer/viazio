package br.usp.lab.oo.planejador_feriado.country.model;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class CountryTest {

    private Country countryWith(String name, String localizedName, String isoCode) {
        return new Country(name, localizedName, isoCode, "Americas", "South America",
                List.of("Brasília"), List.of("Portuguese"), List.of("BRL"), List.of("UTC-03:00"), -10.0, -55.0);
    }

    @Test
    void getDisplayNameShouldPreferLocalizedNameWhenPresent() {
        Country country = countryWith("Brazil", "Brasil", "BR");

        assertEquals("Brasil", country.getDisplayName());
    }

    @Test
    void getDisplayNameShouldFallBackToNameWhenLocalizedNameIsNull() {
        Country country = countryWith("Brazil", null, "BR");

        assertEquals("Brazil", country.getDisplayName());
    }

    @Test
    void getDisplayNameShouldFallBackToNameWhenLocalizedNameIsBlank() {
        Country country = countryWith("Brazil", "   ", "BR");

        assertEquals("Brazil", country.getDisplayName());
    }

    @Test
    void getFlagEmojiShouldCombineRegionalIndicatorSymbols() {
        Country brazil = countryWith("Brazil", "Brasil", "BR");
        Country japan = countryWith("Japan", null, "JP");

        assertEquals("🇧🇷", brazil.getFlagEmoji());
        assertEquals("🇯🇵", japan.getFlagEmoji());
    }

    @Test
    void getFlagEmojiShouldBeNullWhenIsoCodeIsNull() {
        Country country = countryWith("Nowhere", null, null);

        assertNull(country.getFlagEmoji());
    }

    @Test
    void getFlagEmojiShouldBeNullWhenIsoCodeIsNotTwoLetters() {
        Country country = countryWith("Nowhere", null, "XYZ");

        assertNull(country.getFlagEmoji());
    }

    @Test
    void getFlagEmojiShouldBeNullWhenIsoCodeHasNonLetterCharacters() {
        Country country = countryWith("Nowhere", null, "1A");

        assertNull(country.getFlagEmoji());
    }

    @Test
    void getFlagEmojiShouldBeCaseInsensitive() {
        Country country = countryWith("Brazil", "Brasil", "br");

        assertEquals("🇧🇷", country.getFlagEmoji());
    }
}
