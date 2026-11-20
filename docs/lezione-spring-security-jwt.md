# Lezione: Spring Security e API stateless con JWT (60 minuti)

## Preparazione prima della lezione

Serve Java 25, Docker Desktop avviato e accesso alle dipendenze Maven. Il progetto usa Spring Boot 4.1.1. PostgreSQL e MongoDB sono già nel compose: non occorrono altri container per JWT.

In PowerShell, nella radice del progetto:

```powershell
docker compose up -d
$jwtBytes = [byte[]]::new(32)
[System.Security.Cryptography.RandomNumberGenerator]::Fill($jwtBytes)
$env:JWT_SECRET = [Convert]::ToBase64String($jwtBytes)
./mvnw.cmd spring-boot:run
```

Se avvii da IntelliJ, imposta JWT_SECRET anche nella configurazione Run. Mantieni la stessa chiave fra riavvii se vuoi conservare la validità dei token. Non salvarla nel repository. Mongo usa le credenziali del compose: verifica il profilo Mongo del progetto prima di provare il catalogo.

L'admin preesistente viene creato con admin/admin123: solo credenziali dimostrative, da cambiare prima di qualsiasi uso reale.

## Scaletta e copione

| Minuti | Attività |
| --- | --- |
| 0–8 | Autenticazione, autorizzazione, filtri e situazione iniziale |
| 8–16 | Stateless, struttura JWT, firma e scadenza |
| 16–23 | Dipendenza e configurazione chiave/encoder/decoder |
| 23–31 | DTO, autenticazione credenziali ed emissione token |
| 31–41 | SecurityFilterChain e mapping dei ruoli |
| 41–54 | Prove guidate: registrazione, login, 401, 403, accesso |
| 54–60 | Domande, limiti, riepilogo tramite esercizio |

Il tempo di dettatura dipende dall'esperienza: distribuisci JwtConfig e TokenService come scheletro già pronto se la classe non conosce le API Spring. I sorgenti finali nel progetto sono la soluzione del docente; per gli studenti parti dalla revisione precedente e segui l'ordine sotto.

### 1. Teoria da raccontare

Autenticare significa verificare chi fa la richiesta. Autorizzare significa decidere se quell'identità può eseguire l'operazione. Nel progetto UserDetailsService legge l'utente da PostgreSQL, BCrypt confronta la password ricevuta con l'hash e le regole URL controllano USER e ADMIN. BCrypt è un hash adattivo con salt, non una cifratura reversibile.

Spring Security intercetta la richiesta prima del controller con una catena di filtri. Il SecurityContext contiene l'identità autenticata della richiesta. AuthenticationManager delega la verifica delle credenziali a un provider: qui DaoAuthenticationProvider usa il servizio utenti e PasswordEncoder.

Con una sessione il client presenta normalmente un cookie che identifica stato sul server. In questa API il client manda il token su ogni richiesta; Spring non conserva l'autenticazione nella sessione HTTP. Stateless non significa assenza di database: il login continua a interrogare gli utenti, le operazioni applicative continuano a usare i database.

Un JWT firmato ha tre segmenti Base64URL: header.payload.signature. Il payload è leggibile, quindi non deve contenere password o dati riservati. La firma protegge integrità e autenticità, non segretezza. Il bearer è utilizzabile da chiunque lo possieda: HTTPS e protezione del token sono necessari.

Qui usiamo HS256: la stessa chiave firma e verifica. È una scelta compatta per un singolo servizio didattico. In sistemi con molti servizi è spesso preferibile separare emissione e verifica con un identity provider e chiavi asimmetriche. Un login personalizzato non costituisce un authorization server OAuth2 completo.

I claim scelti sono iss (emittente), sub (username), iat (emissione), exp (scadenza) e roles (autorizzazioni). Il decoder controlla firma, algoritmo, tempi e issuer; la validazione temporale Spring include una tolleranza di clock. Non basta decodificare il payload per fidarsi del token.

### 2. Ordine di integrazione e spiegazione del codice

1. **pom.xml**: aggiungi spring-boot-starter-security-oauth2-resource-server. Porta i componenti Bearer e JOSE; le versioni restano gestite da Boot. Ricarica Maven.
2. **application.properties**: aggiungi app.jwt.secret e app.jwt.issuer. La chiave arriva da JWT_SECRET, senza fallback pubblico. L'app non parte senza configurazione.
3. **config/JwtConfig.java**: detta prima jwtKey, poi jwtEncoder, poi jwtDecoder. Base64 è solo il formato della configurazione; controlliamo almeno 32 byte per HS256. L'encoder firma e il decoder verifica. JwtValidators aggiunge issuer e controlli temporali.
4. **controller/model/LoginRequest.java** e **TokenResponse.java**: due record separano input e risposta. NotBlank impedisce credenziali vuote. expiresIn è espresso in secondi.
5. **service/TokenService.java**: cattura un solo istante, imposta durata 900 secondi, inserisci i ruoli provenienti dall'autenticazione verificata, firma con HS256. Non prendere mai i ruoli dal JSON del client.
6. **SecurityConfig.authenticationManager**: collega esplicitamente DaoAuthenticationProvider a CustomUserDetailsService e BCrypt. Nessuna nuova tabella: riutilizziamo gli utenti esistenti.
7. **AuthController**: aggiungi le due dipendenze e POST /api/auth/login. UsernamePasswordAuthenticationToken.unauthenticated rappresenta le credenziali ancora da verificare. Solo dopo authenticate emetti il JWT. Credenziali errate e account disabilitato producono 401 senza rivelare quale condizione è fallita. Cache-Control no-store evita la memorizzazione della risposta di login nelle cache.
8. **SecurityConfig.jwtAuthenticationConverter**: leggi roles come authorities già prefissate ROLE_. hasRole("ADMIN") cerca ROLE_ADMIN: aggiungere nuovamente il prefisso romperebbe l'autorizzazione.
9. **SecurityFilterChain**: rendi pubblici register e login; conserva le regole del catalogo, quindi anyRequest().authenticated(). Rimuovi httpBasic e configura oauth2ResourceServer.jwt. STATELESS evita l'uso della sessione per autenticazione; disabilita anche la request cache.
10. **CSRF**: la disabilitazione è coerente con questa API che riceve bearer nell'header e non autentica mediante cookie automatici. Se in futuro sposti il token in un cookie, rivaluta la protezione CSRF. CORS è un tema distinto: serve configurazione esplicita se il browser chiama da un'altra origine.

