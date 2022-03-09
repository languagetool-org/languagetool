/* LanguageTool, a natural language style checker
 * Copyright (C) 2019 Daniel Naber (http://www.danielnaber.de)
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301
 * USA
 */
package org.languagetool.rules.fr;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jetbrains.annotations.NotNull;
import org.languagetool.AnalyzedSentence;
import org.languagetool.GlobalConfig;
import org.languagetool.rules.Rule;
import org.languagetool.rules.RuleMatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Queries a local Grammalecte server.
 * @since 4.6
 */
public class GrammalecteRule extends Rule {

  private static final Logger logger = LoggerFactory.getLogger(GrammalecteRule.class);
  private static final int TIMEOUT_MILLIS = 500;
  private static final long DOWN_INTERVAL_MILLISECONDS = 5000;

  private static long lastRequestError = 0;

  private final ObjectMapper mapper = new ObjectMapper();
  private final GlobalConfig globalConfig;

  // https://github.com/languagetooler-gmbh/languagetool-premium/issues/197:
  private final Set<String> ignoreRules = new HashSet<>(Arrays.asList(
    "tab_fin_ligne",
    "apostrophe_typographique",
    "typo_guillemets_typographiques_doubles_ouvrants",
    "nbsp_avant_double_ponctuation",
    "typo_guillemets_typographiques_doubles_fermants",
    // for discussion, see https://github.com/languagetooler-gmbh/languagetool-premium/issues/229:
    "nbsp_avant_deux_points",  // Useful only if we decide to have the rest of the non-breakable space rules.
    "nbsp_ajout_avant_double_ponctuation",  // Useful only if we decide to have the rest of the non-breakable space rules.
    "apostrophe_typographique_après_t",  // Not useful. While being the technically correct character, it does not matter much.
    "typo_tiret_début_ligne",  // Arguably the same as 50671 and 17342 ; the french special character for lists is a 'tiret cadratin' ; so it should be that instead of a dash. Having it count as a mistake is giving access to the otherwise unaccessible special character. However, lists are a common occurrence, and the special character does not make a real difference. Not really useful but debatable
    "typo_guillemets_typographiques_simples_fermants",
    "typo_apostrophe_incorrecte",
    "unit_nbsp_avant_unités1",
    "unit_nbsp_avant_unités2",
    "unit_nbsp_avant_unités3",
    "nbsp_après_double_ponctuation",
    "typo_guillemets_typographiques_simples_ouvrants",
    "num_grand_nombre_avec_espaces",
    "num_grand_nombre_soudé",
    "typo_parenthèse_ouvrante_collée",  // we already have UNPAIRED_BRACKETS
    "nbsp_après_chevrons_ouvrants",
    "nbsp_avant_chevrons_fermants",
    "nbsp_avant_chevrons_fermants1",
    "nbsp_avant_chevrons_fermants2",
    "typo_points_suspension1",
    "typo_points_suspension2",
    "typo_points_suspension3",
    "typo_tiret_incise", // picky
    "esp_avant_après_tiret", // picky
    "nbsp_après_tiret1", // picky
    "nbsp_après_tiret2", // picky
    "esp_mélangés1", // picky
    "esp_mélangés2", // picky
    "tab_début_ligne",
    "esp_milieu_ligne", // we already have WHITESPACE_RULE
    "typo_ponctuation_superflue1", // false alarm (1, 2, ...)
    "esp_insécables_multiples", // temp disabled, unsure how this works with the browser add-ons
    "typo_espace_manquant_après1", // false alarm in urls (e.g. '&rk=...')
    "typo_espace_manquant_après2", // false alarm in urls (e.g. '&rk=...')
    "typo_espace_manquant_après3", // false alarm in file names (e.g. 'La teaser.zip')
    "typo_tiret_incise2",  // picky
    "eepi_écriture_épicène_singulier",
    "g1__bs_vidéoprotection__b1_a1_1",
    "g1__eleu_élisions_manquantes__b1_a1_1", // picky
    "typo_tiret_incise1", // picky
    "p_sigle2", // picky
    "g0__imp_verbes_composés_impératifs__b12_a2_1",
    "g0__imp_verbes_composés_impératifs__b12_a3_1",
    "g0__imp_verbes_composés_impératifs__b5_a2_1",
    "g2__gn_tous_det_nom__b1_a2_1",
    "g2__gn_tous_det_nom__b2_a2_1",
    "g2__gn_tous_nom__b2_a1_1",
    "g2__gn_tout_det__b2_a1_1",
    "g2__gn_tout_nom__b1_a1_1",
    "g2__gn_tout_nom__b2_a1_1",
    "g2__gn_toute_nom__b1_a1_1",
    "g2__gn_toute_nom__b2_a1_1",
    "g2__gn_toutes_nom__b1_a1_1",
    "g2__gn_toutes_nom__b2_a1_1",
    "g2__gn_toutes_nom__b2_a2_1",
    "g2__maj_Dieu__b1_a1_1",
    "g3__gn_2m_et_ou__b1_a1_1",
    "g3__gn_2m_et_ou__b1_a2_1",
    "g3__gn_adverbe_fort__b1_a1_1",
    "g3__gn_adverbe_juste__b1_a1_1",
    "g3__gn_au_1m__b1_a1_1",
    "g3__gn_au_1m__b1_a2_1",
    "g3__gn_au_1m__b1_a3_1",
    "g3__gn_au_1m__b2_a1_1",
    "g3__gn_aucun_1m__b1_a1_1",
    "g3__gn_aucun_1m__b1_a2_1",
    "g3__gn_aucun_1m__b1_a3_1",
    "g3__gn_aucune_1m__b1_a3_1",
    "g3__gn_ce_1m__b1_a1_1",
    "g3__gn_ce_1m__b1_a2_1",
    "g3__gn_ce_1m__b1_a3_1",
    "g3__gn_ce_1m__b1_a4_1",
    "g3__gn_celle__b1_a2_1",
    "g3__gn_celles__b1_a1_1",
    "g3__gn_certaines_1m__b1_a2_1",
    "g3__gn_certaines_1m__b1_a3_1",
    "g3__gn_certains_1m__b1_a3_1",
    "g3__gn_ces_aux_pluriel_1m__b1_a1_1",
    "g3__gn_ces_aux_pluriel_1m__b1_a3_1",
    "g3__gn_ces_aux_pluriel_1m__b1_a4_1",
    "g3__gn_cet_1m__b1_a3_1",
    "g3__gn_cette_1m__b1_a1_1",
    "g3__gn_cette_1m__b1_a2_1",
    "g3__gn_cette_1m__b1_a3_1",
    "g3__gn_des_2m__b1_a2_1",
    "g3__gn_des_2m__b1_a3_1",
    "g3__gn_des_2m__b1_a4_1",
    "g3__gn_det_epi_plur_1m__b1_a1_1",
    "g3__gn_det_epi_plur_2m__b1_a2_1",
    "g3__gn_det_epi_plur_2m__b1_a3_1",
    "g3__gn_det_epi_plur_2m__b1_a4_1",
    "g3__gn_det_epi_plur_2m__b2_a3_1",
    "g3__gn_det_epi_plur_2m__b2_a4_1",
    "g3__gn_det_epi_plur_3m__b1_a3_1",
    "g3__gn_det_epi_plur_3m__b1_a4_1",
    "g3__gn_det_epi_sing_2m__b1_a2_1",
    "g3__gn_det_epi_sing_2m__b1_a3_1",
    "g3__gn_det_epi_sing_2m__b2_a2_1",
    "g3__gn_det_epi_sing_2m__b2_a3_1",
    "g3__gn_det_epi_sing_2m_virg__b1_a1_1",
    "g3__gn_det_epi_sing_3m__b1_a2_1",
    "g3__gn_det_fem_plur_2m__b1_a3_1",
    "g3__gn_det_fem_sing_2m__b1_a2_1",
    "g3__gn_det_fem_sing_2m__b1_a3_1",
    "g3__gn_det_fem_sing_2m__b2_a2_1",
    "g3__gn_det_fem_sing_2m_virg__b1_a1_1",
    "g3__gn_det_fem_sing_3m__b1_a1_1",
    "g3__gn_det_les_3m__b1_a2_1",
    "g3__gn_det_les_3m__b1_a3_1",
    "g3__gn_det_les_3m__b1_a4_1",
    "g3__gn_det_les_3m_et__b1_a2_1",
    "g3__gn_det_les_3m_et__b1_a3_1",
    "g3__gn_det_les_3m_et__b3_a5_1",
    "g3__gn_det_les_3m_et__b3_a6_1",
    "g3__gn_det_mas_plur_2m__b1_a2_1",
    "g3__gn_det_mas_plur_3m__b1_a1_1",
    "g3__gn_det_mas_sing_2m__b1_a2_1",
    "g3__gn_det_mas_sing_2m__b1_a3_1",
    "g3__gn_det_mas_sing_2m__b2_a2_1",
    "g3__gn_det_mas_sing_2m__b2_a3_1",
    "g3__gn_det_mas_sing_2m_virg__b1_a1_1",
    "g3__gn_det_mas_sing_3m__b1_a1_1",
    "g3__gn_det_mas_sing_3m_et__b3_a1_1",
    "g3__gn_det_mon_ton_son_3m__b1_a2_1",
    "g3__gn_det_mon_ton_son_3m_et__b4_a1_1",
    "g3__gn_det_nom_de_det_nom_adj_fem_sing__b1_a1_1",
    "g3__gn_det_nom_de_det_nom_adj_fem_sing__b3_a1_1",
    "g3__gn_det_nom_de_det_nom_adj_fem_sing__b7_a1_1",
    "g3__gn_det_nom_de_det_nom_adj_fem_sing__b9_a1_1",
    "g3__gn_det_nom_de_det_nom_adj_mas_sing__b1_a1_1",
    "g3__gn_det_nom_de_det_nom_adj_mas_sing__b3_a1_1",
    "g3__gn_det_nom_de_det_nom_adj_plur__b2_a1_1",
    "g3__gn_det_nom_de_det_nom_adj_sing__b1_a1_1",
    "g3__gn_det_nom_de_det_nom_adj_sing_plur__b1_a1_1",
    "g3__gn_det_nom_de_det_nom_adj_sing_plur__b2_a1_1",
    "g3__gn_det_nom_de_det_nom_adj_sing_plur__b6_a1_1",
    "g3__gn_det_nom_et_det_nom__b1_a1_1",
    "g3__gn_du_1m__b1_a1_1",
    "g3__gn_du_1m__b1_a2_1",
    "g3__gn_du_1m__b1_a3_1",
    "g3__gn_du_1m__b2_a1_1",
    "g3__gn_du_1m__b2_a2_1",
    "g3__gn_groupe_de__b2_a1_1",
    "g3__gn_groupe_de__b3_a1_1",
    "g3__gn_intérieur_extérieur__b1_a1_1",
    "g3__gn_l_1m__b1_a1_1",
    "g3__gn_l_1m__b2_a1_1",
    "g3__gn_l_1m__b3_a1_1",
    "g3__gn_l_2m__b1_a2_1",
    "g3__gn_l_2m__b1_a3_1",
    "g3__gn_l_2m__b1_a4_1",
    "g3__gn_l_2m__b2_a2_1",
    "g3__gn_l_2m__b2_a3_1",
    "g3__gn_l_2m__b2_a4_1",
    "g3__gn_l_2m_virg__b1_a1_1",
    "g3__gn_l_3m__b1_a2_1",
    "g3__gn_l_3m__b1_a3_1",
    "g3__gn_la_1m__b1_a2_1",
    "g3__gn_la_1m__b1_a3_1",
    "g3__gn_la_1m__b2_a2_1",
    "g3__gn_la_1m__b2_a3_1",
    "g3__gn_la_1m__b2_a4_1",
    "g3__gn_la_1m__b3_a1_1",
    "g3__gn_la_1m__b3_a2_1",
    "g3__gn_la_1m__b3_a3_1",
    "g3__gn_la_2m__b1_a2_1",
    "g3__gn_la_2m__b1_a3_1",
    "g3__gn_la_2m__b1_a4_1",
    "g3__gn_la_2m__b2_a2_1",
    "g3__gn_la_2m__b2_a4_1",
    "g3__gn_la_2m__b3_a2_1",
    "g3__gn_la_2m__b3_a4_1",
    "g3__gn_la_2m_virg__b1_a1_1",
    "g3__gn_le_1m__b2_a2_1",
    "g3__gn_le_1m__b2_a3_1",
    "g3__gn_le_1m__b2_a4_1",
    "g3__gn_le_1m__b2_a5_1",
    "g3__gn_le_1m__b3_a2_1",
    "g3__gn_le_1m__b3_a3_1",
    "g3__gn_le_1m__b3_a4_1",
    "g3__gn_le_1m__b3_a5_1",
    "g3__gn_le_1m__b4_a1_1",
    "g3__gn_le_1m__b4_a2_1",
    "g3__gn_le_1m__b4_a3_1",
    "g3__gn_le_1m__b4_a4_1",
    "g3__gn_le_2m__b1_a2_1",
    "g3__gn_le_2m__b1_a3_1",
    "g3__gn_le_2m__b1_a4_1",
    "g3__gn_le_2m__b2_a2_1",
    "g3__gn_le_2m_virg__b1_a1_1",
    "g3__gn_le_3m__b1_a1_1",
    "g3__gn_le_3m_et__b1_a1_1",
    "g3__gn_le_3m_et__b2_a1_1",
    "g3__gn_les_1m__b1_a1_1",
    "g3__gn_les_1m__b2_a1_1",
    "g3__gn_les_1m__b3_a1_1",
    "g3__gn_les_2m__b1_a2_1",
    "g3__gn_les_2m__b1_a3_1",
    "g3__gn_les_2m__b1_a4_1",
    "g3__gn_les_2m__b2_a4_1",
    "g3__gn_leur_1m__b1_a1_1",
    "g3__gn_leur_1m__b1_a2_1",
    "g3__gn_leur_1m__b2_a1_1",
    "g3__gn_leur_1m__b2_a2_1",
    "g3__gn_leur_2m__b1_a2_1",
    "g3__gn_leur_2m__b1_a4_1",
    "g3__gn_leur_2m__b1_a5_1",
    "g3__gn_leur_2m__b1_a5_1",
    "g3__gn_leur_2m__b2_a2_1",
    "g3__gn_leur_2m__b2_a4_1",
    "g3__gn_leurs_1m__b1_a1_1",
    "g3__gn_leurs_1m__b1_a2_1",
    "g3__gn_ma_ta_sa_1m__b1_a2_1",
    "g3__gn_ma_ta_sa_1m__b1_a3_1",
    "g3__gn_ma_ta_sa_1m__b1_a4_1",
    "g3__gn_mon_ton_son_1m__b1_a2_1",
    "g3__gn_mon_ton_son_1m__b1_a3_1",
    "g3__gn_mon_ton_son_1m__b1_a4_1",
    "g3__gn_mon_ton_son_2m__b1_a2_1",
    "g3__gn_mon_ton_son_2m__b1_a3_1",
    "g3__gn_mon_ton_son_2m__b1_a4_1",
    "g3__gn_mon_ton_son_2m__b2_a2_1",
    "g3__gn_nom_adj_2m__b1_a2_1",
    "g3__gn_nom_adj_2m__b1_a3_1",
    "g3__gn_nom_adj_2m__b1_a4_1",
    "g3__gn_nom_adj_2m__b1_a5_1",
    "g3__gn_nombre_chiffres_1m__b1_a1_1",
    "g3__gn_nombre_chiffres_1m__b2_a1_1",
    "g3__gn_nombre_chiffres_1m__b3_a2_1",
    "g3__gn_nombre_chiffres_1m__b3_a3_1",
    "g3__gn_nombre_chiffres_1m__b3_a4_1",
    "g3__gn_nombre_de_1m__b1_a1_1",
    "g3__gn_nombre_lettres_1m__b2_a1_1",
    "g3__gn_nombre_lettres_1m__b4_a1_1",
    "g3__gn_nombre_lettres_1m__b5_a1_1",
    "g3__gn_nombre_lettres_1m__b6_a1_1",
    "g3__gn_nombre_plur_2m__b1_a2_1",
    "g3__gn_nombre_plur_2m__b1_a3_1",
    "g3__gn_nombre_plur_2m__b1_a4_1",
    "g3__gn_notre_votre_chaque_1m__b1_a1_1",
    "g3__gn_nul_1m__b1_a1_1",
    "g3__gn_pfx_de_2m__b1_a2_1",
    "g3__gn_pfx_de_2m__b1_a3_1",
    "g3__gn_pfx_de_2m__b1_a4_1",
    "g3__gn_pfx_en_2m__b1_a1_1",
    "g3__gn_pfx_en_2m__b1_a2_1",
    "g3__gn_pfx_en_2m__b1_a3_1",
    "g3__gn_pfx_en_2m__b1_a4_1",
    "g3__gn_pfx_sur_avec_après_2m__b1_a1_1",
    "g3__gn_pfx_sur_avec_après_2m__b1_a2_1",
    "g3__gn_pfx_sur_avec_après_2m__b1_a3_1",
    "g3__gn_pfx_sur_avec_après_2m__b1_a4_1",
    "g3__gn_pfx_à_par_pour_sans_2m__b1_a1_1",
    "g3__gn_pfx_à_par_pour_sans_2m__b1_a2_1",
    "g3__gn_pfx_à_par_pour_sans_2m__b1_a3_1",
    "g3__gn_pfx_à_par_pour_sans_2m__b1_a4_1",
    "g3__gn_plein_de__b1_a1_1",
    "g3__gn_plusieurs_1m__b1_a1_1",
    "g3__gn_start_2m__b1_a1_1",
    "g3__gn_start_2m__b1_a2_1",
    "g3__gn_start_2m__b1_a3_1",
    "g3__gn_start_2m__b1_a4_1",
    "g3__gn_start_3m__b1_a1_1",
    "g3__gn_start_3m__b1_a2_1",
    "g3__gn_start_3m__b1_a3_1",
    "g3__gn_start_3m__b1_a4_1",
    "g3__gn_start_3m__b1_a5_1",
    "g3__gn_start_3m__b1_a6_1",
    "g3__gn_start_3m_et__b2_a2_1",
    "g3__gn_start_prn_1m__b1_a2_1",
    "g3__gn_un_1m__b2_a1_1",
    "g3__gn_un_1m__b2_a2_1",
    "g3__gn_un_1m__b2_a3_1",
    "g3__gn_un_2m__b1_a2_1",
    "g3__gn_un_2m__b1_a3_1",
    "g3__gn_un_2m__b1_a4_1",
    "g3__gn_un_2m__b2_a2_1",
    "g3__gn_un_2m__b2_a3_1",
    "g3__gn_un_2m__b2_a4_1",
    "g3__gn_un_des_1m__b1_a1_1",
    "g3__gn_un_des_1m__b1_a2_1",
    "g3__gn_une_1m__b1_a1_1",
    "g3__gn_une_1m__b1_a2_1",
    "g3__gn_une_1m__b1_a3_1",
    "g3__gn_une_2m__b1_a2_1",
    "g3__gn_une_2m__b1_a3_1",
    "g3__gn_une_2m__b1_a4_1",
    "g3__gn_une_2m__b2_a2_1",
    "g3__gn_une_2m__b2_a3_1",
    "g3__gn_une_2m__b2_a4_1",
    "g3__gn_une_2m_virg__b1_a1_1",
    "g3__gn_une_des_1m__b1_a1_1",
    "gv1__ppas_3pl_fem_verbe_état__b2_a1_1",
    "gv1__ppas_3pl_mas_verbe_état__b2_a1_1",
    "gv1__ppas_3pl_mas_verbe_état__b8_a1_1",
    "gv1__ppas_3sg_fem_verbe_état__b3_a1_1",
    "gv1__ppas_3sg_mas_verbe_état__b10_a1_1",
    "gv1__ppas_3sg_mas_verbe_état__b12_a1_2",
    "gv1__ppas_3sg_mas_verbe_état__b15_a1_1",
    "gv1__ppas_3sg_mas_verbe_état__b4_a1_1",
    "gv1__ppas_3sg_mas_verbe_état__b4_a1_2",
    "gv1__ppas_3sg_mas_verbe_état__b5_a1_1",
    "gv1__ppas_3sg_mas_verbe_état__b6_a2_1",
    "gv1__ppas_adj_accord_il__b1_a1_1",
    "gv1__ppas_adj_accord_ils__b1_a1_1",
    "gv1__ppas_adj_être_det_nom__b2_a1_1",
    "gv1__ppas_adj_être_det_nom__b3_a2_1",
    "gv1__ppas_avoir__b1_a1_1",
    "gv1__ppas_avoir_l_air__b5_a1_1",
    "gv1__ppas_avoir_l_air__b9_a2_1",
    "gv1__ppas_avoir_été__b2_a1_1",
    "gv1__ppas_det_plur_COD_que_avoir__b1_a3_1",
    "gv1__ppas_det_sing_COD_que_avoir__b4_a3_1",
    "gv1__ppas_fin_loc_verb_état_adj_et_adj__b1_a1_1",
    "gv1__ppas_je_tu_verbe_état__b1_a1_1",
    "gv1__ppas_je_tu_verbe_état__b2_a1_1",
    "gv1__ppas_je_tu_verbe_état__b3_a1_1",
    "gv1__ppas_le_verbe_pensée__b1_a1_1",
    "gv1__ppas_nous_verbe_état__b2_a1_1",
    "gv1__ppas_être_accord_plur__b2_a1_1",
    "gv1__ppas_être_accord_sing__b1_a1_1",
    "gv2__conf_ait_confiance_été_faim_tort__b1_a2_1",
    "gv2__conj_les_nom__b1_a2_1",
    "gv2__conj_quiconque__b1_a1_1",
    "g2__conf_a_à_substantifs__b1_a1_1",
    "g2__conf_a_à_verbe__b14_a2_1",
    "g2__conf_a_à_verbe__b14_a3_1",
    "g2__conf_a_à_verbe__b14_a6_1",
    "g2__conf_a_à_verbe__b15_a2_1",
    "g2__conf_a_à_verbe__b15_a3_1",
    "g2__conf_a_à_verbe__b1_a1_1",
    "g2__conf_celui_celle_à_qui__b1_a1_1",
    "g2__conf_à_a__b1_a1_1",
    "g2__conf_à_a__b2_a1_1",
    "g2__conf_à_a__b7_a1_1",
    "g2__conf_à_a_infinitif__b1_a1_1",
    "g2__conf_à_a__b3_a1_1",
    "g2__conf_à_a__b4_a1_1",
    "g2__conf_à_a__b5_a1_1",
    "g2__conf_à_qui_infinitif__b1_a1_1",
    "g3__conf_à_a_après_verbes__b2_a1_1",
    "g3__infi_à_verbe__b2_a1_1",
    "g3__infi_à_verbe__b3_a1_1",
    "gv1__ppas_avoir__b2_a1_1",
    "gv1__ppas_avoir__b3_a1_1",
    "gv1__ppas_avoir__b4_a1_1",
    "g2__eleu_élisions_manquantes__b1_a1_1",
    "g2__eleu_élisions_manquantes__b2_a1_1",
    "g2__eleu_élisions_manquantes__b4_a1_1",
    "g2__eleu_élisions_manquantes__b5_a1_1",
    "g2__eleu_élisions_manquantes__b6_a1_1",
    "g2__conf_a_à_incohérences__b1_a1_1",
    "g2__conf_a_à_verbe__b11_a1_1",
    "g2__conf_a_à_verbe__b13_a1_1",
    "g2__conf_a_à_verbe__b14_a7_1",
    "g2__conf_a_à_verbe__b3_a1_1",
    "g2__conf_a_à_verbe__b4_a1_1",
    "g2__conf_a_à_verbe__b7_a1_1",
    "g2__conf_a_à_verbe__b8_a1_1",
    "g3__gn_la_3m__b1_a1_1"
  ));

