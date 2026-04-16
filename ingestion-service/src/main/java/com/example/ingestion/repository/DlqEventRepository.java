package com.example.ingestion.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.ingestion.entity.DlqEventEntity;

@Repository
public interface DlqEventRepository extends JpaRepository<DlqEventEntity, String> {
}