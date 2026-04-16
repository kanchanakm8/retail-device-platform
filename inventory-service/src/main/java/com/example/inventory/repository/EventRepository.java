package com.example.inventory.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.inventory.entity.EventEntity;

@Repository
public interface EventRepository extends JpaRepository<EventEntity, String> {

    List<EventEntity> findAllByOrderByTimestampDesc();
}