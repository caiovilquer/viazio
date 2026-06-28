import { describe, expect, it } from "vitest";
import { destinationPhotoUrl, isLikelyFlag } from "./destination-image";

describe("destination-image", () => {
  it("rejeita bandeiras e SVGs da Wikipedia", () => {
    expect(isLikelyFlag(null)).toBe(true);
    expect(isLikelyFlag("https://upload.wikimedia.org/flag_of_brazil.svg")).toBe(
      true,
    );
    expect(
      isLikelyFlag("https://upload.wikimedia.org/wikipedia/commons/photo.jpg"),
    ).toBe(false);
  });

  it("retorna URL apenas para fotos plausíveis", () => {
    expect(destinationPhotoUrl(undefined)).toBeNull();
    expect(destinationPhotoUrl("https://example.com/photo.jpg")).toBe(
      "https://example.com/photo.jpg",
    );
    expect(destinationPhotoUrl("https://example.com/flag.svg")).toBeNull();
  });
});
