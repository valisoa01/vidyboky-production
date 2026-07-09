package com.example.demo.service.event;

import com.example.demo.endpoint.event.model.InvoiceRequested;
import com.example.demo.librairie.dto.InvoiceResponse;
import com.example.demo.librairie.entity.Customer;
import com.example.demo.librairie.entity.Order;
import com.example.demo.librairie.exception.ResourceNotFoundException;
import com.example.demo.librairie.repository.OrderRepository;
import com.example.demo.librairie.service.InvoiceService;
import com.example.demo.mail.Email;
import com.example.demo.mail.Mailer;
import jakarta.mail.internet.InternetAddress;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class InvoiceRequestedService implements Consumer<InvoiceRequested> {

  private final InvoiceService invoiceService;
  private final OrderRepository orderRepository;
  private final Mailer mailer;

  @Override
  public void accept(InvoiceRequested event) {
    UUID orderId = UUID.fromString(event.getOrderId());

    invoiceService.generateAndStore(orderId);

    Order order =
        orderRepository
            .findDetailedById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", orderId));
    Customer customer = order.getCustomer();

    List<InvoiceResponse> invoices = invoiceService.listByCustomer(customer.getId());
    sendInvoiceEmail(customer, invoices);

    log.info("Invoice email sent to {} for order {}", customer.getEmail(), orderId);
  }

  @SneakyThrows
  private void sendInvoiceEmail(Customer customer, List<InvoiceResponse> invoices) {
    var recipient = new InternetAddress(customer.getEmail());
    var htmlBody = buildHtmlBody(customer, invoices);
    mailer.accept(
        new Email(recipient, List.of(), List.of(), "Vos factures Vidy-Boky", htmlBody, List.of()));
  }

  private String buildHtmlBody(Customer customer, List<InvoiceResponse> invoices) {
    var sb = new StringBuilder();
    sb.append("<p>Bonjour ").append(customer.getFirstname()).append(",</p>");
    sb.append("<p>Merci pour votre achat. Voici la liste de vos factures ")
        .append("(liens de téléchargement valables 15 minutes) :</p>")
        .append("<ul>");
    for (InvoiceResponse invoice : invoices) {
      sb.append("<li>Commande ")
          .append(invoice.getOrderId())
          .append(" du ")
          .append(invoice.getGenerationDate())
          .append(" — <a href=\"")
          .append(invoice.getDownloadUrl())
          .append("\">Télécharger la facture</a></li>");
    }
    sb.append("</ul>");
    return sb.toString();
  }
}
