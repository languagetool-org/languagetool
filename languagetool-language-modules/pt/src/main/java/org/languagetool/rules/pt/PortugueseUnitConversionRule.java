/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2018 Fabian Richter
 *  *
 *  * This library is free software; you can redistribute it and/or
 *  * modify it under the terms of the GNU Lesser General Public
 *  * License as published by the Free Software Foundation; either
 *  * version 2.1 of the License, or (at your option) any later version.
 *  *
 *  * This library is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  * Lesser General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU Lesser General Public
 *  * License along with this library; if not, write to the Free Software
 *  * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 *  * USA
 *
 */

package org.languagetool.rules.pt;

import org.languagetool.rules.AbstractUnitConversionRule;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.ResourceBundle;

import static tech.units.indriya.unit.Units.*;

/**
 * Localized from the German version by Tiago F. Santos
 * @since 4.3
 */
public class PortugueseUnitConversionRule extends AbstractUnitConversionRule {

  private final NumberFormat format;

  public PortugueseUnitConversionRule(ResourceBundle messages) {
    super(messages);
    format = NumberFormat.getNumberInstance(Locale.GERMANY);
    format.setMaximumFractionDigits(2);
    format.setRoundingMode(RoundingMode.HALF_UP);

    addUnit("(qui|ki)lo(grama)?", KILOGRAM, "quilogramas", 1, true);
    addUnit("grama", KILOGRAM, "gramas", 1e-3, true);
    addUnit("toneladas?", KILOGRAM, "toneladas", 1e3, true);
    addUnit("libras?", POUND, "libras", 1, false);

    addUnit("milhas?", MILE, "milhas", 1, false);
    addUnit("jardas?", YARD, "jardas", 1, false);
    addUnit("pés?", FEET, "pés", 1, false);
    addUnit("polegadas?", INCH, "polegadas", 1, false);

    addUnit("(Kilometros por Hora)", KILOMETRE_PER_HOUR, "quilómetros por hora", 1, true);
    addUnit("milhas por hora", MILE.divide(HOUR), "milhas por hora", 1, false);

    addUnit("metros?", METRE, "metros", 1, true);
    addUnit("Quilómetros?", METRE, "quilómetros", 1e3, true);
    addUnit("kilómetros?", METRE, "quilómetros", 1e3, true);
    addUnit("decímetros?", METRE, "decímetros", 1e-1, false); // metric, but should not be suggested
    addUnit("centímetros?", METRE, "centímetros", 1e-2, true);
    addUnit("milímetros?", METRE, "milímetros", 1e-3, true);
    addUnit("micrómetros?", METRE, "micrómetros", 1e-6, true);
    addUnit("nanómetros?", METRE, "nanómetros", 1e-9, true);
    addUnit("picómetros?", METRE, "picómetros", 1e-12, true);
    addUnit("fentometros?", METRE, "fectometros", 1e-15, true);

    addUnit("metros? quadrados?", SQUARE_METRE, "metros quadrados", 1, true);
    addUnit("hectár(es)?", SQUARE_METRE, "héctares", 1e4, true);
    addUnit("area?", SQUARE_METRE, "ares", 1e2, true);
    addUnit("(kilómetros?|quilómetros?) quadrados?", SQUARE_METRE,  "quilómetros quadrados",  1e6, true);
    addUnit("decímetros?", SQUARE_METRE,  "decímetros quadrados",  1e-2,  false/*true*/); // Metric, but not commonly used
    addUnit("centímetros? quadrados?", SQUARE_METRE, "centímetros quadrados", 1e-4, true);
    addUnit("milímetros? quadrados?", SQUARE_METRE, "milímetros quadrados", 1e-6, true);
    addUnit("micrómetros? quadrados?", SQUARE_METRE, "micrómetros quadrados", 1e-12, true);
    addUnit("nanómetros? quadrados?", SQUARE_METRE,  "nanómetros quadrados",  1e-18, true);

    addUnit("metros? cúbicos?", CUBIC_METRE,     "metros cúbicos",      1, true);
    addUnit("quilómetros? cúbicos?", CUBIC_METRE, "quilómetros cúbicos",  1e9, true);
    addUnit("decímetros? cúbicos?", CUBIC_METRE, "decímetros cúbicos",  1e-3,  false/*true*/); // Metric, but not commonly used
    addUnit("centímetros? cúbicos?", CUBIC_METRE,"centímetros cúbicos", 1e-6, true);
    addUnit("milímetros? cúbicos?", CUBIC_METRE,"milímetros cúbicos", 1e-9, true);
    addUnit("micrómetros? cúbicos?", CUBIC_METRE,"micrómetros cúbicos", 1e-18, true);
    addUnit("nanómetros? cúbicos?", CUBIC_METRE, "nanómetros cúbicos",  1e-27, true);

    addUnit("litros?", LITRE, "litros", 1, true);
    addUnit("milílitros?", LITRE, "milílitros", 1e-3, true);

    addUnit( "(?:Graus)? Fahrenheit", FAHRENHEIT, "graus Fahrenheit", 1, false);
    addUnit( "(?:Graus)? Celsi[ou]s", CELSIUS, "graus Celsios", 1, true);
  }

  @Override
  public String getId() {
    return "UNIDADES_METRICAS";
  }

  @Override
  public String getDescription() {
    return "Sugere ou verifica informações equivalentes à métrica de unidades de medida específicas.";
  }

  @Override
  protected String getMessage(Message message) {
    switch(message) {
      case CHECK:
        return "Esta conversão parece estar errada. Quer que isso seja corrigido automaticamente?";
      case SUGGESTION:
        return "Deseja adicionar automaticamente uma conversão ao sistema métrico?";
      case CHECK_UNKNOWN_UNIT:
        return "A unidade usada nesta conversão não foi reconhecida.";
      case UNIT_MISMATCH:
        return "Estas unidades não são compatíveis.";
      default:
        throw new RuntimeException("Unknown message type." + message);
    }
  }

  @Override
  protected String getShortMessage(Message message) {
    switch(message) {
      case CHECK:
        return "Conversão errada. Corrigir automaticamente?";
      case SUGGESTION:
        return "Adicionar equivalência de métrica?";
      case CHECK_UNKNOWN_UNIT:
        return "Unidade desconhecida.";
      case UNIT_MISMATCH:
        return "Unidade não relacionada.";
      default:
        throw new RuntimeException("Unknown message type." + message);
    }
  }

  @Override
  protected NumberFormat getNumberFormat() {
    return format;
  }

}
