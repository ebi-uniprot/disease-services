/*
 * Created by sahmad on 07/02/19 12:19
 * UniProt Consortium.
 * Copyright (c) 2002-2019.
 *
 */

package uk.ac.ebi.uniprot.ds.rest.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.uniprot.ds.common.dao.ProteinCrossRefDAO;
import uk.ac.ebi.uniprot.ds.common.dao.ProteinDAO;
import uk.ac.ebi.uniprot.ds.common.model.*;
import uk.ac.ebi.uniprot.ds.rest.exception.AssetNotFoundException;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProteinService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProteinService.class);

    @Autowired
    private ProteinDAO proteinDAO;
    @Autowired
    private DiseaseService diseaseService;
    @Autowired
    private ProteinCrossRefDAO proteinCrossRefDAO;

    @Transactional
    public Protein createProtein(String proteinId, String proteinName, String accession, String gene, String description){
        LOGGER.info("Creating protein with protein id {}", proteinId);
        Protein.ProteinBuilder builder = Protein.builder();
        builder.proteinId(proteinId).name(proteinName);
        builder.accession(accession).gene(gene).desc(description);
        Protein protein = builder.build();
        this.proteinDAO.save(protein);
        LOGGER.info("The protein created with id {}", protein.getId());
        return protein;
    }

    public Optional<Protein> getProteinByProteinId(String proteinId){
        return this.proteinDAO.findByProteinId(proteinId);
    }

    public Optional<Protein> getProteinByAccession(String accession){
        Optional<Protein> optProtein = this.proteinDAO.findProteinByAccession(accession);
        if(!optProtein.isPresent()){
            throw new AssetNotFoundException("Unable to find the accession '" + accession + "'.");
        }
        return optProtein;
    }

    public List<Protein> getAllProteinsByAccessions(List<String> accessions){
        List<Protein> proteins = this.proteinDAO.getProteinsByAccessions(accessions);
        return proteins;
    }

    public List<ProteinCrossRef> getProteinCrossRefsByAccession(String accession){
        Optional<Protein> optProtein = this.proteinDAO.findProteinByAccession(accession);
        List<ProteinCrossRef> proteinCrossRefs = new ArrayList<>();
        if(optProtein.isPresent()){
            proteinCrossRefs = optProtein.get().getProteinCrossRefs();
        }

        // populate all the proteins access where each cross ref primary id is involved
        for(int i = 0; i < proteinCrossRefs.size(); i++){
            ProteinCrossRef pXRef = proteinCrossRefs.get(i);
            List<ProteinCrossRef> xrefs = this.proteinCrossRefDAO.findAllByPrimaryId(pXRef.getPrimaryId());
            List<String> accessions = xrefs.stream()
                                            .filter(xref -> xref.getProtein() != null)
                                            .map(xref -> xref.getProtein().getAccession())
                                            .collect(Collectors.toList());
            pXRef.setProteinAccessions(accessions);

        }

        return proteinCrossRefs;
    }

    public List<Interaction> getProteinInteractions(String accession){
        Set<String> accessions = new HashSet<>();
        return getProteinByAccession(accession)
                .map(protein -> protein.getInteractions()
                        .stream()
                        .filter(intrxn -> {
                            if(accessions.contains(intrxn.getAccession())){
                                return false;
                            } else {
                                accessions.add(intrxn.getAccession());
                                return true;
                            }
                        })
                        .collect(Collectors.toList())
                )
                .orElse(null);
    }

    public List<Protein> getProteinsByDiseaseId(String diseaseId) {
        // check if a protein is mapped by any of the child then that Protein is called internally mapped
        // and isExternallyMapped will be set to false
        Map<String, Boolean> accessionIsMapped = new HashMap<>();
        Set<DiseaseProtein> dps = this.diseaseService.getDiseaseAndItsChildren(diseaseId)
                .stream()
                .map(dis -> dis.getDiseaseProteins())
                .flatMap(Set::stream).collect(Collectors.toSet());

        populateAccessionIsMappedByUniProt(dps, accessionIsMapped);

        Set<Protein> proteins = dps.stream().map(dp -> getProtein(dp, accessionIsMapped))
                .collect(Collectors.toSet());
        return new ArrayList<>(proteins);
    }

    public List<Protein> getProteinsByDrugName(String drugName) {
        return this.proteinDAO.findAllByDrugName(drugName);
    }

    private Protein getProtein(DiseaseProtein dp, Map<String, Boolean> accessionIsMapped) {
        Protein p = dp.getProtein();
        p.setIsExternallyMapped(accessionIsMapped.getOrDefault(p.getAccession(), Boolean.FALSE));
        return p;
    }

    private void populateAccessionIsMappedByUniProt(Set<DiseaseProtein> dps, Map<String, Boolean> accessionIsMapped) {
        for(DiseaseProtein dp : dps){
            Boolean isMapped = Objects.isNull(dp.getIsMapped()) ? Boolean.FALSE : dp.getIsMapped();
            String accession = dp.getProtein().getAccession();
            if(accessionIsMapped.containsKey(accession)){
                accessionIsMapped.put(accession, isMapped && accessionIsMapped.get(accession));
            } else {
                accessionIsMapped.put(accession, isMapped);
            }
        }
    }
}
