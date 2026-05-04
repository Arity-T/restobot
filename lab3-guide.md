```text
lab3-infra создаёт gaar-restobot-stack
из output берём новый floating_ip
этот IP ставим в lab4-deploy TARGET_HOST
```

Или оставляешь `TARGET_HOST` пустым, тогда `lab4-deploy` сам попробует взять IP из Heat output `gaar-restobot-stack.floating_ip`.

**Настройка Jenkins С Нуля**
1. Создать/проверить Jenkins node `labs`

В Jenkins:

```text
Manage Jenkins -> Nodes -> New Node
```

Поля:

```text
Node name: labs
Type: Permanent Agent
```

Дальше:

```text
Number of executors: 1
Remote root directory: /home/ubuntu/jenkins-agent
Labels: labs
Usage: Use this node as much as possible
Launch method: Launch agents via SSH
Host: 192.168.24.19
Credentials: labs-ssh-key
Host Key Verification Strategy: Non verifying Verification Strategy
Availability: Keep this agent online as much as possible
```

На сервере `192.168.24.19` заранее:

```bash
mkdir -p /home/ubuntu/jenkins-agent
sudo chown -R ubuntu:ubuntu /home/ubuntu/jenkins-agent
```

И должны быть пакеты:

```bash
sudo apt update
sudo apt install -y git curl unzip openssh-client postgresql-client python3-openstackclient python3-heatclient openjdk-21-jdk
```

Проверка:

```bash
java -version
git --version
psql --version
openstack stack list
```

**Credentials**
Создавать тут:

```text
Manage Jenkins -> Credentials -> System -> Global credentials -> Add Credentials
```

Нужно 6 credentials.

`labs-ssh-key`

Для подключения Jenkins controller к node `labs`.

```text
Kind: SSH Username with private key
ID: labs-ssh-key
Username: ubuntu
Private Key: ключ от сервера 192.168.24.19
```

Брать приватный ключ с твоего ПК, которым ты заходишь на `ubuntu@192.168.24.19`.

`restobot_env`

`.env` приложения.

```text
Kind: Secret file
ID: restobot_env
File: .env
```

Файл должен содержать реальные переменные приложения: токен Telegram, TripAdvisor key, DB user/password и т.п.

Важно: `MAIN_DB_PASSWORD` должен совпадать с паролем PostgreSQL на VM для деплоя.

`openstack_rc`

Файл окружения OpenStack.

```text
Kind: Secret file
ID: openstack_rc
File: openrc.sh / openstack.rc
```

Взять там, где ты настраивал OpenStack CLI. Обычно это файл, который ты делаешь:

```bash
source openrc.sh
openstack token issue
```

Если файл содержит интерактивный ввод пароля, это нормально: Jenkinsfile вырезает `read OS_PASSWORD`, а пароль берёт из отдельного credential.

`openstack_password`

Пароль от OpenStack.

```text
Kind: Secret text
ID: openstack_password
Secret: твой пароль OpenStack
```

`restobot_heat_env`

Параметры Heat stack.

```text
Kind: Secret file
ID: restobot_heat_env
File: heat/restobot-stack.env
```

Содержимое примерно такое:

```yaml
parameter_defaults:
  instance_name: gaar-restobot-vm
  image: ubuntu-24.04
  flavor: m1.small
  network: student-net
  subnet: student-subnet-1
  external_network: public-ext
  key_name: gaar-key-pair
  ssh_user: restobot
  ssh_public_key: "replace-me"
  app_port: 8089
```

`ssh_public_key` можно оставить заглушкой: `lab3-infra` сам подставит публичный ключ из `restobot_vm_ssh`.

`restobot_vm_ssh`

Для доступа Jenkins к VM, созданной Heat.

```text
Kind: SSH Username with private key
ID: restobot_vm_ssh
Username: restobot
Private Key: приватный ключ, которым Jenkins будет заходить на VM приложения
```

Этот же ключ используется в `lab3-infra`: из него берётся public key и добавляется в создаваемую VM через cloud-init.

**Jobs**
Создать 3 Pipeline job.

`lab2`

```text
New Item -> Pipeline
Name: lab2
Definition: Pipeline script from SCM
SCM: Git
Repository URL: https://github.com/Arity-T/restobot
Branch: */vlad-heat-deploy или */vlad-jenkins
Script Path: Jenkinsfile
```

Для текущей ветки лучше `*/vlad-heat-deploy`, если ты хочешь показывать всё из одной ветки.

`lab3-infra`

```text
New Item -> Pipeline
Name: lab3-infra
Definition: Pipeline script from SCM
SCM: Git
Repository URL: https://github.com/Arity-T/restobot
Branch: */vlad-heat-deploy
Script Path: jenkins/infra.Jenkinsfile
```

`lab4-deploy`

```text
New Item -> Pipeline
Name: lab4-deploy
Definition: Pipeline script from SCM
SCM: Git
Repository URL: https://github.com/Arity-T/restobot
Branch: */vlad-heat-deploy
Script Path: jenkins/deploy.Jenkinsfile
```

**Порядок Прогона**
1. Запустить `lab2`

Параметры node:

```text
LABS_NODE_NAME = labs
LABS_NODE_HOST = 192.168.24.19
LABS_NODE_SSH_USER = ubuntu
LABS_NODE_CREDENTIALS_ID = labs-ssh-key
LABS_NODE_REMOTE_ROOT = /home/ubuntu/jenkins-agent
```

Если node уже создан руками, stage `Create Jenkins node` просто обновит его.

После успеха должен быть artifact:

```text
app/build/libs/app-fat.jar
```

2. Запустить `lab3-infra`

```text
STACK_ACTION = apply
STACK_NAME = gaar-restobot-stack
```

После успеха посмотреть output:

```bash
openstack stack output show gaar-restobot-stack floating_ip
```

Или в Jenkins artifact `heat/stack-outputs.txt`.

3. Подготовить новую VM руками

Зайти на новый floating IP:

```bash
ssh -i ~/.ssh/<key> restobot@<NEW_FLOATING_IP>
```

Поставить зависимости:

```bash
sudo apt update
sudo apt install -y curl ca-certificates gnupg postgresql postgresql-client openjdk-21-jdk
sudo systemctl enable --now postgresql
sudo -u postgres psql -c "alter user postgres with password 'postgres';"
sudo -u postgres createdb main || true
```

Проверить:

```bash
java -version
psql --version
systemctl status postgresql --no-pager
```

4. Запустить `lab4-deploy`

```text
BUILD_JOB_NAME = lab2
BUILD_NUMBER = оставить пустым или номер успешного build
STACK_NAME = gaar-restobot-stack
TARGET_HOST = оставить пустым или указать <NEW_FLOATING_IP>
APP_PORT = 8089
```

Если `TARGET_HOST` пустой, job возьмёт IP из Heat output. Если будут проблемы с OpenStack credential, укажи IP руками.

**Проверка Для Показа**
```bash
curl http://<NEW_FLOATING_IP>:8089/healthcheck
```

На VM:

```bash
sudo systemctl status restobot --no-pager
sudo systemctl cat restobot
sudo journalctl -u restobot -n 50 --no-pager
```