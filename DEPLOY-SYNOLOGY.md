# Deploying 10-kamp on a Synology NAS

Web Station (the package in Synology's "host a website" tutorial) only serves
static files and PHP — it cannot run a Java/Spring Boot app. This app runs on
the NAS in **Container Manager** (Synology's Docker package) instead, with DSM's
built-in reverse proxy providing the public HTTPS address. The NAS builds the
Docker image itself, so you don't need Docker on your Mac.

## 0. Check the prerequisite

Open **Package Center** on the NAS and search for **Container Manager**
(DSM 7.2+; on older DSM versions the same package is called **Docker**).
If neither exists, your NAS model doesn't support containers — see
"If your NAS can't run containers" at the bottom.

## 1. Copy the project to the NAS

1. Install Container Manager. It creates a shared folder called `docker`.
2. Copy this whole project folder to the NAS as `/docker/tiokamp`
   (File Station drag-and-drop, or SMB). What matters:
   `Dockerfile`, `docker-compose.yml`, `.dockerignore`, `pom.xml`, `src/`.
3. Create `/docker/tiokamp/data/` and put your current database in it if you
   want to keep existing accounts: copy `tiokampdb.mv.db` from this folder into
   `data/`, and copy the local `uploads/` folder to `data/uploads/`.
   Starting with an empty `data/` gives a fresh install (everyone registers anew
   — probably what you want before the party anyway).

## 2. Create the project in Container Manager

1. Container Manager → **Project** → **Create**.
2. Project name `tiokamp`, path `/docker/tiokamp`, and it will detect the
   existing `docker-compose.yml` — choose to use it.
3. Build & run. The first build downloads Maven + Java images and compiles the
   app (several minutes).
4. Test on your home network: `http://<NAS-IP>:8082` should show the login page.

## 3. Give it a public HTTPS address

Guests at the venue will be on mobile data, so the NAS must be reachable from
the internet:

1. **DDNS name** — Control Panel → External Access → DDNS → Add → service
   provider *Synology*, pick a hostname, e.g. `bjorcklind.synology.me` (free).
2. **Certificate** — Control Panel → Security → Certificate → Add →
   *Get a certificate from Let's Encrypt* for that hostname.
3. **Reverse proxy** — Control Panel → Login Portal → Advanced → Reverse Proxy
   → Create:
   - Source: HTTPS, hostname `bjorcklind.synology.me`, port `443`
   - Destination: HTTP, `localhost`, port `8082`
4. **Router port forwarding** — forward external **443** (and **80**, which
   Let's Encrypt needs for issuing/renewing the certificate) to the NAS.
   Control Panel → External Access → Router Configuration can do this
   automatically if your router supports UPnP; otherwise set it in the router.
5. **Firewall** (if enabled on the NAS) — allow 80/443 from anywhere; there is
   no need to open 8082 externally, it only serves the reverse proxy locally.

Then the app is live at `https://bjorcklind.synology.me`.

## 4. After deploying

- **Claim the admin account**: register the username in `APP_ADMIN_USERNAMES`
  (in `docker-compose.yml`, default `elli`) yourself, early — whoever registers
  a listed name gets the admin page. Change the list → Project → *Build/Restart*.
- **Backup**: everything lives in `/docker/tiokamp/data/`. To back it up, stop
  the container first (the H2 database file shouldn't be copied while running),
  copy `data/`, start again.
- The H2 web console is **disabled** in the container (`SPRING_H2_CONSOLE_ENABLED=false`)
  — don't re-enable it on an internet-facing deployment; it would expose the
  whole database behind a default blank password.
- Logs: Container Manager → Container → tiokamp → Log.

## If your NAS can't run containers

Any always-on machine with Java 17+ can host the app instead: run
`mvn package` and start it with the same environment variables the Dockerfile
sets (see `Dockerfile`), then still use the NAS/router steps in §3 — or a small
cloud VM. The jar is self-contained (`target/tiokamp-0.0.1-SNAPSHOT.jar`).
