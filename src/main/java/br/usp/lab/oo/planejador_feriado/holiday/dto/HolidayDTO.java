package br.usp.lab.oo.planejador_feriado.holiday.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.util.List;

public record HolidayDTO(
  @JsonFormat(pattern = "yyyy-MM-dd") LocalDate date,

  String name,
  String localName,
  List<String> types,
  boolean global,
  List<String> counties
) {
  public HolidayDTO(
    LocalDate date,
    String name,
    String localName,
    List<String> types
  ) {
    this(date, name, localName, types, true, List.of());
  }
}
