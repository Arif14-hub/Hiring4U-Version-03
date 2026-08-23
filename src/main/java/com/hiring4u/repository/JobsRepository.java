package com.hiring4u.repository;
import com.hiring4u.entity.JobsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface JobsRepository extends JpaRepository<JobsEntity, Long>, JpaSpecificationExecutor<JobsEntity> {

    List<JobsEntity> findAllByOrderByPostedDateDescIdDesc();
}
