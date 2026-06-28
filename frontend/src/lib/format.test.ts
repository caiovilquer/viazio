import { describe, expect, it } from "vitest";
import { pluralize, formatDateRange } from "./format";

describe("format", () => {
  it("pluraliza corretamente em pt-BR", () => {
    expect(pluralize(1, "dia", "dias")).toBe("1 dia");
    expect(pluralize(3, "dia", "dias")).toBe("3 dias");
  });

  it("formata intervalo de datas", () => {
    expect(formatDateRange("2026-06-01", "2026-06-07")).toMatch(/01.*07/);
  });
});
