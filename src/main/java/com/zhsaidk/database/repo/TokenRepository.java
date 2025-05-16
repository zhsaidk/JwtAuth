package com.zhsaidk.database.repo;

import com.zhsaidk.database.entity.Token;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Integer> {
    Optional<Token> findByRefreshToken(String refreshToken);

    boolean existsByRefreshToken(String refreshToken);

    @Modifying
    @Query("delete from Token t where t.refreshToken = :refreshToken")
    void deleteTokenByRefreshToken(String refreshToken);
}
