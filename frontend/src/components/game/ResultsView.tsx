import "./ResultsView.css";
import { Button } from "../common/Button";
import { useNavigate } from "react-router-dom";
import type { DisplayPlayer } from "../../types/game";

interface ResultsViewProps {
    players: DisplayPlayer[];
}

export function ResultsView({ players }: ResultsViewProps) {
    const navigate = useNavigate();

    const sorted = [...players].sort((a, b) => b.score - a.score);
    const winner = sorted[0];
    const isDraw = sorted.length > 1 && sorted[0].score === sorted[1].score;

    return (
        <div className="results-view">
            <div className="results-view__card">
                <h2 className="results-view__title">Game finished</h2>
                <p className="results-view__subtitle">{isDraw ? "Нічия!" : "Winner"}</p>
                {winner && (
                    <>
                        <img
                            src={isDraw ? "/friendship.png" : `/avatars/${winner.avatarFileName}`}
                            alt={isDraw ? "friendship" : winner.username}
                            className="results-view__avatar"
                        />
                        <p className="results-view__name">{isDraw ? "Перемогла дружба" : winner.username}</p>
                        <p className="results-view__points">{isDraw ? "" : `${winner.score} Points`}</p>
                    </>
                )}
                <div className="results-view__all">
                    {sorted.map((p, i) => (
                        <div key={p.userId} className="results-view__player">
                            <span className="results-view__rank">#{i + 1}</span>
                            <img
                                src={`/avatars/${p.avatarFileName}`}
                                alt={p.username}
                                className="results-view__small-avatar"
                            />
                            <span className="results-view__player-name">{p.username}</span>
                            <span className="results-view__player-score">{p.score} pts</span>
                        </div>
                    ))}
                </div>
                <Button onClick={() => navigate("/lobby")} className="results-view__btn">
                    До лобі
                </Button>
            </div>
        </div>
    );
}
