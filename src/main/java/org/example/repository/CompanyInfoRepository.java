package org.example.repository;

import org.example.model.CompanyInfo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface CompanyInfoRepository extends MongoRepository<CompanyInfo, String> {
}
