# VivaPilot 1.23 – testy na telefonie z panelem testowym

Ten plik jest dla Ciebie, bez żargonu. Wersja **1.23** to aplikacja **VivaPilot** (nowa nazwa) z nowymi tekstami prawnymi + **tymczasowy panel testowy**, dzięki któremu wszystko sprawdzisz **samymi przyciskami na telefonie** – bez Play Console, bez kont testerów, bez czekania 5 dni.

---

## 1. Co zainstalować

Z GitHub Actions pobierz plik APK (ten sam sposób co zwykle):

- `app-noads-debug.apk` – zwykła wersja (bez reklam) – **zacznij od niej**,
- `app-ads-debug.apk` – wersja z reklamami (tylko do testu reklam).

**Ważne – nowy identyfikator aplikacji.** VivaPilot ma teraz identyfikator `com.vivapilot.iptv` (wcześniej `com.iptvplayer.app`). Telefon traktuje go jako **zupełnie nową aplikację**: instalacja **nie nadpisze** starej wersji 1.05 – obie będą obok siebie. **Odinstaluj starą aplikację** (ta ze starą nazwą pod ikoną), żeby ich nie mylić. Nowa aplikacja startuje od zera: ekran powitalny, nowy okres próbny, puste listy (playlisty dodasz ponownie albo wczytasz z kopii zapasowej z Ustawień).

> Zgody z wersji 1.05 **nie** przechodzą do 1.12 – rejestr zgód jest nowy (zaszyfrowany), więc zobaczysz ekran powitalny i zaakceptujesz Regulamin 1.12 od nowa. To zamierzone.

**Jak poznać, że to wersja testowa:** przy prawej krawędzi ekranu, mniej więcej w połowie wysokości, jest mały **pomarańczowy znacznik „TEST”**. Dotknij go – otwiera się **panel testowy**. W wersji, którą wrzucisz do Google Play, tego znacznika **nie będzie** (kod panelu w ogóle się tam nie kompiluje).

---

## 2. Jak czytać panel

Panel ma 6 części (przewijasz palcem w dół):

| Część | Do czego służy |
|---|---|
| **1. STAN** | Podgląd, co aplikacja teraz „wie”: dzień okresu próbnego, czy kupione, jakie zdarzenia zgody są zapisane w rejestrze, wersje dokumentów. Zawsze tu zajrzyj, żeby sprawdzić wynik testu. |
| **2. CZAS** | Przyspieszanie okresu próbnego: +1 / +4 / +5 / +30 dni, cofnięcie czasu, start próby od nowa. |
| **3. ZAKUP** | Symulacja płatności: zakup „jak użytkownik”, płatność oczekująca, przywrócony zakup, zwrot. |
| **4. ZGODY I OSTRZEŻENIA** | Czyszczenie zgód (ekran powitalny wraca), udawanie zmiany Regulaminu, ponowne pokazanie ostrzeżeń. |
| **5. GOTOWE SCENARIUSZE** | Jedno kliknięcie = „ostatni dzień próby” albo „próba skończona”. |
| **6. APLIKACJA** | Zamknięcie aplikacji całkiem; wyczyszczenie wszystkiego (jak świeża instalacja). |

Po każdym naciśnięciu przycisku u góry panelu pojawia się zielony napis **✔ co zostało zrobione**. Niektóre przyciski **same zamykają aplikację** (napisane jest to przy nich) – wtedy po prostu **otwórz ją ponownie z ikony**. To jest dokładnie to samo, co „zamknięcie aplikacji i uruchomienie na nowo” u prawdziwego użytkownika.

**Symulowana płatność:** aplikacja zainstalowana z pliku nie może kupować w Google Play, więc przy „Przejdź do płatności” zobaczysz **atrapę okna płatności** (napis „SYMULACJA okna płatności Google Play”) z wyborem: *Zapłać – sukces* / *Płatność oczekująca* / *Anuluj*. Żadne pieniądze nie są pobierane. Wszystko po stronie aplikacji (ekran przed zakupem, zapis zgód do rejestru, ekran potwierdzenia, odblokowanie funkcji) działa **tak samo jak przy prawdziwym zakupie**.

