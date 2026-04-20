package br.usp.lab.oo.planejador_feriado.country.model;

import java.util.List;

public class Country {
    private final String name; 
    private final String isoCode; 
    private final String region; 
    private final String subregion; 
    private final List<String> capitals; 
    private final List<String> languages; 
    private final List<String> timezones; 
    private final List<String> currencies; 
    
    public Country(
            String name, String isoCode, String region,
            String subregion, List<String> capitals, 
            List<String> languages, List<String> timezones,
            List<String> currencies
        ){ 
        this.name = name;
        this.isoCode = isoCode;
        this.region = region;
        this.subregion = subregion;
        this.capitals = List.copyOf(capitals);
        this.languages = List.copyOf(languages);
        this.timezones = List.copyOf(timezones);
        this.currencies = List.copyOf(currencies);
    }

    public String getMainCurrency(){
        return (this.currencies != null && !this.currencies.isEmpty())? this.currencies.get(0) : null;
    }
    public String getMainLanguage(){
        return (this.languages != null && !this.languages.isEmpty())? this.languages.get(0) : null;
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
