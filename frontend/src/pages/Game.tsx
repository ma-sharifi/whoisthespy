import { useEffect, useState } from 'react'
import { useParams, useNavigate, useLocation } from 'react-router-dom'
import { gameApi, Game } from '../api/client'
import { wsService } from '../services/websocket'
import '../App.css'

function Game() {
  const { gameId } = useParams<{ gameId: string }>()
  const navigate = useNavigate()
  const location = useLocation()
  const userId = location.state?.userId
  const isHost = location.state?.isHost || false

  const [game, setGame] = useState<Game | null>(null)
  const [imageUrl, setImageUrl] = useState<string | null>(null)
  const [error, setError] = useState('')
  const [loading, setLoading] = useState(true)
  const [generatingImage, setGeneratingImage] = useState(false)

  useEffect(() => {
    if (!gameId || !userId) {
      navigate('/')
      return
    }

    const loadGame = async () => {
      try {
        const gameData = await gameApi.get(gameId)
        setGame(gameData)
        if (gameData.currentImageUrl) {
          setImageUrl(gameData.currentImageUrl)
        }
        setLoading(false)
      } catch (err) {
        setError('Failed to load game')
        setLoading(false)
      }
    }

    loadGame()

    // Connect WebSocket
    wsService.connect().then(() => {
      // Subscribe to image updates
      wsService.subscribe(`/topic/game/${gameId}/image`, (data) => {
        if (data.imageUrl) {
          setImageUrl(data.imageUrl)
        }
      })

      // Subscribe to turn updates
      wsService.subscribe(`/topic/game/${gameId}/turn`, (data) => {
        loadGame()
      })

      // Subscribe to player updates
      wsService.subscribe(`/topic/game/${gameId}/players`, (data) => {
        loadGame()
      })
    }).catch((err) => {
      console.error('WebSocket connection failed:', err)
    })

    return () => {
      wsService.disconnect()
    }
  }, [gameId, userId, navigate])

  const handleNextTurn = async () => {
    if (!gameId || !userId) return

    try {
      const updatedGame = await gameApi.nextTurn(gameId, userId)
      setGame(updatedGame)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to advance turn')
    }
  }

  const handleGenerateImage = async () => {
    if (!gameId || !userId) return

    setGeneratingImage(true)
    setError('')

    try {
      const result = await gameApi.generateImage(gameId, userId)
      setImageUrl(result.imageUrl)
    } catch (err: any) {
      setError(err.response?.data?.message || 'Failed to generate image')
    } finally {
      setGeneratingImage(false)
    }
  }

  if (loading) {
    return (
      <div className="container">
        <div className="card">
          <div className="loading">Loading game...</div>
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

  const isSpy = game.spyUserIds?.includes(userId || '')
  const role = isSpy ? 'spy' : 'civilian'
  const word = isSpy ? game.spyWord : game.civilianWord
  const currentPlayerIndex = game.currentTurnIndex % (game.players.length || 1)
  const currentPlayerId = game.players[currentPlayerIndex]

  return (
    <div className="container">
      <div className="card">
        <h1>🎭 Who Is The Spy</h1>

        <div className="game-info">
          <div className={`role-badge ${role}`}>
            You are: {role.toUpperCase()}
          </div>
          <div className="word-display">
            Your word: {word || 'Loading...'}
          </div>
        </div>

        <div className="turn-indicator">
          Turn {game.currentTurnIndex + 1} - Player {currentPlayerIndex + 1}'s turn
        </div>

        <div className="image-container">
          {imageUrl ? (
            <img
              src={imageUrl.startsWith('http') ? imageUrl : `http://localhost:8080${imageUrl}`}
              alt="Game image"
              onError={(e) => {
                console.error('Image load error:', e)
                setError('Failed to load image')
              }}
            />
          ) : (
            <div className="loading">No image generated yet</div>
          )}
        </div>

        {isHost && (
          <div className="host-controls">
            <h3>Host Controls</h3>
            <button
              className="btn btn-primary"
              onClick={handleGenerateImage}
              disabled={generatingImage}
            >
              {generatingImage ? 'Generating Image...' : 'Generate Image'}
            </button>
            <button
              className="btn btn-secondary"
              onClick={handleNextTurn}
              disabled={game.gameState !== 'RUNNING'}
            >
              Next Turn
            </button>
          </div>
        )}

        {error && <div className="error">{error}</div>}

        <div className="game-info" style={{ marginTop: '2rem' }}>
          <h3>Game Info</h3>
          <p>Players: {game.players.length}</p>
          <p>Spies: {game.numberOfSpies || 'Not set'}</p>
          <p>State: {game.gameState}</p>
        </div>
      </div>
    </div>
  )
}

export default Game