---

## 3. Scenariusze – „kliknij to, zobacz tamto”

Rób je po kolei – każdy zaczyna się od stanu, w którym skończył poprzedni (gdzie trzeba zacząć od zera, jest to napisane).

### Scenariusz 1 – Świeży użytkownik, ekran powitalny *(Test 1 i 1a)*
1. Dotknij **TEST** → przewiń do **6. APLIKACJA** → **„Zupełnie nowy użytkownik – WYCZYŚĆ WSZYSTKO”** → potwierdź. Aplikacja się zamknie.
2. Otwórz aplikację z ikony.
   → **Tylko** ekran „Witaj w VivaPilot”. Przycisk „Rozpocznij” szary. Pod polem mała czcionka „Bez akceptacji nie możesz korzystać z aplikacji…”. Żadnych list, kanałów ani okna zakupu.
3. Naciśnij **Wstecz** → aplikacja się zamyka. Otwórz znów → znów ekran powitalny.
4. Naciśnij „Nie akceptuję – zamknij aplikację” → zamyka się. Otwórz znów → znów ekran powitalny.
5. Dotknij „Regulamin” i „Polityka prywatności” → otwierają się teksty (Regulamin i Polityka w wersji 1.23) z linią „Wersja aplikacji i dokumentów: 1.23”; Wstecz wraca na ekran powitalny.
6. Zaznacz pole → „Rozpocznij” robi się aktywne → naciśnij. Wchodzisz do aplikacji.
7. Dotknij **TEST** → część **1. STAN** → „Rejestr zgód (zaszyfrowany)” pokazuje jedno zdarzenie: ekran powitalny z datą (UTC), Regulamin 1.23_PL, Polityka 1.23_PL, 18+: tak.

### Scenariusz 2 – Podgląd „Moje zgody” i pobranie potwierdzenia *(Test 11)*
1. Ustawienia › Informacje prawne › **Moje zgody** → „Zdarzenie 1: akceptacja na ekranie powitalnym”: data i godzina (UTC), wersja aplikacji 1.23, Regulamin 1.23_PL, Polityka 1.23_PL, platforma GOOGLE_PLAY, „Oświadczenie o pełnoletności: złożone” i „Akceptacja Regulaminu…” (każde z własną godziną) oraz identyfikator zdarzenia. Niżej: „Brak zdarzenia zakupu…”.
2. Pod zapisem jest tekst „**Zapisu nie można zmienić. Jeśli przestajesz zgadzać się z Regulaminem, zaprzestań korzystania z aplikacji i ją odinstaluj.**”. Ekran jest tylko do odczytu – nie ma żadnego przycisku edycji, cofnięcia ani kasowania (są tylko „Pobierz potwierdzenie zgód” i „Wstecz”).
3. Naciśnij **„Pobierz potwierdzenie zgód”** → system pyta, gdzie zapisać plik (`VivaPilot_potwierdzenie_zgod.txt`). Zapisz i otwórz plik w telefonie: na górze czytelny opis zdarzeń (po polsku, w języku aplikacji), na dole ten sam zapis w formacie JSON. Pojawia się komunikat „Potwierdzenie zgód zapisane.”.

### Scenariusz 3 – Koniec okresu próbnego *(Test 2)*
1. **TEST** → **2. CZAS** → **„+5 dni”**. W STAN: „Okres próbny: MINĄŁ”.
2. Zamknij panel („Zamknij panel” albo Wstecz). Spróbuj użyć wyszukiwania, „Moich list” albo pobierania.
   → Okno „Okres próbny zakończony” z „Kup Pełną wersję” i „Nie teraz”. Te trzy funkcje są zablokowane.
