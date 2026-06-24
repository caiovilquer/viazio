package br.usp.lab.oo.planejador_feriado.holiday.model;

import java.time.LocalDate;
import java.util.List;

public class Holiday {
    private final LocalDate date;
    private final String name;
    private final String localName;
    private final List<String> types;
    private final boolean global;
    private final List<String> subdivisions;

    public Holiday (LocalDate date, String name, String localName, List<String> types){
        this(date, name, localName, types, true, List.of());
    }

    public Holiday(
            LocalDate date,
            String name,
            String localName,
            List<String> types,
            boolean global,
            List<String> subdivisions) {
        this.date = date;
        this.name = name;
        this.localName = localName;
        this.types = types != null ? List.copyOf(types) : List.of();
        this.global = global;
        this.subdivisions = subdivisions != null ? List.copyOf(subdivisions) : List.of();
    }

    public boolean isPublicHoliday() {
        return types.contains("Public");
    }

    /**
     * Sem subdivisão informada, somente feriados nacionais são aplicáveis.
     * Quando há subdivisão, entram os nacionais e os explicitamente associados a ela.
     */
    public boolean appliesTo(String subdivisionCode) {
        if (global) {
            return true;
        }
        if (subdivisionCode == null || subdivisionCode.isBlank()) {
            return false;
        }
        return subdivisions.stream().anyMatch(subdivisionCode::equalsIgnoreCase);
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
    public boolean isGlobal() {
        return global;
    }
    public List<String> getSubdivisions() {
        return subdivisions;
    }
}
