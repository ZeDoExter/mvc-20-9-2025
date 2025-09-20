package com.ZeDoExter.mvc.repository;

import com.ZeDoExter.mvc.model.Pledge;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class PledgeRepository {
    private final DataStore dataStore;

    public PledgeRepository(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public void add(Pledge pledge) {
        dataStore.addPledge(pledge);
        dataStore.recalcDerived();
        try { dataStore.persistAll(); } catch (Exception ignored) {}
    }

    public List<Pledge> findByProjectId(String projectId) {
        return dataStore.getPledgesByProject(projectId);
    }

    public List<Pledge> findAll() {
        return dataStore.getAllPledges();
    }
}
