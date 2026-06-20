package enums;


public enum RoomStatus {
    WAITING,      // очікування гравців, гра ще не почалась
    IN_PROGRESS,  // гра триває
    FINISHED      // гра завершена адміністратором або після останнього раунду
}