package com.springboot.MyTodoList.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "USERAVAILABILITY")
public class UserAvailability {
  @Id int ID_USER;

  @Column(name = "AVAILABLEHOURS")
  Integer AVAILABLE_HOURS;

  public UserAvailability() {}

  public UserAvailability(int ID_USER, Integer AVAILABLE_HOURS) {
    this.ID_USER = ID_USER;
    this.AVAILABLE_HOURS = AVAILABLE_HOURS;
  }

  public int getID_USER() {
    return ID_USER;
  }

  public void setID_USER(int ID_USER) {
    this.ID_USER = ID_USER;
  }

  public Integer getAVAILABLE_HOURS() {
    return AVAILABLE_HOURS;
  }

  public void setAVAILABLE_HOURS(Integer AVAILABLE_HOURS) {
    this.AVAILABLE_HOURS = AVAILABLE_HOURS;
  }

  @Override
  public String toString() {
    return "UserAvailability" + "ID_USER=" + ID_USER + ", AVAILABLE_HOURS=" + AVAILABLE_HOURS;
  }
}
