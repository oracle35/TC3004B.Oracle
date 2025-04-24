package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.TaskDependency;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Repository
@Transactional
@EnableTransactionManagement
public interface TaskDependencyRepository extends JpaRepository<TaskDependency, Integer> {}
