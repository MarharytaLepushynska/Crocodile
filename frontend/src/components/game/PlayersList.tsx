import "./PlayersList.css";
import type { DisplayPlayer } from "../../types/game";

interface PlayersListProps {
    players: DisplayPlayer[];
}

export function PlayersList({ players }: PlayersListProps) {
    return (
        <div className="players-list">
            <h2 className="players-list__title">Гравці</h2>
            <ul className="players-list__items">
                {players.map((player) => (
                    <li key={player.userId} className="players-list__item">
                        <div className="players-list__row">
                            <img
                                src={`/avatars/${player.avatarFileName}`}
                                alt={player.username}
                                className="players-list__avatar"
                            />
                            <span className="players-list__username">{player.username}</span>
                            {player.isAdmin && <span className="players-list__badge players-list__badge--admin">Admin</span>}
                            {player.isDrawer && <span className="players-list__badge players-list__badge--drawer">Drawer</span>}
                        </div>
                        <span className="players-list__score">Очки: {player.score}</span>
                    </li>
                ))}
            </ul>
        </div>
    );
}
