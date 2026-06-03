package com.immobilier.repository;

import com.immobilier.entity.Conversation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ConversationRepository extends MongoRepository<Conversation, String> {

    Optional<Conversation> findByPropertyIdAndBuyerIdAndOwnerId(String propertyId, String buyerId, String ownerId);

    @Query("{ '$or': [ { 'buyerId': ?0 }, { 'ownerId': ?1 } ] }")
    List<Conversation> findUserConversations(String buyerId, String ownerId);
}
