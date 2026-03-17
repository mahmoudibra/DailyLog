# Production Deployment Guide

## Environment Variables

The server requires environment variables for database credentials, JWT signing, and file storage. These are **never baked into the Docker image** — they are injected at runtime.

### Local Development

Use the `.env` file (gitignored). Copy from the template:
```bash
cp .env.example .env
```

Docker Compose reads `.env` automatically when you run `./start.sh`.

### CI/CD (GitHub Actions)

Secrets are stored in **GitHub repo Settings → Secrets and variables → Actions**. They're encrypted at rest, masked in logs, and only decrypted at workflow runtime.

Set these secrets in your repo:
- `DATABASE_PASSWORD`
- `JWT_SECRET`

Reference them in workflows:
```yaml
# .github/workflows/deploy.yml
name: Deploy

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4

      - name: Build and push Docker image
        env:
          DATABASE_PASSWORD: ${{ secrets.DATABASE_PASSWORD }}
          JWT_SECRET: ${{ secrets.JWT_SECRET }}
        run: |
          docker build -t dailytracker-server .
          # Push to container registry (e.g. AWS ECR, Docker Hub)
```

GitHub Secrets limitations:
- Max 100 per repo, 48 KB per value
- Cannot be read back after saving — only updated or deleted
- Not available in pull requests from forks

## Deployment Flow

```
Build Phase (CI/CD):
  Code → Docker Image (no secrets) → Push to Container Registry

Deploy Phase (on cloud):
  Pull Image + Inject Secrets at Runtime → Running Container
```

The Docker image only contains the compiled fat JAR. Secrets live externally and are injected when the container starts.

## AWS Deployment Options

### Option 1: ECS (Elastic Container Service)

The most common approach. ECS pulls your image and injects secrets from AWS Secrets Manager.

```
GitHub Actions → Build Image → Push to ECR → ECS pulls image → Injects secrets → Container runs
```

1. Store secrets in **AWS Secrets Manager**:
   ```bash
   aws secretsmanager create-secret --name dailytracker/jwt-secret --secret-string "your-strong-secret"
   aws secretsmanager create-secret --name dailytracker/db-password --secret-string "your-strong-password"
   ```

2. Reference them in your **ECS Task Definition**:
   ```json
   {
     "containerDefinitions": [{
       "image": "123456789.dkr.ecr.us-east-1.amazonaws.com/dailytracker-server:latest",
       "portMappings": [{ "containerPort": 8080 }],
       "secrets": [
         {
           "name": "JWT_SECRET",
           "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789:secret:dailytracker/jwt-secret"
         },
         {
           "name": "DATABASE_PASSWORD",
           "valueFrom": "arn:aws:secretsmanager:us-east-1:123456789:secret:dailytracker/db-password"
         }
       ],
       "environment": [
         { "name": "DATABASE_URL", "value": "jdbc:postgresql://your-rds-host:5432/dailytracker" },
         { "name": "DATABASE_USER", "value": "dailytracker" }
       ]
     }]
   }
   ```

Non-sensitive values (like `DATABASE_URL`) can go directly in the task definition. Sensitive values should always come from Secrets Manager.

### Option 2: EC2 (Simple VPS)

SSH into your server and manage secrets manually:

```bash
# On the EC2 instance
echo 'JWT_SECRET=your-strong-secret' >> .env
echo 'DATABASE_PASSWORD=your-strong-password' >> .env

docker pull your-image
docker run --env-file .env -p 8080:8080 your-image
```

Simple but requires manual secret management. Suitable for small projects.

### Option 3: App Runner / Lightsail

AWS provides a UI to set environment variables directly — no config files or secret managers needed. Easiest option for getting started.

## Secrets by Environment

| Environment | Where secrets live | How they're injected |
|-------------|-------------------|----------------------|
| Local dev | `.env` file (gitignored) | Docker Compose reads it automatically |
| CI/CD | GitHub Secrets | `${{ secrets.NAME }}` in workflow files |
| AWS ECS | AWS Secrets Manager | ECS task definition `secrets` field |
| AWS EC2 | `.env` file on server | `docker run --env-file .env` |
| AWS App Runner | AWS Console UI | Set in service configuration |

## Other Cloud Providers

The same pattern applies — image is clean, secrets are external:

| Provider | Secret Storage | Container Service |
|----------|---------------|-------------------|
| AWS | Secrets Manager / SSM Parameter Store | ECS, App Runner, EC2 |
| GCP | Secret Manager | Cloud Run, GKE |
| Azure | Key Vault | Container Apps, AKS |
| DigitalOcean | App Platform env vars | App Platform |
