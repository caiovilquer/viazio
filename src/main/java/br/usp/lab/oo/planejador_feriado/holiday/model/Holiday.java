package br.usp.lab.oo.planejador_feriado.holiday.model;

import java.time.LocalDate;
import java.util.List;

public class Holiday {
    private final LocalDate date;
    private final String name;
    private final String localName;
    private final List<String> types;

    public Holiday (LocalDate date, String name, String localName, List<String> types){
        this.date = date;
        this.name = name;
        this.localName = localName;
        this.types = types != null ? List.copyOf(types) : List.of();
    }

    public boolean isPublicHoliday() {
        return types.contains("Public");
    }

    @Override
    public String toString() {
        return String.format(
                "%s (%s) - %s [%s]",
                name,
                localName,
                date,
                types
        );
    }

    // getters default

    public LocalDate getDate() {
        return date;
    }
    public String getName() {
        return name;
    }
    public String getLocalName() {
        return localName;
    }
    public List<String> getTypes() {
        return types;
    }
}