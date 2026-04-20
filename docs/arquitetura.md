# Arquitetura e Modelagem do Sistema

## Testes para UML com mermaid

```mermaid

classDiagram
    class Country{
        + String name
        + String isoCode
        + String region
        + String subregion
        + String[] currencies
        + String[] languages
        + String[] timezones
        + String[] capitals
    }

    class Holiday{
        + LocalDate date
        + String name
        + String localName
        + String types
    }

    class Exchange{
        + String currency
        + double valueInReais
    }

    class TravelSearchEngine{
        + searchInfoCountry(String isoCode) Country
        + searchHolidays(String isoCode) List~Holiday~
        + searchCurrency(String currency) Exchange
    }
```