Flusso login: JSON → AuthenticationManager → DaoAuthenticationProvider → database e BCrypt → Authentication verificata → TokenService → JWT.

Flusso API: Authorization Bearer → filtro Spring → JwtDecoder → Authentication con ruoli → regole URL → controller. Non occorre scrivere un filtro JWT personalizzato.

### 3. Laboratorio PowerShell (in un secondo terminale)

```powershell
$base = 'http://localhost:8080'
# Scegli uno username nuovo; ripetere la registrazione produce 409.
$credentials = @{username='corsista'; password='Password123!'} | ConvertTo-Json
Invoke-RestMethod "$base/api/auth/register" -Method Post -ContentType 'application/json' -Body $credentials
$login = Invoke-RestMethod "$base/api/auth/login" -Method Post -ContentType 'application/json' -Body $credentials
$headers = @{Authorization="Bearer $($login.accessToken)"}
Invoke-RestMethod "$base/api/catalog/products/user/category/NOTEBOOK" -Headers $headers
```

Con database Mongo configurato e vuoto l'ultima risposta è una lista vuota con 200. Per osservare anche gli status usa curl.exe -i:

```powershell
curl.exe -i "$base/api/catalog/products/user/category/NOTEBOOK"
curl.exe -i -H "Authorization: Bearer token-inventato" "$base/api/catalog/products/user/category/NOTEBOOK"
curl.exe -i -X DELETE -H "Authorization: Bearer $($login.accessToken)" "$base/api/catalog/products/admin/delete/inesistente"
$adminBody = @{username='admin'; password='admin123'} | ConvertTo-Json
$admin = Invoke-RestMethod "$base/api/auth/login" -Method Post -ContentType 'application/json' -Body $adminBody
curl.exe -i -H "Authorization: Bearer $($admin.accessToken)" "$base/api/catalog/products/user/category/NOTEBOOK"
```

Risultati: senza token 401; token inventato 401; token USER sulla DELETE admin 403; token ADMIN sulla lettura categoria 200. Per mostrare una DELETE admin riuscita crea prima un prodotto valido e cancellane l'id reale. Non usare un id inesistente per dedurre l'esito della logica applicativa.

Prova anche login con password errata (401), body con username vuoto (400), endpoint pubblico senza header (200). Un token non valido presentato volontariamente può essere rifiutato anche su endpoint pubblico. Per la scadenza, riduci temporaneamente i 900 secondi in TokenService, riavvia ed emetti un nuovo token: considera anche la tolleranza temporale del decoder e ripristina poi 900.

### 4. Domande per verificare l'apprendimento

- Posso modificare roles nel payload? Posso leggerlo, ma modificandolo invalido la firma.
- Logout significa cancellare la sessione? Qui il client elimina il token; una copia rubata rimane valida fino alla scadenza.
- Un utente disabilitato perde immediatamente tutti i token? No: i token già emessi non interrogano il database. Servono durata breve e, se richiesto, revoca o verifica aggiuntiva.
- Cambiare ruolo ha effetto immediato? Solo sui nuovi token; quelli vecchi conservano i claim firmati.
- Perché 401 e 403? 401 indica autenticazione mancante/non valida, 403 identità valida ma permessi insufficienti.
- JWT è obbligatorio per essere stateless? No, è uno dei modi di trasportare credenziali verificabili su ciascuna richiesta.

Refresh token, revoca, rotazione chiavi, rate limiting del login e gestione distribuita delle identità sono sviluppi successivi. Non mettere token in URL, log o servizi pubblici di decodifica. Per un browser la scelta di conservazione richiede una valutazione di XSS e CSRF.

## Test preesistenti

I test HTTP ProductControllerIntegrationTest effettuavano già richieste anonime verso endpoint protetti dalla configurazione iniziale: devono autenticarsi. Ora occorre effettuare login e aggiungere l'header Bearer alle richieste, conservando le verifiche CRUD. I test repository non richiedono header. Tutti i contesti Spring richiedono JWT_SECRET. I test Testcontainers richiedono Docker.

## Riferimenti ufficiali

- https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html
- https://docs.spring.io/spring-security/reference/servlet/authentication/passwords/dao-authentication-provider.html
- https://docs.spring.io/spring-security/reference/servlet/exploits/csrf.html
