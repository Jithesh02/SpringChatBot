package com.example.demo.email;


import com.example.demo.model.Customer;
import com.example.demo.model.Ticket;
import com.example.demo.service.customer.ICustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.support.SimpleTriggerContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailNotificationService {

    public final EmailService emailService;
    private final ICustomerService customerService;

    public void sendTicketNotificationEmail(Ticket ticket)
    {
        Customer customer = customerService.getCustomerByEmail(ticket.getConverstion().getCustomer().getEmailAddress());
        String customerName = customer.getFullName();
        String customerEmail = customer.getEmailAddress();
        String senderName = "Customer Support Service";
        String subject = "Support Ticket Created";
        String body = getHtmlBody(ticket, customerName, senderName);

        try{
            emailService.sendNotificationEmail(customerEmail, subject, senderName,body);
        }catch(Exception e)
        {
            throw new RuntimeException("Failed to send notification email");
        }
    }


       private static String getHtmlBody(Ticket ticket, String userName, String senderName) {
        String ticketDetails = ticket.getConverstion().getConversationSummary();
        String ticketTitle = ticket.getConverstion().getConversationTitle();
        String referenceNumber = ticket.getReferenceNumber();
        return "<html>" +
                "<body>" +
                "<p>Dear " + userName + ",</p>" +
                "<p>Thank you for contacting Customer Support. Your support ticket has been created successfully.</p>" +
                "<h3>Ticket Details:</h3>" +
                "<ul>" +
                "<li><strong>Reference Number:</strong> " + referenceNumber + "</li>" +
                "<li><strong>Title:</strong> " + ticketTitle + "</li>" +
                "<li><strong>Description:</strong> " + ticketDetails + "</li>" +
                "</ul>" +
                "<p>If you have any questions, feel free to reply to this email or contact us at support@example.com.</p>" +
                "<p>Best regards,<br/>" + senderName + "</p>" +
                "</body>" +
                "</html>";
    }
}
