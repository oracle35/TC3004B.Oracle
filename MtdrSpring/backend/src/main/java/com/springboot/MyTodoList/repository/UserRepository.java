package com.springboot.MyTodoList.repository;

import com.springboot.MyTodoList.model.User;
import javax.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Repository
@Transactional
@EnableTransactionManagement
public interface UserRepository extends JpaRepository<User, Integer> {}