3. Oglądanie kanałów/filmów/seriali i przeglądanie działają **bez ograniczeń**.
4. Wariant krok po kroku: **TEST** → **2. CZAS** → „Zacznij okres próbny od nowa”, potem „+1 dzień” kilka razy i patrz w STAN: „TRWA – dzień 2 z 5”, „dzień 3 z 5”… aż „MINĄŁ”.

### Scenariusz 4 – Zakup od początku do końca *(Test 3)*
Zacznij ze stanu po Scenariuszu 3 (próba skończona, okno zakupu widoczne).
1. Kliknij **„Kup Pełną wersję”**.
   → Ekran „Pełna wersja VivaPilot”: **dwa puste pola** i zawsze widoczna linia „Wydawca: Damian Gałka, adres przedsiębiorstwa i do korespondencji: ul. Przybyszewskiego 209B/9, 93-120 Łódź, dg.komunikacja@gmail.com”. „Przejdź do płatności” nieaktywne.
   Pole 1 zaczyna się od „Rozumiem i osobno akceptuję, że działanie odtwarzania i katalogu…”, pole 2 od „Wyrażam zgodę na natychmiastowe rozpoczęcie świadczenia…”.
2. Zaznacz tylko jedno pole → nadal nieaktywne (sprawdź oba warianty: samo pole 1 i samo pole 2). Zaznacz oba → aktywne.
   **Pod przyciskiem** „Przejdź do płatności” jest drobny druk: „Klikając „Przejdź do płatności”, potwierdzasz, że zapoznałeś się z Regulaminem i Polityką prywatności. Płatność obsługuje Google Play. Szczegóły: § 4, § 8 i § 9 Regulaminu.” Słowa **Regulaminem** i **Polityką prywatności** są podkreślone i klikalne – dotknij każde: otwiera się odpowiedni dokument, Wstecz wraca na ekran zakupu (pola zostają zaznaczone).
3. Naciśnij **„Przejdź do płatności”** → pojawia się atrapa okna płatności.
4. Najpierw wybierz **„Anuluj”** → wracasz bez zakupu (STAN: „brak zakupu”, funkcje nadal zablokowane).
5. Powtórz 1–3 i wybierz **„Płatność oczekująca”** → STAN: „OCZEKUJE na potwierdzenie płatności”, funkcje **nadal zablokowane** (płatność niezatwierdzona nie odblokowuje).
6. Powtórz 1–3 i wybierz **„Zapłać – sukces”**.
   → Pełna wersja odblokowuje się **od razu**. Ekran „Pełna wersja odblokowana” pokazuje numer zamówienia (zaczyna się od `GPA.TEST-`), datę, linię „Wydawca: …dg.komunikacja@gmail.com”, tekst „Przed zakupem: (1) odrębnie zaakceptowałeś… (Regulamin § 4 ust. 4–5); (2) wyraziłeś zgodę na natychmiastowe rozpoczęcie świadczenia… (art. 38 ust. 1 pkt 13 ustawy o prawach konsumenta).” oraz wersję Regulaminu. Są dwa przyciski zapisu: „Zapisz potwierdzenie” (plik `VivaPilot_potwierdzenie_zakupu.txt`: potwierdzenie + tekst Regulaminu) i **„Pobierz potwierdzenie zgód”** (plik `VivaPilot_potwierdzenie_zgod.txt`). Wypróbuj oba.
7. Zamknij ekran. Wyszukiwanie, Moje listy i pobieranie działają. STAN: „KUPIONE”, a w „Rejestrze zgód” są **dwa zdarzenia** (ekran powitalny i zakup z numerem zamówienia).
8. Ustawienia › Informacje prawne › **Moje zgody** → „Zdarzenie 2: zakup Pełnej wersji”: numer zamówienia (transakcji), Regulamin/Polityka 1.23_PL oraz **dwie osobne godziny** zgód: „Odrębna akceptacja: działanie funkcji zależnych od źródła treści…” i „Zgoda na natychmiastowe rozpoczęcie świadczenia…”. Przycisk „Pobierz potwierdzenie zgód” zapisuje teraz oba zdarzenia (sprawdź w pliku: sekcja „Zdarzenie 2” z numerem zamówienia i JSON z `store_transaction_id`).

