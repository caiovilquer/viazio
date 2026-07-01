import { describe, expect, it } from "vitest";
import {
  buildSearchPageHref,
  criteriaToFormState,
  criteriaToSearchParams,
  searchParamsToCriteria,
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
});
