package app.hopps;

import app.hopps.model.InvoiceDocument;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.eclipse.microprofile.reactive.messaging.Outgoing;

@ApplicationScoped
public class KafkaConnector {

    @Incoming("source")
    @Outgoing("sink")
    public String process(InvoiceDocument invoiceDocument) {
        // Process the incoming message payload and return an updated payload
        return invoiceDocument.URL().toUpperCase();
    }
}
