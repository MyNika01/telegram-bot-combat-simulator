package demo.telegrambotcombatsimulator.entity;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.MongoId;

import java.time.LocalDateTime;

@Document ("usersData")
public record User (Long chatId, String firstName, String lastName, String userName, LocalDateTime createdTime) {

    public User (Long chatId, String firstName, String lastName, String userName) {
        this (chatId, firstName, lastName, userName, LocalDateTime.now());
    }

    public User (Long chatId, String userName) {
        this (chatId, null, null, userName, LocalDateTime.now());
    }
    public String toString() {
        return "Пользователь " + userName + "\n" +
                "[Номер чата: " + chatId +
                ", Имя: " + firstName +
                ", Фамилия: " + lastName +
                ", Дата регистрации: " + createdTime +
                "]";
    }
}
