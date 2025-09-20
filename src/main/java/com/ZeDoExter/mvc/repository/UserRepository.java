package com.ZeDoExter.mvc.repository;

import com.ZeDoExter.mvc.model.User;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepository {
    private final DataStore dataStore;

    public UserRepository(DataStore dataStore) {
        this.dataStore = dataStore;
    }

    public Optional<User> findByUsername(String username) {
        return dataStore.getUserByUsername(username);
    }

    public Optional<User> findById(String id) {
        return dataStore.getUserById(id);
    }
}

