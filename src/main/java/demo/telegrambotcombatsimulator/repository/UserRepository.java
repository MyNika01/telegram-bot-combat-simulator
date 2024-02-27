package demo.telegrambotcombatsimulator.repository;

import demo.telegrambotcombatsimulator.entity.User;
import org.springframework.data.mongodb.repository.DeleteQuery;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, UUID> {

    @Query("{chatId :?0}")
    Optional<User> findByChatId(long chatId);

    @Query(value = "{'chatId' : ?0}", delete = true)
    void deleteByChatId(long chatId);

//    @DeleteQuery
//    void deleteByChatId(long chatId);

}
