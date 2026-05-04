# Отчет по лабораторным работам 3 и 4

## Тема

Автоматизация создания инфраструктуры через OpenStack Heat и деплой Java-приложения через Jenkins.

## Кратко про Heat

Heat - это сервис оркестрации OpenStack. Он позволяет описывать инфраструктуру декларативно в YAML-шаблоне: виртуальные машины, порты, security group, floating IP и другие ресурсы. Вместо ручного создания ресурсов через Horizon или отдельные CLI-команды создается один stack, а Heat сам приводит облачную инфраструктуру к описанному состоянию.

В этой работе Heat используется как аналог "инфраструктуры как кода": шаблон хранится в Git, а Jenkins запускает создание или обновление stack.

## Что было сделано

### Лабораторная 3

Для создания инфраструктуры был добавлен Heat-шаблон `heat/restobot-stack.yaml`.

Шаблон создает:

- виртуальную машину для приложения RestoBot;
- отдельную security group;
- правила доступа по SSH на порт `22`;
- правило доступа к API приложения на порт `8089`;
- Neutron port во внутренней сети OpenStack;
- floating IP из внешней сети;
- пользователя для деплоя через `cloud-init`.

Для запуска Heat из Jenkins была создана отдельная pipeline job с файлом `jenkins/infra.Jenkinsfile`.

Pipeline выполняет:

- checkout репозитория;
- подготовку OpenStack RC-файла из Jenkins credentials;
- подготовку Heat env-файла;
- валидацию Heat-шаблона;
- создание или обновление stack через `openstack stack create/update`;
- сохранение outputs stack в артефакты Jenkins.

Для избежания конфликтов в общем OpenStack используются уникальные имена:

- Heat stack: `gaar-restobot-stack`;
- VM для развёртывания: `gaar-restobot-vm`.

Используемые credentials:

- `openstack_rc` - файл с переменными окружения OpenStack;
- `openstack_password` - пароль OpenStack;
- `restobot_heat_env` - параметры Heat stack;
- `restobot_vm_ssh` - SSH-ключ для доступа к VM.

### Лабораторная 4

Для деплоя приложения была создана отдельная pipeline job с файлом `jenkins/deploy.Jenkinsfile`.

Pipeline выполняет:

- checkout репозитория;
- загрузку `.env` приложения из Jenkins credentials;
- получение `app-fat.jar` из successful build job лабораторной 2;
- определение IP целевой VM через параметр `TARGET_HOST` или Heat output `floating_ip`;
- копирование артефакта, `.env`, SQL-миграций и systemd unit на VM;
- создание/обновление базы `main`;
- применение SQL-миграций, если таблицы еще не созданы;
- установку systemd service;
- перезапуск приложения через `systemctl`;
- проверку `/healthcheck`.

Для запуска приложения без `nohup` был добавлен systemd unit `deploy/restobot.service`.

## Использованные файлы

- `heat/restobot-stack.yaml` - Heat template для создания VM и сетевых ресурсов.
- `heat/restobot-stack.env.example` - пример параметров Heat stack.
- `jenkins/infra.Jenkinsfile` - Jenkins pipeline для лабораторной 3.
- `jenkins/deploy.Jenkinsfile` - Jenkins pipeline для лабораторной 4.
- `deploy/deploy.sh` - скрипт деплоя приложения на VM.
- `deploy/restobot.service` - systemd unit приложения.
- `deploy/manual-vm-setup.md` - инструкция для ручной подготовки VM.

## Результат лабораторной 3

Stack был создан успешно:

```text
+-----------------+----------------------+-----------------+----------------------+--------------+
| ID              | Stack Name      | Stack Status    | Creation Time     | Updated Time |
+-----------------+----------------------+-----------------+----------------------+--------------+
| d4d855d6-9ff5-  | gaar-restobot-stack | CREATE_COMPLETE | 2026-04-30T13:12:39Z | None       |
+-----------------+----------------------+-----------------+----------------------+--------------+
```

Outputs stack:

```text
+-------------+---------------------------------+
| output_key  | description                     |
+-------------+---------------------------------+
| fixed_ip    | Internal IPv4 address.          |
| ssh_user    | SSH user created by cloud-init. |
| floating_ip | Public floating IP.             |
| server_id   | Created VM ID.                  |
| server_name | Created VM name.                |
+-------------+---------------------------------+
```

Floating IP созданной VM:

```text
output_value | 185.216.204.165
```

## Результат лабораторной 4

Jenkins job `lab4-deploy` успешно:

- получила артефакт `app-fat.jar` из job `lab2`;
- подключилась к VM `185.216.204.165`;
- скопировала приложение и конфигурацию;
- настроила systemd service;
- перезапустила приложение;
- выполнила внешний healthcheck.

Проверка приложения:

```bash
curl http://185.216.204.165:8089/healthcheck
```

Ожидаемый ответ:

```json
{
  "status": "OK",
  "authors": ["Тищенко Артём", "Гаар Владислав", "Губковский Дмитрий"]
}
```

## Команды для демонстрации

Проверить Heat stack:

```bash
openstack stack list
openstack stack output list gaar-restobot-stack
openstack stack output show gaar-restobot-stack floating_ip
```

Подключиться к VM:

```bash
ssh -i ~/.ssh/id_ed25519_labs restobot@185.216.204.165
```

Проверить systemd service:

```bash
sudo systemctl status restobot --no-pager
sudo journalctl -u restobot -n 100 --no-pager
```

Проверить healthcheck:

```bash
curl http://185.216.204.165:8089/healthcheck
```

Удалить stack после демонстрации, если инфраструктура больше не нужна:

```bash
openstack stack delete --yes --wait gaar-restobot-stack
```

Либо через Jenkins job `lab3-infra`:

```text
STACK_ACTION = delete
STACK_NAME = gaar-restobot-stack
```

## Выводы

В результате работ создание инфраструктуры и деплой приложения были вынесены в Jenkins. Инфраструктура описана в Heat YAML-шаблоне и хранится в Git, поэтому ее можно воспроизвести повторно. Деплой приложения также автоматизирован: Jenkins берет готовый артефакт из job лабораторной 2, доставляет его на VM и управляет запуском через systemd.

Такой подход лучше ручного деплоя, потому что снижает количество ручных действий, фиксирует процесс в коде и позволяет повторять создание VM и деплой приложения через Jenkins pipeline.
