package com.ZeDoExter.mvc.repository;

import com.ZeDoExter.mvc.model.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataStore {

    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());
    private Path dataDir;

    private final Map<String, Project> projects = new LinkedHashMap<>();
    private final Map<String, User> users = new LinkedHashMap<>();
    private final List<Pledge> pledges = new ArrayList<>();

    @PostConstruct
    public void init() throws IOException {
        String configured = System.getProperty("app.data.dir");
        if (configured == null || configured.isBlank()) {
            configured = System.getProperty("user.dir") + File.separator + "data";
        }
        dataDir = Paths.get(configured);
        if (!Files.exists(dataDir)) {
            Files.createDirectories(dataDir);
        }

        loadOrCreateUsers();
        loadOrCreateProjects();
        loadOrCreatePledges();

        recalcDerived();
        ensureCreatedAtAndPersistIfMissing();
    }

    private void loadOrCreateUsers() throws IOException {
        File f = dataDir.resolve("users.json").toFile();
        if (f.exists()) {
            List<User> list = mapper.readValue(f, new TypeReference<>(){});
            for (User u : list) users.put(u.getId(), u);
        } else {
            List<User> list = sampleUsers();
            saveList(list, f);
            for (User u : list) users.put(u.getId(), u);
        }
    }

    private void loadOrCreateProjects() throws IOException {
        File f = dataDir.resolve("projects.json").toFile();
        if (f.exists()) {
            List<Project> list = mapper.readValue(f, new TypeReference<>(){});
            for (Project p : list) projects.put(p.getId(), p);
        } else {
            List<Project> list = sampleProjects();
            saveList(list, f);
            for (Project p : list) projects.put(p.getId(), p);
        }
    }

    private void loadOrCreatePledges() throws IOException {
        File f = dataDir.resolve("pledges.json").toFile();
        if (f.exists()) {
            List<Pledge> list = mapper.readValue(f, new TypeReference<>(){});
            pledges.addAll(list);
        } else {
            List<Pledge> list = samplePledges();
            saveList(list, f);
            pledges.addAll(list);
        }
    }

    private <T> void saveList(List<T> list, File file) throws IOException {
        mapper.writerWithDefaultPrettyPrinter().writeValue(file, list);
    }

    public synchronized void persistAll() throws IOException {
        saveList(new ArrayList<>(users.values()), dataDir.resolve("users.json").toFile());
        saveList(new ArrayList<>(projects.values()), dataDir.resolve("projects.json").toFile());
        saveList(new ArrayList<>(pledges), dataDir.resolve("pledges.json").toFile());
    }

    public synchronized Collection<Project> getAllProjects() {
        return projects.values();
    }

    public synchronized Optional<Project> getProject(String id) {
        return Optional.ofNullable(projects.get(id));
    }

    public synchronized void updateProject(Project p) {
        projects.put(p.getId(), p);
    }

    public synchronized Optional<User> getUserByUsername(String username) {
        return users.values().stream().filter(u -> u.getUsername().equalsIgnoreCase(username)).findFirst();
    }

    public synchronized Optional<User> getUserById(String id) {
        return Optional.ofNullable(users.get(id));
    }

    public synchronized void addPledge(Pledge pledge) {
        pledges.add(pledge);
    }

    public synchronized List<Pledge> getPledgesByProject(String projectId) {
        return pledges.stream().filter(p -> p.getProjectId().equals(projectId)).collect(Collectors.toList());
    }

    public synchronized void recalcDerived() {
        // Reset currentRaised, rejections, and reward remaining quotas
        for (Project p : projects.values()) {
            p.setCurrentRaised(0);
            p.setRejectedPledges(0);
            if (p.getRewardTiers() != null) {
                for (RewardTier rt : p.getRewardTiers()) {
                    // reset remaining to total before recount
                    rt.setRemainingQuota(rt.getTotalQuota());
                }
            }
        }

        for (Pledge pledge : pledges) {
            Project p = projects.get(pledge.getProjectId());
            if (p == null) continue;
            if (pledge.getStatus() == PledgeStatus.ACCEPTED) {
                p.setCurrentRaised(p.getCurrentRaised() + pledge.getAmount());
                if (pledge.getRewardTierId() != null && p.getRewardTiers() != null) {
                    for (RewardTier rt : p.getRewardTiers()) {
                        if (rt.getId().equals(pledge.getRewardTierId())) {
                            rt.setRemainingQuota(Math.max(0, rt.getRemainingQuota() - 1));
                            break;
                        }
                    }
                }
            } else if (pledge.getStatus() == PledgeStatus.REJECTED) {
                p.setRejectedPledges(p.getRejectedPledges() + 1);
            }
        }
    }

    public synchronized List<Pledge> getAllPledges() {
        return new ArrayList<>(pledges);
    }

    private void ensureCreatedAtAndPersistIfMissing() throws IOException {
        boolean dirty = false;
        for (Project p : projects.values()) {
            if (p.getCreatedAt() == null) {
                // set a deterministic createdAt based on id hash so it stays stable across runs
                int offset = Math.abs(p.getId().hashCode() % 30);
                p.setCreatedAt(LocalDate.now().minusDays(offset));
                dirty = true;
            }
        }
        if (dirty) persistAll();
    }

    private List<User> sampleUsers() {
        List<User> list = new ArrayList<>();
        for (int i = 1; i <= 10; i++) {
            String id = String.format("U%03d", i);
            list.add(new User(id, "user" + i, "pass", "User " + i));
        }
        return list;
    }

    public synchronized void resetWithDiverseSamples() throws IOException {
        users.clear();
        projects.clear();
        pledges.clear();
        // Populate
        for (User u : sampleUsers()) users.put(u.getId(), u);
        for (Project p : sampleProjects()) projects.put(p.getId(), p);
        pledges.addAll(samplePledges());
        // Save
        persistAll();
        recalcDerived();
    }

    private List<Project> sampleProjects() {
        List<Project> list = new ArrayList<>();
        // 9 projects, 3+ categories, including one expired
        list.add(projectWithRewardsAndStretchCustom("10000001", "Smart Plant Monitor", "Tech", 50000, 20,
                new int[]{100,50,10}, true));
        list.add(projectWithRewardsAndStretchCustom("10000002", "Indie Board Game", "Games", 80000, 25,
                new int[]{200,80,15}, true));
        list.add(projectWithRewardsOnlyCustom("10000003", "Community Garden", "Community", 30000, 18,
                new int[]{2,1,3})); // small quotas (R1 left 1 after seeding)
        list.add(projectWithRewardsOnlyCustom("10000004", "Art Exhibition", "Art", 45000, 15,
                new int[]{100,50,5}));
        list.add(projectWithRewardsAndStretchCustom("10000005", "STEM Workshop", "Education", 60000, 30,
                new int[]{150,70,10}, true));
        list.add(projectWithRewardsOnlyCustom("10000006", "Local Coffee Roaster", "Food", 55000, 22,
                new int[]{80,40,1})); // R3 quota 1 (will be exhausted)
        list.add(projectWithRewardsAndStretchCustom("10000007", "Solar Charger v2", "Tech", 120000, 28,
                new int[]{120,60,12}, true));
        list.add(projectWithRewardsOnlyCustom("10000008", "Music Album", "Art", 70000, 26,
                new int[]{100,50,10}));
        // expired project
        list.add(projectExpiredWithRewards("10000009", "Retro Tech Revival", "Tech", 40000, 7,
                new int[]{1,1,1}));
        // stagger createdAt for 'newest' sorting variety
        for (int i = 0; i < list.size(); i++) {
            list.get(i).setCreatedAt(LocalDate.now().minusDays(5L * i));
        }
        return list;
    }

    private Project projectWithRewardsOnlyCustom(String id, String name, String category, double goal, int daysAhead, int[] quotas) {
        Project p = new Project(id, name, category, goal, LocalDate.now().plusDays(daysAhead), false);
        p.setRewardTiers(new ArrayList<>(List.of(
                new RewardTier(id+"-R1", "Supporter", 200, quotas[0], quotas[0]),
                new RewardTier(id+"-R2", "Backer", 500, quotas[1], quotas[1]),
                new RewardTier(id+"-R3", "VIP", 1200, quotas[2], quotas[2])
        )));
        p.setCreatedAt(LocalDate.now());
        return p;
    }

    private Project projectWithRewardsAndStretchCustom(String id, String name, String category, double goal, int daysAhead, int[] quotas, boolean hasStretch) {
        Project p = new Project(id, name, category, goal, LocalDate.now().plusDays(daysAhead), hasStretch);
        p.setRewardTiers(new ArrayList<>(List.of(
                new RewardTier(id+"-R1", "Early Bird", 300, quotas[0], quotas[0]),
                new RewardTier(id+"-R2", "Standard", 600, quotas[1], quotas[1]),
                new RewardTier(id+"-R3", "Deluxe", 1500, quotas[2], quotas[2])
        )));
        // stretch goals: 120%, 150%, 200%
        double g = goal;
        p.setStretchGoals(new ArrayList<>(List.of(
                new StretchGoal(id+"-S1", Math.round(g*1.2), "Extra feature set"),
                new StretchGoal(id+"-S2", Math.round(g*1.5), "Premium materials upgrade"),
                new StretchGoal(id+"-S3", Math.round(g*2.0), "Bonus reward for all backers")
        )));
        p.setHasStretchGoals(true);
        p.setCreatedAt(LocalDate.now());
        return p;
    }

    private Project projectExpiredWithRewards(String id, String name, String category, double goal, int daysPast, int[] quotas) {
        Project p = new Project(id, name, category, goal, LocalDate.now().minusDays(daysPast), false);
        p.setRewardTiers(new ArrayList<>(List.of(
                new RewardTier(id+"-R1", "Early", 300, quotas[0], quotas[0]),
                new RewardTier(id+"-R2", "Standard", 600, quotas[1], quotas[1]),
                new RewardTier(id+"-R3", "Deluxe", 1500, quotas[2], quotas[2])
        )));
        p.setCreatedAt(LocalDate.now().minusDays(40));
        return p;
    }

    private List<Pledge> samplePledges() {
        List<Pledge> list = new ArrayList<>();
        // Accepted pledges — create variety for totals/quota
        list.add(makeAccepted("U001", "10000001", 500, "10000001-R2"));
        list.add(makeAccepted("U002", "10000001", 1600, "10000001-R3"));
        list.add(makeAccepted("U003", "10000002", 300, "10000002-R1"));
        list.add(makeAccepted("U004", "10000002", 700, "10000002-R2"));
        // Community Garden: R1 total 2 -> accept 1 so remaining becomes 1; R2 total 1 -> accept 1 so remaining 0
        list.add(makeAccepted("U005", "10000003", 250, "10000003-R1"));
        list.add(makeAccepted("U006", "10000003", 600, "10000003-R2"));
        // Art Exhibition high tier
        list.add(makeAccepted("U007", "10000004", 1500, "10000004-R3"));
        // STEM Workshop push to top
        list.add(makeAccepted("U008", "10000005", 600, "10000005-R2"));
        list.add(makeAccepted("U009", "10000005", 1500, "10000005-R3"));
        list.add(makeAccepted("U010", "10000005", 300, "10000005-R1"));
        // Local Coffee Roaster: R3 total 1 -> accept 1 so quota exhausted
        list.add(makeAccepted("U001", "10000006", 1500, "10000006-R3"));
        // Solar Charger mix
        list.add(makeAccepted("U002", "10000007", 300, "10000007-R1"));
        list.add(makeAccepted("U003", "10000007", 600, "10000007-R2"));
        // Music Album
        list.add(makeAccepted("U004", "10000008", 500, "10000008-R2"));

        // Rejected examples
        list.add(makeRejected("U005", "10000003", 100, "10000003-R1", "Below minimum for reward"));
        list.add(makeRejected("U006", "10000004", -50, null, "Amount must be > 0"));
        // Quota exhausted: R3 of 10000006 has 0 remaining after accepted above
        list.add(makeRejected("U007", "10000006", 1500, "10000006-R3", "Reward quota exhausted"));
        // Past deadline: project 10000009 expired
        list.add(makeRejected("U008", "10000009", 500, "10000009-R2", "Project deadline has passed"));
        return list;
    }

    private Pledge makeAccepted(String userId, String projectId, double amount, String rewardTierId) {
        Pledge p = new Pledge();
        p.setId(UUID.randomUUID().toString());
        p.setUserId(userId);
        p.setProjectId(projectId);
        p.setAmount(amount);
        p.setTimestamp(Instant.now());
        p.setRewardTierId(rewardTierId);
        p.setStatus(PledgeStatus.ACCEPTED);
        return p;
    }

    private Pledge makeRejected(String userId, String projectId, double amount, String rewardTierId, String reason) {
        Pledge p = new Pledge();
        p.setId(UUID.randomUUID().toString());
        p.setUserId(userId);
        p.setProjectId(projectId);
        p.setAmount(amount);
        p.setTimestamp(Instant.now());
        p.setRewardTierId(rewardTierId);
        p.setStatus(PledgeStatus.REJECTED);
        p.setRejectionReason(reason);
        return p;
    }
}