### Scenariusz 5 – Zamknięcie i ponowne otwarcie po zakupie *(Test 14, pkt 1)*
1. **TEST** → **6. APLIKACJA** → **„Zamknij aplikację całkiem”**, potem otwórz z ikony.
   → Aplikacja od razu jest „kupiona” (STAN: KUPIONE), funkcje działają, **ekran potwierdzenia NIE wyskakuje ponownie** (pokazuje się tylko raz, zaraz po zakupie).
2. „Moje zgody” nadal pokazują zdarzenie zakupu (te same godziny i numer – nic się nie nadpisało, nie ma też trzeciego zdarzenia).
3. Ustawienia › Informacje prawne › Zakup › „Pokaż potwierdzenie” (albo w panelu **3. ZAKUP** → „Pokaż ekran potwierdzenia zakupu”) → ekran potwierdzenia na żądanie.

### Scenariusz 6 – Zwrot *(Test 4 i Test 14, pkt 2)*
1. **TEST** → **3. ZAKUP** → **„ZWROT / brak zakupu”**. STAN: „brak zakupu”.
2. Zamknij panel i spróbuj wyszukać.
   → Pełna wersja znów **zablokowana** (okno zakupu), bo okres próbny minął.
3. Opcjonalnie: **6. APLIKACJA** → „Zamknij aplikację całkiem”, otwórz ponownie → nadal brak zakupu (zwrot „przetrwał” restart). W „Moje zgody” stare zdarzenie zakupu **zostaje** – to historia, nie odblokowanie.

### Scenariusz 7 – Zakup przywrócony po reinstalacji / na nowym urządzeniu *(Test 5)*
1. **TEST** → **3. ZAKUP** → **„Ustaw: ZAKUP PRZYWRÓCONY”**. (Udaje, że Google Play mówi „to konto już kiedyś kupiło” – bez ekranu przed zakupem.)
   → STAN: KUPIONE, funkcje odblokowane, ekran potwierdzenia **nie** wyskakuje sam.
2. Ustawienia › Informacje prawne › Zakup › „Pokaż potwierdzenie” – ekran dostępny na żądanie.
3. „Moje zgody” pokazują wtedy zdarzenie „zakup wykryty na koncie Google (bez ekranu przed zakupem w tej instalacji)” – z numerem zamówienia, ale **bez** zgód zakupowych (w tej instalacji nikt ich nie składał).
4. Przycisk **„Sprawdź zakup ponownie”** (w panelu lub w Ustawieniach) – bez błędu, zakup nadal wykryty.

### Scenariusz 8 – Zmiana Regulaminu *(część 10)*
1. **TEST** → **4. ZGODY I OSTRZEŻENIA** → **„Udawaj, że Regulamin i Polityka się zmieniły…”**. Aplikacja się zamknie.
2. Otwórz z ikony.
   → Raz pojawia się „Zmieniamy Regulamin od [data]. Najważniejsze zmiany: …” (data i opis zmian pochodzą z nagłówka pliku z Regulaminem 1.23) z „Przeczytaj” i „OK”. Po „OK” **nie wraca** (zamknij i otwórz jeszcze raz, żeby się upewnić).

### Scenariusz 9 – Ostrzeżenia *(część 8)*
- „Wyślij do TV” (rzutowanie): ostrzeżenie tylko przy pierwszym użyciu, przycisk „Rozumiem”. Żeby zobaczyć je znowu: **TEST** → **4.** → „Pokaż znów ostrzeżenie o rzutowaniu”.
- Eksport kopii zapasowej: ostrzeżenie **za każdym razem**, „Eksportuj” / „Anuluj”.
- Pobrane › czerwony status „Nieudane” → okno z wyjaśnieniem i „OK”.

