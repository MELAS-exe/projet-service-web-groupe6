# ════════════════════════════════════════════════════════════════
# GUIDE DÉPLOIEMENT — Voom Matching Service sur Cloud Run
# ════════════════════════════════════════════════════════════════
# Projet GCP : voom-482712
# Région     : europe-west9 (Paris)
# BDD        : voom-matching-service-dev (Cloud SQL PostgreSQL)
# ════════════════════════════════════════════════════════════════

# ────────────────────────────────────────────────────────────────
# ÉTAPE 1 — Prérequis (à faire une seule fois)
# ────────────────────────────────────────────────────────────────

# 1.1 Activer les APIs nécessaires
gcloud services enable \
  run.googleapis.com \
  artifactregistry.googleapis.com \
  cloudbuild.googleapis.com \
  secretmanager.googleapis.com \
  sqladmin.googleapis.com \
  --project=voom-482712

# 1.2 Créer le repository Artifact Registry
gcloud artifacts repositories create voom \
  --repository-format=docker \
  --location=europe-west9 \
  --description="Images Docker Voom" \
  --project=voom-482712

# 1.3 Créer le compte de service pour Cloud Run
gcloud iam service-accounts create matchingservice-sa \
  --display-name="Matching Service SA" \
  --project=voom-482712

# 1.4 Donner les permissions au compte de service
# Connexion Cloud SQL
gcloud projects add-iam-policy-binding voom-482712 \
  --member="serviceAccount:matchingservice-sa@voom-482712.iam.gserviceaccount.com" \
  --role="roles/cloudsql.client"

# Lecture des secrets
gcloud projects add-iam-policy-binding voom-482712 \
  --member="serviceAccount:matchingservice-sa@voom-482712.iam.gserviceaccount.com" \
  --role="roles/secretmanager.secretAccessor"

# ────────────────────────────────────────────────────────────────
# ÉTAPE 2 — Stocker les secrets dans Secret Manager
# ────────────────────────────────────────────────────────────────

# 2.1 Mot de passe de la base de données
echo -n "TON_MOT_DE_PASSE_ICI" | gcloud secrets create voom-db-password \
  --data-file=- \
  --project=voom-482712

# 2.2 Nom d'utilisateur de la base de données
echo -n "postgres" | gcloud secrets create voom-db-username \
  --data-file=- \
  --project=voom-482712

# Pour mettre à jour un secret existant :
# echo -n "NOUVEAU_MOT_DE_PASSE" | gcloud secrets versions add voom-db-password --data-file=-

# ────────────────────────────────────────────────────────────────
# ÉTAPE 3 — Donner les droits Cloud Build
# ────────────────────────────────────────────────────────────────

# Récupérer le numéro de projet
PROJECT_NUMBER=$(gcloud projects describe voom-482712 --format='value(projectNumber)')

# Cloud Build peut déployer sur Cloud Run
gcloud projects add-iam-policy-binding voom-482712 \
  --member="serviceAccount:${PROJECT_NUMBER}@cloudbuild.gserviceaccount.com" \
  --role="roles/run.admin"

# Cloud Build peut utiliser le compte de service
gcloud iam service-accounts add-iam-policy-binding \
  matchingservice-sa@voom-482712.iam.gserviceaccount.com \
  --member="serviceAccount:${PROJECT_NUMBER}@cloudbuild.gserviceaccount.com" \
  --role="roles/iam.serviceAccountUser"

# Cloud Build peut pousser vers Artifact Registry
gcloud projects add-iam-policy-binding voom-482712 \
  --member="serviceAccount:${PROJECT_NUMBER}@cloudbuild.gserviceaccount.com" \
  --role="roles/artifactregistry.writer"

# ────────────────────────────────────────────────────────────────
# ÉTAPE 4 — Premier déploiement manuel
# ────────────────────────────────────────────────────────────────

# 4.1 Authentifier Docker avec Artifact Registry
gcloud auth configure-docker europe-west9-docker.pkg.dev

# 4.2 Builder l'image localement
docker build -t europe-west9-docker.pkg.dev/voom-482712/voom/matchingservice:latest .

# 4.3 Pousser l'image
docker push europe-west9-docker.pkg.dev/voom-482712/voom/matchingservice:latest

# 4.4 Déployer sur Cloud Run
gcloud run deploy matchingservice \
  --image=europe-west9-docker.pkg.dev/voom-482712/voom/matchingservice:latest \
  --region=europe-west9 \
  --platform=managed \
  --no-allow-unauthenticated \
  --add-cloudsql-instances=voom-482712:europe-west9:voom-matching-service-dev \
  --service-account=matchingservice-sa@voom-482712.iam.gserviceaccount.com \
  --set-env-vars=SPRING_PROFILES_ACTIVE=prod \
  --set-env-vars=CLOUD_SQL_INSTANCE=voom-482712:europe-west9:voom-matching-service-dev \
  --update-secrets=DB_PASSWORD=voom-db-password:latest \
  --update-secrets=DB_USERNAME=voom-db-username:latest \
  --memory=512Mi \
  --cpu=1 \
  --min-instances=0 \
  --max-instances=3 \
  --concurrency=80 \
  --timeout=60 \
  --project=voom-482712

# ────────────────────────────────────────────────────────────────
# ÉTAPE 5 — CI/CD automatique avec Cloud Build
# ────────────────────────────────────────────────────────────────

# Connecter ton dépôt GitHub à Cloud Build
# → Console GCP → Cloud Build → Triggers → Connect Repository
# → Sélectionner GitHub → Choisir le repo matchingservice
# → Créer un trigger sur la branche main avec cloudbuild.yaml

# ────────────────────────────────────────────────────────────────
# ÉTAPE 6 — Tester le déploiement
# ────────────────────────────────────────────────────────────────

# 6.1 Récupérer l'URL du service
gcloud run services describe matchingservice \
  --region=europe-west9 \
  --format='value(status.url)' \
  --project=voom-482712

# 6.2 Tester avec authentification (service privé)
curl -H "Authorization: Bearer $(gcloud auth print-identity-token)" \
  https://matchingservice-XXXX-ew.a.run.app/actuator/health

# Réponse attendue : { "status": "UP" }

# ────────────────────────────────────────────────────────────────
# COMMANDES UTILES
# ────────────────────────────────────────────────────────────────

# Voir les logs en temps réel
gcloud run services logs tail matchingservice \
  --region=europe-west9 \
  --project=voom-482712

# Voir les révisions déployées
gcloud run revisions list \
  --service=matchingservice \
  --region=europe-west9 \
  --project=voom-482712

# Rollback vers la révision précédente
gcloud run services update-traffic matchingservice \
  --to-revisions=REVISION_PRECEDENTE=100 \
  --region=europe-west9 \
  --project=voom-482712