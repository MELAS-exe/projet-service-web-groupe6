package sn.Voom.matchingservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import sn.Voom.matchingservice.dto.request.CreerBagageRequest;
import sn.Voom.matchingservice.dto.response.BagageResponse;
import sn.Voom.matchingservice.entite.Bagage;
import sn.Voom.matchingservice.entite.enums.StatutBagage;
import sn.Voom.matchingservice.entite.enums.TypeBagage;
import sn.Voom.matchingservice.exception.BagageNotFoundException;
import sn.Voom.matchingservice.exception.DemandeCourseNotFoundException;
import sn.Voom.matchingservice.repository.BagageRepository;
import sn.Voom.matchingservice.repository.DemandeCourseRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BagageService {

    private final BagageRepository bagageRepo;
    private final DemandeCourseRepository demandeRepo;

    public BagageResponse creer(CreerBagageRequest req) {
        // Vérifier que la demande existe
        demandeRepo.trouverParId(req.getDemandeId())
                .orElseThrow(() -> new DemandeCourseNotFoundException(req.getDemandeId()));

        Bagage bagage = Bagage.builder()
                .demandeId(UUID.fromString(req.getDemandeId()))
                .expediteurId(req.getExpediteurId())
                .destinataireId(req.getDestinataireId())
                .description(req.getDescription())
                .types(req.getTypes().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList()))
                .poidsKg(req.getPoidsKg())
                .fragile(req.isFragile())
                .entreAmis(req.isEntreAmis())
                .amiDestinataireId(req.getAmiDestinataireId())
                .statut(StatutBagage.EN_ATTENTE)
                .dateCreation(LocalDateTime.now())
                .build();

        bagageRepo.sauvegarder(bagage);

        // En JPA, le lien est assuré par bagage.demandeId en base.
        // Pas besoin de mettre à jour la demande manuellement.

        return versResponse(bagage);
    }

    public BagageResponse trouverParId(String id) {
        return versResponse(bagageRepo.trouverParId(id)
                .orElseThrow(() -> new BagageNotFoundException(id)));
    }

    public List<BagageResponse> listerParDemande(String demandeId) {
        return bagageRepo.trouverParDemande(demandeId).stream()
                .map(this::versResponse).collect(Collectors.toList());
    }

    /** Le destinataire confirme la réception du bagage. */
    public BagageResponse confirmerReception(String id) {
        Bagage b = bagageRepo.trouverParId(id).orElseThrow(() -> new BagageNotFoundException(id));
        b.setStatut(StatutBagage.LIVRE);
        b.setDateConfirmationReception(LocalDateTime.now());
        bagageRepo.sauvegarder(b);
        bagageRepo.mettreAJourStatut(id, StatutBagage.LIVRE);
        return versResponse(b);
    }

    public BagageResponse versResponse(Bagage b) {
        return BagageResponse.builder()
                .id(String.valueOf(b.getId()))
                .demandeId(String.valueOf(b.getDemandeId()))
                .expediteurId(String.valueOf(b.getExpediteurId()))
                .destinataireId(b.getDestinataireId())
                .description(b.getDescription())
                .types(b.getTypes().stream()
                        .map(t -> TypeBagage.valueOf(t))  // String → TypeBagage si tu lis depuis la BDD
                        .collect(Collectors.toList()))
                .poidsKg(b.getPoidsKg())
                .fragile(b.isFragile())
                .entreAmis(b.isEntreAmis())
                .amiDestinataireId(b.getAmiDestinataireId())
                .statut(b.getStatut())
                .dateCreation(b.getDateCreation())
                .build();
    }
}