package com.example.demo.dao;


import com.example.demo.domain.UserApiKey;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserApiKeyRepository extends CrudRepository<UserApiKey, String> {

    List<UserApiKey> findAllByUsername(String username);
}
