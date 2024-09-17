package demo.telegrambotcombatsimulator.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

@Service
@EnableScheduling
public class QueueService {

    @Autowired
    private CombatService combatService;

    // очередь поиска соперника
    private final HashMap<Long, String> playersQueue = new HashMap<>();

    // очередь отправки готовности к бою
    private final List<Long> messagesQueue = new ArrayList<>();

    public void addToPlayersQueue(long chatId, String userName) {

        // добавляем игрока в очередь поиска соперника
        playersQueue.put(chatId, userName);
    }

    public long getAndDeleteFirstFromMessagesQueue() {

        // получаем, удаляем, возвращаем первый элемент
        long m = messagesQueue.get(0);
        messagesQueue.remove(m);
        return m;
    }

    // шедулер для обработки очереди на поиск матча
    @Scheduled(cron = "*/10 * * * * *")
    public void matchOpponents() {

        // проваливаемся, если в очереди более двух игроков
        while (playersQueue.size() >= 2) {

            Iterator<Entry<Long, String>> iterator = playersQueue.entrySet().iterator();

            Entry<Long, String> firstEntry = iterator.next();
            iterator.remove();

            Entry<Long, String> secondEntry = iterator.next();
            iterator.remove();

            //создаём сессию из двух игроков
            combatService.createSession(firstEntry.getValue(), secondEntry.getValue());

            //отправляем сообщения о готовности к бою
            messagesQueue.add(firstEntry.getKey());
            messagesQueue.add(secondEntry.getKey());
        }
    }
}