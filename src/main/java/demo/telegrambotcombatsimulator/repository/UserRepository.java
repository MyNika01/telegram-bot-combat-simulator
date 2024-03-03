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

    @Query("{userName :?0}")
    Optional<User> findByUserName(String userName);

    @Query(value = "{'chatId' : ?0}", delete = true)
    void deleteByChatId(long chatId);

    default boolean isExistByUserName(String userName) {
        return findByUserName(userName).isPresent();
    }

//    @DeleteQuery
//    void deleteByChatId(long chatId);

}
