/*
 * Created by sahmad on 07/02/19 10:37
 * UniProt Consortium.
 * Copyright (c) 2002-2019.
 *
 */

package uk.ac.ebi.uniprot.ds.common.dao;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import uk.ac.ebi.uniprot.ds.common.model.Disease;
import uk.ac.ebi.uniprot.ds.common.model.Protein;

import java.util.List;
import java.util.Optional;

public interface DiseaseDAO extends JpaRepository<Disease, Long>, DiseaseDAOCustom {
    @Transactional
    Optional<Disease> findByDiseaseId(String diseaseId);

    @Transactional
    void deleteByDiseaseId(String diseaseId);

    Optional<Disease> findDiseaseByNameIgnoreCase(String diseaseName);

    Optional<Disease> findDiseaseByDiseaseIdOrNameOrAcronym(String diseaseId, String name, String acronym);

    List<Disease> findByNameContainingIgnoreCaseOrDescContainingIgnoreCase(String name, String desc, Pageable pageable);
    List<Disease> findByNameContainingIgnoreCase(String name, Pageable pageable);
    List<Disease> findByDescContainingIgnoreCase(String desc, Pageable pageable);

    @Query(nativeQuery = true,
            value = "select dd.* from ds_disease dd \n" +
                    "join ds_drug dr on dd.id = dr.ds_disease_id \n" +
                    "where dr.\"name\" = ?1")
    List<Disease> findAllByDrugName(String name);
}
