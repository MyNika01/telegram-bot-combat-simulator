package demo.telegrambotcombatsimulator.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Getter
@Setter
@Document ("usersData")
public class User {

    private Long chatId;

    private String firstName;

    private String lastName;

    private String userName;

    private String createdTime;

//  private Date createdTime; todo

    public String toString() {
        return "User{" +
                "chatId=" + chatId +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", userName='" + userName + '\'' +
                ", createdTime='" + createdTime + '\'' +
                '}';
    }
}
