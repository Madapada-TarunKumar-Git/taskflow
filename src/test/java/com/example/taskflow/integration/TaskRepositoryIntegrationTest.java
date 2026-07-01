package com.example.taskflow.integration;

import com.example.taskflow.domain.enums.TaskPriority;
import com.example.taskflow.domain.enums.TaskStatus;
import com.example.taskflow.domain.enums.TaskType;
import com.example.taskflow.infrastructure.persistence.entity.TaskEntity;
import com.example.taskflow.infrastructure.persistence.repository.JpaTaskRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class TaskRepositoryIntegrationTest {
    @Autowired
    JpaTaskRepository jpaTaskRepository;

    @Autowired
    EntityManager entityManager;

    @Test
    void shouldSaveTask() {
        TaskEntity entity = getTaskEntity();

        TaskEntity savedEntity = jpaTaskRepository.save(entity);
        assertNotNull(savedEntity.getId());
    }

    @Test
    void shouldClaimTaskForProcessing() {
        TaskEntity entity = getTaskEntity();
        entity.setStatus(TaskStatus.QUEUED);

        TaskEntity savedEntity = jpaTaskRepository.saveAndFlush(entity);


        TaskEntity check = jpaTaskRepository.findById(savedEntity.getId()).orElseThrow();
        System.out.println("DB status before update = " + check.getStatus());

        int updatedRows = jpaTaskRepository.claimTaskForProcessing(
                savedEntity.getId(), TaskStatus.QUEUED, TaskStatus.PROCESSING);
        assertEquals(1, updatedRows);

        entityManager.clear();

        TaskEntity updatedEntity = jpaTaskRepository.findById(savedEntity.getId()).orElseThrow();
        System.out.println("After updated = " + updatedEntity.getStatus());
        assertEquals(TaskStatus.PROCESSING, updatedEntity.getStatus());

    }

    @Test
    void shouldFindTop100ByStatusOrderByCreatedAtAsc() {
        List<TaskEntity> entities = getEntities();
        jpaTaskRepository.saveAllAndFlush(entities);

        List<TaskEntity> topRecords = jpaTaskRepository.findTop100ByStatusOrderByCreatedAtAsc(TaskStatus.CREATED);

        assertEquals(2, topRecords.size());
    }

    @Test
    void shouldCountTasks(){
        List<TaskEntity> entities = getEntities();
        jpaTaskRepository.saveAllAndFlush(entities);

        Long count = jpaTaskRepository.countByStatus(TaskStatus.CREATED);
        assertEquals(2,count);
    }

    private TaskEntity getTaskEntity() {
        TaskEntity entity = new TaskEntity();
        entity.setTaskName("Import Customers");
        entity.setDescription("Description");
        entity.setTaskType(TaskType.CUSTOMER_IMPORT);
        entity.setStatus(TaskStatus.CREATED);
        entity.setPriority(TaskPriority.MEDIUM);
        entity.setRetryCount(0);
        entity.setCreatedBy("Tester");
        entity.setCreatedAt(Instant.now());
        entity.setUpdatedAt(Instant.now());

        return entity;
    }

    private List<TaskEntity> getEntities() {
        TaskEntity entity1 = new TaskEntity();
        entity1.setTaskName("Import Customers");
        entity1.setDescription("Description");
        entity1.setTaskType(TaskType.CUSTOMER_IMPORT);
        entity1.setStatus(TaskStatus.CREATED);
        entity1.setPriority(TaskPriority.MEDIUM);
        entity1.setRetryCount(0);
        entity1.setCreatedBy("Tester");
        entity1.setCreatedAt(Instant.now());
        entity1.setUpdatedAt(Instant.now());

        TaskEntity entity2 = new TaskEntity();
        entity2.setTaskName("Import Customers");
        entity2.setDescription("Description");
        entity2.setTaskType(TaskType.CUSTOMER_IMPORT);
        entity2.setStatus(TaskStatus.CREATED);
        entity2.setPriority(TaskPriority.MEDIUM);
        entity2.setRetryCount(0);
        entity2.setCreatedBy("Tester");
        entity2.setCreatedAt(Instant.now());
        entity2.setUpdatedAt(Instant.now());

        TaskEntity entity3 = new TaskEntity();
        entity3.setTaskName("Import Customers");
        entity3.setDescription("Description");
        entity3.setTaskType(TaskType.CUSTOMER_IMPORT);
        entity3.setStatus(TaskStatus.QUEUED);
        entity3.setPriority(TaskPriority.MEDIUM);
        entity3.setRetryCount(0);
        entity3.setCreatedBy("Tester");
        entity3.setCreatedAt(Instant.now());
        entity3.setUpdatedAt(Instant.now());

        return List.of(
                entity1, entity2, entity3
        );
    }
}
