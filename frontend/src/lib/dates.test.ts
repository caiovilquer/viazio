import { describe, expect, it, vi, afterEach } from "vitest";
import { todayIso, toLocalIsoDate } from "./dates";

describe("dates", () => {
  afterEach(() => {
    vi.useRealTimers();
  });

  it("formata data local sem deslocamento UTC", () => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date(2026, 5, 27, 22, 30, 0));

    expect(todayIso()).toBe("2026-06-27");
    expect(todayIso(3)).toBe("2026-06-30");
    expect(toLocalIsoDate(new Date(2026, 0, 5))).toBe("2026-01-05");
  });
});
