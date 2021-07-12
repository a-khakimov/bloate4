[![Scala CI](https://github.com/a-khakimov/bloate4/actions/workflows/main.yml/badge.svg)](https://github.com/a-khakimov/bloate4/actions/workflows/main.yml)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=bloate4&metric=coverage)](https://sonarcloud.io/dashboard?id=bloate4)
[![Lines of Code](https://sonarcloud.io/api/project_badges/measure?project=bloate4&metric=ncloc)](https://sonarcloud.io/dashboard?id=bloate4)

# bloate4

* [x] Подрубить к postgres
* [x] Миграция
* [x] Логирование
* [x] Кэши
* [x] Метрики
* [ ] Авторизованный доступ к метрикам
* [x] Настроить github-CI
* [ ] Настроить деплой и запуск на сервере с релизной ветки
* [x] Подрубить Sonar
* [ ] Написать тесты
* [ ] Нагрузочное тестирование
* [ ] Фронт (королев? Scala.js?)
* [ ] swagger nnado?

```bash
DB_DRIVER=org.postgresql.Driver DB_URL=jdbc:postgresql://localhost:5432/bloate4 DB_USER=bloate4 DB_PASSWD=pony sbt run
```
