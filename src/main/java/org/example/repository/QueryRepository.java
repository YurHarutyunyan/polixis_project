package org.example.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.example.model.Query;

public interface QueryRepository extends MongoRepository<Query, String> {

    public Query findBySearchQuery(String searchQuery);
}
