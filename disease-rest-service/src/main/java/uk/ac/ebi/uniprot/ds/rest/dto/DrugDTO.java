package uk.ac.ebi.uniprot.ds.rest.dto;

import lombok.*;

import java.util.List;
import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugDTO {
    private String name;
    private String sourceType;
    private String sourceId;
    private String moleculeType;
    private Integer clinicalTrialPhase;
    private String mechanismOfAction;
    private String clinicalTrialLink;
    private Set<String> evidences;
    private Set<String> proteins;
    private Set<BasicDiseaseDTO> diseases;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode
    public static class BasicDiseaseDTO{
        private String diseaseId;
        private String diseaseName;
        private Integer proteinCount;
    }
}
