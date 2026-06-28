import { describe, expect, it } from "vitest";
import {
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
});
