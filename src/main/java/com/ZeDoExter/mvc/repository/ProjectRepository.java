package com.ZeDoExter.mvc.repository;

import com.ZeDoExter.mvc.model.Project;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ProjectRepository {
    private final DataStore dataStore;

    public ProjectRepository(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public List<Project> findAllOrderByDeadline() {
        Collection<Project> all = dataStore.getAllProjects();
        return all.stream()
                .sorted(Comparator.comparing(Project::getDeadline))
                .collect(Collectors.toList());
    }

    public Optional<Project> findById(String id) {
        return dataStore.getProject(id);
    }

    public void save(Project p) {
        dataStore.updateProject(p);
    }
}

