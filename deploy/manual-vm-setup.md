# Manual VM preparation for labs 3 and 4

This is the one-time manual setup expected by lab 3 before Jenkins deploys the
artifact in lab 4.

## 1. Base packages

```bash
sudo apt-get update
sudo apt-get install -y curl ca-certificates gnupg postgresql
```

## 2. Install Java 23

```bash
curl -fsSL https://packages.adoptium.net/artifactory/api/gpg/key/public | sudo gpg --dearmor -o /usr/share/keyrings/adoptium.gpg
echo "deb [signed-by=/usr/share/keyrings/adoptium.gpg] https://packages.adoptium.net/artifactory/deb jammy main" | sudo tee /etc/apt/sources.list.d/adoptium.list
sudo apt-get update
sudo apt-get install -y temurin-23-jre
java -version
```

## 3. Prepare PostgreSQL

```bash
sudo -u postgres psql -c "alter user postgres with password 'replace-with-main-db-password';"
sudo -u postgres psql -tc "select 1 from pg_database where datname = 'main'" | grep -q 1 || sudo -u postgres createdb main
```

## 4. Prepare deploy user and directories

If the VM was created from `heat/restobot-stack.yaml`, user `restobot` already
exists. Otherwise:

```bash
sudo useradd --system --create-home --shell /bin/bash restobot
sudo usermod -aG sudo restobot
sudo install -d -o restobot -g restobot /opt/restobot /opt/restobot/app
```

## 5. Check prerequisites

```bash
systemctl status postgresql --no-pager
java -version
psql --version
```

After that, Jenkins deploy job can copy the artifact, write `/opt/restobot/.env`,
install the systemd unit and restart the application.
