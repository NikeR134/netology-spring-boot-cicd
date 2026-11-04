# Deployment on an Ubuntu VM

The workflow builds and tests the application, uploads its JAR as an artifact, publishes a Docker image to GHCR, then deploys it over SSH when VM secrets are configured.

## Prepare the VM

Install Docker Engine with Compose plugin, nginx, curl and OpenSSH Server on Ubuntu 22.04/24.04. Create a dedicated SSH user for Actions, add it to the `docker` group and configure passwordless sudo for `mv`/`tee` in `/etc/nginx/conf.d`, `nginx -t`, and `systemctl reload nginx`. Restrict SSH ingress to trusted IPs; open HTTP/HTTPS as needed. Never commit a private key to Git.

Copy `deploy/nginx.conf` to `/etc/nginx/sites-available/todo-app`, enable it with a symlink in `sites-enabled`, and initialize the upstream to the inactive slot:

```bash
echo 'server 127.0.0.1:8082 max_fails=3 fail_timeout=10s;' | sudo tee /etc/nginx/conf.d/todo-app-upstream.conf
sudo nginx -t && sudo systemctl reload nginx
```

The app listens on loopback ports 8081/8082. Users access nginx on port 80/443; do not expose app ports directly. The workflow starts the other slot, checks `GET /tasks`, switches nginx after the check passes, then removes the previous container. A failed health check leaves the active container untouched.

## Add GitHub Actions secrets

In `Settings → Secrets and variables → Actions`, add:

| Secret | Value |
|---|---|
| `VM_HOST` | Public IP address or DNS name of the VM |
| `VM_USER` | Dedicated SSH username |
| `VM_SSH_KEY` | Private SSH key matching the public key in `authorized_keys` |
| `VM_SSH_PORT` | Optional SSH port (default 22) |
| `GHCR_TOKEN` | Optional classic PAT with `read:packages` if the GHCR package is private |

After the three required `VM_*` values are present, pushes to `main` trigger deployment. Without them, the Docker image is published and the workflow summary explicitly marks deployment as skipped. The JAR artifact is retained for 14 days; images have `sha-<commit>` and `latest` tags.
