package com.springboot.MyTodoList.service;

import java.util.List;
import java.util.Optional;

import org.jvnet.hk2.annotations.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.springboot.MyTodoList.model.Project;
import com.springboot.MyTodoList.repository.ProjectRepository;

@Service
public class ProjectService {
    @Autowired
    private ProjectRepository projectRepository;

    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    public ResponseEntity<Project> getItemById(int id) {
        Optional<Project> projectItems = projectRepository.findById(id);
        return projectItems.map(project -> new ResponseEntity<>(project, HttpStatus.OK)).orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    public Project addProject(Project project) {
        return projectRepository.save(project);
    }

    public void deleteProject(int id) {
        try {
            projectRepository.deleteById(id);
        } catch (Exception ignored) {
        }
    }

    /**
     * 
     * @param id:         int
     * @param newProject: Project
     * @return project updated
     */

    public Project updateProject(int id, Project newProject) {
        Optional<Project> projectData = projectRepository.findById(id);
        if (projectData.isPresent()) {
            Project project_to_be_updated = projectData.get();
            project_to_be_updated.setID_Project(id);
            project_to_be_updated.setName(newProject.getName());
            project_to_be_updated.setDescription(newProject.getDescription());
            return projectRepository.save(project_to_be_updated);
        } else {
            return null;
        }
    }
}
