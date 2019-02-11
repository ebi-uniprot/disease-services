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
import uk.ac.ebi.uniprot.ds.common.dao.ProteinDAO;
import uk.ac.ebi.uniprot.ds.common.model.Protein;
import uk.ac.ebi.uniprot.ds.rest.exception.AssetNotFoundException;

import java.util.List;
import java.util.Optional;

@Service
public class ProteinService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProteinService.class);

    @Autowired
    private ProteinDAO proteinDAO;

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

    @Transactional
    public Protein createProtein(Protein protein){
        this.proteinDAO.save(protein);
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

    @Transactional
    public List<Protein> saveAll(List<Protein> proteins){
        return this.proteinDAO.saveAll(proteins);
    }

    public List<Protein> getAllProteinsByAccessions(List<String> accessions){
        List<Protein> proteins = this.proteinDAO.getProteinsByAccessions(accessions);
        return proteins;
    }



}