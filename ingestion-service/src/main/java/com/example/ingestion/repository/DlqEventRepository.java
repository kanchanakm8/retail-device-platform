package com.example.ingestion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ingestion.entity.DlqEventEntity;
import com.example.ingestion.entity.DlqStatus;

@Repository
public interface DlqEventRepository extends JpaRepository<DlqEventEntity, String> {
	long countByStatus(DlqStatus status);

    List<DlqEventEntity> findByStatus(DlqStatus status);
}