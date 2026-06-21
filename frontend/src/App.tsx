import { BrowserRouter, Navigate, Route, Routes } from "react-router-dom";
import { GameProvider } from "./context/GameContext.tsx";
import AuthPage from "./pages/AuthPage";
import LobbyPage  from "./pages/LobbyPage";
import  GamePage  from "./pages/GamePage";
//import "./styles/theme.css";
//import "./styles/shared.css";
import logo from "./assets/ui/logo.png";


export default function App() {
  return (
      <GameProvider>
        <BrowserRouter>
         <div style={{ textAlign: "center", padding: "16px" }}>
          <img src={logo} alt="logo" style={{ height: "200px" }} />
        </div>
          <Routes>
            <Route path="/" element={<AuthPage />} />
            <Route path="/lobby" element={<LobbyPage />} />
            <Route path="/room/:roomId" element={<GamePage />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </GameProvider>
  );
}
