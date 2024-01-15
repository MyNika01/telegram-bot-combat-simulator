package demo.telegrambotcombatsimulator.repository;

import demo.telegrambotcombatsimulator.entity.Combat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CombatRepository extends MongoRepository<Combat, UUID> {

    @Query("{ $or: [ {firstPlayerName: ?0}, {secondPlayerName: ?0} ] }")
    Combat getByPlayerName(String playerName);

    @Query("{ $or: [ {firstPlayerName: ?0}, {secondPlayerName: ?0} ] }")
    Optional <Combat> findByPlayerName(String playerName);

    @Query(value="{ $or: [ {firstPlayerName: ?0}, {secondPlayerName: ?0} ] }", delete = true)
    void deleteByPlayerName(String playerName);

}

//https://javatechonline.com/spring-boot-mongodb-query-examples/
