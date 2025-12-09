import { useEffect, useState } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import { gameApi, Game } from '../api/client'
import { wsService } from '../services/websocket'
import '../App.css'

function Lobby() {
  const { gameId } = useParams<{ gameId: string }>()
  const navigate = useNavigate()
  const location = useLocation()
  const userId = location.state?.userId
  const isHost = location.state?.isHost || false

  const [game, setGame] = useState<Game | null>(null)
  const [numberOfSpies, setNumberOfSpies] = useState(1)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)

  useEffect(() => {
    if (!gameId || !userId) {
      navigate('/')
      return
    }

    const loadGame = async () => {
      try {
        const gameData = await gameApi.get(gameId)
        setGame(gameData)
        setLoading(false)
      } catch (err) {
        setError('Failed to load game')
        setLoading(false)
      }
    }

    loadGame()

    // Connect WebSocket
    wsService.connect().then(() => {
      // Subscribe to player updates
      wsService.subscribe(`/topic/game/${gameId}/players`, () => {
        loadGame()
      })
    }).catch((err) => {
      console.error('WebSocket connection failed:', err)
    })

    return () => {
      wsService.disconnect()
    }
  }, [gameId, userId, navigate])

  const handleStartGame = async () => {
    if (!gameId || !userId) return

    try {
      const updatedGame = await gameApi.start(gameId, userId, numberOfSpies)
      setGame(updatedGame)
      navigate(`/game/${gameId}`, { state: { userId, isHost } })
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to start game')
    }
  }

  if (loading) {
    return (
      <div className="container">
        <div className="card">
          <div className="loading">Loading...</div>
        </div>
      </div>
    )
  }

  if (!game) {
    return (
      <div className="container">
        <div className="card">
          <div className="error">Game not found</div>
        </div>
      </div>
    )
  }

  return (
    <div className="container">
      <div className="card">
        <h1>Game Lobby</h1>
        
        <div className="game-info">
          <h3>Join Code</h3>
          <div className="join-code">{game.joinCode}</div>
        </div>

        <div className="game-info">
          <h3>Players ({game.players.length})</h3>
          <ul className="players-list">
            {game.players.map((playerId) => (
              <li key={playerId}>
                Player: {playerId.slice(0, 8)}...
                {playerId === game.hostUserId && ' (Host)'}
              </li>
            ))}
          </ul>
        </div>

        {isHost && game.gameState === 'WAITING' && (
          <div className="host-controls">
            <h3>Host Controls</h3>
            <div className="form-group">
              <label htmlFor="spies">Number of Spies</label>
              <input
                id="spies"
                type="number"
                min="1"
                max={game.players.length - 1}
                value={numberOfSpies}
                onChange={(e) => setNumberOfSpies(parseInt(e.target.value) || 1)}
              />
            </div>
            <button className="btn btn-success" onClick={handleStartGame}>
              Start Game
            </button>
          </div>
        )}

        {game.gameState === 'RUNNING' && (
          <div className="success">
            <p>Game has started! Redirecting...</p>
            <button
              className="btn btn-primary"
              onClick={() => navigate(`/game/${gameId}`, { state: { userId, isHost } })}
            >
              Go to Game
            </button>
          </div>
        )}

        {error && <div className="error">{error}</div>}
      </div>
    </div>
  )
}

export default Lobby

