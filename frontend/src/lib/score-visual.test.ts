import { describe, expect, it } from "vitest";
import { scoreTierColor, scoreTone } from "./score-visual";

describe("score-visual", () => {
  it("classifica faixas de nota", () => {
    expect(scoreTone(85)).toBe("excellent");
    expect(scoreTone(70)).toBe("good");
    expect(scoreTone(50)).toBe("fair");
    expect(scoreTone(20)).toBe("poor");
  });

  it("expõe cores por faixa", () => {
    expect(scoreTierColor.excellent).toBe("var(--gold)");
    expect(scoreTierColor.poor).toBe("var(--chart-5)");
  });
});
