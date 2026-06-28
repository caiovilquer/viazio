package br.usp.lab.oo.planejador_feriado.meta.dto;

import java.util.List;
import java.util.Map;

public record ApiMetaResponse(
  String apiVersion,
  String catalogVersion,
  ApiLimits limits,
  List<RegionOption> regions,
  List<CriterionOption> criteria,
  List<ProfileOption> profiles,
  List<CountryOption> countries,
  List<DataSourceInfo> dataSources,
  Map<String, Boolean> capabilities
) {
  public ApiMetaResponse {
    regions = List.copyOf(regions);
    criteria = List.copyOf(criteria);
    profiles = List.copyOf(profiles);
    countries = List.copyOf(countries);
    dataSources = List.copyOf(dataSources);
    capabilities = Map.copyOf(capabilities);
  }
}
