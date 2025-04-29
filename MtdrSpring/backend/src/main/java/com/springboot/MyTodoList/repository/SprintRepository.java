package com.springboot.MyTodoList.repository;

import java.time.OffsetDateTime;
import java.util.List;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.springboot.MyTodoList.model.Sprint;

@Repository
@Transactional
@EnableTransactionManagement
public interface SprintRepository extends JpaRepository<Sprint, Integer> {
  @Query("SELECT s FROM Sprint s WHERE :currentDate BETWEEN s.startsAt AND s.endsAt")
  List<Sprint> findCurrentSprints(OffsetDateTime currentDate);

  @Query(
      "SELECT s FROM Sprint s WHERE :currentDate BETWEEN s.startsAt AND s.endsAt AND s.ID_Project ="
          + " :projectId")
  List<Sprint> findCurrentSprintsByProject(OffsetDateTime currentDate, int projectId);
}