### Scenariusz 10 – Wersja i język *(Test 10 i 13)*
- Ustawienia › Informacje prawne: „Wersja aplikacji i dokumentów: **1.23**”; ten sam numer na górze Regulaminu i Polityki; w panelu STAN: „Wersja aplikacji 1.23 (kod 13)”.
- Przełącz język aplikacji na np. angielski i otwórz Regulamin/Politykę → **nad** tekstem jest notka „The Polish version of the document is the binding one…”. Po polsku notki **nie ma**.

### Scenariusz 10a – Nazwa VivaPilot i dane wydawcy *(punkty 0 i 5)*
- Nazwa pod ikoną na telefonie: **VivaPilot**. Ekran startowy, baner na Android TV, ekran powitalny, Ustawienia › O aplikacji i „Informacje prawne” – wszędzie widać nowe logo/napis **VivaPilot**; nigdzie nie ma starej nazwy ani napisu „IPTV Player”.
- Linia „Wydawca: Damian Gałka, adres przedsiębiorstwa i do korespondencji: ul. Przybyszewskiego 209B/9, 93-120 Łódź, dg.komunikacja@gmail.com” jest w **trzech** miejscach: na ekranie przed zakupem, na ekranie potwierdzenia zakupu i w Ustawieniach › Informacje prawne (pod listą licencji). Starego adresu (skrytki pocztowej) ani starego e-maila **nie ma nigdzie** – sprawdź też teksty Regulaminu i Polityki oraz wpis o libVLC pod „Licencje” (e-mail w nim to dg.komunikacja@gmail.com).

### Scenariusz 11 – Skracanie dróg (gotowe scenariusze)
W części **5. GOTOWE SCENARIUSZE**:
- **„Ostatni dzień próby”** → aplikacja się zamyka; po otwarciu STAN: „TRWA – dzień 5 z 5”. Dodaj „+1 dzień” w CZAS i próba się kończy.
- **„Okres próbny SKOŃCZONY, brak zakupu”** → po otwarciu od razu stan z okna zakupu.

### Scenariusz 12 – Wariant `ads` *(Test 6, część dostępna na telefonie)*
Zainstaluj `app-ads-debug.apk`, zaakceptuj ekran powitalny, w panelu ustaw **2. CZAS → „+5 dni”** (brak zakupu). Sprawdź: baner nad dolnym paskiem na ekranach przeglądania, reklama pełnoekranowa co jakiś czas przy zmianie zakładki, **brak** reklam w odtwarzaczu. Potem zrób Scenariusz 4 (zakup) → **reklamy znikają natychmiast**. Wstęp o reklamach pokażesz znów przyciskiem w **4.** („Pokaż znów wstęp o reklamach”).

---

## 4. Czego panel NIE sprawdzi (i jak to zrobić inaczej)

- **Prawdziwa płatność Google Play** – wymaga instalacji przez Google Play (ścieżka testów wewnętrznych) i konta testera licencji. Panel ma przełącznik „Symulacja płatności włączona” (część 3) – po wyłączeniu aplikacja rozmawia z prawdziwym Google Play, ale z pliku APK kupić się nie da. Prawdziwy zakup testowy zrobisz dopiero na wersji z Google Play.
- **Okno zgody Google (UMP) w wersji `ads`** – Google pokazuje je tylko w UE/UK albo na zarejestrowanym urządzeniu testowym (opis w komentarzu w `AdsController.kt`).
- **Android TV / pilot (D-pad)** i **brak uprawnienia AD_ID w `noads`** – ten panel jest pod dotyk; pilota sprawdź na TV, a AD_ID narzędziem `aapt dump permissions` (Test 7 w głównym dokumencie).
- **Brak wysyłania czegokolwiek przed akceptacją** (Test 1a, pkt 2) – jeśli chcesz to udowodnić, użyj darmowej aplikacji PCAPdroid.

---

## 5. Jak to potem usunąć

