/* LanguageTool, a natural language style checker
 * Copyright (C) 2013 Andriy Rysin
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
package org.languagetool.rules.uk;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.languagetool.AnalyzedSentence;
import org.languagetool.JLanguageTool;
import org.languagetool.TestTools;
import org.languagetool.language.Ukrainian;
import org.languagetool.rules.RuleMatch;

public class TokenAgreementNounVerbRuleTest {

  private JLanguageTool lt;
  private TokenAgreementNounVerbRule rule;

  @Before
  public void setUp() throws IOException {
    rule = new TokenAgreementNounVerbRule(TestTools.getMessages("uk"));
    lt = new JLanguageTool(new Ukrainian());
  }

  @Test
  public void testRule() throws IOException {

    assertMatches(1, "Тарас прибігла");
    assertMatches(1, "вони прибіг");
    assertMatches(1, "я прибіжиш");

    assertMatches(1, "вони швидко прибіг");

    assertMatches(1, "та з інших питань перевірка проведено не повно");

    assertMatches(1, "з часом пара вирішили узаконити");

    assertMatches(0, "На честь Джудіт Резнік названо кратер");
    
    //TODO:
    //assertEmptyMatch("подружжя під прізвищем Крилови оселилося в Москві");
    
    // inf
    assertEmptyMatch("чи зуміє наша держава забезпечити власні потреби");
    assertEmptyMatch("так навчила мене бабуся місити пухке дріжджове тісто");
    assertEmptyMatch("чи можуть російськомовні громадяни вважатися українцями");
    
    // predic + inf
    assertEmptyMatch("не шкода віддати життя");
    assertEmptyMatch("не шкода було віддати життя");

    // correct sentences:
    assertEmptyMatch("чоловік прибіг");
    assertEmptyMatch("я прибіг");
    assertEmptyMatch("я прибігла");
    
    assertEmptyMatch("кандидат в президенти поїхав");
    assertEmptyMatch("кандидат в народні депутати поїхав");
    
    // handled by styling rule
//  assertMatches(1, "40 тисяч чоловік виявили бажання");
    assertEmptyMatch("40 тисяч чоловік виявили бажання");
    
    // було + impers
    assertEmptyMatch("клан Алькапоне було знищено");
    
    // було + verb:past
    assertEmptyMatch("він було вмовк");
    assertEmptyMatch("тобто Угорщина було пішла шляхом");
    assertEmptyMatch("він було трохи скис");
    
    // ніж + v_naz
    assertEmptyMatch("більше ніж будь-хто маємо повне право");

    assertEmptyMatch("а вона візьми і зроби");
    
    assertEmptyMatch("Збережені Я позбудуться необхідності");

    // unknown name
    assertEmptyMatch("Андрій Качала");
    assertEmptyMatch("Любов Євтушок зауважила");

    assertEmptyMatch("а він давай пити горілку");
    
    assertEmptyMatch("Тарас ЗАКУСИЛО");

    assertEmptyMatch("не сила була йти далі");
    
    // пора було
    assertEmptyMatch("Вже давно пора було Мовчану відійти від керма.");
    
    // як
    assertEmptyMatch("тому, що як австрієць маєте");
    
    // TODO: would hide good occasions, Тунець тут inanim:v_zna
//    assertEmptyMatch("Тунець розморозьте і поріжте на порційні частинки.");
    
    assertMatches(1, "не встиг вона отямитися");

     assertEmptyMatch("про припинення їхньої діяльності ми ухвалити, зрозуміло, не могли");
     //TODO: ignore insert words
//    assertEmptyMatch("Почав, значить, я рости.");
    
    assertMatches(1, "Ми може спробувати знайти");

    assertEmptyMatch(GOOD_TEXT);

    assertEmptyMatch("— це були невільники");
    
    assertMatches(1, "щоб конкуренти підішли до виборів");
  }

  @Test
  public void testRuleNe() throws IOException {
    assertMatches(1, "Тарас не прибігла");
    assertMatches(1, "Тарас би не прибігла");
    assertMatches(1, "вони не прибіг");
    assertMatches(1, "я не прибіжиш");

    assertEmptyMatch("ні він, ні вона не хотіли");
    assertEmptyMatch("уряд та поліція не контролюють події");
    assertEmptyMatch("— це не була хвороба");
    assertEmptyMatch("— це не передбачено");
    assertEmptyMatch("Решта не мають жодних дозволів");
    assertEmptyMatch("ні лауреат, ні його жінка не розмовляють жодними мовами");
    assertEmptyMatch("Чи ж могла я не повернутися назад?");
    assertEmptyMatch("кефаль, хамса не затримуються");
    assertMatches(1, "інша мова, вона примітивізовано й");
    assertEmptyMatch("душ, одеколони, навіть хлорка не допомогли");
    assertEmptyMatch("і Виговський, ні навіть Мазепа не розглядали");
    assertEmptyMatch("уряд та поліція не контролюють");
    assertEmptyMatch("кадрове забезпечення, матеріальна база не дісталися");
    
    assertEmptyMatch("Волосожар/Морозов не припинили");
    assertEmptyMatch("ні Європа, ні тим більше Україна не мають");
  }

  @Test
  public void testProperNames() throws IOException {
    
    // posessive insert
    assertEmptyMatch("Конституційний суд Республіки Молдова визнав румунську державною");
//    assertEmptyMatch("Творіння братів Люм’єр знало.");
    assertEmptyMatch("Мешканці планети Земля споживають щороку");
    assertEmptyMatch("жителі селища Новобудова зверталися.");
    
    assertMatches(1, "при своїх дружинах Крішна не роздягалася і одразу...");
    
    
    assertEmptyMatch("всі українські жінки з ім’ям Марія мають знати");
    assertEmptyMatch("а ім’я Франклін згадують не досить часто");
    assertEmptyMatch("шимпанзе на прізвисько Чита зіграли 16 «акторів»");
    
    // proper name insert
    assertEmptyMatch("лижний курорт Криниця розташувався в Бескидах");
    assertEmptyMatch("містечко Баришівка потрапило до історії");

    assertEmptyMatch("У місті Ліда незабаром мають встановити");
    
    // proper name passive place
    assertEmptyMatch("ми в державі Україна маємо такі підстави");
    assertEmptyMatch("У литовський порт Клайпеда прибуло плавуче сховище");
    assertEmptyMatch("на австралійський штат Вікторія налетів сильний шторм");
    assertEmptyMatch("в селі Червона Слобода було вирішено перейменувати");
    assertEmptyMatch("Хоча б межі курорту Східниця визначено?");
    assertEmptyMatch("комітет порятунку села Оляниця вирішив");
    assertEmptyMatch("колишній кандидат у губернатори штату Аризона їхав до Чернівців");
    assertEmptyMatch("У невизнаній республіці Південна Осетія відбулися вибори");
    assertEmptyMatch("Рибалки італійського острова Лампедуза заблокували");
    assertEmptyMatch("у латвійське курортне містечко Юрмала з’їхався весь бомонд");
    assertEmptyMatch("Суд американського штату Каліфорнія присудив акторці");
    assertEmptyMatch("У штатах Техас і Луїзіана запроваджено надзвичайний стан");
    
    assertEmptyMatch("на прізвисько Михайло відбулася");

    assertMatches(1, "Вистава зроблено чесно, професійно.");
    
    //TODO: next 2 fall into common exceptions
//    assertMatches(1, "свою першу залікову вагу в поштовху Надія зафіксували лише (!) в третій спробі");
//    assertMatches(1, "по втягуванні України в європейську орбіту Швеція усвідомлюють факт");
    
  }

  
  @Test
  public void testNounAsAdv() throws IOException {
    assertEmptyMatch("під три чорти пертися");
  }
  
  @Test
  public void testPron() throws IOException {
    assertMatches(1, "яка прибіг");
    assertMatches(1, "яка не мають паспортів");
    assertMatches(1, "яка не залежать від волі");
    assertMatches(1, "яка було нещодавно опубліковано");
//    assertMatches(1, "хто прийшла");
//    assertMatches(1, "одна інший радила");
    assertMatches(0, "вони як ніхто інший знали");
    
    assertMatches(0, "який прибила хвиля");
    assertMatches(0, "Ті, хто зрозуміли");
    assertMatches(0, "ті, хто сповідує");
    assertMatches(0, "ті, хто не сповідують");
    assertMatches(1, "всі хто зрозуміли"); // пропущена кома
    assertEmptyMatch("про те, хто була ця клята Пандора");

    assertEmptyMatch("що можна було й інший пошукати");
  }
  
  @Test
  public void testVerbInf() throws IOException {
    // modal verb + noun + verb:inf
    assertEmptyMatch("не встиг я отямитися");
    assertEmptyMatch("що я зробити встиг");
    assertEmptyMatch("це я робити швидко вмію");
    assertEmptyMatch("Саудівську Аравію ми проходити зобов’язані");
    assertMatches(1, "я бігати");
//    assertMatches(1, "машина бігати");

    assertEmptyMatch("ми воювати з нашими людьми не збираємося");
    
    // noun + inf + не + verb/adj:rv_inf
    assertEmptyMatch("що ми зробити не зможемо");
    assertEmptyMatch("Я уявити себе не можу без нашої програми.");
    assertEmptyMatch("ми розраховувати не повинні");
    assertEmptyMatch("Хотів би я подивитися");
    assertEmptyMatch("на останніх ми працювати не згідні");
    assertMatches(1, "на останніх ми працювати не питаючи нікого");
    assertEmptyMatch("те, чого я слухати не дуже хочу");
    assertEmptyMatch("чи гідні ми бути незалежними");
    assertEmptyMatch("Чи здатен її автор навести хоча б один факт");
    
    // rv_inf
    assertEmptyMatch("чи готові ми сидіти без світла");
    assertEmptyMatch("Чи повинен я просити");

    assertEmptyMatch("ніхто робити цього не буде");
    assertEmptyMatch("ніхто знижувати тарифи на газ, зрозуміло, не збирається");
    //TODO:
//    assertEmptyMatch("Іноземець в’їздити і виїздити з цієї країни може тільки");

    assertMatches(1, "Та припинити ти переживати");
    
    assertEmptyMatch("та я купувати цю куртку не дуже хотіла");
    
    assertEmptyMatch("Правоохоронці зняти провокаційний стяг не змогли, оскільки");
    
    // ignore quotes
    assertEmptyMatch("Однак Банкова виконувати приписи Закону \"Про очищення влади\" не поспішає");
    
    assertEmptyMatch("Хтось намагається її торкнутися, хтось сфотографувати.");
    
    assertEmptyMatch("Ми не проти сплачувати податки");
  }
  
  @Test
  public void testPlural() throws IOException {
    assertMatches(1, "21 гравець дивилися");
    assertMatches(1, "один гравець дивилися");
    assertMatches(1, "Серед вбитих і полонених радянських солдат були чоловіки");
    assertMatches(1, "Пригадую випадок, коли одна аудіокомпанія звернулися до провідних київських «ефемок»");
    assertMatches(1, "підписку про невиїзд двох молодиків, яких міліція затримали першими");
    assertMatches(1, "рук та ніг Параски, темниця розчинилася і дівчина опинилися за стінами фортеці");
    assertMatches(1, "його арештували і вислали у Воркуту, я залишилися одна з дитиною.");
    assertMatches(1, "На проспекті Чорновола, ближче до центру, вона зупинилися на перехресті");
    assertMatches(1, "порадилися і громада запропонували мені зайняти його місце");
    assertMatches(1, "то й небо вона бачите саме таким");
    assertMatches(1, "молочного туману, потім вона заснувалися");
    assertMatches(1, "Наташа смикала за волосся, а Софія намагалися бризнути");
    
    assertEmptyMatch("моя мама й сестра мешкали");
    assertEmptyMatch("чи то Вальтер, чи я вжили фразу");
    assertEmptyMatch("То вона, то Гриць виринають перед її душею");
    assertEmptyMatch("Кожен чоловік і кожна жінка мають");
    assertEmptyMatch("каналізація і навіть охорона пропонувалися");
    assertEmptyMatch("Кавказ загалом і Чечня зокрема лишаться");
    assertEmptyMatch("Європейський Союз і моя рідна дочка переживуть це збурення");
    assertEmptyMatch("Бразилія, Мексика, Індія збувають");
    assertEmptyMatch("Банкова й особисто президент дістали");
    assertEmptyMatch("українська мода та взагалі українська культура впливають");
    assertEmptyMatch("Тато і Юзь Федорків були прикладом");
    assertEmptyMatch("Клочкова ти Лисогор перемагають на своїх дистанціях");
    assertEmptyMatch("він особисто й облдержадміністрація винесли");
    
    assertEmptyMatch("і “більшовики”, і Президент звинуватили опозицію у зриві");
    assertEmptyMatch("І “швидка“, і міліція приїхали майже вчасно");
    assertEmptyMatch("І уряд, і президент позитивно оцінюють");
    
    assertEmptyMatch("Андрій Ярмоленко, Євген Коноплянка, Ярослав Ракицький допомогли вітчизняній «молодіжці»");
    //TODO: unknown proper nouns
    assertEmptyMatch("Мустафа Джемілєв, Рефат Чубаров зможуть");
    assertEmptyMatch("Єжи Тур, Александр Рибіцький перебороли");
    
    assertEmptyMatch("27-річний водій та 54-річна пасажирка були травмовані");
    assertEmptyMatch("фізична робота та щоденна 30–хвилинна фіззарядка приносять");
    
    assertEmptyMatch("Б. Єльцин і Л. Кучма погодилися вважати Азовське море внутрішнім морем");
    
    // unknown proper nouns
    assertEmptyMatch("Ґорбачов і його дружина виглядали");
    assertEmptyMatch("і Ципкалов, і Кисельов могли одразу");
    assertEmptyMatch("«Самопоміч» та Радикальна партія дали більшість голосів");

    // multiple adj + noun
    //TODO:
//  assertEmptyMatch("Бережна й «мирна» тусовка перебувають");
    // TODO: can't easily detect special case with погода
//    assertMatches(1, "Цього року дощова та холодна погода стояли практично в усіх регіонах");

    
    assertEmptyMatch("Канада, Австралія та й Західна Європа знають");
    assertEmptyMatch("процес формування уряду та його відставка залежать");
    assertEmptyMatch("Усі розписи, а також архітектура відрізняються");
    assertEmptyMatch("Німеччина (ще демократична) та Росія почали «дружити»");
    assertEmptyMatch("Як Україна, так і Німеччина відчувають");
    assertEmptyMatch("обласної ради, а й уся львівська громада виявилися обманутими");
    assertEmptyMatch("Авіація ж і космонавтика справили на розвиток науки");
    
    
    //TODO: conj + adv + conj
    assertEmptyMatch("І спочатку Білорусь, а тепер і Україна пішли");
    assertEmptyMatch("Саме тоді Англія, а невдовзі й уся Європа дізналися");
    assertEmptyMatch("але концепція, а потім і програма мають бути");
    
    assertEmptyMatch("Низка трагедій, а потім громадянська війна вигубили чимало люду");
    //TODO: 
//    assertEmptyMatch("допомога з безробіття, а згодом – невелика зарплатня покриють витрати на ліки.");

    //TODO: inserts
//    assertEmptyMatch("для яких культура, а отже, і культурна дипломатія належать");
//    assertEmptyMatch("Франція, Китай і, певною мірою, Росія зуміли поставити");
    assertEmptyMatch("Наша родина, я особисто і наша політична сила займаємося благодійністю");
    assertEmptyMatch("і держава, і, відповідно, посада перестали існувати");

//  assertEmptyMatch("українка й на півтора роки молодша кубинка ведуть");

//    assertEmptyMatch("Революція 1917-го і націоналізація стали стресом, але глобально на підприємстві нічого не змінили.");

    // noun:v_naz noun:v_rod
    //TODO: conflicts with test below
//  assertMatches(1, "Згідно розуміння класиків, народна мова надавалися лише для творів");
    assertEmptyMatch("Швидке заселення земель, вирубування лісів, меліорація призвели");
    assertEmptyMatch("узагалі, почуття гумору, іронія були притаманні");
    assertEmptyMatch("почуття гумору, іронія були притаманні");
    assertEmptyMatch("оскільки назва фільму, прізвище режисера, країна будуть викарбувані на бронзовій плиті");
    assertEmptyMatch("Дух Києва, його атмосфера складаються");
    assertEmptyMatch("Верховний суд, Рада суддів, Кваліфікаційна комісія могли б перевірити");
    assertEmptyMatch("зеленкуваті плями моху, сіро-коричнева бруківка повертають нас на кілька століть назад");
    assertEmptyMatch("водій “Мазди” та її пасажир загинули");
//    assertEmptyMatch("водій “Жигулів” та його пасажирка померли.");

    //TODO: noun:v_naz noun:v_oru conj
//    assertEmptyMatch("Біг підтюпцем і швидка ходьба знижують ризик гіпертонії");
    
    // adj conj adj noun
    assertEmptyMatch("біологічна і ядерна зброя стають товаром");
    assertEmptyMatch("І та й інша група вводили в клітини шкіри");
    
    //TODO: noun+verb + conj ...
//    assertEmptyMatch("бажання співати і наполеглива праця допомогли");
    
    //TODO: ignore quotes and parenthesis
//    assertEmptyMatch("руки в мозолях та “робоча” засмага підказували");
//    assertEmptyMatch("Ресурси та (або) політична воля закінчилися 21 лютого.");
    
    // пара
//    assertEmptyMatch("зіркова пара Пишняк — Толстой вирішили вивести");
    
    // latin/cyr mix
//  assertEmptyMatch("Дівчата та їхнiй брат належать до касти");

    assertEmptyMatch("за яким 50%+1 акція закріплюються у власності держави");
    assertEmptyMatch("злість плюс іронія можуть вбити");
    assertEmptyMatch("із яких 50% плюс одна акція знаходяться");
    assertEmptyMatch("Матеріальна заінтересованість плюс гарна вивіска зіграли злий жарт");
    
    assertEmptyMatch("Колесніков/Ахметов посилили");
        
    // plural "semi-numeric"
    assertEmptyMatch("решта забороняються");
    assertEmptyMatch("все решта відійшло на другий план");
    assertEmptyMatch("Більш ніж половина віддали голоси");
    assertEmptyMatch("Левова їхня частка працюють через російських туроператорів");
    assertEmptyMatch("дві групи з трьох осіб кожна виконували просте завдання");
    
    assertEmptyMatch("Що пачка цигарок, що ковбаса коштують");
    assertEmptyMatch("не вулична злочинність, не корупція відлякували");
    
    // very long
    assertEmptyMatch("Леонід Новохатько й керівник головного управління з питань гуманітарного розвитку адміністрації\n" + 
        "Президента Юрій Богуцький обговорили");
    assertEmptyMatch("інший нардеп та лідер «Правого сектору» Дмитро Ярош просили");
    
//    assertEmptyMatch("Квіташвілі, міністр інфраструктури Андрій Пивоварський відкликали");
    
    //TODO:
//    assertEmptyMatch("Ні світ, ані навіть Європа чекати не будуть");
    
    // пара
    assertEmptyMatch("пара Катерина Морозова/Дмитро Алексєєв тріумфувала");
  }
  
  @Test
  public void testNum() throws IOException {
    assertEmptyMatch("понад тисяча отримали поранення");
    assertEmptyMatch("Решта 121 депутат висловилися проти");
    assertEmptyMatch("понад сотня отримали поранення");

    assertMatches(1, "22 льотчики удостоєно");
    assertEmptyMatch("два сини народилося там");
    //TODO:
    //assertEmptyMatch("Троє пілотів і 31 глядач загинули миттєво.");
  }

  @Test
  public void testMascFem() throws IOException {
    // masc-fem
    assertEmptyMatch("німецький канцлер зателефонувала російському президенту");
    assertEmptyMatch("екс-міністр повторила у телезверненні");
    assertEmptyMatch("Прем’єр-міністр повторила у телезверненні");
    assertEmptyMatch("Прем’єр—міністр повторила у телезверненні");
    assertEmptyMatch("єврокомісар зазначила, що");
    assertEmptyMatch("кінолог пояснила");
    
    // compound
    assertEmptyMatch("автор-упорядник назвала збірник");
    
    assertMatches(1, "Прем’єр-міністр повторило у телезверненні");
    assertMatches(1, "приятель повторила у телезверненні");
  }

  
  @Test
  public void testIgnoreByIntent() throws IOException {
    // handled by xml rule
    //  assertMatches(1, "тому що воно привнесено ззовні");
    //  assertMatches(1, "Воно просочено історією");
    assertEmptyMatch("тому що воно привнесено ззовні");
    assertEmptyMatch("Воно просочено історією");
    assertEmptyMatch("Все решта зафіксовано");
    assertEmptyMatch("решта зафіксовано");

    assertEmptyMatch("З охопленого війною Сектора Газа вивезли «більш як 80 громадян України».");
    
    // має бути «подружжя Обам» - ловиться в xml
//    assertMatches(1, "подружжя Обама запросило 350 гостей");
    assertEmptyMatch("подружжя Обама запросило 350 гостей");
  }
  
  @Test
  public void testOverTheWord() throws IOException {
    assertEmptyMatch("діагноз дизентерія підтвердився");

    //TODO:
    // assertEmptyMatch("Свого часу породу фокстер’єр вивели");
  }
  
  @Test
  public void testCaseGovernment() throws IOException {
    assertEmptyMatch("коли українцям пора показати");
    assertEmptyMatch("або пропозиція збільшити частку");
  }
  
  private void assertEmptyMatch(String text) throws IOException {
    AnalyzedSentence analyzedSentence = lt.getAnalyzedSentence(text);
    try {
      assertEquals(Collections.<RuleMatch>emptyList(), Arrays.asList(rule.match(analyzedSentence)));
    }
    catch (AssertionError e) {
      System.out.println("Sentence: " + analyzedSentence);
      throw e;
    }
    
  }

  
  @Test
  public void testRuleWithAdjOrKly() throws IOException {
    assertMatches(1, "Ви постане перед вибором");
    assertMatches(1, "військовий прибігла");
    assertMatches(1, "Фехтувальна збірна було до цього готова");
    assertMatches(1, "наші дівчата виграти загальний залік");
    assertMatches(1, "окремі ентузіасти переймалася");
    assertMatches(1, "угорська влада пообіцяли переглянути");
    assertMatches(1, "точно також ви буде платити");
    
    assertMatches(0, "багато хто в драматурги прийшов");
    assertMatches(0, "з кандидатом у президенти не визначився");
    assertMatches(0, "Мої співвітчизники терпіти це далі не повинні");
    assertMatches(0, "Ви може образились");
    assertMatches(0, "Моя ти зоре в тумані");
    assertMatches(0, "На кожен покладіть по кільцю");
    assertMatches(0, "Любителі фотографувати їжу");
  }
  
  @Test
  public void testSpecialChars() throws IOException {
    assertEmptyMatch("Тарас при\u00ADбіг.");

    RuleMatch[] matches = rule.match(lt.getAnalyzedSentence("Тарас при\u00ADбігла."));
    assertEquals(1, matches.length);

    matches = rule.match(lt.getAnalyzedSentence("Та\u00ADрас прибігла."));
    assertEquals(1, matches.length);
  }

  
  private void assertMatches(int num, String text) throws IOException {
    RuleMatch[] match = rule.match(lt.getAnalyzedSentence(text));
    assertEquals("Unexpected: " + Arrays.asList(match), num, match.length);
  }

  private static final String GOOD_TEXT = "Хоча упродовж десятиліть ширилися численні історії про те, що я був у ряду наступників трону Тембу, щойно наведений простий генеалогічний екскурс викриває міфічність таких тверджень."
      + " Я був членом королівської родини, проте не належав до небагатьох привілейованих, що їх виховували на правителів."
      + " Натомість мене як нащадка Лівого дому навчали — так само, як і раніше мого батька — бути радником правителів племені."
      + " Мій батько був високим темношкірим чоловіком із прямою й величною поставою, яку я, хочеться думати, успадкував."
      + " У батька було пасмо білого волосся якраз над чолом, і хлопчиком я бувало брав сірий попіл і втирав його у своє волосся, щоб воно було таке саме, як у тата."
      + " Батько мій мав сувору вдачу й не шкодував різки, виховуючи дітей. Він міг бути дивовижно впертим, і це ще одна риса, яка, на жаль, теж могла перейти від батька до сина."
      + " Мого батька інколи називали прем’єр-міністром Тембуленду за врядування Далінд’єбо, батька Сабати, який правив на початку 1900-х років, та його сина й наступника Джонгінтаба."
      + " Насправді ж такого титулу не існувало, але мій батько справді відіграв роль, яка не надто відрізнялася від функції прем’єра."
      + " Як шанований і високо цінований радник обох королів, він супроводжував їх у подорожах і зазвичай перебував поруч із ними на важливих зустрічах із урядовими чиновниками."
      + " Він був також визнаним хоронителем історії коса, і частково через це його порадами так дорожили. Моє власне зацікавлення історією прокинулося рано, і батько його підживлював."
      + " Він не вмів ні читати, ні писати, але мав репутацію чудового оратора, який захоплював слухачів, розважаючи й водночас повчаючи їх.";

}
