package demo.telegrambotcombatsimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import static com.vdurmont.emoji.EmojiParser.parseToUnicode;

@SpringBootApplication
@EnableMongoRepositories
public class TelegramBotCombatSimulatorApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelegramBotCombatSimulatorApplication.class, args);
	}
}
