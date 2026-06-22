import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { GameProvider } from "./context/GameContext.tsx";
import { AuthPage } from "./pages/AuthPage";
//import { LobbyPage } from "./pages/LobbyPage";
//import { GamePage } from "./pages/GamePage";
import "./styles/mainTheme.css";
import "./styles/commonStyles.css";

export default function App() {
  return (
      <GameProvider>
        <BrowserRouter>
          <Routes>
            <Route path="/" element={<AuthPage />} />
              {/*<Route path="/lobby" element={<LobbyPage />} />
            <Route path="/room/:roomId" element={<GamePage />} />*/}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </GameProvider>
  );
}
