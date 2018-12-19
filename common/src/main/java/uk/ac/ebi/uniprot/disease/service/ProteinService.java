package uk.ac.ebi.uniprot.disease.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.kraken.interfaces.uniprot.*;
import uk.ac.ebi.kraken.interfaces.uniprot.comments.CommentType;
import uk.ac.ebi.kraken.interfaces.uniprot.comments.FunctionComment;
import uk.ac.ebi.kraken.interfaces.uniprot.comments.InteractionComment;
import uk.ac.ebi.kraken.interfaces.uniprot.description.FieldType;
import uk.ac.ebi.kraken.interfaces.uniprot.description.Name;
import uk.ac.ebi.kraken.interfaces.uniprot.features.FeatureType;
import uk.ac.ebi.uniprot.disease.model.Disease;
import uk.ac.ebi.uniprot.disease.model.Protein;
import uk.ac.ebi.uniprot.disease.utils.Constants;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class ProteinService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProteinService.class);
    // TODO make it spring autowired
    private DiseaseService diseaseService = new DiseaseService();

    public void createProtein(UniProtEntry uniProtEntry) {
        Protein protein = convertToProtein(uniProtEntry);
        Set<Disease> diseases = diseaseService.getDiseases(uniProtEntry.getComments(CommentType.DISEASE), protein);
        protein.setDiseases(diseases);
        // DAO layer code goes here to persist the protein object
        LOGGER.debug("The protein saved: {}", protein);
    }

    public Protein convertToProtein(UniProtEntry entry) {
        Protein.ProteinBuilder builder = Protein.builder();
        builder.id(entry.getUniProtId().getValue());
        builder.name(getDescription(entry.getProteinDescription()));
        builder.accession(entry.getPrimaryUniProtAccession().getValue());
        builder.gene(getGene(entry.getGenes()));
        builder.functions(getFunctions(entry.getComments(CommentType.FUNCTION)));
        builder.interactionCount(getInteractionCount(entry.getComments(CommentType.INTERACTION)));
        builder.diseaseCount(entry.getComments(CommentType.DISEASE).size());
        builder.variantCount(entry.getFeatures(FeatureType.VARIANT).size());
        builder.pathwayCount(getPathwayCount(entry));
        builder.publicationCount(entry.getCitationsNew().size());
        return builder.build();
    }

    private Integer getPathwayCount(UniProtEntry entry) {
        List<DatabaseCrossReference> dbXRefs = entry.getDatabaseCrossReferences(DatabaseType.REACTOME);
        return dbXRefs.size();
    }

    private Integer getInteractionCount(List<InteractionComment> comments) {

        Integer iCount = comments.parallelStream().map(InteractionComment::getInteractions).mapToInt(List::size).sum();

        return iCount;
    }

    private List<String> getFunctions(List<FunctionComment> comments) {

        List<String> functions = comments.stream().map(FunctionComment::getValue).collect(Collectors.toList());

        return functions;
    }

    private String getDescription(ProteinDescription pd) {
        Name name;

        if (pd.hasRecommendedName()) {
            name = pd.getRecommendedName();
        } else {
            name = pd.getSubNames().get(Constants.ZERO);
        }

        return name.getFieldsByType(FieldType.FULL).get(Constants.ZERO).getValue();
    }

    private String getGene(List<Gene> genes) {
        String geneName = null;
        String orfName = null;
        String olnName = null;

        for (Gene gene : genes) {
            if (gene.hasGeneName()) {
                geneName = gene.getGeneName().getValue();
                break;
            } else if (!isListEmpty(gene.getOrderedLocusNames())) {
                olnName = gene.getOrderedLocusNames().get(Constants.ZERO).getValue();
            } else if (!isListEmpty(gene.getORFNames())) {
                orfName = gene.getORFNames().get(Constants.ZERO).getValue();
            }
        }

        return (geneName != null) ? geneName : ((olnName != null) ? olnName : orfName);
    }

    // probably move it to a util class
    private boolean isListEmpty(List<?> list) {
        return list == null || list.isEmpty();
    }
}