package com.example.model.repository;

import com.example.model.entity.VkGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Description:
 * Creation date: 11.07.2016 14:42
 *
 * @author sks
 */
@Repository
public interface VkGroupRepository extends JpaRepository<VkGroup, Integer>{
}
