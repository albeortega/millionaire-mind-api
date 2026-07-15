package com.niloortega.millionairemind.repository;

import com.niloortega.millionairemind.entity.ConversationEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConversationRepository extends JpaRepository<ConversationEntity, UUID> {
}
