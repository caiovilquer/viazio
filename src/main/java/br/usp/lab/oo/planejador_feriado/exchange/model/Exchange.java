package br.usp.lab.oo.planejador_feriado.exchange.model;

public class Exchange {

  private final String currency;
  private final double valueInReais;

  public Exchange(String currency, double valueInReais) {
    this.currency = currency;
    this.valueInReais = valueInReais;
  }

  public String getCurrency() {
    return currency;
  }

  public double getValueInReais() {
    return valueInReais;
  }

  @Override
  public String toString() {
    return (
      "Câmbio: 1 " +
      this.currency +
      " = R$ " +
      String.format("%.2f", this.valueInReais)
    );
  }
}
