package com.niloortega.millionairemind.repository;

import com.niloortega.millionairemind.entity.MessageEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MessageRepository extends JpaRepository<MessageEntity, UUID> {
}
