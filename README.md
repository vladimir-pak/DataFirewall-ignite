# DataFirewall-ignite

Приложение на Spring Boot с Apache Ignite Client для парсинга SQL в исполняемый Java байткод.
Байткод записывается в IgniteCache.

### IgniteCache
- **Аутентификация** - Подключение к Ignite Server по логину и паролю через тонкий клиент Ignite.
- **Кэш с проверками** - В кэш харнятся байткоды всех проверок, выгруженных из базы Postgres. Хранение в формате IgniteCache<String, byte[]>
- **Кэш с политиками** - В кэш хранятся политики применения проверок.

### Парсинг SQL
Парсинг SQL осуществляется с использованием ANTLR грамматики.

### Загрузка Java классов
Загрузка Java классов из byte[] осуществляется с использованием Janino.

### Запуск приложения
Запуск собранного jar выполняется по команде:
```bash
java \
  --add-opens=java.base/jdk.internal.access=ALL-UNNAMED \
  --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED \
  --add-opens=java.base/sun.nio.ch=ALL-UNNAMED \
  --add-opens=java.base/sun.util.calendar=ALL-UNNAMED \
  --add-opens=java.management/com.sun.jmx.mbeanserver=ALL-UNNAMED \
  --add-opens=jdk.internal.jvmstat/sun.jvmstat.monitor=ALL-UNNAMED \
  --add-opens=java.base/sun.reflect.generics.reflectiveObjects=ALL-UNNAMED \
  --add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED \
  --add-opens=java.base/java.io=ALL-UNNAMED \
  --add-opens=java.base/java.nio=ALL-UNNAMED \
  --add-opens=java.base/java.net=ALL-UNNAMED \
  --add-opens=java.base/java.util=ALL-UNNAMED \
  --add-opens=java.base/java.util.concurrent=ALL-UNNAMED \
  --add-opens=java.base/java.util.concurrent.locks=ALL-UNNAMED \
  --add-opens=java.base/java.util.concurrent.atomic=ALL-UNNAMED \
  --add-opens=java.base/java.lang=ALL-UNNAMED \
  --add-opens=java.base/java.lang.invoke=ALL-UNNAMED \
  --add-opens=java.base/java.math=ALL-UNNAMED \
  --add-opens=java.sql/java.sql=ALL-UNNAMED \
  --add-opens=java.base/java.lang.reflect=ALL-UNNAMED \
  --add-opens=java.base/java.time=ALL-UNNAMED \
  --add-opens=java.base/java.text=ALL-UNNAMED \
  --add-opens=java.management/sun.management=ALL-UNNAMED \
  --add-opens=java.desktop/java.awt.font=ALL-UNNAMED \
  -jar datafirewall-0.0.1-SNAPSHOT.jar
  ```

Для удобства запуска в корне проекта добавлен start.sh

### Примеры использования
Для использования добавлены контроллеры для парсинга и компиляции SQL -> java class и тестирования логики самих проверок.

- **/api/v1/test/compile/{serviceName}** - для парсинга и компиляции. По результатам выполнения в кэш складываются byte[] проверок, а в памяти сохраняются экземпляры классов проверок выгруженных через ClassLoader.

- **/api/v1/test/start** - запуск проверок. В теле запроса подается json с данными для проверки.

- **/api/v1/cache/refresh** - запуск обновления всего кэша.


### Kafka Producer
В application.yaml указать реквизиты для подключения к Kafka с TLS.
Для отладки можно выклчить Kafka в параметре spring.kafka.cache-update.enabled

### Actuator
Для healthcheck использовать endpoint-ы
```bash
  /actuator/health
  /actuator/health/liveness
  /actuator/health/readiness
  ```
