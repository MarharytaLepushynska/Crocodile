package service;

import java.util.List;
import java.util.Random;

public class WordService {
    private static final List<String> WORDS = List.of(
            "фреш", "бадді", "незарах", "керол", "оліверсмтіт", "кошеня", "кеглі", "поні",
            "іпз", "карась", "котоміль", "папуга", "нонна", "черешня", "доброговечорамизУкраїни"
    );

    private final Random random = new Random();

    public String getRandomWord() {
        return WORDS.get(random.nextInt(WORDS.size()));
    }

    public String getRandomWordExcluding(List<String> usedWords) {
        List<String> available = WORDS.stream()
                .filter(w -> !usedWords.contains(w))
                .toList();

        if (available.isEmpty())
            return getRandomWord(); // слова скінчились - дозволяємо повтор

        return available.get(random.nextInt(available.size()));
    }
}