Gdy skończysz testy, napisz mi **„usuń moduł testowy”**. Wtedy:
- kasuję katalog `app/src/debug/.../testtools` i pusty odpowiednik w `app/src/release/`,
- usuwam trzy wywołania modułu w `BillingManager.kt` i jedno w `MainActivity.kt`,
- usuwam pomocnicze funkcje testowe z `LocalStore.kt`,
- podnoszę wersję zgodnie z regułą (+0,01 i `versionCode` +1) i oddaję czystą paczkę.

Do tego czasu wersja przeznaczona do Google Play (`release`) **i tak nie zawiera ani linijki** kodu panelu – znacznik „TEST” istnieje wyłącznie w plikach `*-debug.apk`.

---

## 6. Uczciwa uwaga

Nie mam tu Android SDK, więc **nie skompilowałem** tej wersji – sprawdziłem ją statycznie (nawiasy, importy, tłumaczenia, zgodność wersji debug/release). Jeśli GitHub Actions pokaże błąd kompilacji, wklej mi log – poprawię od razu.

---

## Nowość w 1.13: „Propozycje do obejrzenia” (ekran główny), od 1.14 zablokowane po okresie próbnym

Nie wymaga panelu testowego. Na ekranie głównym, nad „Ostatnio dodane • Filmy”, sprawdź półkę z plakatami: kostka 🎲 losuje od nowa, pasek obok niej otwiera okno wyboru gatunków i lat produkcji (można zaznaczyć dowolnie wiele, np. Akcja + Komedia, Lata 90. + Lata 80.). Pełny opis i lista kontrolna: `TESTY_ZGODNOSCI_PRAWNEJ.md`, sekcja 6 (Test 16).

**Blokada po okresie próbnym (1.14):** panel TEST → 2. CZAS → „+5 dni” → wróć na ekran główny. Zamiast plakatów jest wiersz z kłódką „Propozycje do obejrzenia są dostępne w Pełnej wersji. Dotknij, aby odblokować.”; dotknięcie otwiera okno „Kup Pełną wersję”. Po (symulowanym) zakupie (panel TEST → 3. ZAKUP) półka wraca do normalnego działania.

---

## Nowość w 1.16: pasek u góry, układ ekranów, porządki w Ustawieniach

1. **Pasek u góry:** między „Główna” a kropką jest „Próba: N dn.” (N maleje co 24 h; panel TEST → 2. CZAS → „+1 dzień” przyspiesza). Po „+5 dni” bez zakupu napis zmienia się na **„Bezpłatna”** – dotknij go, a otworzy się ekran zakupu. Po symulowanym zakupie (panel TEST → 3. ZAKUP) jest **„Pełna”**. Napisy skalują się do szerokości ekranu.
2. **Kropka połączenia:** obok niej jest samo „OK” (zielona) albo „NOK” (żółta/czerwona); słowa „Połączenie” już nie ma.
3. **Karta z liczbą playlist/kanałów i przyciskiem Odśwież** zniknęła z ekranu głównego – jest teraz na górze **Ustawień**.
4. **Propozycje do obejrzenia** są na ekranie głównym tuż nad „Ostatnio dodane • Filmy”.
5. **Z Ustawień zniknęły:** własne kategorie wyszukiwania, Log błędów i opcje Debug.

## Nowość w 1.16: blokada „Zarządzania kategoriami” i „Pobrane” po okresie próbnym

Panel TEST → 2. CZAS → „+5 dni” → Ustawienia: „Zarządzanie kategoriami” i „Pobrane” są wyszarzone z kłódką; dotknięcie otwiera okno zakupu. Wcześniej ukryte kategorie nadal są ukryte, a pobrane pliki zostają na telefonie. Po symulowanym zakupie (3. ZAKUP) wszystko działa jak wcześniej.

---

## Nowość w 1.20–1.22: testy zmian (lista z promptu 1.16 → 1.22, część 14)

