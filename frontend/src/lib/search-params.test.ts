import { describe, expect, it } from "vitest";
import {
  buildJanelasPageHref,
  buildSearchPageHref,
  criteriaToFormState,
  criteriaToSearchParams,
  janelasCriteriaToQuery,
  janelasCriteriaToSearchParams,
  searchParamsToCriteria,
  searchParamsToJanelasCriteria,
  type JanelasCriteria,
  type SearchCriteria,
} from "./search-params";

const baseCriteria: SearchCriteria = {
  from: "2026-09-04",
  to: "2026-09-07",
  countries: ["AR", "CL"],
  region: null,
  profile: "economico",
  weights: { weather: 0.4 },
  travelers: 2,
  origin: { countryCode: "BR", city: "São Paulo" },
  maxGroundBudgetBrl: 5000,
};

describe("search-params", () => {
  it("serializa e desserializa critérios de busca", () => {
    const params = criteriaToSearchParams(baseCriteria);
    expect(params.get("from")).toBe("2026-09-04");
    expect(params.get("countries")).toBe("AR,CL");
    expect(params.get("profile")).toBe("economico");
    expect(params.get("weights")).toBe("weather:0.4");
    expect(params.get("travelers")).toBe("2");
    expect(params.get("originCity")).toBe("São Paulo");
    expect(params.get("maxBudget")).toBe("5000");

    const parsed = searchParamsToCriteria(params);
    expect(parsed).toEqual(baseCriteria);
  });

  it("retorna null quando datas obrigatórias faltam", () => {
    expect(searchParamsToCriteria(new URLSearchParams("from=2026-01-01"))).toBe(
      null,
    );
  });

  it("converte critérios para estado do formulário de busca", () => {
    const params = criteriaToSearchParams({
      ...baseCriteria,
      profile: null,
      weights: { weather: 0.5, cost: 0.2, distance: 0.2, festivities: 0.1 },
    });
    const criteria = searchParamsToCriteria(params)!;
    const form = criteriaToFormState(criteria);

    expect(form.from).toBe("2026-09-04");
    expect(form.countries).toEqual(["AR", "CL"]);
    expect(form.customWeights).toBe(true);
    expect(form.profile).toBeNull();
    expect(form.weights.weather).toBe(0.5);
    expect(form.travelers).toBe(2);
    expect(form.maxBudget).toBe("5000");
    expect(form.originCountry).toBe("BR");
  });

  it("monta link de ajustar busca com query string", () => {
    const params = criteriaToSearchParams(baseCriteria);
    expect(buildSearchPageHref(params)).toBe(`/buscar?${params.toString()}`);
    expect(buildSearchPageHref(new URLSearchParams())).toBe("/buscar");
  });

  it("serializa e desserializa critérios de janelas", () => {
    const janelasCriteria: JanelasCriteria = {
      ...baseCriteria,
      minDays: 5,
      region: "europa",
      countries: [],
    };
    const params = janelasCriteriaToSearchParams(janelasCriteria);
    expect(params.get("minDays")).toBe("5");
    expect(params.get("region")).toBe("europa");

    const parsed = searchParamsToJanelasCriteria(params);
    expect(parsed).toEqual(janelasCriteria);

    const query = janelasCriteriaToQuery(janelasCriteria);
    expect(query.minDays).toBe(5);
    expect(query.region).toBe("europa");
    expect(query.countries).toBeUndefined();
  });

  it("monta link de janelas com query string", () => {
    const params = janelasCriteriaToSearchParams({
      ...baseCriteria,
      minDays: 3,
    });
    expect(buildJanelasPageHref(params)).toBe(`/janelas?${params.toString()}`);
    expect(buildJanelasPageHref(new URLSearchParams())).toBe("/janelas");
  });

  it("retorna null para janelas sem destino", () => {
    const params = criteriaToSearchParams({
      ...baseCriteria,
      countries: [],
      region: null,
    });
    params.set("minDays", "3");
    expect(searchParamsToJanelasCriteria(params)).toBeNull();
  });
});