  public GrammalecteRule(ResourceBundle messages, GlobalConfig globalConfig) {
    super(messages);
    //addExamplePair(Example.wrong(""),
    //               Example.fixed(""));
    this.globalConfig = globalConfig;
  }

  @Override
  public String getId() {
    return "FR_GRAMMALECTE";
  }

  @Override
  public String getDescription() {
    return "Returns matches of a local Grammalecte server";
  }

  @Override
  public RuleMatch[] match(AnalyzedSentence sentence) throws IOException {
    // very basic health check -> mark server as down after an error for given interval
    if (System.currentTimeMillis() - lastRequestError < DOWN_INTERVAL_MILLISECONDS) {
      logger.warn("Warn: Temporarily disabled Grammalecte server because of recent error.");
      return new RuleMatch[0];
    }

    URL serverUrl = new URL(globalConfig.getGrammalecteServer());
    HttpURLConnection huc = (HttpURLConnection) serverUrl.openConnection();
    HttpURLConnection.setFollowRedirects(false);
    huc.setConnectTimeout(TIMEOUT_MILLIS);
    huc.setReadTimeout(TIMEOUT_MILLIS*2);
    if (globalConfig.getGrammalecteUser() != null && globalConfig.getGrammalectePassword() != null) {
      String authString = globalConfig.getGrammalecteUser() + ":" + globalConfig.getGrammalectePassword();
      String encoded = Base64.getEncoder().encodeToString(authString.getBytes());
      huc.setRequestProperty("Authorization", "Basic " + encoded);
    }
    huc.setRequestMethod("POST");
    huc.setDoOutput(true);
    try {
      huc.connect();
      try (DataOutputStream wr = new DataOutputStream(huc.getOutputStream())) {
        String urlParameters = "text=" + encode(sentence.getText());
        byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
        wr.write(postData);
      }
      InputStream input = huc.getInputStream();
      List<RuleMatch> ruleMatches = parseJson(input);
      return toRuleMatchArray(ruleMatches);
    } catch (Exception e) {
      lastRequestError = System.currentTimeMillis();
      // These are issue that can be request-specific, like wrong parameters. We don't throw an
      // exception, as the calling code would otherwise assume this is a persistent error:
      logger.warn("Warn: Failed to query Grammalecte server at " + serverUrl + ": " + e.getClass() + ": " + e.getMessage());
      e.printStackTrace();
    } finally {
      huc.disconnect();
    }
    return new RuleMatch[0];
  }

