package com.example.demo.librairie.service;

import com.example.demo.librairie.entity.Order;
import com.example.demo.librairie.entity.OrderLine;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class InvoicePdfGenerator {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Font TITLE_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
    private static final Font HEADER_FONT = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font NORMAL_FONT = FontFactory.getFont(FontFactory.HELVETICA, 10);

    public File generate(Order order) {
        try {
            var pdfFile = File.createTempFile("invoice-" + order.getId(), ".pdf");
            var document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter.getInstance(document, new FileOutputStream(pdfFile));

            document.open();
            writeHeader(document, order);
            writeCustomer(document, order);
            writeLines(document, order);
            writeTotal(document, order);
            document.close();

            return pdfFile;
        } catch (Exception e) {
            throw new InvoiceGenerationException(order.getId(), e);
        }
    }

    private void writeHeader(Document document, Order order) throws IOException {
        try {
            document.add(new Paragraph("FACTURE - VIDY-BOKY", TITLE_FONT));
            document.add(new Paragraph("Commande n° " + order.getId(), NORMAL_FONT));
            document.add(
                    new Paragraph("Date : " + order.getOrderDate().format(DATE_FORMATTER), NORMAL_FONT));
            document.add(new Paragraph("Type : " + order.getOrderType(), NORMAL_FONT));
            document.add(new Paragraph(" "));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void writeCustomer(Document document, Order order) throws IOException {
        try {
            var customer = order.getCustomer();
            document.add(new Paragraph("Client", HEADER_FONT));
            document.add(
                    new Paragraph(customer.getFirstname() + " " + customer.getName(), NORMAL_FONT));
            document.add(new Paragraph(customer.getEmail(), NORMAL_FONT));
            document.add(new Paragraph(customer.getPhone(), NORMAL_FONT));
            document.add(new Paragraph(" "));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void writeLines(Document document, Order order) throws IOException {
        try {
            var table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[] {4, 2, 1, 2});

            addHeaderCell(table, "Livre / Format");
            addHeaderCell(table, "Prix unitaire");
            addHeaderCell(table, "Qté");
            addHeaderCell(table, "Total ligne");

            for (OrderLine line : order.getLines()) {
                var bookFormat = line.getBookFormat();
                var label = bookFormat.getBook().getTitle() + " (" + bookFormat.getFormat().getFormatType() + ")";
                var lineTotal = line.getUnitPrice() * line.getQuantity();

                addBodyCell(table, label);
                addBodyCell(table, formatAmount(line.getUnitPrice()));
                addBodyCell(table, String.valueOf(line.getQuantity()));
                addBodyCell(table, formatAmount(lineTotal));
            }

            document.add(table);
            document.add(new Paragraph(" "));
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void writeTotal(Document document, Order order) throws IOException {
        try {
            var total =
                    order.getLines().stream().mapToDouble(l -> l.getUnitPrice() * l.getQuantity()).sum();

            var totalParagraph = new Paragraph("Total : " + formatAmount(total), HEADER_FONT);
            totalParagraph.setAlignment(Element.ALIGN_RIGHT);
            document.add(totalParagraph);

            if (order.getPayment() != null) {
                document.add(
                        new Paragraph(
                                "Paiement : " + order.getPayment().getPaymentType() + " le "
                                        + order.getPayment().getPaymentDate().format(DATE_FORMATTER),
                                NORMAL_FONT));
            }
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    private void addHeaderCell(PdfPTable table, String text) {
        var cell = new PdfPCell(new Paragraph(text, HEADER_FONT));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private void addBodyCell(PdfPTable table, String text) {
        var cell = new PdfPCell(new Paragraph(text, NORMAL_FONT));
        cell.setPadding(6);
        table.addCell(cell);
    }

    private String formatAmount(double amount) {
        return String.format(Locale.FRANCE, "%,.2f Ar", amount);
    }
}