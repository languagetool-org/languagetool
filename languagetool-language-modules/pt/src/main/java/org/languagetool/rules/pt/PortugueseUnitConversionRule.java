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
    addUnit("onças?", OUNCE, "onças", 1, false);

    addUnit("milhas?", MILE, "milhas", 1, false);
    addUnit("jardas?", YARD, "jardas", 1, false);
    addUnit("pés?", FEET, "pés", 1, false);
    addUnit("polegadas?", INCH, "polegadas", 1, false);

    addUnit("(qu|k)ilômetros? por hora", KILOMETRE_PER_HOUR, "quilômetros por hora", 1, true);
    addUnit("milhas? por hora", MILE.divide(HOUR), "milhas por hora", 1, false);

    addUnit("metros?", METRE, "metros", 1, true);
    addUnit("(qu|k)ilômetros?", METRE, "quilômetros", 1e3, true);
    addUnit("decímetros?", METRE, "decímetros", 1e-1, false); // metric, but should not be suggested
    addUnit("centímetros?", METRE, "centímetros", 1e-2, true);
    addUnit("milímetros?", METRE, "milímetros", 1e-3, true);
    addUnit("micrômetros?", METRE, "micrômetros", 1e-6, true);
    addUnit("nanômetros?", METRE, "nanômetros", 1e-9, true);
    addUnit("picômetros?", METRE, "picômetros", 1e-12, true);
    addUnit("fentômetros?", METRE, "fentômetros", 1e-15, true);

    addUnit("metros? quadrados?", SQUARE_METRE, "metros quadrados", 1, true);
    addUnit("hectar(es)?", SQUARE_METRE, "hectares", 1e4, true);
    addUnit("ares?", SQUARE_METRE, "ares", 1e2, true);
    addUnit("(k|qui)ilômetros? quadrados?", SQUARE_METRE,  "quilômetros quadrados",  1e6, true);
    addUnit("decímetros? quadrados?", SQUARE_METRE,  "decímetros quadrados",  1e-2,  false/*true*/); // Metric, but not commonly used
    addUnit("centímetros? quadrados?", SQUARE_METRE, "centímetros quadrados", 1e-4, true);
    addUnit("milímetros? quadrados?", SQUARE_METRE, "milímetros quadrados", 1e-6, true);
    addUnit("micrômetros? quadrados?", SQUARE_METRE, "micrômetros quadrados", 1e-12, true);
    addUnit("nanômetros? quadrados?", SQUARE_METRE,  "nanômetros quadrados",  1e-18, true);

    addUnit("metros? cúbicos?", CUBIC_METRE,"metros cúbicos",      1, true);
    addUnit("(k|qu)ilômetros? cúbicos?", CUBIC_METRE, "quilômetros cúbicos",  1e9, true);
    addUnit("decímetros? cúbicos?", CUBIC_METRE, "decímetros cúbicos",  1e-3,  false/*true*/); // Metric, but not commonly used
    addUnit("centímetros? cúbicos?", CUBIC_METRE,"centímetros cúbicos", 1e-6, true);
    addUnit("milímetros? cúbicos?", CUBIC_METRE,"milímetros cúbicos", 1e-9, true);
    addUnit("micrômetros? cúbicos?", CUBIC_METRE,"micrômetros cúbicos", 1e-18, true);
    addUnit("nanômetros? cúbicos?", CUBIC_METRE, "nanômetros cúbicos",  1e-27, true);

    addUnit("litros?", LITRE, "litros", 1, true);
    addUnit("mililitros?", LITRE, "mililitros", 1e-3, true);

    addUnit( "(?:Graus)? Fahrenheit", FAHRENHEIT, "graus Fahrenheit", 1, false);
    addUnit( "(?:Graus)? (Celsi[ou]s|[cC]entígrados?)", CELSIUS, "graus Celsius", 1, true);
  }

  @Override
  public String getId() {
    return "UNIDADES_METRICAS";
  }

  @Override
  protected String formatRounded(String s) {
    return "aprox. " + s;
  }

  @Override
  public String getDescription() {
    return "Sugere ou verifica informações equivalentes à métrica de unidades de medida específicas.";
  }

  @Override
  protected String getMessage(Message message) {
    switch(message) {
      case CHECK:
        return "Esta conversão não parece estar precisa. Gostaria de corrigi-la?";
      case SUGGESTION:
        return "Deseja adicionar automaticamente uma conversão ao sistema métrico?";
      case CHECK_UNKNOWN_UNIT:
        return "A unidade usada nesta conversão não foi reconhecida.";
      case UNIT_MISMATCH:
        return "Estas unidades de medida não são compatíveis.";
      default:
        throw new RuntimeException("Unknown message type." + message);
    }
  }

  @Override
  protected String getShortMessage(Message message) {
    switch(message) {
      case CHECK:
        return "Conversão incorreta. Corrigir?";
      case SUGGESTION:
        return "Adicionar conversão ao sistema métrico?";
      case CHECK_UNKNOWN_UNIT:
        return "Unidade desconhecida.";
      case UNIT_MISMATCH:
        return "Unidade incompatível.";
      default:
        throw new RuntimeException("Unknown message type." + message);
    }
  }

  @Override
  protected NumberFormat getNumberFormat() {
    return format;
  }

}
