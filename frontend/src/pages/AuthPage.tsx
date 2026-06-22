import "./AuthPage.css";
import { useState } from "react";
import drawing from "../assets/ui/drawing.png";
import { useNavigate } from "react-router-dom";
import { PageBackground } from "../components/layout/PageBackground";
import { Logo } from "../components/common/Logo";
import { Panel } from "../components/common/Panel";
import { InputForm } from "../components/common/InputForm";
import { Button } from "../components/common/Button";
import { AvatarPicker } from "../components/avatar/AvatarPicker";
import { useGameContext } from "../context/GameContext";
import { register, login } from "../api/authApi";
import { ApiError } from "../api/client";
import { AVAILABLE_AVATARS } from "../types/game";
import type { Avatar } from "../types/game";

type AuthMode = "login" | "register";

export function AuthPage() {
    const navigate = useNavigate();
    const { signIn } = useGameContext();

    const [mode, setMode] = useState<AuthMode>("register");
    const [username, setUsername] = useState("");
    const [password, setPassword] = useState("");
    const [avatar, setAvatar] = useState<Avatar>(AVAILABLE_AVATARS[0]);
    const [isSubmitting, setIsSubmitting] = useState(false);
    const [error, setError] = useState<string | null>(null);

    function switchMode(nextMode: AuthMode) {
        setMode(nextMode);
        setError(null);
    }

    async function handleSubmit() {
        const trimmedName = username.trim();
        if (trimmedName.length === 0) {
            setError("Введіть імʼя користувача");
            return;
        }
        if (password.length === 0) {
            setError("Введіть пароль");
            return;
        }

        setIsSubmitting(true);
        setError(null);

        try {
            const auth =
                mode === "register"
                    ? await register({
                        username: trimmedName,
                        password,
                        avatarFileName: avatar.fileName,
                    })
                    : await login({ username: trimmedName, password });

            signIn(auth);
            navigate("/lobby");
        } catch (err) {
            setError(
                err instanceof ApiError
                    ? err.message
                    : mode === "register"
                        ? "Не вдалося зареєструватись"
                        : "Не вдалося увійти",
            );
        } finally {
            setIsSubmitting(false);
        }
    }

    return (
        <PageBackground>
            <div className="auth-page">
                <Logo/>
                <Panel className="auth-page__panel">
                    <div className="auth-page__tabs">
                        <button type = "button"
                                className={`auth-page__tab ${mode === "register" ? "auth-page__tab--active" : ""}`}
                                 onClick={() => switchMode("register")}>
                            Реєстрація
                        </button>
                        <button type="button"
                            className={`auth-page__tab ${mode === "login" ? "auth-page__tab--active" : ""}`}
                            onClick={() => switchMode("login")}>
                            Вхід
                        </button>
                    </div>
                        <InputForm
                            placeholder="Введіть імʼя"
                            value={username}
                            onChange={(event) => setUsername(event.target.value)}
                            onKeyDown={(event) => {
                                if (event.key === "Enter") void handleSubmit();
                            }}
                        />
                        <InputForm
                            type="password"
                            placeholder="Введіть пароль"
                            value={password}
                            onChange={(event) => setPassword(event.target.value)}
                            onKeyDown={(event) => {
                                if (event.key === "Enter") void handleSubmit();
                            }}
                        />

                        {mode === "register" && <AvatarPicker onChange={setAvatar} />}

                        {error && <p className="auth-page__error">{error}</p>}

                        <Button
                            className="auth-page__submit-button"
                            onClick={() => void handleSubmit()}
                            disabled={isSubmitting}
                        >
                            {mode === "register" ? "Грати" : "Увійти"}
                        </Button>
                </Panel>
            <img src={drawing} alt="drawing" className="auth-page__drawing" />
            </div>
        </PageBackground>
    );
}
