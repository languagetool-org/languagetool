INFORMACJA

Morfologik to projekt tworzenia polskich słowników morfosyntaktycznych (stąd nazwa) służących do znakowania 
morfosyntaktycznego i syntezy gramatycznej.

WERSJA: 2.0 PoliMorf

UTWORZONA:  8 mar 2013 12:05:02

LICENCJA

Copyright © 2013 Marcin Miłkowski
Wszelkie prawa zastrzeżone
Redystrybucja i używanie, czy to w formie kodu źródłowego, czy w formie kodu wykonawczego, są dozwolone pod warunkiem spełnienia poniższych warunków:
1.	Redystrybucja kodu źródłowego musi zawierać powyższą notę copyrightową, niniejszą listę warunków oraz poniższe oświadczenie o wyłączeniu odpowiedzialności.
2.	Redystrybucja kodu wykonawczego musi zawierać powyższą notę copyrightową, niniejszą listę warunków oraz poniższe oświadczenie o wyłączeniu odpowiedzialności w dokumentacji i/lub w innych materiałach dostarczanych wraz z kopią oprogramowania.
TO OPROGRAMOWANIE JEST DOSTARCZONE PRZEZ <POSIADACZA PRAW AUTORSKICH> „TAKIM, JAKIE JEST”. KAŻDA, DOROZUMIANA LUB BEZPOŚREDNIO WYRAŻONA GWARANCJA, NIE WYŁĄCZAJĄC DOROZUMIANEJ GWARANCJI PRZYDATNOŚCI HANDLOWEJ I PRZYDATNOŚCI DO OKREŚLONEGO ZASTOSOWANIA, JEST WYŁĄCZONA. W ŻADNYM WYPADKU <POSIADACZE PRAW AUTORSKICH> NIE MOGĄ BYĆ ODPOWIEDZIALNI ZA JAKIEKOLWIEK BEZPOŚREDNIE, POŚREDNIE, INCYDENTALNE, SPECJALNE, UBOCZNE I WTÓRNE SZKODY (NIE WYŁĄCZAJĄC OBOWIĄZKU DOSTARCZENIA PRODUKTU ZASTĘPCZEGO LUB SERWISU, ODPOWIEDZIALNOŚCI Z TYTUŁU UTRATY WALORÓW UŻYTKOWYCH, UTRATY DANYCH LUB KORZYŚCI, A TAKŻE PRZERW W PRACY PRZEDSIĘBIORSTWA) SPOWODOWANE W JAKIKOLWIEK SPOSÓB I NA PODSTAWIE ISTNIEJĄCEJ W TEORII ODPOWIEDZIALNOŚCI KONTRAKTOWEJ, CAŁKOWITEJ LUB DELIKTOWEJ (WYNIKŁEJ ZARÓWNO Z NIEDBALSTWA JAK INNYCH POSTACI WINY), POWSTAŁE W JAKIKOLWIEK SPOSÓB W WYNIKU UŻYWANIA LUB MAJĄCE ZWIĄZEK Z UŻYWANIEM OPROGRAMOWANIA, NAWET JEŚLI O MOŻLIWOŚCI POWSTANIA TAKICH SZKÓD OSTRZEŻONO.

ŹRÓDŁO

Dane pochodzą ze słownika sjp.pl oraz słownika PoliMorf i są licencjonowane na powyższej licencji. Dane źródłowe pochodzą z polskiego słownika ispell, następnie redagowanego na stronach kurnik.pl/slownik i sjp.pl, a także Słownika Gramatycznego Języka Polskiego. Autorzy: (1) ispell: Mirosław Prywata, Piotr Gackiewicz, Włodzimierz Macewicz, Łukasz Szałkiewicz, Marek Futrega.
(2) SGJP: Zygmunt Saloni, Włodzimierz Gruszczyński, Marcin Woliński, Robert Wołosz.

Wersja PoliMorf została opracowana w ramach projektu CESAR realizowanego w Zespole Inżynierii Lingwistycznej IPI PAN.

PLIKI

1. morfologik.txt to plik tekstowy z polami rozdzielanymi tabulatorem,
o następującym formacie:

forma-odmienionaHTforma-podstawowaHTznaczniki

gdzie HT oznacza tabulator poziomy.

Kodowanie: UTF-8