1. **Koniec próby blokuje (panel TEST → 2. CZAS → „+5 dni”, bez zakupu):**
   - Odtwarzacz: ikona ⬇ pokazuje okno „Kup Pełną wersję”; „Kup” przenosi do ekranu zakupu. Nic się nie pobiera.
   - Ekran główny: pole wyszukiwania i mikrofon mają kłódkę; dotknięcie otwiera okno zakupu.
   - Moje listy: utworzenie nowej listy i dodanie pozycji do listy otwiera okno zakupu (Ulubione i oglądanie działają jak wcześniej).
2. **Teksty z pięcioma pozycjami:** okno końca próby oraz ekran powitalny (pierwsze uruchomienie) wymieniają: wyszukiwanie, Moje listy, pobieranie (z ekranem „Pobrane”), „Propozycje do obejrzenia”, zarządzanie kategoriami. Ta sama lista jest na ekranie przed zakupem. Sprawdź też inny język (Ustawienia).
3. **Ekran przed zakupem, pole 1:** w tekście jest „…funkcji „Wyślij do TV” oraz funkcji „Propozycje do obejrzenia” nie jest gwarantowane…”. Pole 2 bez zmian.
4. **Kopia Google (tylko znacznik próby):** (a) zainstaluj, uruchom, zaakceptuj ekran powitalny, dodaj źródło i listę; (b) wymuś kopię (np. `adb shell bmgr backupnow com.vivapilot.iptv`) albo poczekaj na nocną; (c) odinstaluj i zainstaluj ponownie na tym samym koncie. Oczekiwane: **próba nie zaczyna się od nowa** (data pierwszego uruchomienia wraca), ale hasła, źródła, listy, ulubione i zgody **nie wracają** – ekran powitalny pojawia się od nowa, źródła trzeba dodać ponownie. Zakup (jeśli był) wraca z Google Play. Uwaga: przywrócenie z kopii działa tylko przy instalacji ze sklepu/z kopią włączoną; przy zwykłej instalacji APK może nie zadziałać – wtedy test sprawdź na wersji z Google Play (test wewnętrzny).
5. **Aktualizacja z 1.16:** po aktualizacji (bez odinstalowania) data pierwszego uruchomienia zostaje ta sama (znacznik przeniesiony do osobnego pliku).
6. **Odbiornik „Wyślij do TV” (tylko TV):** wyślij z telefonu materiał na TV – na TV pojawia się okno „Urządzenie [nazwa] chce odtworzyć [tytuł]” z przyciskami [Odtwórz] i [Odrzuć] (pilot; fokus startowy na [Odrzuć]). Bez [Odtwórz] nic się nie uruchamia. Odbiornik nie nasłuchuje przed akceptacją ekranu powitalnego. Polecenia spoza sieci lokalnej i adresy inne niż http/https/rtsp/rtmp/udp są odrzucane po cichu.
7. **Brak `FOREGROUND_SERVICE`:** Ustawienia systemu › Aplikacje › VivaPilot › Uprawnienia nie pokazują nic związanego z usługą na pierwszym planie; w pliku manifestu (np. `aapt dump permissions`) nie ma `FOREGROUND_SERVICE`.
8. **Nota libVLC:** Ustawienia › Informacje prawne › wpis o libVLC mówi o LGPL i o informacjach do zbudowania aplikacji z własną, zmodyfikowaną wersją biblioteki (e-mail dg.komunikacja@gmail.com).
9. **Regulamin/Polityka 1.23:** w Polityce nie ma zdania o udostępnianiu zapisu awarii; nagłówek „Wersja aplikacji i dokumentów: 1.23”.

---

## Nowość w 1.22: pobieranie w ograniczonej wersji (część 15)

