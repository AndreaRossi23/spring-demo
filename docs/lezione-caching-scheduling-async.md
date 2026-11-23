# Lezione – Caching, Scheduling e Async

Questa implementazione aggiunge al progetto tre funzionalità Spring utilizzabili direttamente durante la lezione.

## 1. Caching

Dipendenza aggiunta:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-cache</artifactId>
</dependency>
```

Abilitazione in `CacheConfig`. Il catalogo JPA usa la cache `products`:

- `ProductService.findById()` → `@Cacheable`
- `ProductService.update()` → `@CachePut`
- `ProductService.delete()` → `@CacheEvict`

Il provider usato dal profilo `dev` è la cache in memoria di Spring.

### Prove

```http
GET /api/products/{id}
GET /api/products/{id}
PUT /api/products/{id}
DELETE /api/products/{id}
```

Per il secondo GET dello stesso ID il valore viene restituito dalla cache. L'update aggiorna la voce e il delete la rimuove.

## 2. Scheduling

`SchedulingConfig` abilita `@Scheduled`. `ProductScheduler` esegue periodicamente un conteggio dei prodotti e lo scrive nei log.

Nel profilo `dev`:

```properties
app.scheduler.product-stats.fixed-rate=30000
app.scheduler.product-stats.initial-delay=5000
```

Quindi il primo job parte dopo 5 secondi e successivamente ogni 30 secondi.

Cercare nei log:

```text
SCHEDULED - catalog statistics: totalProducts=..., thread=...
```

I valori possono essere cambiati senza modificare il codice.

## 3. Async

`AsyncConfig` abilita `@Async` e crea un `ThreadPoolTaskExecutor` con 2 thread core, 4 thread massimi e coda da 20 elementi.

`ProductAsyncService.findById()` è un esempio asincrono che simula un'operazione lenta di 3 secondi.

Endpoint:

```http
GET /api/products/{id}/async
```

Nei log compariranno messaggi simili a:

```text
ASYNC START - productId=..., thread=async-1
ASYNC END - productId=..., thread=async-1
```

Il `CompletableFuture` permette a Spring MVC di gestire la risposta asincrona.

## 4. File introdotti/modificati

### Configurazione

- `config/CacheConfig.java`
- `config/SchedulingConfig.java`
- `config/AsyncConfig.java`

### Service

- `service/ProductService.java`
- `service/ProductScheduler.java`
- `service/ProductAsyncService.java`

### Controller

- `controller/ProductController.java`

È stato aggiunto anche `PUT /api/products/{id}` per poter mostrare concretamente `@CachePut`.

### Test

`ProductControllerIntegrationTest` verifica:

- presenza dell'elemento in cache dopo `findById`;
- aggiornamento della cache dopo `PUT`;
- rimozione della cache dopo `DELETE`;
- disponibilità dell'endpoint asincrono.

## 5. Nota didattica

La cache configurata è locale alla JVM e si perde al riavvio. In un'applicazione con più istanze non è condivisa automaticamente. Per scenari distribuiti si possono introdurre provider come Redis.

`@Async` usa un thread pool, ma non è una coda persistente: un task in memoria non è garantito in caso di arresto dell'applicazione. Per elaborazioni che non devono andare perse sono più adatti sistemi di messaging/persistenza come Kafka o RabbitMQ.
