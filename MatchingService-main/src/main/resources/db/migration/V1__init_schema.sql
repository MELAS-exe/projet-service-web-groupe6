-- ════════════════════════════════════════════════════════════════════════════
-- V1__init_schema.sql
-- Flyway exécute ce fichier UNE SEULE FOIS au premier démarrage
-- Chemin : src/main/resources/db/migration/V1__init_schema.sql
-- ════════════════════════════════════════════════════════════════════════════

CREATE EXTENSION IF NOT EXISTS postgis;
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ── TABLE : demandes_course ───────────────────────────────────────────────────

CREATE TABLE demandes_course (
                                 id                      UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                 passager_id             VARCHAR(255) NOT NULL,
                                 type_course             VARCHAR(50)  NOT NULL,
                                 type_paiement           VARCHAR(50)  NOT NULL,
                                 ville_depart            VARCHAR(100) NOT NULL,
                                 ville_arrivee           VARCHAR(100) NOT NULL,
                                 adresse_depart          TEXT,
                                 adresse_arrivee         TEXT,
                                 coords_depart           GEOGRAPHY(POINT, 4326) NOT NULL,
                                 coords_arrivee          GEOGRAPHY(POINT, 4326) NOT NULL,
                                 date_heure_depart       TIMESTAMPTZ  NOT NULL,
                                 tolerance_minutes       INT          NOT NULL DEFAULT 15,
                                 nombre_places           INT          NOT NULL DEFAULT 1,
                                 nombre_bagages          INT                   DEFAULT 0,
                                 poids_estime_bagages_kg DECIMAL(6,2)          DEFAULT 0,
                                 description_bagages     TEXT,
                                 prix_propose            DECIMAL(10,2)         DEFAULT 0,
                                 statut                  VARCHAR(50)  NOT NULL DEFAULT 'EN_ATTENTE',
                                 affectation_id          UUID,
                                 trajet_id               UUID,
                                 invitation_trajet_id    UUID,
                                 conducteurs_refus_ids   TEXT[]                DEFAULT '{}',
                                 date_creation           TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                                 date_modification       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

-- Index de matching
CREATE INDEX idx_demande_matching ON demandes_course
    (ville_depart, ville_arrivee, type_course, statut, date_heure_depart);

-- Index géospatial PostGIS
CREATE INDEX idx_demande_coords ON demandes_course USING GIST (coords_depart);

CREATE INDEX idx_demande_passager ON demandes_course (passager_id);

-- ── TABLE : trajets ───────────────────────────────────────────────────────────

CREATE TABLE trajets (
                         id                              UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         conducteur_id                   VARCHAR(255),
                         vehicule_id                     VARCHAR(255),
                         type_course                     VARCHAR(50)  NOT NULL,
                         ville_depart                    VARCHAR(100) NOT NULL,
                         ville_arrivee                   VARCHAR(100) NOT NULL,
                         date_heure_depart               TIMESTAMPTZ  NOT NULL,
                         date_heure_fin                  TIMESTAMPTZ,
                         places_occupees                 INT          NOT NULL DEFAULT 0,
                         places_totales                  INT          NOT NULL DEFAULT 4,
                         capacite_chargement_utilisee_kg DECIMAL(6,2)          DEFAULT 0,
                         capacite_chargement_totale_kg   DECIMAL(6,2)          DEFAULT 50,
                         tarif_total                     DECIMAL(10,2)         DEFAULT 0,
                         statut                          VARCHAR(50)  NOT NULL DEFAULT 'EN_ATTENTE_CONDUCTEUR',
                         affectation_id                  UUID,
                         reservation_amis_autorisee      BOOLEAN               DEFAULT TRUE,
                         coords_actuelles                GEOGRAPHY(POINT, 4326),
                         demandes_ids                    TEXT[]                DEFAULT '{}',
                         date_creation                   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                         date_modification               TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_trajet_statut     ON trajets (statut);
CREATE INDEX idx_trajet_conducteur ON trajets (conducteur_id);
CREATE INDEX idx_trajet_villes     ON trajets (ville_depart, ville_arrivee);

-- ── TABLE : affectations ──────────────────────────────────────────────────────

CREATE TABLE affectations (
                              id                    UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                              trajet_id             UUID NOT NULL REFERENCES trajets(id),
                              conducteur_id         VARCHAR(255),
                              demandes_ids          TEXT[]                DEFAULT '{}',
                              revenus_estimes       DECIMAL(10,2)         DEFAULT 0,
                              distance_totale_km    DECIMAL(8,2)          DEFAULT 0,
                              date_affectation      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                              date_expiration       TIMESTAMPTZ  NOT NULL,
                              statut                VARCHAR(50)  NOT NULL DEFAULT 'PROPOSEE',
                              tentative_numero      INT          NOT NULL DEFAULT 1,
                              conducteurs_refus_ids TEXT[]                DEFAULT '{}',
                              date_creation         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                              date_modification     TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_affectation_statut     ON affectations (statut);
CREATE INDEX idx_affectation_conducteur ON affectations (conducteur_id);
CREATE INDEX idx_affectation_trajet     ON affectations (trajet_id);
-- Index partiel pour les expirations (très performant)
CREATE INDEX idx_affectation_expiration ON affectations (date_expiration)
    WHERE statut = 'PROPOSEE';

-- ── TABLE : bagages ───────────────────────────────────────────────────────────

CREATE TABLE bagages (
                         id                          UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                         demande_id                  UUID NOT NULL REFERENCES demandes_course(id),
                         expediteur_id               VARCHAR(255) NOT NULL,
                         destinataire_id             VARCHAR(255),
                         description                 TEXT         NOT NULL,
                         types                       TEXT[]       NOT NULL,
                         poids_kg                    DECIMAL(6,2) NOT NULL,
                         fragile                     BOOLEAN               DEFAULT FALSE,
                         entre_amis                  BOOLEAN               DEFAULT FALSE,
                         ami_destinataire_id         VARCHAR(255),
                         statut                      VARCHAR(50)  NOT NULL DEFAULT 'EN_ATTENTE',
                         date_creation               TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                         date_confirmation_reception TIMESTAMPTZ
);

CREATE INDEX idx_bagage_demande     ON bagages (demande_id);
CREATE INDEX idx_bagage_expediteur  ON bagages (expediteur_id);

-- ── TABLE : invitations_trajet ────────────────────────────────────────────────

CREATE TABLE invitations_trajet (
                                    id               UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
                                    trajet_id        UUID NOT NULL REFERENCES trajets(id),
                                    inviteur_id      VARCHAR(255) NOT NULL,
                                    invite_id        VARCHAR(255) NOT NULL,
                                    statut           VARCHAR(50)  NOT NULL DEFAULT 'ENVOYEE',
                                    date_invitation  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
                                    date_expiration  TIMESTAMPTZ  NOT NULL DEFAULT NOW() + INTERVAL '24 hours',
                                    date_reponse     TIMESTAMPTZ,
                                    message          TEXT,
                                    places_proposees INT                   DEFAULT 1,
                                    tarif_ami        DECIMAL(10,2)         DEFAULT 0
);

CREATE INDEX idx_invitation_invite ON invitations_trajet (invite_id);
CREATE INDEX idx_invitation_trajet ON invitations_trajet (trajet_id);

-- ── TRIGGER : date_modification automatique ───────────────────────────────────

CREATE OR REPLACE FUNCTION update_date_modification()
RETURNS TRIGGER AS $$
BEGIN NEW.date_modification = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_demande_modif     BEFORE UPDATE ON demandes_course  FOR EACH ROW EXECUTE FUNCTION update_date_modification();
CREATE TRIGGER trg_trajet_modif      BEFORE UPDATE ON trajets           FOR EACH ROW EXECUTE FUNCTION update_date_modification();
CREATE TRIGGER trg_affectation_modif BEFORE UPDATE ON affectations      FOR EACH ROW EXECUTE FUNCTION update_date_modification();

ALTER TABLE trajets ADD COLUMN IF NOT EXISTS distance_reelle_km FLOAT DEFAULT 0;
ALTER TABLE trajets ADD COLUMN IF NOT EXISTS date_heure_arrivee TIMESTAMP;
ALTER TABLE trajets ADD COLUMN IF NOT EXISTS coords_depart geography(Point,4326);
ALTER TABLE trajets ADD COLUMN IF NOT EXISTS coords_arrivee geography(Point,4326);

ALTER TABLE bagages ADD COLUMN IF NOT EXISTS type_operation VARCHAR(50) DEFAULT 'BAGAGE_PERSONNEL';
ALTER TABLE bagages ADD COLUMN IF NOT EXISTS prix_calcule FLOAT DEFAULT 0;









-- ════════════════════════════════════════════════════════════════
-- Migration : Gestion des colis
-- ════════════════════════════════════════════════════════════════

-- Table des demandes d'envoi de colis
CREATE TABLE IF NOT EXISTS demandes_envoi (
                                              id                          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    expediteur_id               VARCHAR(255) NOT NULL,
    destinataire_id             VARCHAR(255) NOT NULL,
    ville_depart                VARCHAR(100) NOT NULL,
    ville_arrivee               VARCHAR(100) NOT NULL,
    adresse_depart              VARCHAR(255),
    adresse_arrivee             VARCHAR(255),
    date_heure_souhaitee        TIMESTAMP NOT NULL,
    tolerance_minutes           INT DEFAULT 60,
    description                 VARCHAR(500) NOT NULL,
    poids_kg                    FLOAT NOT NULL,
    fragile                     BOOLEAN DEFAULT FALSE,
    dimensions_cm               VARCHAR(50),
    type_paiement               VARCHAR(50) NOT NULL,
    prix_propose                FLOAT DEFAULT 0,
    prix_final                  FLOAT DEFAULT 0,
    statut                      VARCHAR(50) NOT NULL DEFAULT 'EN_ATTENTE',
    trajet_id                   UUID,
    affectation_id              UUID,
    code_confirmation           VARCHAR(10),
    date_confirmation_livraison TIMESTAMP,
    date_creation               TIMESTAMP DEFAULT NOW(),
    date_modification           TIMESTAMP DEFAULT NOW()
    );

-- Index pour les requêtes fréquentes
CREATE INDEX IF NOT EXISTS idx_envoi_expediteur
    ON demandes_envoi(expediteur_id);

CREATE INDEX IF NOT EXISTS idx_envoi_destinataire
    ON demandes_envoi(destinataire_id);

CREATE INDEX IF NOT EXISTS idx_envoi_statut
    ON demandes_envoi(statut);

CREATE INDEX IF NOT EXISTS idx_envoi_trajet
    ON demandes_envoi(trajet_id);

-- Index partiel pour le matching (uniquement EN_ATTENTE)
CREATE INDEX IF NOT EXISTS idx_envoi_matching
    ON demandes_envoi(ville_depart, ville_arrivee, date_heure_souhaitee)
    WHERE statut = 'EN_ATTENTE';

-- ── Ajouter colisIds dans la table trajets ────────────────────────────────────
ALTER TABLE trajets ADD COLUMN IF NOT EXISTS colis_ids TEXT[] DEFAULT '{}';


ALTER TABLE trajets ADD COLUMN IF NOT EXISTS colis_ids TEXT[] DEFAULT '{}';

ALTER TABLE demandes_envoi ADD COLUMN IF NOT EXISTS coords_depart geography(Point,4326);
ALTER TABLE demandes_envoi ADD COLUMN IF NOT EXISTS coords_arrivee geography(Point,4326);

ALTER TABLE demandes_course ADD COLUMN IF NOT EXISTS prix_calcule INT DEFAULT 0;
ALTER TABLE demandes_course DROP COLUMN IF EXISTS prix_propose;


ALTER TABLE demandes_envoi ADD COLUMN IF NOT EXISTS region_depart VARCHAR(50);
ALTER TABLE demandes_envoi ADD COLUMN IF NOT EXISTS region_arrivee VARCHAR(50);
ALTER TABLE demandes_envoi ADD COLUMN IF NOT EXISTS categorie_colis VARCHAR(50);
ALTER TABLE demandes_course ADD COLUMN IF NOT EXISTS region_depart VARCHAR(50);
ALTER TABLE demandes_course ADD COLUMN IF NOT EXISTS region_arrivee VARCHAR(50);