  @NotNull
  private List<RuleMatch> parseJson(InputStream inputStream) throws IOException {
    Map map = mapper.readValue(inputStream, Map.class);
    List matches = (ArrayList) map.get("data");
    if (matches == null) {
      throw new RuntimeException("No 'data' found in grammalecte JSON: " + map);  // handled in match()
    }
    List<RuleMatch> result = new ArrayList<>();
    for (Object match : matches) {
      List<RuleMatch> remoteMatches = getMatches((Map<String, Object>)match);
      result.addAll(remoteMatches);
    }
    return result;
  }

  protected String encode(String plainText) throws UnsupportedEncodingException {
    return URLEncoder.encode(plainText, StandardCharsets.UTF_8.name());
  }

  @NotNull
  private List<RuleMatch> getMatches(Map<String, Object> match) {
    List<RuleMatch> remoteMatches = new ArrayList<>();
    ArrayList matches = (ArrayList) match.get("lGrammarErrors");
    for (Object o : matches) {
      Map pairs = (Map) o;
      int offset = (int) pairs.get("nStart");
      int endOffset = (int)pairs.get("nEnd");
      String id = (String)pairs.get("sRuleId");
      if (ignoreRules.contains(id)) {
        continue;
      }
      String message = pairs.get("sMessage").toString();
      GrammalecteInternalRule rule = new GrammalecteInternalRule("grammalecte_" + id, message);
      RuleMatch extMatch = new RuleMatch(rule, null, offset, endOffset, message);
      List<String> suggestions = (List<String>) pairs.get("aSuggestions");
      //SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss ZZZZ");
      //System.out.println(sdf.format(new Date()) + " Grammalecte: " + pairs.get("sRuleId") + "; " + pairs.get("sMessage") + " => " + suggestions);
      extMatch.setSuggestedReplacements(suggestions);
      remoteMatches.add(extMatch);
    }
    return remoteMatches;
  }

  static class GrammalecteInternalRule extends Rule {
    private final String id;
    private final String desc;

    GrammalecteInternalRule(String id, String desc) {
      this.id = id;
      this.desc = desc;
    }

    @Override
    public String getId() {
      return id;
    }

    @Override
    public String getDescription() {
      return desc;
    }

    @Override
    public RuleMatch[] match(AnalyzedSentence sentence) {
      throw new RuntimeException("Not implemented");
    }
  }

}
