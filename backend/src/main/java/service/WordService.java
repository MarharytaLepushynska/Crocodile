package service;

import java.util.List;
import java.util.Random;

public class WordService {
    private static final List<String> WORDS = List.of(
            "фреш", "бадді", "незарах", "керол", "оліверсмтіт", "кошеня", "кеглі", "поні",
            "іпз", "карась", "котоміль", "папуга", "нонна", "черешня", "доброговечорамизУкраїни", "зарах", "Житомир",
            "апельсин", "міньйон", "перемога", "незнаю", "макдональдс", "кфс", "пухатахата", "поділ",
            "хіпстер", "нішевий", "67", "депешмод", "коліна", "дубайський шоколад", "лабубу",
            "зомбі", "емо", "гот", "няшка", "щастя", "бублик", "пани на двох одні штани", "моля", "банан",
            "флатершай", "поні", "крокодил", "пепсі", "пепсі зеро", "капіталізм", "рошен", "шалена бджілка",
            "пакет атб", "сільпо", "глово", "бабл ті", "матча лате", "мигладеле молоко", "перший плац",
            "інфопошук", "саз", "банкрут", "кокакола", "спрайт", "платише молоко"
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