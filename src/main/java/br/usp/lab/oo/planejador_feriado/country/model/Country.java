package br.usp.lab.oo.planejador_feriado.country.model;

import java.util.List;

public class Country {

  private static final int REGIONAL_INDICATOR_BASE = 0x1F1E6;

  private final String name;
  private final String localizedName;
  private final String isoCode;
  private final String region;
  private final String subregion;
  private final List<String> capitals;
  private final List<String> languages;
  private final List<String> currencies;
  private final List<String> timezones;
  private final Double latitude;
  private final Double longitude;
  private final boolean independent;
  private final boolean unMember;
  private final String status;

  public Country(
    String name,
    String isoCode,
    String region,
    String subregion,
    List<String> capitals,
    List<String> languages,
    List<String> currencies,
    List<String> timezones
  ) {
    this(
      name,
      null,
      isoCode,
      region,
      subregion,
      capitals,
      languages,
      currencies,
      timezones,
      null,
      null,
      true,
      false,
      null
    );
  }

  public Country(
    String name,
    String isoCode,
    String region,
    String subregion,
    List<String> capitals,
    List<String> languages,
    List<String> currencies,
    List<String> timezones,
    Double latitude,
    Double longitude
  ) {
    this(
      name,
      null,
      isoCode,
      region,
      subregion,
      capitals,
      languages,
      currencies,
      timezones,
      latitude,
      longitude,
      true,
      false,
      null
    );
  }

  public Country(
    String name,
    String localizedName,
    String isoCode,
    String region,
    String subregion,
    List<String> capitals,
    List<String> languages,
    List<String> currencies,
    List<String> timezones,
    Double latitude,
    Double longitude
  ) {
    this(
      name,
      localizedName,
      isoCode,
      region,
      subregion,
      capitals,
      languages,
      currencies,
      timezones,
      latitude,
      longitude,
      true,
      false,
      null
    );
  }

  public Country(
    String name,
    String localizedName,
    String isoCode,
    String region,
    String subregion,
    List<String> capitals,
    List<String> languages,
    List<String> currencies,
    List<String> timezones,
    Double latitude,
    Double longitude,
    boolean independent,
    boolean unMember,
    String status
  ) {
    this.name = name;
    this.localizedName = localizedName;
    this.isoCode = isoCode;
    this.region = region;
    this.subregion = subregion;
    this.capitals = capitals != null ? List.copyOf(capitals) : List.of();
    this.languages = languages != null ? List.copyOf(languages) : List.of();
    this.currencies = currencies != null ? List.copyOf(currencies) : List.of();
    this.timezones = timezones != null ? List.copyOf(timezones) : List.of();
    this.latitude = latitude;
    this.longitude = longitude;
    this.independent = independent;
    this.unMember = unMember;
    this.status = status;
  }

  public String getMainCurrency() {
    return this.currencies.isEmpty() ? null : this.currencies.get(0);
  }

  public String getMainLanguage() {
    return this.languages.isEmpty() ? null : this.languages.get(0);
  }

  /** Indica se o país tem coordenadas geográficas conhecidas (necessárias p/ clima e distância). */
  public boolean hasCoordinates() {
    return latitude != null && longitude != null;
  }

  /** Apenas Estados independentes entram automaticamente em rankings regionais. */
  public boolean isTravelEligible() {
    return independent;
  }

  /** Nome em português, quando disponível; cai para o nome padrão (inglês) senão. */
  public String getDisplayName() {
    return localizedName != null && !localizedName.isBlank()
      ? localizedName
      : name;
  }

  /**
   * Emoji de bandeira derivado do código ISO 3166-1 alpha-2, combinando os dois
   * "regional indicator symbols" Unicode correspondentes (ex.: "BR" → 🇧🇷). Evita
   * depender de mais um dado externo só para exibir a bandeira.
   */
  public String getFlagEmoji() {
    if (isoCode == null || isoCode.length() != 2) {
      return null;
    }
    String upper = isoCode.toUpperCase();
    int first = REGIONAL_INDICATOR_BASE + (upper.charAt(0) - 'A');
    int second = REGIONAL_INDICATOR_BASE + (upper.charAt(1) - 'A');
    if (
      first < REGIONAL_INDICATOR_BASE ||
      second < REGIONAL_INDICATOR_BASE ||
      first > REGIONAL_INDICATOR_BASE + 25 ||
      second > REGIONAL_INDICATOR_BASE + 25
    ) {
      return null;
    }
    return (
      new String(Character.toChars(first)) +
      new String(Character.toChars(second))
    );
  }

  @Override
  public String toString() {
    return (
      "Country: " +
      this.name +
      " (" +
      this.isoCode +
      ")\n" +
      "Region: " +
      this.region +
      " - " +
      this.subregion +
      "\n" +
      "Capital(s): " +
      this.capitals +
      "\n" +
      "Language(s): " +
      this.languages +
      "\n" +
      "Currency(s): " +
      this.currencies +
      "\n" +
      "Timezones: " +
      this.timezones +
      "\n"
    );
  }

  // Getters padrão

  public String getName() {
    return name;
  }

  public String getLocalizedName() {
    return localizedName;
  }

  public String getIsoCode() {
    return isoCode;
  }

  public String getRegion() {
    return region;
  }

  public String getSubregion() {
    return subregion;
  }

  public List<String> getCapitals() {
    return capitals;
  }

  public List<String> getLanguages() {
    return languages;
  }

  public List<String> getTimezones() {
    return timezones;
  }

  public List<String> getCurrencies() {
    return currencies;
  }

  public Double getLatitude() {
    return latitude;
  }

  public Double getLongitude() {
    return longitude;
  }

  public boolean isIndependent() {
    return independent;
  }

  public boolean isUnMember() {
    return unMember;
  }

  public String getStatus() {
    return status;
  }
}
