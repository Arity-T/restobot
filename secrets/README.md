# Секреты для Jenkins (restobot)

Файлы в этой папке **не коммитьте** с реальными ключами. В репозитории лежат только примеры и эта инструкция.

## Что нужно в Jenkins

| Credential ID | Тип в Jenkins | Файл-источник |
|---------------|----------------|---------------|
| `restobot_kubeconfig` | **Secret file** | `kubeconfig` (см. ниже) |
| `restobot_env` | **Secret file** | `restobot.env` (скопируйте из `restobot.env.example`) |
| `docker_registry` | **Username with password** | Только если включите `PUSH_IMAGE` — логин в Docker Hub / другой registry |

### Как добавить в Jenkins (GUI)

1. **Manage Jenkins** → **Credentials** → выберите домен (часто `(global)`) → **Add Credentials**.
2. **Kind**: для kubeconfig и `.env` выберите **Secret file**, загрузите файл, **ID** задайте ровно как в таблице (`restobot_kubeconfig`, `restobot_env`).
3. Для registry: **Username with password**, ID: `docker_registry`.

Pipeline уже ссылается на эти ID в `Jenkinsfile`.

## Подготовка `kubeconfig` (Minikube)

На той же машине, где крутится агент Jenkins:

```bash
cd /path/to/restobot/secrets
minikube kubectl -- config view --flatten > kubeconfig
chmod 600 kubeconfig
```

Либо скопируйте актуальный `~/.kube/config`, если контекст уже указывает на minikube.

Загрузите файл `kubeconfig` в credential **`restobot_kubeconfig`**.

## Подготовка `restobot.env`

```bash
cp restobot.env.example restobot.env
# отредактируйте: TELEGRAM_*, MAIN_DB_*, TRIPADVISOR_*
```

Загрузите **`restobot.env`** в credential **`restobot_env`**.

Переменные `MAIN_DB_URL` для **Kubernetes** в манифесте переопределяются на `jdbc:postgresql://restobot-postgres:5432/main`; для стадии Gradle пайплайн подставит URL к временному Postgres из `docker compose`.

## Minikube и Docker

Сборка образа приложения выполняется через **`eval $(minikube docker-env)`**, чтобы образ оказался в Docker внутри Minikube и поды могли его запустить без registry. Команда `minikube` должна быть в `PATH` у агента (как у тебя в терминале).

## Что нужно от тебя вручную

- Реальные значения в **`restobot.env`**: токен Telegram, пароль БД (для локальной сборки), ключ TripAdvisor (если используешь).
- Учётные данные registry — **только** если включишь параметр **`PUSH_IMAGE`**; для чисто локального Minikube обычно достаточно `PUSH_IMAGE=false`.