1. **Pobranie filmu i odcinka w tle:** na plakacie filmu (ikona ⬇), na ekranie odcinków serialu i w odtwarzaczu. Pobieranie idzie dalej po zamknięciu aplikacji (powiadomienie systemowe z postępem). Ekran Ustawienia › **Pobrane** pokazuje postęp, a po zakończeniu przycisk ▶ odtwarza plik w VivaPilot.
2. **Kanał na żywo nie ma pobierania:** na kafelkach kanałów Live i w odtwarzaczu (gdy gra kanał na żywo) nie ma ikony ⬇.
3. **Plik nie jest w publicznym folderze:** po pobraniu otwórz aplikację „Moje pliki” › Pobrane (i galerię) – pobranego filmu tam **nie ma**. Plik leży w pamięci aplikacji (Android/data/com.vivapilot.iptv/files/Download – widoczny np. przez komputer po kablu).
4. **Odinstalowanie usuwa pliki:** odinstaluj aplikację – pobrane pliki znikają razem z nią.
5. **Pobrany plik tylko w VivaPilot:** odtwórz pobrany film z ekranu „Pobrane” – w odtwarzaczu nie ma ikony pobierania, „Otwórz w VLC” ani „Wyślij do TV”.
6. **Stare pobrania (z wersji sprzed 1.23):** zostają na urządzeniu w folderze „Pobrane” (nic ich nie usuwa), ale ekran „Pobrane” w aplikacji ich **nie pokazuje** – pokazuje tylko pobrania zapisane w katalogu aplikacji.
7. **Uprawnienia:** w ustawieniach systemu (Aplikacje › VivaPilot › Uprawnienia) nie ma „Zdjęcia i filmy”/„Pamięć”; brak WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE i READ_MEDIA_VIDEO. Zakładka **Folder** nadal działa (wskazanie folderu przez systemowy wybierak).
8. **Po okresie próbnym** (panel TEST → 2. CZAS → „+5 dni”): pobieranie jest zablokowane wszędzie, także w odtwarzaczu (okno zakupu); „Pobrane” jest wyszarzone.
9. **Komunikat o zmianie Regulaminu:** po aktualizacji z wcześniejszej wersji pojawia się „Zmieniamy Regulamin od **21 października 2026**. Najważniejsze zmiany: Pobieranie materiałów jest tylko w wersji na Androida…” (data słownie, w języku aplikacji).

---

## Nowość w 1.23: wydania sklepowe bez pobierania, wariant `private` z pobieraniem

Są teraz trzy warianty: `noads` i `ads` (sklepowe) oraz `private` (tylko do Twoich testów, nigdy do sklepu; osobna aplikacja „VivaPilot Private”, może stać obok sklepowej).

**A. Wydania sklepowe (`noads`, `ads`) – pobierania nie ma wcale:**
1. Plakat filmu i serialu, ekran szczegółów filmu, lista odcinków, menu kafelka w „Kontynuuj oglądanie”, odtwarzacz: nigdzie nie ma ikony ani polecenia pobierania.
2. Ustawienia: nie ma sekcji „POBRANE” ani wiersza „Pobrane”.
3. Teksty: ekran powitalny, okno końca próby (panel TEST → 2. CZAS → „+5 dni”), ekran przed zakupem i potwierdzenie zakupu wymieniają cztery funkcje (wyszukiwanie, Moje listy, „Propozycje do obejrzenia”, zarządzanie kategoriami) i nie mówią o pobieraniu. Pole 1 na ekranie przed zakupem nie ma słów „pobierania materiałów”.
4. Manifest (np. `aapt dump permissions`): brak READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE i READ_MEDIA_VIDEO.

**B. Wariant `private`:**
1. Na górze „Pełna” (bez „Próba: N dn.”), nic nie jest zablokowane, brak reklam i okna zakupu.
2. Pobranie filmu (ikona ⬇ na plakacie, na szczegółach, w odtwarzaczu) i odcinka (lista odcinków) działa w tle (powiadomienie systemowe z postępem); Ustawienia › **Pobrane** pokazuje postęp i odtwarza plik.
3. Kanał na żywo nie ma opcji pobierania.
4. Plik nie jest w publicznym folderze „Pobrane” ani w galerii (leży w Android/data/com.vivapilot.iptv.private/files/Download); po odinstalowaniu znika.
5. Pobrany plik: bez „Otwórz w VLC”, „Wyślij do TV” i pobierania.
