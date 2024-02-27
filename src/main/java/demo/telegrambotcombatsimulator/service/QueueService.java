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

    private final HashMap<Long, String> playersQueue = new HashMap<>();

    private final List<Long> messagesQueue = new ArrayList<>();

    public void addToPlayersQueue(long chatId, String userName) {

        playersQueue.put(chatId, userName);
    }

    public long getAndDeleteFirstFromMessagesQueue() {

        long m = messagesQueue.get(0);
        messagesQueue.remove(m);
        return m;
    }

    @Scheduled(cron = "*/10 * * * * *")
    public void matchOpponents() {

        while (playersQueue.size() >= 2) {

            Iterator<Entry<Long, String>> iterator = playersQueue.entrySet().iterator();

            Entry<Long, String> firstEntry = iterator.next();
            iterator.remove();

            Entry<Long, String> secondEntry = iterator.next();
            iterator.remove();

            combatService.createSession(firstEntry.getValue(), secondEntry.getValue());

            messagesQueue.add(firstEntry.getKey());
            messagesQueue.add(secondEntry.getKey());
        }
    }


}
