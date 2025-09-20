package com.ZeDoExter.mvc.service;

import com.ZeDoExter.mvc.model.*;
import com.ZeDoExter.mvc.repository.ProjectRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProjectService {
    private final ProjectRepository projectRepository;

    public ProjectService(ProjectRepository projectRepository) {
        this.projectRepository = projectRepository;
    }

    public List<Project> listProjectsByDeadline() {
        return projectRepository.findAllOrderByDeadline();
    }

    public Optional<Project> getProject(String id) {
        return projectRepository.findById(id);
    }

    public BasicFundingModel toBasicModel(Project p) {
        return new BasicFundingModel(p.getId(), p.getGoalAmount(), p.getCurrentRaised());
    }

    public StretchFundingModel toStretchModel(Project p) {
        StretchFundingModel m = new StretchFundingModel(p.getId(), p.getGoalAmount(), p.getCurrentRaised());
        if (p.getStretchGoals() != null) {
            p.getStretchGoals().forEach(sg -> {
                if (p.getCurrentRaised() >= sg.getThresholdAmount()) {
                    m.getUnlockedGoalIds().add(sg.getId());
                }
            });
        }
        return m;
    }

    public List<Project> listProjects(String category, String sort) {
        List<Project> all = new ArrayList<>(projectRepository.findAllOrderByDeadline());
        if (category != null && !category.isBlank()) {
            all = all.stream().filter(p -> category.equalsIgnoreCase(p.getCategory())).collect(Collectors.toList());
        }
        Comparator<Project> comparator;
        if ("newest".equalsIgnoreCase(sort)) {
            comparator = Comparator.comparing(Project::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())).reversed();
        } else if ("top".equalsIgnoreCase(sort)) { // most raised
            comparator = Comparator.comparing(Project::getCurrentRaised).reversed();
        } else { // default: soonest deadline
            comparator = Comparator.comparing(Project::getDeadline, Comparator.nullsLast(Comparator.naturalOrder()));
        }
        all.sort(comparator);
        return all;
    }

    public List<String> allCategories() {
        return listProjectsByDeadline().stream()
                .map(Project::getCategory)
                .filter(Objects::nonNull)
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .collect(Collectors.toList());
    }
}

