package com.immobilier.service;

import com.immobilier.dto.OfferRequest;
import com.immobilier.dto.VisitRequest;
import com.immobilier.entity.AppNotification;
import com.immobilier.entity.ChatMessage;
import com.immobilier.entity.Conversation;
import com.immobilier.entity.Property;
import com.immobilier.entity.User;
import com.immobilier.exception.ResourceNotFoundException;
import com.immobilier.repository.AppNotificationRepository;
import com.immobilier.repository.ChatMessageRepository;
import com.immobilier.repository.ConversationRepository;
import com.immobilier.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InteractionService {

    private final PropertyRepository propertyRepository;
    private final ConversationRepository conversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AppNotificationRepository notificationRepository;

    public Conversation createOfferConversation(String propertyId, OfferRequest request, User buyer) {
        Property property = getProperty(propertyId);
        ensureNotOwner(property, buyer);

        Conversation conversation = conversationRepository
                .findByPropertyIdAndBuyerIdAndOwnerId(propertyId, buyer.getId(), property.getOwnerId())
                .orElseGet(() -> conversationRepository.save(Conversation.builder()
                        .propertyId(property.getId())
                        .propertyTitle(property.getTitle())
                        .buyerId(buyer.getId())
                        .buyerName(buyer.getFullName())
                        .ownerId(property.getOwnerId())
                        .ownerName(property.getOwnerName())
                        .build()));

        String content = buildOfferMessage(request);
        saveMessage(conversation.getId(), buyer, content);

        notificationRepository.save(AppNotification.builder()
                .recipientId(property.getOwnerId())
                .actorId(buyer.getId())
                .actorName(buyer.getFullName())
                .propertyId(property.getId())
                .propertyTitle(property.getTitle())
                .type("OFFER")
                .title("Nouvelle offre")
                .message(buyer.getFullName() + " a fait une offre sur " + property.getTitle())
                .build());

        return conversation;
    }

    public AppNotification requestVisit(String propertyId, VisitRequest request, User visitor) {
        Property property = getProperty(propertyId);
        ensureNotOwner(property, visitor);

        String message = visitor.getFullName() + " souhaite planifier une visite pour " + property.getTitle();
        if (request.getRequestedDate() != null && !request.getRequestedDate().isBlank()) {
            message += " le " + request.getRequestedDate();
        }
        message += ". Message: " + request.getMessage();

        return notificationRepository.save(AppNotification.builder()
                .recipientId(property.getOwnerId())
                .actorId(visitor.getId())
                .actorName(visitor.getFullName())
                .propertyId(property.getId())
                .propertyTitle(property.getTitle())
                .type("VISIT_REQUEST")
                .title("Demande de visite")
                .message(message)
                .build());
    }

    public List<Conversation> getUserConversations(User user) {
        return conversationRepository.findUserConversations(user.getId(), user.getId()).stream()
                .sorted(Comparator.comparing(
                        Conversation::getUpdatedAt,
                        Comparator.nullsLast(Comparator.naturalOrder())
                ).reversed())
                .collect(Collectors.toList());
    }

    public Conversation getConversationForUser(String conversationId, User user) {
        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException("Conversation non trouvee"));
        if (!conversation.getBuyerId().equals(user.getId()) && !conversation.getOwnerId().equals(user.getId())) {
            throw new IllegalArgumentException("Vous n'avez pas acces a cette conversation");
        }
        return conversation;
    }

    public List<ChatMessage> getMessages(String conversationId, User user) {
        getConversationForUser(conversationId, user);
        return chatMessageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId);
    }

    public ChatMessage sendMessage(String conversationId, User sender, String content) {
        Conversation conversation = getConversationForUser(conversationId, sender);
        ChatMessage message = saveMessage(conversationId, sender, content);

        String recipientId = conversation.getBuyerId().equals(sender.getId())
                ? conversation.getOwnerId()
                : conversation.getBuyerId();

        notificationRepository.save(AppNotification.builder()
                .recipientId(recipientId)
                .actorId(sender.getId())
                .actorName(sender.getFullName())
                .propertyId(conversation.getPropertyId())
                .propertyTitle(conversation.getPropertyTitle())
                .type("CHAT_MESSAGE")
                .title("Nouveau message")
                .message(sender.getFullName() + " vous a envoye un message sur " + conversation.getPropertyTitle())
                .build());

        return message;
    }

    private ChatMessage saveMessage(String conversationId, User sender, String content) {
        return chatMessageRepository.save(ChatMessage.builder()
                .conversationId(conversationId)
                .senderId(sender.getId())
                .senderName(sender.getFullName())
                .content(content)
                .build());
    }

    private Property getProperty(String propertyId) {
        return propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Propriete non trouvee"));
    }

    private void ensureNotOwner(Property property, User user) {
        if (property.getOwnerId() != null && property.getOwnerId().equals(user.getId())) {
            throw new IllegalArgumentException("Vous ne pouvez pas effectuer cette action sur votre propre bien");
        }
    }

    private String buildOfferMessage(OfferRequest request) {
        String content = request.getMessage();
        if (request.getAmount() != null) {
            content = "Offre proposee: " + request.getAmount() + " FCFA. " + content;
        }
        return content;
    }
}
