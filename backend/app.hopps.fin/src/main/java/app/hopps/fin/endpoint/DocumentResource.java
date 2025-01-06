package app.hopps.fin.endpoint;

import app.hopps.commons.DocumentType;
import app.hopps.fin.S3Handler;
import app.hopps.fin.jpa.TransactionRecordRepository;
import app.hopps.fin.jpa.entities.TransactionRecord;
import app.hopps.fin.kafka.DocumentProducer;
import io.quarkus.security.Authenticated;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.jboss.resteasy.reactive.PartType;
import org.jboss.resteasy.reactive.RestForm;
import org.jboss.resteasy.reactive.multipart.FileUpload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;

import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Optional;

@Authenticated
@Path("/document")
public class DocumentResource {
    private static final Logger LOG = LoggerFactory.getLogger(DocumentResource.class);

    private final DocumentProducer documentProducer;
    private final S3Handler s3Handler;
    private final TransactionRecordRepository repository;

    @Inject
    public DocumentResource(DocumentProducer documentProducer, S3Handler s3Handler,
            TransactionRecordRepository repository) {
        this.documentProducer = documentProducer;
        this.s3Handler = s3Handler;
        this.repository = repository;
    }

    @GET
    @Path("{documentKey}")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream getDocumentByKey(@PathParam("documentKey") String documentKey) {
        // TODO: How to verify that user has access? --> FGAC
        // TODO: Set the media type header dynamic dependent on PNG, JPEG or PDF
        // Go against the database get the bommel id then towards openfga?
        try {
            return s3Handler.getFile(documentKey);
        } catch (NoSuchKeyException ignored) {
            LOG.info("File with key {} not found", documentKey);
            throw new NotFoundException(Response.status(Response.Status.NOT_FOUND)
                    .entity("Document with key " + documentKey + " not found")
                    .build());
        }
    }

    @POST
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces(MediaType.APPLICATION_JSON)
    public Response uploadDocument(
            @RestForm("file") FileUpload file,
            @RestForm @PartType(MediaType.TEXT_PLAIN) Optional<Long> bommelId,
            @RestForm @PartType(MediaType.TEXT_PLAIN) Optional<DocumentType> type) {
        s3Handler.saveFile(file);

        // Save in database
        TransactionRecord transactionRecord = new TransactionRecord(BigDecimal.ZERO);
        transactionRecord.setDocumentKey(file.fileName());
        bommelId.ifPresent(transactionRecord::setBommelId);

        persistTransactionRecord(transactionRecord);

        // Sent to kafka to process
        documentProducer.sendToProcess(transactionRecord, type.orElse(DocumentType.INVOICE));

        return Response.accepted().build();
    }

    @Transactional
    void persistTransactionRecord(TransactionRecord transactionRecord) {
        repository.persist(transactionRecord);
    }
}
