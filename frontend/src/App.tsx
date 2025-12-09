import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import JoinGame from './pages/JoinGame'
import Lobby from './pages/Lobby'
import Game from './pages/Game'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <div className="app">
        <Routes>
          <Route path="/" element={<JoinGame />} />
          <Route path="/lobby/:gameId" element={<Lobby />} />
          <Route path="/game/:gameId" element={<Game />} />
          <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </BrowserRouter>
  )
}

export default App

