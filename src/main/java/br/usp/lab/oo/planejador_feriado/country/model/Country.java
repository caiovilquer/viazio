package br.usp.lab.oo.planejador_feriado.country.model;

import java.util.List;

public class Country {
    private final String name; 
    private final String isoCode; 
    private final String region; 
    private final String subregion; 
    private final List<String> capitals; 
    private final List<String> languages; 
    private final List<String> currencies; 
    private final List<String> timezones;
    
    public Country(
            String name, String isoCode,
            String region, String subregion,
            List<String> capitals, List<String> languages,
            List<String> currencies, List<String> timezones
        ){ 
        this.name = name;
        this.isoCode = isoCode;
        this.region = region;
        this.subregion = subregion;
        this.capitals = capitals != null ? List.copyOf(capitals) : List.of();
        this.languages = languages != null ? List.copyOf(languages) : List.of();
        this.currencies = currencies != null ? List.copyOf(currencies) : List.of();
        this.timezones = timezones != null ? List.copyOf(timezones) : List.of();
    }

    public String getMainCurrency(){
        return this.currencies.isEmpty()? this.currencies.get(0) : null;
    }
    public String getMainLanguage(){
        return this.languages.isEmpty()? this.languages.get(0) : null;
    }

    @Override
    public String toString() {
        return "Country: " + this.name + " (" + this.isoCode + ")\n" +
               "Region: " + this.region + " - " + this.subregion + "\n" +
               "Capital(s): " + this.capitals + "\n" +
               "Language(s): " + this.languages + "\n" +
               "Currency(s): " + this.currencies + "\n" +
               "Timezones: " + this.timezones + "\n";
    }

    // Getters Default

    public String getName() {
        return name;
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
}