2. polish.dict to binarny plik słownika dla programu fsa_morph Jana Daciuka 
(zob. http://www.eti.pg.gda.pl/katedry/kiw/pracownicy/Jan.Daciuk/personal/fsa.html), 
wykorzystywany również bezpośrednio przez korektor gramatyczny LanguageTool.

3. polish_synth.dict to binarny plik słownika syntezy gramatycznej,
używany w fsa_morph i LanguageTool. Aby uzyskać formę odmienioną,
należy używać następującej składni w zapytaniu programu fsa_morph:

<wyraz>|<znacznik>

Na przykład:

niemiecki|adjp

daje "niemiecku+".

Uwaga: słownik polish_synth.dict dostarczany jest z kompresją cfsa2 obsługiwaną obecnie
jedynie przez bibliotekę morfologik-stemming.

4. polish.info i polish_synth.info - pliki wymagane do użycia plików
binarnych w bibliotece morfologik-stemming.

ZNACZNIKI MORFOSYNTAKTYCZNE

Zestaw znaczników jest zbliżony do zestawu korpusu IPI (www.korpus.pl).

    * adj - przymiotnik (np. „niemiecki”)
	* adjc - przymiotnik przedykatywny (np. „ciekaw”, „dłużen”)
    * adjp - przymiotnik poprzyimkowy (np. „niemiecku”)
    * adv - przysłówek (np. „głupio”)
	* burk - burkinostka (np. „Burkina Faso”)
	* depr - forma deprecjatywna
	* ger - rzeczownik odsłowny
    * conj - spójnik łączący zdania współrzędne
	* comp - spójnik wprowadzający zdanie podrzędne
    * num - liczebnik
    * pact - imiesłów przymiotnikowy czynny
    * pant - imiesłów przysłówkowy uprzedni
    * pcon - imiesłów przysłówkowy współczesny
    * ppas - imiesłów przymiotnikowy bierny
    * ppron12 - zaimek nietrzecioosobowy
    * ppron3 - zaimek trzecioosobowy
    * pred - predykatyw (np. „trzeba”)
    * prep - przyimek
    * siebie - zaimek "siebie"
    * subst - rzeczownik
    * verb - czasownik
    * brev - skrót
    * interj - wykrzyknienie
	* qub - kublik (np. „nie” lub „tak”)

Atrybuty podstawowych form:

    * sg / pl - liczba pojedyncza / liczba mnoga    
    * nom - mianownik
    * gen - dopełniacz
    * acc - biernik
    * dat - celownik
    * inst - narzędnik
    * loc - miejscownik
    * voc - wołacz
    * pos - stopień równy
    * com - stopień wyższy
    * sup - stopień najwyższy
    * m1, m2, m3 - rodzaje męskie
    * n1, n2 - rodzaje nijakie
	* p1, p2, p3 - rodzaje rzeczowników mających tylko liczbę mnogą (pluralium tantum)
    * f - rodzaj żeński
    * pri - pierwsza osoba
    * sec - druga osoba
    * ter - trzecia osoba
    * aff - forma niezanegowana
    * neg - forma zanegowana
    * refl - forma zwrotna czasownika
	* nonrefl - forma niezwrotna czasownika
	* refl.nonrefl - forma może być zwrotna lub niezwrotna
    * perf - czasownik dokonany
    * imperf - czasownik niedokonany
    * imperf.perf - czasownik, który może występować zarówno jako dokonany, jak i jako niedokonany
    * nakc - forma nieakcentowana zaimka
    * akc - forma akcentowana zaimka
    * praep - forma poprzyimkowa
    * npraep - forma niepoprzyimkowa
    * ger - rzeczownik odsłowny
    * imps - forma bezosobowa
    * impt - tryb rozkazujący
    * inf - bezokolicznik
    * fin - forma nieprzeszła
    * bedzie - forma przyszła "być"
    * praet - forma przeszła czasownika (pseudoimiesłów)
    * pot - tryb przypuszczający [nie występuje w znacznikach IPI]
    * pun - skrót z kropką [za NKJP]
    * npun - bez kropki [za NKJP]	
	* wok / nwok: forma wokaliczna / niewokaliczna

W znacznikach Morfologika nie występuje i nie będzie występować znacznik aglt, a to ze względu na inną zasadę segmentacji wyrazów.

Morfologik, (c) 2007-2013 Marcin Miłkowski.
