package uk.ac.ebi.uniprot.disease.model.uniprot;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Keyword {
    private String keyId;
    private String keyValue;
    private Integer diseaseId;

}
