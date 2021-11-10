/*
 *  LanguageTool, a natural language style checker
 *  * Copyright (C) 2020 Fabian Richter
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

package org.languagetool.tools;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

public class LoggingTools {
  public static final Marker INIT = MarkerFactory.getMarker("INIT");
  public static final Marker CHECK = MarkerFactory.getMarker("CHECK");
  public static final Marker REQUEST = MarkerFactory.getMarker("REQUEST");
  public static final Marker BAD_REQUEST = MarkerFactory.getMarker("BAD_REQUEST");
  public static final Marker AUTH = MarkerFactory.getMarker("AUTH");
  public static final Marker SYSTEM = MarkerFactory.getMarker("SYSTEM");
  public static final Marker DB = MarkerFactory.getMarker("DB");
  public static final Marker REDIS = MarkerFactory.getMarker("REDIS");
  public static final Marker CACHE = MarkerFactory.getMarker("CACHE");
}
